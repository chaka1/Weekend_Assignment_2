package com.example.chaka.weekendassignment2.models;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class Result {

    @Expose
    private String ShortResultText;
    @Expose
    private List<Restaurant> Restaurants = new ArrayList<Restaurant>();

    /**
     *
     * @return
     * The ShortResultText
     */
    public String getShortResultText() {
        return ShortResultText;
    }

    /**
     *
     * @param ShortResultText
     * The ShortResultText
     */
    public void setShortResultText(String ShortResultText) {
        this.ShortResultText = ShortResultText;
    }

    /**
     *
     * @return
     * The Restaurants
     */
    public List<Restaurant> getRestaurants() {
        return Restaurants;
    }

    /**
     *
     * @param Restaurants
     * The Restaurants
     */
    public void setRestaurants(List<Restaurant> Restaurants) {
        this.Restaurants = Restaurants;
    }

}