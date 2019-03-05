package me.smeef.flickstr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import me.smeef.flickstr.models.Movie;

public class DetailActivity extends YouTubeBaseActivity {

    private String YOUTUBE_API_KEY;
    private static final String TRAILERS_API = "https://api.themoviedb.org/3/movie/%d/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed";

    TextView tvTitle;
    TextView tvOverview;
    TextView tvReleaseDate;
    RatingBar ratingBar;

    YouTubePlayerView youTubePlayerView;

    Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        YOUTUBE_API_KEY = getString(R.string.youtube_api_key);

        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        ratingBar = findViewById(R.id.ratingBar);

        youTubePlayerView = findViewById(R.id.player);


        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra("movie"));
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(String.format("Overview: \n%s", movie.getOverview()));
        tvReleaseDate.setText(String.format("Released on: %s", movie.getReleaseDate()));
        ratingBar.setRating((float) movie.getVoteAverage());

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(String.format(TRAILERS_API, movie.getMovieId()), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    JSONArray results = response.getJSONArray("results");

                    if(results.length() == 0) {
                        return;
                    }

                    String youtubeKey = null;

                    for(int index = 0; index < results.length(); index++) {
                        JSONObject movieTrailer = results.getJSONObject(index);
                        String site = movieTrailer.getString("site");

                        if(site.equals("YouTube")) {
                            youtubeKey = movieTrailer.getString("key");
                            break;
                        }
                    }

                    if(youtubeKey == null) {
                        return;
                    }

                    initializeYoutube(youtubeKey);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable) {
                super.onFailure(statusCode, headers, errorResponse, throwable);
            }
        });
    }

    private void initializeYoutube(final String youtubeKey) {
        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.d("smile", "on init success");
                youTubePlayer.cueVideo(youtubeKey);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("smile", "on init failure");
            }
        });
    }
}