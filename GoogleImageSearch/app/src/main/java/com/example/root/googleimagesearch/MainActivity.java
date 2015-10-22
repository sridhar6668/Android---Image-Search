package com.example.root.googleimagesearch;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private SearchView searchView;
    private final int IMAGE_SIZE = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        addSearchViewOptions(searchView);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        displayLog("Inside configuration changed");
    }

    private void addSearchViewOptions(SearchView searchView){
        searchView.setQueryHint("Search image");
        //searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);

        // Listens for enter key
        searchView.setOnQueryTextListener(searchQueryListener);

        // display suggestions
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

    }


    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            search(query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String query) {
          //  search(query);
            return false;
        }


        public void search(String query) {

            displayLog(query);
            RelativeLayout enlargedDisplaylayout = (RelativeLayout) findViewById(R.id.enlargedDisplayLayout);
            enlargedDisplaylayout.setVisibility(View.GONE);


            LinearLayout layout = (LinearLayout) findViewById(R.id.imagesContainer);
            layout.removeAllViews();

            fetchImageUrl(query);
        }

    };

    public void hideSoftKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    void fetchImageUrl(String query){

          displayLog("preparing code");
        hideSoftKeyboard(this.getCurrentFocus());
            String uri = "http://www.ajax.googleapis.com/ajax/services/search/images";
            RequestParameterPackage p = new RequestParameterPackage();
         //    p.setMethod("POST");
        p.setMethod("GET");
            p.setUri(uri);

            p.setParam("q", query);
            p.setParam("v", "1.0");

            connectionToServer connection = new connectionToServer();
           // connection.execute(p);
            connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);

    }




    private class connectionToServer extends AsyncTask<RequestParameterPackage, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestParameterPackage... params) {
            HttpManager httpManager = new HttpManager();
            String output = null;
            try {
                output = httpManager.makeHttpRequest(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            parseJSON(s);

        }

    }



    private void parseJSON(String output){

        if(output !=null)
        {
            displayLog("Output: " + output);
            try {
                displayLog("Json array stage 1");

                JSONObject jsonOuterObject = new JSONObject(output);
                JSONObject jsonResponseDataObject = jsonOuterObject.getJSONObject("responseData");

                JSONArray jsonArray = jsonResponseDataObject.getJSONArray("results");

                for(int i = 0 ; i < jsonArray.length();i++ ){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //  displayLog("Inside json array");
                    String url = jsonObject.getString("url");
                    displayLog("url: "+ url);
                    fetchImages(url);
                }

            } catch (JSONException e) {
                displayLog(" JSON exception" + e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                displayLog(" IO exception while writing to allCodeJSON file" + e.toString());
                e.printStackTrace();
            }
        }
        else {
            displayLog("Something went wrong. Please try again...");
        }

    }



    private void fetchImages(String url) {

        displayLog("preparing code");
        String uri = url;
        RequestParameterPackage p = new RequestParameterPackage();
        //    p.setMethod("POST");
        p.setMethod("GET");
        p.setUri(uri);


        connectionToServerFetchImages connection = new connectionToServerFetchImages();
        //connection.execute(p);
        connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

    }


    private class connectionToServerFetchImages extends AsyncTask<String, String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            return loadImageFromNetwork(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap imageBitmap) {

            super.onPostExecute(imageBitmap);
            displayImage(imageBitmap);

        }

    }


    private void displayImage(Bitmap imageBitmap){

        if(imageBitmap != null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.imagesContainer);

            final ImageView im = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            layoutParams.setMargins(4, 10, 4, 10);

            //layoutParams
            im.setLayoutParams(layoutParams);

            layout.addView(im);

            im.setImageBitmap(imageBitmap);

            im.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RelativeLayout enlargedDisplaylayout = (RelativeLayout) findViewById(R.id.enlargedDisplayLayout);
                    Bitmap bitmap = ((BitmapDrawable)im.getDrawable()).getBitmap();
                    ImageView enlargerImage = (ImageView) findViewById(R.id.enlargedImageView);
                    enlargerImage.setImageBitmap(bitmap);
                    enlargedDisplaylayout.setVisibility(View.VISIBLE);

                }
            });
        }
    }


    private Bitmap loadImageFromNetwork(String imageUrl)
    {

        try {
            java.net.URL url = new java.net.URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return getResizedBitmap(myBitmap, IMAGE_SIZE, IMAGE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        if(bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    matrix, false);

            return resizedBitmap;
        }
        return null;
    }



    public void enlargedLinearLayoutGone(View view) {
        RelativeLayout enlargedDisplaylayout = (RelativeLayout) findViewById(R.id.enlargedDisplayLayout);
        enlargedDisplaylayout.setVisibility(View.GONE);


    }

    private void displayLog(String s){

        Log.d("imageapi", "main:  " + s);
    }


}
