package com.example.chaka.weekendassignment2.models;

import com.google.gson.annotations.Expose;

public class Logo {

    @Expose
    private String StandardResolutionURL;

    /**
     *
     * @return
     * The StandardResolutionURL
     */
    public String getStandardResolutionURL() {
        return StandardResolutionURL;
    }

    /**
     *
     * @param StandardResolutionURL
     * The StandardResolutionURL
     */
    public void setStandardResolutionURL(String StandardResolutionURL) {
        this.StandardResolutionURL = StandardResolutionURL;
    }

}
