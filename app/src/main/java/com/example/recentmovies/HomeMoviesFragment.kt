package com.example.recentmovies

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.example.recentmovies.Api_Url.API_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class HomeMoviesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var adapterView = GroupAdapter<ViewHolder>()
    var recviewHome : RecyclerView? = null
    var recviewPopular : RecyclerView? = null
    var progressHome : ProgressBar? = null
    var URL = API_URL
    val Token = Api_Url.Token
    val API_KEY = Api_Url.API_KEY

    private var requestQueue : RequestQueue? = null
    private var idgenre : String? = null
    private var namegenre : String? = null


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

        val layoutManager = GridLayoutManager(getActivity(),1)
        (recviewHome!!.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        recviewHome!!.setLayoutManager(layoutManager)


        return view
    }


}