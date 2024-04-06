/*
Adapter for the RecycleViews in News fragment, Search Fragment and Favourites Fragment
defines how to display the news articles and controls the functionality of the RecycleView items
 */

package com.joela.quicknews

import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.joela.quicknews.activities.WebviewActivity
import com.joela.quicknews.models.Article
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import java.lang.Exception

class RecycleViewAdaptor(NewsCardList: ArrayList<Article>, favFrag: Boolean = false): RecyclerView.Adapter<RecycleViewHolder>() {
    var NewsCardList: ArrayList<Article>
    lateinit var auth: FirebaseAuth
    lateinit var databaseRef: DatabaseReference
    var favFrag: Boolean

    //Initialise the adapter with NewsCardList which is a ArrayList of all the news Articles
    init{
        this.NewsCardList = NewsCardList
        this.favFrag = favFrag
    }

    //returns the RecycleViewHolder class being used for this adapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        val inflator: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflator.inflate(R.layout.news_card, parent, false)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("favourites/"+auth.currentUser?.uid.toString())

        return RecycleViewHolder(view)
    }

    //returns the number of items to be displayed in the RecycleView
    override fun getItemCount(): Int {
        return NewsCardList.size
    }

    //Initialises the adapter with functionalities
    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        //Set the News Cards with data like Title, Author, Image, Url, Source Name
        holder.news_title.setText(NewsCardList.get(position).title)
        if(NewsCardList.get(position).author.toString() =="null")
            holder.news_author.setText("")
        else
            holder.news_author.setText(NewsCardList.get(position).author.toString())

        holder.news_source.setText(NewsCardList.get(position).source.name)
        holder.news_date.setText(NewsCardList.get(position).publishedAt.substring(0,10))

        /*
        reference to main thread
        Only the thread that creates a view can update the view
        */
        val handler = Handler(Looper.getMainLooper())
        var url = NewsCardList.get(position).urlToImage.toString()

        val con = holder.itemView.context

        //The Ion library is used to load the images from the given URL
        Ion.with(holder.itemView.context)
            .load("GET",url)
            .asBitmap()
            .setCallback(object: FutureCallback<Bitmap> {
                //operation to load the image is complete
                override fun onCompleted(e: Exception?, result: Bitmap?) {
                    //successfully loaded the image
                    if (result != null) {
                        handler.post{
                            //The loaded image is set to the Image View
                            holder.news_image.setImageBitmap(result)
                        }
                    }
                    //Loading failed
                    else{
                        //A default error image is set to the ImageView
                        handler.post{
                            holder.news_image.setImageBitmap(con.getDrawable(R.drawable.error_image)!!.toBitmap())
                        }
                    }
                }
            })

        //When the news card is clicked the WebView Activity is opened with the URL of the news article
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, WebviewActivity::class.java)
            intent.putExtra("url", NewsCardList.get(position).url)
            holder.itemView.context.startActivity(intent)
        }

        /*
        When the share button is clicked a sharing menu is opened,
        The news article URL can be shared
        */
        holder.news_share.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, NewsCardList.get(position).title+" :\n\n"+ NewsCardList.get(position).url)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            holder.itemView.context.startActivity(shareIntent)
        }

        //If user is in favourites fragment the save button image is changed to delete image
        if(favFrag == true){
            holder.news_save.setImageBitmap(con.getDrawable(R.drawable.delete_news)!!.toBitmap())
        }

        /*
        Save button is clicked
        News Articles are saved to Firebase Database
        Or if in Favourites Fragment News Articles are deleted
        */
        holder.news_save.setOnClickListener{
            //Hash the news title to create a unique ID for each article
            val newsTitleHash: String = NewsCardList.get(position).title.hashCode().toString()

            //Save to Firebase
            if(!favFrag){
                databaseRef.child(newsTitleHash).get().addOnCompleteListener {
                    if (!it.result.exists()) {
                        databaseRef.child(newsTitleHash).setValue(
                            Article(
                                NewsCardList.get(position).author,
                                NewsCardList.get(position).content,
                                NewsCardList.get(position).description,
                                NewsCardList.get(position).publishedAt,
                                NewsCardList.get(position).source,
                                NewsCardList.get(position).title,
                                NewsCardList.get(position).url,
                                NewsCardList.get(position).urlToImage
                            )
                        )
                        Toast.makeText(
                            holder.itemView.context,
                            "Article saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    //Article already saved in Firebase
                    else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Article is already saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            //Delete the article from Firebase
            else{
                databaseRef.child(newsTitleHash).removeValue()
            }
        }

    }
}