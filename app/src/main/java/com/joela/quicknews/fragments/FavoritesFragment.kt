/*
Fragment for showing the saved news articles of the user
 */

package com.joela.quicknews.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.joela.quicknews.RecycleViewAdaptor
import com.joela.quicknews.databinding.FragmentFavoritesBinding
import com.joela.quicknews.models.Article

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    lateinit var NewsCardList: ArrayList<Article>
    lateinit var adapter: RecycleViewAdaptor
    lateinit var recyclerView: RecyclerView
    lateinit var auth: FirebaseAuth
    lateinit var databaseRef: DatabaseReference

    //load the layout and initialise fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val view = binding.root

        NewsCardList = ArrayList<Article>()

        /*
        Adapter for recycle view is initialised with NewsCardList ArrayList and
        the adapter is set to the recycle view
        */
        recyclerView= binding.favoritesfeedRecycleview
        adapter= RecycleViewAdaptor(NewsCardList, true)
        recyclerView.setAdapter(adapter)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("favourites/"+auth.currentUser?.uid.toString())

        //Listen to changes in the favourites list of the user on the database
        databaseRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                NewsCardList.clear()
                for(article in snapshot.children){
                    /*
                    Convert the response from Firebase Database to json with Gson library and
                    Load into NewsCardList and update the adapter
                    */
                    var gson: Gson = Gson()
                    var json = gson.toJson(article.value)

                    NewsCardList.add(gson.fromJson(json, Article::class.java))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Fravourites Fragment", "Database Error onCancelled: "+error.details)
            }
        })
        return view
    }

    //Delete the binding when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}