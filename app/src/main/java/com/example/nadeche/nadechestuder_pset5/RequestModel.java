package com.example.nadeche.nadechestuder_pset5;


/**
 * Created by Nadeche
 */
public class RequestModel {

    public int sol;        // holds the martian solar date to find
    public boolean latest;   // is true if the latest data is requested

    RequestModel(int sol, boolean latest){
        this.sol = sol;
        this.latest = latest;
    }
}
