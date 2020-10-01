package com.ludovic.go4lunch.Nearby;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OpeningHours implements Serializable {

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours_ openingHours;
    private final static long serialVersionUID = -170902697916306093L;

    /**
     * No args constructor for use in serialization
     */
    public OpeningHours() {
    }

    /**
     * @param openingHours
     */
    public OpeningHours(OpeningHours_ openingHours) {
        super();
        this.openingHours = openingHours;
    }

    public OpeningHours_ getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours_ openingHours) {
        this.openingHours = openingHours;
    }

    public OpeningHours withOpeningHours(OpeningHours_ openingHours) {
        this.openingHours = openingHours;
        return this;
    }


    public class OpeningHours_ implements Serializable {

        @SerializedName("open_now")
        @Expose
        private Boolean openNow;
        private final static long serialVersionUID = -2836367103499752083L;

        /**
         * No args constructor for use in serialization
         */
        public OpeningHours_() {
        }

        /**
         * @param openNow
         */
        public OpeningHours_(Boolean openNow) {
            super();
            this.openNow = openNow;
        }

        public Boolean getOpenNow() {
            return openNow;
        }

        public void setOpenNow(Boolean openNow) {
            this.openNow = openNow;
        }

        public OpeningHours_ withOpenNow(Boolean openNow) {
            this.openNow = openNow;
            return this;
        }

    }
}