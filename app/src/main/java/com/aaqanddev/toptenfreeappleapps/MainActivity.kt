package com.aaqanddev.toptenfreeappleapps

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.net.URL

class FeedEntry( var name: String = "", var artist: String = "", var releaseDate: String = "",
    var summary: String = "",
    var imageURL: String = ""){

    override fun toString(): String {
        return """
            name = $name
            artist = $artist
            releaseDate = $releaseDate
            imageURL = $imageURL
        """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        val downloadData = DownloadData()
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
        Log.d(TAG, "onCreate: done")
    }

    companion object {
        private class DownloadData : AsyncTask<String, Void, String>() {
            private val TAG = "downloadData"

            override fun doInBackground(vararg url: String?): String {

                Log.d(TAG, "doInBackground: starts with ${url[0]}")
                val rssFeed = downloadXML(url[0])
                if (rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                //Log.d(TAG, "onPostExecute: parameter is $result")
                //val entries = parseXML(result)
                val parseApplications = ParseApplications()
                if (result!=null){
                    parseApplications.parse(result)

                }
            }

            private fun downloadXML(urlPath: String?): String {
                return URL(urlPath).readText()
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
    }
}
