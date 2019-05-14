/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.KingsNews;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.KingsNews.utilities.JsonParser;
import com.example.android.KingsNews.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String> {

    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    private static final int NEWS_SEARCH_LOADER = 22;

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_news_search_results_json);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String queryUrl = savedInstanceState.getString(SEARCH_QUERY_URL_EXTRA);

            mUrlDisplayTextView.setText(queryUrl);
        }

        /*
         * Initialize the loader
         */
        getSupportLoaderManager().initLoader(NEWS_SEARCH_LOADER, null, this);
    }


    private void makeNewsSearchQuery() {
        String newsQuery = mSearchBoxEditText.getText().toString();


        if (TextUtils.isEmpty(newsQuery)) {
            mUrlDisplayTextView.setText("No query entered, nothing to search for.");
            return;
        }

        URL newsSearchUrl = NetworkUtils.buildUrl(newsQuery);
        mUrlDisplayTextView.setText(newsSearchUrl.toString());

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, newsSearchUrl.toString());


        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> newsSearchLoader = loaderManager.getLoader(NEWS_SEARCH_LOADER);
        if (newsSearchLoader == null) {
            loaderManager.initLoader(NEWS_SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(NEWS_SEARCH_LOADER, queryBundle, this);
        }
    }


    private void showJsonDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the JSON data is visible */
        mSearchResultsTextView.setVisibility(View.VISIBLE);

        mUrlDisplayTextView.setVisibility(View.GONE);
    }


    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String mNewsJson;

            @Override
            protected void onStartLoading() {

                if (args == null) {
                    return;
                }


                if (mNewsJson != null) {
                    deliverResult(mNewsJson);
                } else {

                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    mUrlDisplayTextView.setVisibility(View.GONE);
                    mSearchResultsTextView.setText("Searching...");
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {

                String searchQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);

                if (TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }

                try {
                    URL newsUrl = new URL(searchQueryUrlString);
                    String newsSearchResults = NetworkUtils.getResponseFromHttpUrl(newsUrl);
                    return newsSearchResults;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String newsJson) {
                mNewsJson = newsJson;
                super.deliverResult(newsJson);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        String[] News = null;
        String Display = "";
        if (null == data) {
            showErrorMessage();
        } else {
            try {
                News= JsonParser.parserJSON(MainActivity.this,data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            for (int x=0;x<News.length;x++)
            {
                Display=Display+News[x]+"\n\n--------------------\n\n";
            }
            mSearchResultsTextView.setText(Display);
            
            
            showJsonDataView();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeNewsSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String queryUrl = mUrlDisplayTextView.getText().toString();
        outState.putString(SEARCH_QUERY_URL_EXTRA, queryUrl);
    }
}