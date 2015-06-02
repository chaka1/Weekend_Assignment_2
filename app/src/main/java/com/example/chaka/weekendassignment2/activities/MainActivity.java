package com.example.chaka.weekendassignment2.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
import com.example.chaka.weekendassignment2.helpers.FetchAddressIntentService;
import com.example.chaka.weekendassignment2.interfaces.JustEatApiService;
import com.example.chaka.weekendassignment2.listeners.HidingScrollListener;
import com.example.chaka.weekendassignment2.listeners.RecycleViewClickListener;
import com.example.chaka.weekendassignment2.models.Restaurant;
import com.example.chaka.weekendassignment2.models.Result;
import com.example.chaka.weekendassignment2.util.Util;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Context mContext;

    private ProgressDialog mProgressDialog;

    private RecyclerView mRVRestaurantList;

    private LinearLayoutManager mLinearLayoutManager;

    private List<Restaurant> mRestaurantList;

    private Toolbar mToolbar;

    private TextView mTVEmpty;

    SearchView mSearchView = null;

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    /*
            * Provides the entry point to Google Play services.
            */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The formatted location address.
     */
    protected String mAddressOutput;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     * The user requests an address by pressing the Fetch Address button. This may happen
     * before GoogleApiClient connects. This activity uses this boolean to keep track of the
     * user's intent. If the value is true, the activity tries to fetch the address as soon as
     * GoogleApiClient connects.
     */
    protected boolean mAddressRequested;

    private String last_searched;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        mContext = this;


        mResultReceiver = new AddressResultReceiver(new Handler());

        initToolbar();
        initUI();
        initRecyclerView();

        buildGoogleApiClient();
        requestCurrentLocation();
    }

    private void requestCurrentLocation() {
        mAddressRequested = true;
        // We only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        // If GoogleApiClient isn't connected, we process the user's request by setting
        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;

    }

    private void initUI() {

        mProgressDialog = new ProgressDialog(MainActivity.this);
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

                Intent restaurantIntent = new Intent(mContext,RestaurantActivity.class);
                restaurantIntent.putExtra(Constants.NAME,mRestaurantList.get(position).getName());
                restaurantIntent.putExtra(Constants.LAST_SEARCH,mRestaurantList.get(position).getPostcode());

                Gson gson = new Gson();
                String restaurant = gson.toJson(mRestaurantList.get(position));
                restaurantIntent.putExtra(Constants.RESTAURANT_JSON,restaurant);
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mRestaurantList.get(position).getUrl()));
                startActivity(restaurantIntent);
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

        last_searched = location;

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

        if(id == R.id.action_show_map){

            Intent intent = new Intent(mContext,MapsActivity.class);
            intent.putExtra(Constants.LAST_SEARCH, last_searched);
            startActivity(intent);
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

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        intent.putExtra(Constants.FETCH_ADDRESS_INTENT_ACTION,Constants.GEOCODE);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("TAG", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        //Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //displayAddressOutput();
            String outcode = mAddressOutput.substring(0,mAddressOutput.indexOf(" ")).trim();
            // Show a toast message if an address was found.
           /* if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }*/

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            //mAddressRequested = false;
            //updateUIWidgets();


            search(outcode);
        }
    }
}
