/*
Fragment that shows the news articles to user
User can select drop down box to select categories
 */

package com.joela.quicknews.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joela.quicknews.ApiInterface
import com.joela.quicknews.R
import com.joela.quicknews.RecycleViewAdaptor
import com.joela.quicknews.databinding.FragmentNewsBinding
import com.joela.quicknews.models.Article
import com.joela.quicknews.models.ProfileViewModel
import com.joela.quicknews.models.ResponseModel
import com.joela.quicknews.models.Source
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    lateinit var NewsCardList: ArrayList<Article>
    lateinit var adapter: RecycleViewAdaptor
    lateinit var recyclerView: RecyclerView
    private val viewModel: ProfileViewModel by activityViewModels()
    var prefList: ArrayList<String>? = null
    lateinit var retrofitBuilder: ApiInterface
    var prefnotset: Boolean? = null

    //Load layout and initialise fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        val view = binding.root

        NewsCardList = ArrayList<Article>()

        /*
        Adapter for recycle view is initialised with NewsCardList ArrayList and
        the adapter is set to the recycle view
        */
        recyclerView= binding.newsfeedRecycleview
        adapter= RecycleViewAdaptor(NewsCardList)
        recyclerView.setAdapter(adapter)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        /*
        Retrofit library is used to make API call to newsapi.org and
        retrieve the response with all the news articles
         */
        retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(this.getString(R.string.API_BASE_URL))
            .build()
            .create(ApiInterface::class.java)

        //Users preferences are taken from viewmodel when it is updated
        viewModel.preflist.observe(viewLifecycleOwner, Observer{ item->
            prefList = item
            if(binding.newscategoriesSpinner.selectedItem.toString() == "Preferences"){
                getNews()
            }
        })

        viewModel.prefnotset.observe(viewLifecycleOwner, { item->
            prefnotset = item
            if(item){
                binding.newsfeedProgressbar.isVisible = false
                Toast.makeText(activity, "Set preferences in settings", Toast.LENGTH_SHORT).show()
            }
        })

        //get the news when the drop down box is selected
        binding.newscategoriesSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(activity, "No item selected on spinner", Toast.LENGTH_SHORT).show()
                Log.w("News Fragment", "Nothing Selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getNews()
            }
        }
        return view
    }

    //delete the binding when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //get the news articles based on selected category
    fun getNews(){
        binding.newsfeedRecycleview.isVisible = false
        binding.newsfeedProgressbar.isVisible = true

        var category = binding.newscategoriesSpinner.selectedItem.toString()

        //Switch-Case statements
        when(category){
            //Queries for the HTTP GET request are set
            "Business" ->{
                val retrofitData = retrofitBuilder.getData("in","business",this.getString(R.string.NEWS_API_KEY))
                setRecycleView(retrofitData)
            }
            "Health" ->{
                val retrofitData = retrofitBuilder.getData("in","health",this.getString(R.string.NEWS_API_KEY))
                setRecycleView(retrofitData)
            }
            "Sports" ->{
                val retrofitData = retrofitBuilder.getData("in","sports",this.getString(R.string.NEWS_API_KEY))
                setRecycleView(retrofitData)
            }
            "Technology" ->{
                val retrofitData = retrofitBuilder.getData("in","technology",this.getString(R.string.NEWS_API_KEY))
                setRecycleView(retrofitData)
            }
            "Science" ->{
                val retrofitData = retrofitBuilder.getData("in","science",this.getString(R.string.NEWS_API_KEY))
                setRecycleView(retrofitData)
            }
            "Entertainment" ->{
                val retrofitData = retrofitBuilder.getData("in","entertainment",this.getString(R.string.NEWS_API_KEY))
                setRecycleView(retrofitData)
            }
            "Preferences" ->{
                //Get news according to preferences if preferences are set
                if(prefList != null){
                    NewsCardList.clear()
                    var retrofitData: Call<ResponseModel>

                    for(preference in prefList!!){
                        //Delays API calls by 500 miliseconds
                        Handler().postDelayed({
                            retrofitData = retrofitBuilder.getData("in",preference,this.getString(R.string.NEWS_API_KEY))
                            retrofitData.enqueue(object : Callback<ResponseModel?> {
                                //Response recieved successfully
                                override fun onResponse(
                                    call: Call<ResponseModel?>,
                                    response: Response<ResponseModel?>
                                ) {
                                    //add articles to NewsCardList and update the adapter
                                    val responseBody = response.body()!!
                                    for(article in responseBody.articles){
                                        NewsCardList.add((article))
                                    }
                                    adapter.notifyDataSetChanged()
                                    binding.newsfeedProgressbar.isVisible = false
                                    binding.newsfeedRecycleview.isVisible = true
                                }

                                //Response not recieved
                                override fun onFailure(p0: Call<ResponseModel?>, t: Throwable) {
                                    Log.w("News Fragment", "onFailure: Failed Call "+t.message)
                                }
                            })
                        },500)
                    }
                }
                //Preferences not set by User
                else{
                    recyclerView.isVisible = false
                    binding.newsfeedProgressbar.isVisible = true
                    if(prefnotset != null && prefnotset == true){
                        binding.newsfeedProgressbar.isVisible = false
                        Toast.makeText(activity, "Set preferences in settings", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            //Default case, just for testing
            else ->{
                NewsCardList.clear()
                NewsCardList.add(Article("Joe","","","2024-03-29",Source("Abh123","MyNews"),"Breaking: News Title","http://www.google.com","https://store-images.s-microsoft.com/image/apps.60323.13774133678237924.758ba261-9ad2-4b6c-aae4-54f1307835d0.5c6bbf61-af87-400b-ae20-330496c5a9ac?q=90&w=480&h=270",))
                adapter.notifyDataSetChanged()
                binding.newsfeedProgressbar.isVisible = false
                binding.newsfeedRecycleview.isVisible = true
            }
        }
    }

    //Set the recycle view with data recieved from API
    fun setRecycleView(retrofitData: Call<ResponseModel>){
        retrofitData.enqueue(object : Callback<ResponseModel?> {
            //Response recieved successfully
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
                binding.newsfeedProgressbar.isVisible = false
                binding.newsfeedRecycleview.isVisible = true
                recyclerView.scrollToPosition(0)
            }

            //Response not recieved
            override fun onFailure(p0: Call<ResponseModel?>, t: Throwable) {
                Log.e("News Fragment", "Bad Response: "+t.message)
            }
        })
    }
}