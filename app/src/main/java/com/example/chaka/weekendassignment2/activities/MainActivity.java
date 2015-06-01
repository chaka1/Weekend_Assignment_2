package com.example.chaka.weekendassignment2.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chaka.weekendassignment2.R;
import com.example.chaka.weekendassignment2.adapters.RestaurantAdapter;
import com.example.chaka.weekendassignment2.conf.Constants;
import com.example.chaka.weekendassignment2.interfaces.JustEatApiService;
import com.example.chaka.weekendassignment2.listeners.HidingScrollListener;
import com.example.chaka.weekendassignment2.listeners.RecycleViewClickListener;
import com.example.chaka.weekendassignment2.models.Restaurant;
import com.example.chaka.weekendassignment2.models.Result;
import com.example.chaka.weekendassignment2.util.Util;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    Context mContext;

    private ProgressDialog mProgressDialog;

    private RecyclerView mRVRestaurantList;

    private LinearLayoutManager mLinearLayoutManager;

    private List<Restaurant> mRestaurantList;

    private Toolbar mToolbar;

    private TextView mTVEmpty;

    SearchView mSearchView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        mContext = this;

        initToolbar();
        initUI();
        initRecyclerView();
    }

    private void initUI() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.progress_message));
        mProgressDialog.setCancelable(false);

        mTVEmpty = (TextView)findViewById(R.id.activity_main_tv_empty_text);
    }

    private void initToolbar() {

        mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }

    private void initRecyclerView() {

        mRVRestaurantList = (RecyclerView) findViewById(R.id.activity_main_rv_restaurant_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRVRestaurantList.setLayoutManager(mLinearLayoutManager);

        mRVRestaurantList.addOnItemTouchListener(new RecycleViewClickListener(this, new RecycleViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mRestaurantList.get(position).getUrl()));
                startActivity(browserIntent);
            }
        }));
        mRVRestaurantList.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });

        hideEmptyRecyclerView();

    }

    private void hideEmptyRecyclerView() {
        if(mRestaurantList!=null) {

            if (mRestaurantList.isEmpty()) {
                mRVRestaurantList.setVisibility(View.GONE);
                mTVEmpty.setVisibility(View.VISIBLE);
            } else {
                mRVRestaurantList.setVisibility(View.VISIBLE);
                mTVEmpty.setVisibility(View.GONE);
            }
        }else{
            {
                mRVRestaurantList.setVisibility(View.GONE);
                mTVEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    private void search(String location) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API_ROOT_URI)
                .build();



        JustEatApiService service = restAdapter.create(JustEatApiService.class);
        showProgressDialog();
        service.getResult(location, new Callback<Result>() {

            @Override
            public void success(Result result, Response response) {
                mRestaurantList = result.getRestaurants();
                RestaurantAdapter mRestaurantAdapter = new RestaurantAdapter(mRestaurantList);
                mRVRestaurantList.setAdapter(mRestaurantAdapter);
                hideEmptyRecyclerView();
                hideProgressDialog();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Retrofit error", error.toString());
                Log.d("Retrofit error", error.getUrl());

                Toast.makeText(mContext, "Fail", Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        });
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            mSearchView = (SearchView) searchItem.getActionView();
        }
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if(!Util.checkOutCode(s)){
                    SearchView.SearchAutoComplete  mSearchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);

                    mSearchAutoComplete.setError(getString(R.string.main_activity_search_view_error));
                    return true;
                }else{
                    showProgressDialog();
                    return false;
                }

            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
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

    /**
     * Hide toolbar and image button
     */
    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));

        //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        //int fabBottomMargin = lp.bottomMargin;
        //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * Show toolbar and image button
     */
    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }
}
