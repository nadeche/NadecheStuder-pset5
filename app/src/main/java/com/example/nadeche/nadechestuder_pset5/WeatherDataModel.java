package com.example.nadeche.nadechestuder_pset5;

import java.io.Serializable;

/**
 * Created by Nadeche Studer
 *
 * This class contains the martian weather data received from the API.
 * This data is shown on screen.
 * The class implements Serializable so it can be passed to onSaveInstanceState Bundle.
 **/
public class WeatherDataModel implements Serializable{

    private String terrestrial_date;    // contains the earth date
    private long min_temp;              // contains the minimum temperature in celsius
    private long max_temp;              // contains the maximum temperature in celsius
    private Long wind_speed = 0L;       // contains the wind speed (scale unknown)
    private String atmo_opacity;        // contains the weather status
    private long sol;                   // contains the martian solar day since Curiosity's landing (i.e., Curiosity's landing = 0)
    private String season;              // contains the martian season

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public long getSol() {
        return sol;
    }

    public void setSol(long sol) {
        this.sol = sol;
    }

    public String getAtmo_opacity() {
        return atmo_opacity;
    }

    public void setAtmo_opacity(String atmo_opacity) {
        this.atmo_opacity = atmo_opacity;
    }

    public long getMax_temp() {
        return max_temp;
    }

    public void setMax_temp(long max_temp) {
        this.max_temp = max_temp;
    }

    public long getMin_temp() {
        return min_temp;
    }

    public void setMin_temp(long min_temp) {
        this.min_temp = min_temp;
    }

    public String getTerrestrial_date() {
        return terrestrial_date;
    }

    public void setTerrestrial_date(String terrestrial_date) {
        this.terrestrial_date = terrestrial_date;
    }

    public Long getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(Long wind_speed) {
        this.wind_speed = wind_speed;
    }
}
