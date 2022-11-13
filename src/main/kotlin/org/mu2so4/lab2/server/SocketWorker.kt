package org.mu2so4.lab2.server

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

private const val UPLOAD_DIR_NAME = "uploads"
private const val OK_RESPONSE = "File uploaded successfully"
private const val ERR_RESPONSE = "Failed to upload file"
private const val BUF_SIZE = 8192
private const val SPEED_MEASURE_INTERVAL = 3000

class SocketWorker(private val clientSocket: Socket, private val id: Int):
    Runnable {

    override fun run() {
        val inputStream = clientSocket.getInputStream()

        val uploadsDirectory = File(UPLOAD_DIR_NAME)
        if(uploadsDirectory.isFile) {
            uploadsDirectory.delete()
        }
        if(!uploadsDirectory.exists()) {
            uploadsDirectory.mkdir()
        }

        val fileHeader = ByteArray(10)
        inputStream.read(fileHeader)
        val wrap = ByteBuffer.wrap(fileHeader)
        val fileNameSize = wrap.short
        val fileSize = wrap.long

        logger.info("JOB ID $id, filename size: $fileNameSize, " +
                "file size: $fileSize")
        val byteFileName = ByteArray(fileNameSize.toInt())
        inputStream.read(byteFileName)
        val fullFile = File(byteFileName.toString(Charsets.UTF_8))
        var file = File("$UPLOAD_DIR_NAME/${fullFile.name}")
        try {
            file.createNewFile()
        }
        catch(e: IOException) {
            logger.info("JOB ID $id: ${e.message}. Used standard name")
            file = File("$UPLOAD_DIR_NAME/id$id")
        }

        val fileStream = FileOutputStream(file)
        var receivedByteCount = 0L
        val buffer = ByteArray(BUF_SIZE)
        val startTime = System.currentTimeMillis()
        var lastMeasuredTime = startTime
        while(receivedByteCount < fileSize) {
            val iterationStart = System.currentTimeMillis()
            val read: Int
            try {
                read = inputStream.read(buffer)
                if(read == 0)
                    break
                fileStream.write(buffer, 0, read)
                receivedByteCount += read
            }
            catch(e: SocketTimeoutException) {
                break
            }
            val currentTime = System.currentTimeMillis()
            if(currentTime - lastMeasuredTime >= SPEED_MEASURE_INTERVAL) {
                logger.info("JOB ID $id,\tprogress: ${receivedByteCount *
                        100 / fileSize}%,\tav: ${receivedByteCount /
                        (currentTime - startTime)} bps,\tcurrent: ${read /
                        (currentTime - iterationStart)} bps")
                lastMeasuredTime = currentTime
            }
        }
        val allTime =  System.currentTimeMillis() - startTime
        logger.info("JOB ID $id,\tTOTAL: av: ${receivedByteCount / allTime} bps")
        fileStream.close()

        val outputStream = clientSocket.getOutputStream()
        val response = if(receivedByteCount == fileSize) {
            OK_RESPONSE
        } else {
            ERR_RESPONSE
        }

        val responseSize = ByteBuffer.allocate(4).
        putInt(response.length).array()
        outputStream.write(responseSize)
        outputStream.write(response.toByteArray())

        clientSocket.close()
        logger.info("JOB ID $id finished")
    }
}