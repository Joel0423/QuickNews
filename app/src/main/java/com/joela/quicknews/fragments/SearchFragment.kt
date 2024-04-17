/*
Fragment that allows Users to search for News Articles
 */

package com.joela.quicknews.fragments

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joela.quicknews.ApiInterface
import com.joela.quicknews.R
import com.joela.quicknews.RecycleViewAdaptor
import com.joela.quicknews.databinding.FragmentSearchBinding
import com.joela.quicknews.models.Article
import com.joela.quicknews.models.ResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    lateinit var NewsCardList: ArrayList<Article>
    lateinit var adapter: RecycleViewAdaptor
    lateinit var recyclerView: RecyclerView
    lateinit var retrofitBuilder: ApiInterface

    //Load layout and initialise
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        NewsCardList = ArrayList<Article>()

        /*
        Adapter for recycle view is initialised with NewsCardList ArrayList and
        the adapter is set to the recycle view
        */
        recyclerView= binding.searchfeedRecycleview
        adapter= RecycleViewAdaptor(NewsCardList)
        recyclerView.setAdapter(adapter)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        //Search Button is clicked, function to get news is called
        binding.searchImagebutton.setOnClickListener({
            binding.searchImagebutton.background.setTint(Color.parseColor("#979797"))
            if (!isInternetAvailable()) {
                internetToast("No internet connection")
            }
            else
            {
                getNews()
            }
            Handler().postDelayed({
                binding.searchImagebutton.background.setTintList(null)
            },220)
        })

        //Search box is checked if enter key is pressed
        binding.searchEdittext.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                if (!isInternetAvailable()) {
                    internetToast("No internet connection")
                }
                else
                {//get news if enter is pressed
                    getNews()
                    return@OnKeyListener true
                }
            }
            false
        })

        /*
        Retrofit is used to make API call to newsapi.org
         */
        retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(this.getString(R.string.API_BASE_URL))
            .build()
            .create(ApiInterface::class.java)

        return view
    }

    //delete the binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Get the news articles based of search query
    fun getNews() {
        val query: String = binding.searchEdittext.text.toString()
        if(!query.isNullOrEmpty()){

            recyclerView.isVisible = false
            binding.searchfeedProgressbar.isVisible = true
            //HTTP GET request queries are set
            val retrofitData = retrofitBuilder.getSearch(query,this.getString(R.string.NEWS_API_KEY))

            retrofitData.enqueue(object : Callback<ResponseModel?> {
                //Response successfully recieved
                override fun onResponse(
                    call: Call<ResponseModel?>,
                    response: Response<ResponseModel?>
                ) {
                    NewsCardList.clear()

                    //Add articles to NewsCardList and update adapter
                    val responseBody = response.body()!!
                    for(article in responseBody.articles){
                        NewsCardList.add((article))
                    }
                    adapter.notifyDataSetChanged()
                    binding.searchfeedProgressbar.isVisible = false
                    recyclerView.isVisible = true
                    recyclerView.scrollToPosition(0)
                }

                //No response
                override fun onFailure(p0: Call<ResponseModel?>, t: Throwable) {
                    Log.e("Search Fragment", "Bad Response: "+t.message)
                }
            })
        }
        else{
            Toast.makeText(activity, "Enter query to search", Toast.LENGTH_SHORT).show()
        }
    }

    //returns true or false if the internet is available or not
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun internetToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}