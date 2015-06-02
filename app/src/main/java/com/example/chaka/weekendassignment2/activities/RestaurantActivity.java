package com.example.chaka.weekendassignment2.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chaka.weekendassignment2.R;
import com.example.chaka.weekendassignment2.conf.Constants;
import com.example.chaka.weekendassignment2.helpers.FetchAddressIntentService;
import com.example.chaka.weekendassignment2.models.Restaurant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.List;

public class RestaurantActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private List<Restaurant> mRestaurantList;

    private Context mContext;

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

    protected  int currentPosition;

    private TextView mNameText;

    private TextView mAddressText;

    private TextView mCityText;

    private TextView mPostcodeText;

    private TextView mIsOpenText;

    private Restaurant mRestaurant;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_restaurant);
        mResultReceiver = new AddressResultReceiver(new Handler());
        buildGoogleApiClient();
        setUpMapIfNeeded();

        mNameText = (TextView)findViewById(R.id.restaurantsActivityTVName);
        mNameText.setText(getIntent().getStringExtra(Constants.NAME));
        Gson gson = new Gson();

        mRestaurant = gson.fromJson(getIntent().getStringExtra(Constants.RESTAURANT_JSON),Restaurant.class);

        mAddressText = (TextView)findViewById(R.id.restaurantsActivityTVAddress);
        mAddressText.setText(mRestaurant.getAddress());

        mCityText = (TextView)findViewById(R.id.restaurantsActivityTVCity);
        mCityText.setText(mRestaurant.getCity());

        mPostcodeText = (TextView)findViewById(R.id.restaurantsActivityTVPostCode);
        mPostcodeText.setText(mRestaurant.getPostcode());

        mIsOpenText = (TextView)findViewById(R.id.restaurantsActivityTVIsOpen);
        if(mRestaurant.getIsOpenNow()){
            mIsOpenText.setText("Open");
        }else{
            mIsOpenText.setText("Closed");
        }

        String mLastSearched = getIntent().getStringExtra(Constants.LAST_SEARCH);
        addMarker(mLastSearched);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private void addMarker(String location) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);

        intent.putExtra(Constants.FETCH_ADDRESS_INTENT_ACTION,Constants.REVERSE_GEOCODE);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        //intent.putExtra(Constants.RESTAURANTPOSITION,current);


        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);

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


     public void navigateTo(View view){

         Uri gmmIntentUri = Uri.parse("google.navigation:q="+mRestaurant.getPostcode()+", UK");
         Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
         mapIntent.setPackage("com.google.android.apps.maps");
         startActivity(mapIntent);


     }
    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        intent.putExtra(Constants.RESTAURANTPOSITION, currentPosition);


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
    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }
    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {

        mMap.animateCamera(update, callback);

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
            //String outcode = mAddressOutput.substring(0,mAddressOutput.indexOf(" ")).trim();
            // Show a toast message if an address was found.
           /* if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }*/

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            //mAddressRequested = false;
            //updateUIWidgets();


            String[] address = TextUtils.split(mAddressOutput, System.getProperty("line.separator"));
/*            Log.d("onReceiveRestult",mAddressOutput + " Cuisine type id: " + mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId()+
                    " Cuisine type name: " + mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getName());*/
            //search(outcode);
                CameraPosition firstRestaurant =
                        new CameraPosition.Builder().target(new LatLng(Double.valueOf(address[2]), Double.valueOf(address[1])))
                                .zoom(15f)
                                .build();
                changeCamera(CameraUpdateFactory.newCameraPosition(firstRestaurant));



            MarkerOptions marker = new MarkerOptions().position(new LatLng(Double.valueOf(address[2]), Double.valueOf(address[1]))).title(mNameText.getText().toString());

            try {
                if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 31) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 82) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 33) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                }else if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 118) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 79) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                }else if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 83) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }else if (mRestaurantList.get(currentPosition).getCuisineTypes().get(0).getId() == 27) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                }

            }catch(Exception e){
                Log.e("EXCEPTION","cuisine exception",e);
            }

            mMap.addMarker(marker);


        }
    }
}
