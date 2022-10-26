package al10101.android.decimalai

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.RuntimeException

fun Context.readTextFileFromResource(resourceId: Int): String {

    val body = StringBuilder()

    try {

        val inputStream: InputStream = resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var nextLine = bufferedReader.readLine()
        while (nextLine != null) {
            body.append(nextLine)
            body.append("\n")
            nextLine = bufferedReader.readLine()
        }

    } catch (e: IOException) {
        throw RuntimeException("Could not open resource: $resourceId", e)
    } catch (nfe: Resources.NotFoundException) {
        throw RuntimeException("Resource not found: $resourceId", nfe)
    }

    return body.toString()

}