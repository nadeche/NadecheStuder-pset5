package com.example.nadeche.nadechestuder_pset5;

import java.io.Serializable;

/**
 * Created by Nadeche Studer
 */
public class WeatherDataModel implements Serializable{

    private String terrestrial_date;
    private long min_temp;
    private long max_temp;
    private Long wind_speed = 0L;
    private String atmo_opacity;
    private long sol;

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
