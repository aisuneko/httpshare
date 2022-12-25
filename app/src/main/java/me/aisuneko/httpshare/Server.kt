// TODO: File metadata

package me.aisuneko.httpshare

import android.content.Context
import android.net.Uri
import fi.iki.elonen.NanoHTTPD
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val HOST = "0.0.0.0"
private const val PORT = 6789

class Server(uri: Uri, name: String, context: Context) : NanoHTTPD(HOST, PORT) {
    private var inputStream: InputStream? = null
    private var mime: String? = null
    private var applicationContext = context
    private var link = uri
    private var fileName = name
    private val timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-kkmmss"))
    private var tempFile: File? = null

    //    private val name = link.lastPathSegment
    override fun serve(session: IHTTPSession?): Response {
        return try {
            val fis = FileInputStream(tempFile?.path)
            val resp: Response = newFixedLengthResponse(
                Response.Status.OK, mime, fis,
                fis.available().toLong()
            )
            resp.addHeader("Content-Disposition", "inline; filename=\"$fileName\"")
//            resp.addHeader("Content-Length", fileSize.toString())
            resp
//            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT,"Hello!!!1")
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                ioe.localizedMessage
            )
        }
    }

    override fun start() {
        super.start()
        inputStream = applicationContext.contentResolver.openInputStream(link)
        mime = applicationContext.contentResolver.getType(link)
        if (tempFile?.exists() == true) {
            tempFile?.delete()
        }
        tempFile = File.createTempFile(timestamp, "httpshare", applicationContext.cacheDir)
        val fileOutStream = FileOutputStream(tempFile)
        inputStream.use { input ->
            fileOutStream.use { output ->
                input?.copyTo(output)
            }
        }
    }

    override fun stop() {
        super.stop()
        if (tempFile?.exists() == true) {
            tempFile?.delete()
        }
    }
}