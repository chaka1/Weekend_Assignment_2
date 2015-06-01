package com.example.chaka.weekendassignment2.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.chaka.weekendassignment2.R;
import com.example.chaka.weekendassignment2.models.Restaurant;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by Chaka on 29/05/2015.
 */


public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurantsList;

    public RestaurantAdapter(List<Restaurant> pinList) {
        this.restaurantsList = pinList;
    }

    @Override
    public int getItemCount() {
        return restaurantsList.size();
    }

    @Override
    public void onBindViewHolder(final RestaurantViewHolder restaurantViewHolder, int i) {
        final Restaurant restaurant = restaurantsList.get(i);

        restaurantViewHolder.vLogo.setImageURI(Uri.parse(restaurant.getLogo().get(0).getStandardResolutionURL()));
        restaurantViewHolder.vNameText.setText(restaurant.getName());
        restaurantViewHolder.vRestaurantRatingBar.setRating(restaurant.getRatingStars().floatValue());


    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View itemView;

        itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_activity_main_restaurant_item, viewGroup, false);

        return new RestaurantViewHolder(itemView);
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder {

        protected SimpleDraweeView vLogo;
        protected TextView vNameText;
        protected RatingBar vRestaurantRatingBar;
        protected View vView;


        public RestaurantViewHolder(View v) {
            super(v);
            vLogo= (SimpleDraweeView) v.findViewById(R.id.card_activity_main_restaurant_item_sdv_restaurant_logo);
            vNameText = (TextView) v.findViewById(R.id.card_activity_main_restaurant_item_tv_restaurant_name);
            vRestaurantRatingBar = (RatingBar)v.findViewById(R.id.card_activity_main_restaurant_item_rb_rating);
            vView = v;


        }
    }

}
