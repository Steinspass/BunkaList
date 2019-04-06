package com.bunkalogic.bunkalist.Activities.DetailsActivities

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bunkalogic.bunkalist.Dialog.AddListDialog
import com.bunkalogic.bunkalist.Others.Constans
import com.bunkalogic.bunkalist.R
import com.bunkalogic.bunkalist.Retrofit.OnGetMovieCallback
import com.bunkalogic.bunkalist.Retrofit.OnGetSeriesCallback
import com.bunkalogic.bunkalist.Retrofit.OnGetTrailersCallback
import com.bunkalogic.bunkalist.Retrofit.Response.Movies.Movie
import com.bunkalogic.bunkalist.Retrofit.Response.SeriesAndAnime.Series
import com.bunkalogic.bunkalist.Retrofit.Response.Trailer
import com.bunkalogic.bunkalist.RxBus.RxBus
import com.bunkalogic.bunkalist.data.ViewModelSearch
import com.bunkalogic.bunkalist.db.ItemListRating
import com.bunkalogic.bunkalist.db.NewListRating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.search_item_full_details.*
import org.jetbrains.anko.toast
import com.bumptech.glide.request.RequestOptions
import android.content.Intent
import android.net.Uri
import android.widget.ImageView


class ItemDetailsActivity : AppCompatActivity() {

    private lateinit var searchViewModel: ViewModelSearch

    private lateinit var toolbar: android.support.v7.widget.Toolbar

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var currentUser: FirebaseUser

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var addItemListDBRef: CollectionReference

    private var itemRatingSubscription: ListenerRegistration? = null
    private lateinit var itemRatingBusListener: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.search_item_full_details)
        super.onCreate(savedInstanceState)
        setUpToolbarTitle()

        //instantiate the ViewModel
        searchViewModel = ViewModelProviders.of(this).get(ViewModelSearch::class.java)
        setUpAddListDB()
        setUpCurrentUser()
        isMovieOrSerie()
        onClick()
        addToNewItemRating()
    }

    // You can select the title of the toolbar
    private fun setUpToolbarTitle(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val extrasTitle = intent.extras.getString("title")
        val extrasName = intent.extras.getString("name")

        if (extrasTitle == null){
            supportActionBar!!.title = extrasName
        }else{
            supportActionBar!!.title = extrasTitle
        }

    }




    // Initializing the currentUser
    private fun setUpCurrentUser(){
        currentUser = mAuth.currentUser!!
    }

    private fun onClick(){
        buttonSearchItemDetailsAddInYourList.setOnClickListener {
            AddListDialog().show(supportFragmentManager, "")
        }
    }

    // check if it is a series or a film and around it loads a function or another
    private fun isMovieOrSerie(){
        val type = "tv"
        val extrasType = intent.extras.getString("type")
        Log.d("SearchItemDetailsAct", "type: $extrasType")

        if (extrasType == type){
            isSerieAndAnime()
            getTrailersSeries()
        }else{
            isMovie()
            getTrailersMovies()
        }
    }

    // is responsible for collecting the Id of the film and filling the function of the ViewModel
    private fun getMovieContentForID(callback: OnGetMovieCallback){
        val extrasIdMovies = intent.extras.getInt("id")
        Log.d("SearchItemDetailsActMov", "Id: $extrasIdMovies")
        searchViewModel.getMovie(extrasIdMovies, callback)
    }
    // is responsible for filling the layout with the elements obtained from the API
    private fun isMovie(){
        getMovieContentForID(object: OnGetMovieCallback{
            override fun onSuccess(movie: Movie) {
                textViewTitleDetails.text = movie.title
                textViewDateRelease.text = movie.releaseDate
                textViewSearchDetailsAllDescription.text = movie.overview
                textViewDetailsRating.text = movie.voteAverage.toString()
                //Todo: Implement the list of genres

                val imageBackground = movie.backdropPath
                val imagePoster = movie.posterPath

                // Here I check if imageBackground is different from null and if it is null charge the imagePoster
                if (imageBackground != null){
                    Glide.with(this@ItemDetailsActivity)
                        .load(Constans.API_MOVIE_SERIES_ANIME_BASE_URL_IMG_PATH_BACKDROP + imageBackground)
                        .into(imageViewBackDrop)
                }else{
                    Glide.with(this@ItemDetailsActivity)
                        .load(Constans.API_MOVIE_SERIES_ANIME_BASE_URL_IMG_PATH_POSTER + imagePoster)
                        .into(imageViewBackDrop)
                }
            }

            override fun onError() {
                Log.d("SearchItemDetailsActMov", "Error data Movie")
                toast("Please check your internet connection")
            }

        })
    }

    // is responsible for collecting the Id of the film and filling the function of the ViewModel
    private fun getMovieTrailersContentForID(callback: OnGetTrailersCallback){
        val extrasIdMovies = intent.extras.getInt("id")
        searchViewModel.getTrailersMovies(extrasIdMovies, callback)
    }

    // is responsible for collecting the key to create the YouTube URL and show the video image
    private fun getTrailersMovies(){
        getMovieTrailersContentForID(object : OnGetTrailersCallback{
            override fun onSuccess(trailers: List<Trailer>) {
              trailersLabel.visibility = View.VISIBLE
                LinearTrailers.removeAllViews()

                for (trailer in trailers) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_trailer, LinearTrailers, false)
                    val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                    thumbnail.requestLayout()
                    thumbnail.setOnClickListener{
                        showTrailer(String.format(Constans.YOUTUBE_VIDEO_URL, trailer.key))
                    }
                    Glide.with(this@ItemDetailsActivity)
                        .load(String.format(Constans.YOUTUBE_THUMBNAIL_URL, trailer.key))
                        .apply(RequestOptions.placeholderOf(R.color.colorPrimary).centerCrop())
                        .into(thumbnail)
                    LinearTrailers.addView(parent)
                }

            }

            override fun onError() {
                trailersLabel.visibility = View.GONE
            }

        })
    }



    // is responsible for collecting the Id of the series and filling the function of the ViewModel
    private fun getSeriesContentForID(callback: OnGetSeriesCallback){
        val extrasIdSeries = intent.extras.getInt("id")
        Log.d("SearchItemDetailsActTV", "Id: $extrasIdSeries")
        searchViewModel.getSeriesAndAnime(extrasIdSeries, callback)
    }
    // is responsible for filling the layout with the elements obtained from the API
    private fun isSerieAndAnime(){
            getSeriesContentForID(object : OnGetSeriesCallback{
                override fun onSuccess(series: Series) {
                    textViewTitleDetails.text = series.name
                    textViewDateRelease.text = series.firstAirDate
                    textViewSearchDetailsAllDescription.text = series.overview
                    textViewDetailsRating.text = series.voteAverage.toString()
                    //Todo: Implement the list of genres

                    val imageBackground = series.backdropPath
                    val imagePoster = series.posterPath
                    // Here I check if imageBackground is different from null and if it is null charge the imagePoster
                    if (imageBackground != null){
                        Glide.with(this@ItemDetailsActivity)
                            .load(Constans.API_MOVIE_SERIES_ANIME_BASE_URL_IMG_PATH_BACKDROP + imageBackground)
                            .into(imageViewBackDrop)
                    }else{
                        Glide.with(this@ItemDetailsActivity)
                            .load(Constans.API_MOVIE_SERIES_ANIME_BASE_URL_IMG_PATH_POSTER + imagePoster)
                            .into(imageViewBackDrop)
                    }
                }

                override fun onError() {
                    Log.d("SearchItemDetailsActTV", "Error data Series And Anime")
                    toast("Please check your internet connection")
                }

            })
    }

    // is responsible for collecting the Id of the series and filling the function of the ViewModel
    private fun getSeriesTrailersContentForID(callback: OnGetTrailersCallback){
        val extrasIdSeries = intent.extras.getInt("id")
        searchViewModel.getTrailersSeries(extrasIdSeries, callback)
    }

    // is responsible for collecting the key to create the YouTube URL and show the video image
    private fun getTrailersSeries(){
        getSeriesTrailersContentForID(object : OnGetTrailersCallback{
            override fun onSuccess(trailers: List<Trailer>) {
                trailersLabel.visibility = View.VISIBLE
                LinearTrailers.removeAllViews()

                for (trailer in trailers) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_trailer, LinearTrailers, false)
                    val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                    thumbnail.requestLayout()
                    thumbnail.setOnClickListener{
                        showTrailer(String.format(Constans.YOUTUBE_VIDEO_URL, trailer.key))
                    }
                    Glide.with(this@ItemDetailsActivity)
                        .load(String.format(Constans.YOUTUBE_THUMBNAIL_URL, trailer.key))
                        .apply(RequestOptions.placeholderOf(R.color.colorPrimary).centerCrop())
                        .into(thumbnail)
                    LinearTrailers.addView(parent)
                }

            }

            override fun onError() {
                trailersLabel.visibility = View.GONE
            }

        })
    }

    // Is responsible for launching the YouTube App
    private fun showTrailer(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    //Creating the name instance in the database
    private fun setUpAddListDB() {
         addItemListDBRef = store.collection("RatingList")
     }

     //Creating the new instance in the database
    private fun saveItemRatingList(itemListRating: ItemListRating){
        addItemListDBRef.add(itemListRating)
            .addOnCompleteListener {
                toast("Add in your list")
                Log.d("SearchItemDetailsAct", "itemRating added on firestore")
            }
            .addOnFailureListener {
                toast("Fail add in your list")
                Log.d("SearchItemDetailsAct", "itemRating error not added on firestore")
            }
    }


   private fun addToNewItemRating(){
       itemRatingBusListener = RxBus.listen(NewListRating::class.java).subscribe {
           saveItemRatingList(it.itemListRating)
       }
   }

   override fun onDestroy() {
       itemRatingBusListener.dispose()
       itemRatingSubscription?.remove()
       super.onDestroy()
   }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}
