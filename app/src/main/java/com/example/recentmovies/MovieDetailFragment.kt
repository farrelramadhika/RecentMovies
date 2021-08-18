package com.example.recentmovies

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.list_movie_detail.view.*
import org.json.JSONObject


class MovieDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var adapterView = GroupAdapter<ViewHolder>()
    var youtubeplayer : YouTubePlayerView? = null
    var coverMovie : ImageView? = null
    var tvTitle : TextView? = null
    var tvOverview : TextView? = null
    var recviewDetail : RecyclerView? = null
    var bundleLink : Bundle? = null

    var URL = Api_Url.API_URL
    val Token = Api_Url.Token
    val API_KEY = Api_Url.API_KEY

    private var requestQueue : RequestQueue? = null
    private var movieId: String? = null
    private var movieImg: String? = null
    private var movieTitle: String? = null
    private var movieOverview: String? = null
    private var movieVideo: String? = null

    private var reviewImg: String? = null
    private var reviewName: String? = null
    private var reviewOverview: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_movie_detail, container, false)

        movieId = arguments?.getString("id")

        val cache = DiskBasedCache(requireActivity().cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

        requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        bundleLink = Bundle()

        youtubeplayer = view.findViewById(R.id.ytb)
        lifecycle.addObserver(youtubeplayer!!)

        youtubeplayer!!.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                Log.d("ytkey",movieVideo!!)
                youTubePlayer.loadVideo(movieVideo!!, 0F)
            }
        })

        coverMovie = view.findViewById(R.id.imageView2)
        tvTitle = view.findViewById(R.id.textView3)
        tvOverview = view.findViewById(R.id.textView5)
        recviewDetail = view.findViewById(R.id.recviewReview)

        val layoutManager = GridLayoutManager(getActivity(),1)
        (recviewDetail!!.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        recviewDetail!!.setLayoutManager(layoutManager)
        recviewDetail!!.adapter = adapterView


        fetchVideo(movieId!!)
        fetchDetail(movieId!!)
        fetchReview(movieId!!)

        return view
    }

    private fun fetchVideo(movieId : String){
        val address = URL+"movie/"+movieId+"/videos?api_key="+API_KEY+"&language=en-US"
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,address, null,
            Response.Listener<JSONObject>{ response ->


//                if(response.getInt("status_code")==200){
                val results = response.getJSONArray("results")
                val jsonobj : JSONObject = results.getJSONObject(0)

                movieVideo = jsonobj.getString("key")
                Log.d("vid_key",movieVideo)
                bundleLink!!.putString("key",movieVideo.toString())


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

    private fun fetchDetail(movieId : String){
        val address = URL+"movie/"+movieId+"?api_key="+API_KEY+"&language=en-US"
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,address, null,
            Response.Listener<JSONObject>{ response ->



                movieOverview = response.getString("overview")
                movieTitle = response.getString("original_title")
                movieImg = response.getString("poster_path")

                tvTitle!!.text = movieTitle.toString()
                tvOverview!!.text = movieOverview.toString()
//            Picasso.get().load("https://image.tmdb.org/t/p/w500"+movieimg.toString()).into(movieimage)
                Glide.with(requireContext())
                    .load("https://image.tmdb.org/t/p/w500"+movieImg.toString())
                    .into(coverMovie!!)

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

    private fun fetchReview(movieId : String){

        val address = URL+"/movie/"+movieId+"/reviews?api_key="+API_KEY+"&language=en-US"+"&page=1"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,address, null,
            Response.Listener<JSONObject>{ response ->


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

                    for(i in 0 until results.length()){
                        val jsonobj : JSONObject = results.getJSONObject(i)

                        val authorD  = jsonobj.getJSONObject("author_details")

                        reviewImg = authorD.getString("avatar_path")
                        Log.d("avatar_review",reviewImg)
                        reviewName = authorD.getString("username")

                        reviewOverview = jsonobj.getString("content")

                        adapterView.add(
                            ReviewItem(
                                reviewImg!!,
                                reviewName!!,
                                reviewOverview!!,
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

    class ReviewItem (val revImg : CharSequence,val revName : CharSequence,val revOvr : CharSequence,val context: Context): Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {
            val rname = viewHolder.itemView.prodName
            val rimg = viewHolder.itemView.prodImg
            val rovr = viewHolder.itemView.prodPrice

            rname.text = revName.toString()
            rovr.text = revOvr.toString()
            Picasso.get().load(revImg.toString()).into(rimg)
//            Glide.with(context)
//                .load(revImg.toString())
//                .into(rimg)
        }

        override fun getLayout(): Int {
            return R.layout.list_movie_detail
        }
    }

}