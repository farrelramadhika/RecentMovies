package com.example.recentmovies

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.recentmovies.Api_Url.API_URL
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.list_genre.view.*
import kotlinx.android.synthetic.main.list_movie_popular.view.*
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class HomeMoviesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var adapterView = GroupAdapter<ViewHolder>()
    var adapterView2 = GroupAdapter<ViewHolder>()
    var recviewHome : RecyclerView? = null
    var recviewPopular : RecyclerView? = null
    var progressHome : ProgressBar? = null
    var URL = API_URL
    val Token = Api_Url.Token
    val API_KEY = Api_Url.API_KEY

    private var requestQueue : RequestQueue? = null

    private var idgenre : String? = null
    private var namegenre : String? = null
    private var movietitle : String? = null
    private var movieimg : String? = null
    private var movieid : String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_home_movies, container, false)

        val cache = DiskBasedCache(requireActivity().cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

        recviewHome = view.findViewById(R.id.rvgenre)
        recviewPopular = view.findViewById(R.id.rvpopular)
        progressHome = view.findViewById(R.id.progressHome)

        val layoutManager = GridLayoutManager(getActivity(),3)
        (recviewHome!!.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        recviewHome!!.setLayoutManager(layoutManager)
        recviewHome!!.adapter = adapterView

        val layoutManager2 = GridLayoutManager(getActivity(),3)
        (recviewPopular!!.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        recviewPopular!!.setLayoutManager(layoutManager2)
        recviewPopular!!.adapter = adapterView2

        adapterView.setOnItemClickListener{item, view ->
            val genreitem = item as GenreItem
            val bundle = Bundle()
            bundle.putString("id",genreitem.idgenre.toString())
            bundle.putString("genre",genreitem.namegenre.toString())
            view.findNavController().navigate(R.id.action_mainFragment_to_ListGenre,bundle)
        }

        adapterView2.setOnItemClickListener{item, view ->
            val popularitem = item as PopularItem
            val bundlepop = Bundle()
            bundlepop.putString("id",popularitem.movieid.toString())
            view.findNavController().navigate(R.id.action_mainFragment_to_DetailMovie,bundlepop)
        }

        requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        recviewPopular!!.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy>0){
                    var vItem = layoutManager2.childCount
                    var lItem = layoutManager2.findFirstCompletelyVisibleItemPosition()
                    var count = adapterView2.itemCount

                    Log.d("aaa",vItem.toString())
                    Log.d("bbb",lItem.toString())
                    Log.d("ccc",count.toString())

                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })

        fetchGenre(recviewHome!!)
        fetchPopular(recviewPopular!!)
        return view
    }

    private fun fetchGenre(recviewHome : RecyclerView){
        val address = URL+"genre/movie/list?api_key="+API_KEY+"&language=en-US"
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,address, null,
            Response.Listener<JSONObject>{ response ->


//                if(response.getInt("status_code")==200){
                    val genres = response.getJSONArray("genres")
                    if(genres.length()<1){
                        if(adapterView.itemCount == 0){
                            Toast.makeText(requireContext().applicationContext,"The item you looking doesn't exist",Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(requireContext().applicationContext,"The item you looking doesn't exist",Toast.LENGTH_LONG).show()
                        }

                    } else{
                        for(i in 0 until genres.length()){
                            val jsonobj : JSONObject = genres.getJSONObject(i)

                            namegenre = jsonobj.getString("name")
                            idgenre = jsonobj.getString("id")

                            adapterView.add(GenreItem(idgenre!!,namegenre!!))
                        }

                    }

//                }

            },
            Response.ErrorListener {

            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers["authorization"] = "Bearer "+Token
                return headers
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            7500,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue!!.add(jsonObjectRequest)

    }

    private fun fetchPopular(recviewPopular : RecyclerView){
        val address = URL+"movie/popular?api_key="+API_KEY+"&language=en-US"+"&page=1"
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,address, null,
            Response.Listener<JSONObject>{ response ->


//                if(response.getInt("status_code")==200){
                val page = response.getString("page")
                val results = response.getJSONArray("results")
                if(results.length()<1){
                    if(adapterView2.itemCount == 0){
                        Toast.makeText(requireContext().applicationContext,"The item you looking doesn't exist",Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(requireContext().applicationContext,"The item you looking doesn't exist",Toast.LENGTH_LONG).show()
                    }

                } else{
                    for(i in 0 until results.length()){
                        val jsonobj : JSONObject = results.getJSONObject(i)

                        movietitle = jsonobj.getString("original_title")
                        movieimg = jsonobj.getString("poster_path")
                        movieid = jsonobj.getString("id")

                        adapterView2.add(PopularItem(movieid!!,movietitle!!,movieimg!!,requireContext()))
                    }

                }

//                }

            },
            Response.ErrorListener {

            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers["authorization"] = "Bearer "+Token
                return headers
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            7500,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue!!.add(jsonObjectRequest)

    }

    override fun onDestroy() {
        if(adapterView.itemCount>0){
            adapterView.clear()
        }
        if(adapterView2.itemCount>0){
            adapterView2.clear()
        }
        if(requestQueue!=null){
            requestQueue!!.cancelAll { true }
            requestQueue!!.cache.clear()
        }
        super.onDestroy()
    }

    override fun onDestroyView() {
        if(adapterView2.itemCount>0){
            adapterView2.clear()
        }
        if(adapterView.itemCount>0){
            adapterView.clear()
        }
        if(requestQueue!=null){
            requestQueue!!.cancelAll { true }
            requestQueue!!.cache.clear()
        }
        super.onDestroyView()
    }

    class GenreItem(val idgenre: CharSequence, val namegenre :CharSequence): Item<ViewHolder>() {

        override fun bind(viewHolder: ViewHolder,position :Int){
            val genrelist = viewHolder.itemView.genre_list

            genrelist.text = namegenre.toString()
        }
        override fun getLayout(): Int {
            return R.layout.list_genre
        }

    }

    class PopularItem(val movieid: CharSequence,val movietitle: CharSequence, val movieimg : CharSequence, val context:Context): Item<ViewHolder>(){

        override fun bind(viewHolder: ViewHolder, position: Int) {
            val movieimage = viewHolder.itemView.prodImg
//            Picasso.get().load(movieimg.toString()).into(movieimage)
//            Picasso.get().load("https://image.tmdb.org/t/p/w500"+movieimg.toString()).into(movieimage)
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500"+movieimg.toString())
                .into(movieimage)
        }

        override fun getLayout(): Int {
            return R.layout.list_movie_popular
        }
    }



}