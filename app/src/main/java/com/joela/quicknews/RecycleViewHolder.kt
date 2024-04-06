/*
Defines the items to be shown in RecycleView
It allows the adapter to access the views from each item
 */

package com.joela.quicknews

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var news_image: ImageView
    var news_source: TextView
    var news_author: TextView
    var news_title: TextView
    var news_date: TextView
    var news_share: ImageButton
    var news_save: ImageButton

    init {
        news_image = itemView.findViewById(R.id.news_imageview)
        news_author = itemView.findViewById(R.id.news_author_textview)
        news_title = itemView.findViewById(R.id.news_title_textview)
        news_date = itemView.findViewById(R.id.news_date_textview)
        news_source = itemView.findViewById(R.id.news_source_textview)
        news_share = itemView.findViewById(R.id.newsshare_button)
        news_save = itemView.findViewById(R.id.newssave_button)
    }
}
