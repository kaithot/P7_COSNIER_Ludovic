package com.ludovic.go4lunch.Nearby;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ludovic.go4lunch.Details.RestaurantResult;

import java.util.List;

public class ListDetailResult {

    @SerializedName("html_attributions")
@Expose
private List<Object> htmlAttributions = null;
    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;
    @SerializedName("result")
    @Expose
    private RestaurantResult result = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public RestaurantResult getResult() {
        return result;
    }

    public void setResults(RestaurantResult result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
