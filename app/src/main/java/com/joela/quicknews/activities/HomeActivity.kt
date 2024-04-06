/*
This activity contains the four main fragments of the application-
News, Search, Favourites and Profile fragments
User can navigate between the fragments with a toolbar at bottom
 */

package com.joela.quicknews.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.joela.quicknews.fragments.FavoritesFragment
import com.joela.quicknews.fragments.NewsFragment
import com.joela.quicknews.fragments.ProfileFragment
import com.joela.quicknews.R
import com.joela.quicknews.fragments.SearchFragment
import com.joela.quicknews.databinding.ActivityHomeBinding
import com.joela.quicknews.models.ProfileViewModel

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    var auth = FirebaseAuth.getInstance()
    private val viewModel: ProfileViewModel by viewModels()

    //reloads the cached data of the current user from Firebase Authentication service
    override fun onResume() {
        super.onResume()
        auth.currentUser?.reload()
    }

    //Loading the layout and initialise the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //if the app is opened for first time the news fragment is shown first
        if(savedInstanceState == null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(binding.frameLayout.id, NewsFragment())
            transaction.commit()
        }

        //get current user ID
        val userid = auth.currentUser?.uid.toString()
        //get reference to current user's data in database
        val databaseRef = FirebaseDatabase.getInstance().getReference("users/" + userid)
        val databasePreferenceRef = FirebaseDatabase.getInstance().getReference("preferences/"+userid)
        /*
        Listen for changes in user's preference settings on the database
        and update the viewModel so the changed data can be seen in other locations
        */
        databasePreferenceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var prefList = ArrayList<String>()
                if(snapshot.exists()){

                    for(pref in snapshot.children){
                        prefList.add(pref.value.toString())
                    }
                    viewModel.updatePrefList(prefList)
                    viewModel.updatePrefNotSet(false)
                }
                else{
                    viewModel.updatePrefList(null)
                    viewModel.updatePrefNotSet(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Home Activity", "Database listener onCancelled: "+error.details)
                if(auth.currentUser != null)
                    Toast.makeText(this@HomeActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
        //Another listener for profile data
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){

                    val phone = dataSnapshot.child("phone").getValue(String::class.java)
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    viewModel.update(username.toString(),phone.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Home Activity", "Database listener onCancelled: "+error.details)
                if(auth.currentUser != null)
                    Toast.makeText(this@HomeActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.updateEmailVerified(auth.currentUser!!.isEmailVerified)
    }

    /*
    Changes the fragment based on which item is selected in the bottom navigation bar
     */
    fun changeFragment(item: MenuItem){

        item.isChecked = true

        when(item.itemId){
            R.id.profile_item ->{
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.frameLayout.id, ProfileFragment())
                transaction.commit()
            }
            R.id.news_item ->{
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.frameLayout.id, NewsFragment())
                transaction.commit()
            }
            R.id.favorites_item ->{
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.frameLayout.id, FavoritesFragment())
                transaction.commit()
            }
            R.id.search_item ->{
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(binding.frameLayout.id, SearchFragment())
                transaction.commit()
            }
        }
    }
}