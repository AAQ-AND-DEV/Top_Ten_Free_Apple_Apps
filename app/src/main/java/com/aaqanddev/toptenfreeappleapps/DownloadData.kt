package com.aaqanddev.toptenfreeappleapps

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

private const val TAG = "downloadData"
class DownloadData(private val callBack: DownloaderCallBack) : AsyncTask<String, Void, String>() {

    interface DownloaderCallBack{
        fun onDataAvailable(data: List<FeedEntry>)
    }
    override fun doInBackground(vararg url: String): String {

        Log.d(TAG, "doInBackground: starts with ${url[0]}")
        val rssFeed = downloadXML(url[0])
        if (rssFeed.isEmpty()) {
            Log.e(TAG, "doInBackground: Error downloading")
        }
        return rssFeed
    }

    override fun onPostExecute(result: String) {

        val parseApplications = ParseApplications()
        if (result.isNotEmpty()){
            parseApplications.parse(result)

        }
        callBack.onDataAvailable(parseApplications.applications)

    }

    private fun downloadXML(urlPath: String): String {
        try{

        return URL(urlPath).readText()
        } catch (e: MalformedURLException){
            Log.d(TAG, "downloadXml: Invalid URL " + e.message)
        } catch(e: IOException){
            Log.d(TAG, "downloadXml: IO exception reading data"  + e.message)
        } catch(e: SecurityException){
            Log.d(TAG, "downloadXML: security exception. needs Permission? " + e.message)
        }
        return ""   //return empty string if exception
    }
}

//My parser
fun parseXML(result: String?): List<FeedEntry> {
    val output = mutableListOf<FeedEntry>()
    var ind = 0
    if (result != null) {
        while (ind < result.length && ind != -1) {
            //val firstInd = result.indexOf("<entry>", ind)
            //println("reached")
            val titleInd = result.indexOf("<title>", ind)
            //println(titleInd)
            val titleEndInd = result.indexOf("</title", titleInd)
            val title = result.substring(titleInd + 6, titleEndInd)
            val summaryInd = result.indexOf("<summary>", titleEndInd)
            val summaryEndInd = result.indexOf("</summary>", summaryInd)
            val summary = result.substring(summaryInd + 8, summaryEndInd)
            val nameInd = result.indexOf("<im:name>", summaryEndInd)
            val nameEndInd = result.indexOf("</im:name", nameInd)
            val name = result.substring(nameInd + 9, nameEndInd)
            val artistInd = result.indexOf("<im:artist", nameEndInd)
            val artistTagEndInd = result.indexOf(">", artistInd)
            val artistEndInd = result.indexOf("</im:artist>", artistTagEndInd)
            val artist = result.substring(artistTagEndInd+1, artistEndInd)
            val imageTagInd = result.indexOf("<im:image", artistEndInd)
            val imageTagEndInd = result.indexOf(">", imageTagInd)
            val imageEndInd = result.indexOf("</im:image>", imageTagEndInd)
            val imageUrl = result.substring(imageTagEndInd+1, imageEndInd)
            val releaseInd = result.indexOf("<im:releaseDate", imageEndInd)
            val releaseEndTagInd = result.indexOf(">", releaseInd)
            val releaseEndInd = result.indexOf("</im:releaseDate>", releaseEndTagInd)
            val releaseDate = result.substring(releaseEndTagInd+1, releaseEndInd)
            val feedEntry = FeedEntry(name, artist, releaseDate, summary, imageUrl)
            output.add(feedEntry)
            println(feedEntry)
            ind = result.indexOf("<entry>", releaseEndInd)
        }

    }
    return output

}