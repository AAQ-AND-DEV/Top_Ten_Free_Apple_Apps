package com.aaqanddev.toptenfreeappleapps

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class ViewHolder(v: View){
    val tvName = v.findViewById<TextView>(R.id.tvName)
    val tvArtist = v.findViewById<TextView>(R.id.tvArtist)
    val tvSummary = v.findViewById<TextView>(R.id.tvSummary)
    val ivlogo = v.findViewById<ImageView>(R.id.ivLogo)
}


class FeedAdapter(context: Context, private val resource: Int, private val applications: List<FeedEntry>)
    : ArrayAdapter<FeedEntry>(context, resource) {
    private val TAG = "FeedAdapter"
    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //Log.d(TAG, "getView() called")
        val viewHolder: ViewHolder

        val view: View
        if (convertView==null){
            //Log.d(TAG, "getView() called with null convertView")
            view = inflater.inflate(resource, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else{
            //Log.d(TAG, "getView provided a convertView")
            view = convertView
            viewHolder = view.tag as ViewHolder

        }

//        val tvName = view.findViewById<TextView>(R.id.tvName)
//        val tvArtist = view.findViewById<TextView>(R.id.tvArtist)
//        val tvSummary = view.findViewById<TextView>(R.id.tvSummary)


        val currentApp = applications[position]

        viewHolder.tvName.text = currentApp.name
        viewHolder.tvArtist.text = currentApp.artist
        viewHolder.tvSummary.text = currentApp.summary

        Picasso.get().load(currentApp.imageURL).into(viewHolder.ivlogo)
        return view
    }

    override fun getCount(): Int {
        Log.d(TAG, "getCount() called")
        return applications.size
    }
}