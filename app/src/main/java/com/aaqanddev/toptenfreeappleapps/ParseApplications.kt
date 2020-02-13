package com.aaqanddev.toptenfreeappleapps


import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

private const val TAG = "ParseApplications"

class ParseApplications {

    val applications = ArrayList<FeedEntry>()

    fun parse(xmlData: String): Boolean{
        //Log.d(TAG,"parse called with $xmlData")
        var status = true
        var inEntry = false
        var textValue = ""

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = FeedEntry()
            while (eventType != XmlPullParser.END_DOCUMENT){
                val tagName = xpp.name?.toLowerCase() //done: uses safe call operator b/c xpp.name is null at beginning
                when (eventType){
                    XmlPullParser.START_TAG -> {
                        //Log.d(TAG, "parse: Starting tage for " + tagName)
                        if (tagName == "entry"){
                            inEntry = true
                        }
                    }
                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {
                        //Log.d(TAG, "parse: Ending tag for " + tagName)
                        if (inEntry){
                            when (tagName){
                                "entry" -> {
                                    applications.add(currentRecord)
                                    inEntry = false
                                    currentRecord = FeedEntry()
                                }
                                "name" -> currentRecord.name = textValue
                                "artist" -> currentRecord.artist = textValue
                                "releasedate" -> currentRecord.releaseDate = textValue
                                "summary" -> currentRecord.summary = textValue
                                "image" -> currentRecord.imageURL = textValue
                            }
                        }
                    }
                }
                eventType = xpp.next()
            }
//            for (app in applications){
//                Log.d(TAG, "********")
//                Log.d(TAG, app.toString())
//            }
        } catch (e:Exception){
            e.printStackTrace()
            status = false
        }
        return status
    }
}