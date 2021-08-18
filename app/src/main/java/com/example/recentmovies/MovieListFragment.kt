package com.example.recentmovies

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.list_movie_list.view.*
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class MovieListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var adapterView = GroupAdapter<ViewHolder>()
    var genreTitle :String? = null
    var genreId :String? = null
    var recviewList : RecyclerView? = null
    var genreText : TextView? = null

    var URL = Api_Url.API_URL
    val Token = Api_Url.Token
    val API_KEY = Api_Url.API_KEY
    var page = 1

    private var requestQueue : RequestQueue? = null
    private var movieTitle : String? = null
    private var movieImg : String? = null
    private var movieOverview : String? = null
    private var movieId : String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_movie_list, container, false)

        genreTitle = arguments?.getString("genre")
        genreId = arguments?.getString("id")

        val cache = DiskBasedCache(requireActivity().cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

        recviewList = view.findViewById(R.id.rvlistgenre)
        genreText = view.findViewById(R.id.textView4)
        genreText!!.text = genreTitle.toString()

        val layoutManager = GridLayoutManager(getActivity(),1)
        (recviewList!!.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        recviewList!!.setLayoutManager(layoutManager)
        recviewList!!.adapter = adapterView

        requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        adapterView.setOnItemClickListener{item, view ->
            val listitem = item as ListItem
            val bundle = Bundle()
            bundle.putString("id",listitem.movieid.toString())
            view.findNavController().navigate(R.id.action_ListGenre_to_DetailMovie,bundle)
        }

        recviewList!!.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy>0){
                    var vItem = layoutManager.childCount
                    var lItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                    var count = adapterView.itemCount

                    Log.d("aaa",vItem.toString())
                    Log.d("bbb",lItem.toString())
                    Log.d("ccc",count.toString())

                    if(vItem + lItem >= count){
                        requestQueue = RequestQueue(cache, network).apply {
                            start()
                        }
                        fetchList(genreId!!)
                    }

                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })


        fetchList(genreId!!)
        return view
    }

    private fun fetchList(genreID : String){

        val address = URL+"discover/movie?api_key="+API_KEY+"&language=en-US"+"&with_genres="+genreID+"&page="+page
        Log.d("gen_id",genreID.toString())
        Log.d("add",address.toString())
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,address, null,
            Response.Listener<JSONObject>{ response ->


//                if(response.getInt("status_code")==200){
                val pages = response.getString("page")
                val results = response.getJSONArray("results")
                if(results.length()<1){
                    if(adapterView.itemCount == 0){
                        Toast.makeText(requireContext().applicationContext,"The item you looking doesn't exist",
                            Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(requireContext().applicationContext,"The item you looking doesn't exist",
                            Toast.LENGTH_LONG).show()
                    }

                } else{
                    page = response.getString("page").toInt()+1

                    for(i in 0 until results.length()){
                        val jsonobj : JSONObject = results.getJSONObject(i)

                        movieTitle = jsonobj.getString("original_title")
                        movieOverview = jsonobj.getString("overview")
                        movieImg = jsonobj.getString("poster_path")
                        movieId = jsonobj.getString("id")

                        adapterView.add(
                            ListItem(
                                movieId!!,
                                movieTitle!!,
                                movieImg!!,
                                movieOverview!!,
                                requireContext()
                            )
                        )
                    }

                }

//                }

            },
            Response.ErrorListener {
                Toast.makeText(requireContext().applicationContext,"Connection Error",
                    Toast.LENGTH_LONG).show()
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

    class ListItem(val movieid: CharSequence,val movietitle: CharSequence, val movieimg : CharSequence, val movieoverview : CharSequence, val context: Context): Item<ViewHolder>(){

        override fun bind(viewHolder: ViewHolder, position: Int) {
            val movieimage = viewHolder.itemView.prodImg
            val moviettl = viewHolder.itemView.prodName
            val movieovr = viewHolder.itemView.prodPrice
            moviettl.text = movietitle.toString()
            movieovr.text = movieoverview.toString()
//            Picasso.get().load("https://image.tmdb.org/t/p/w500"+movieimg.toString()).into(movieimage)
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500"+movieimg.toString())
                .into(movieimage)
        }

        override fun getLayout(): Int {
            return R.layout.list_movie_list
        }
    }

}