package com.aaqanddev.toptenfreeappleapps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

private const val TAG = "FeedViewModel"

val EMPTY_FEED_LIST: List<FeedEntry> = Collections.emptyList()

class FeedViewModel: ViewModel(), DownloadData.DownloaderCallBack {
    private var downloadData : DownloadData? = null
    private var feedCachedURL = "INVALIDATED"

    private val feed = MutableLiveData<List<FeedEntry>>()
    val feedEntries : LiveData<List<FeedEntry>>
        get() = feed

    init{
        feed.postValue(EMPTY_FEED_LIST)
    }

    fun downloadUrl(feedUrl: String){
        Log.d(TAG, "DownloadUrl: called with url $feedUrl")
        if (feedUrl != feedCachedURL){

            Log.d(TAG, "downloadUrl starting AsyncTask")
            downloadData = DownloadData(this)
            downloadData?.execute(feedUrl)
            feedCachedURL = feedUrl
            Log.d(TAG, "downloadUrl done")
        }else{
            Log.d(TAG, "downloadUrl - URL Not Changed")
        }
    }

    fun invalidate(){
        feedCachedURL=="INVALIDATE"
    }

    override fun onDataAvailable(data: List<FeedEntry>) {
        Log.d(TAG, "onDataAvailable called")
        feed.value = data
        Log.d(TAG, "onDataAvailable ends")
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared  cancelling pending downloads")
        downloadData?.cancel(true)
    }
}