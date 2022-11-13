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
private const val SPEED_MEASURE_INTERVAL_NANOS = 3000000000L

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
        val startTime = System.nanoTime()
        var lastMeasuredTime = startTime
        while(receivedByteCount < fileSize) {
            val iterationStart = System.nanoTime()
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
            val currentTime = System.nanoTime()
            if(currentTime - lastMeasuredTime >= SPEED_MEASURE_INTERVAL_NANOS) {
                val progress = receivedByteCount * 100.0 / fileSize
                val avSpeed = receivedByteCount * 1000.0 / (currentTime - startTime)
                val currentSpeed = read * 1000.0 / (currentTime - iterationStart)
                logger.info(String.Companion.format("JOB ID $id,\tprogress: " +
                        "%.1f%%,\tav: %.2f Mbps,\tcurrent: %.2f Mbps",
                        progress, avSpeed, currentSpeed))
                lastMeasuredTime = currentTime
            }
        }
        val allTime = System.nanoTime() - startTime
        val avSpeed = receivedByteCount * 1000.0 / allTime
        logger.info(String.Companion.format("JOB ID $id,\tTOTAL: av: %.2f Mbps",
            avSpeed))
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