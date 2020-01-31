package com.aaqanddev.toptenfreeappleapps

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

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

    private var downloadData : DownloadData? = null

    private  var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10

    private var feedCachedURL = "INVALIDATED"
    private val STATE_URL = "feedUrl"
    private val STATE_LIMIT = "feedLimit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate Called")
        if (savedInstanceState!=null){
            feedUrl = savedInstanceState.getString(STATE_URL).toString()
            feedLimit = savedInstanceState.getInt(STATE_LIMIT)

        }
        downloadUrl(feedUrl.format(feedLimit))

    }

    private fun downloadUrl(feedUrl: String){
        if (feedUrl != feedCachedURL){

            Log.d(TAG, "downloadUrl starting AsyncTask")
            downloadData = DownloadData(this, xmlListView)
            downloadData?.execute(feedUrl)
            feedCachedURL = feedUrl
            Log.d(TAG, "downloadUrl done")
        }else{
            Log.d(TAG, "downloadUrl - URL Not Changed")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu, menu)

        if (feedLimit == 10){
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else{
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId){
            R.id.mnuFree -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25 -> {
                if (!item.isChecked){
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "OnOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                } else{
                    Log.d(TAG, "OnOptionsItemSelected: ${item.title} setting feedLimit unchanged")

                }
            }
            R.id.mnuRefresh -> feedCachedURL = "INVALIDATED"
            else ->return super.onOptionsItemSelected(item)
        }
        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, feedUrl)
        outState.putInt(STATE_LIMIT, feedLimit)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    companion object {
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "downloadData"

            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init{
                propContext = context
                propListView = listView
            }
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

                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter
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
