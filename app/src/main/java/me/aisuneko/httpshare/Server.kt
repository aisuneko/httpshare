// TODO: File metadata

package me.aisuneko.httpshare

import android.content.Context
import android.net.Uri
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream

private const val HOST = "0.0.0.0"
private const val PORT = 6789

class Server(uri: Uri, context: Context) : NanoHTTPD(HOST, PORT) {
    private var inputStream: InputStream? = null
    private var mime: String? = null
    private var applicationContext = context
    private var link = uri

    //    private val name = link.lastPathSegment
    override fun serve(session: IHTTPSession?): Response {
        return try {
            inputStream = applicationContext.contentResolver.openInputStream(link)
            mime = applicationContext.contentResolver.getType(link)
//            inputStream = FileInputStream(File(URI(link.toString())))
            newChunkedResponse(Response.Status.OK, mime, inputStream)
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
}