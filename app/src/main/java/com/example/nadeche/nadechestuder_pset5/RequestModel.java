package com.example.nadeche.nadechestuder_pset5;


/**
 * Created by Nadeche Studer
 *
 * This class holds information about the type of date request to bee done.
 * It holds whether the latest data is requested or which martian solar day is requested.
 * Both values need to be given to the constructor. (sol = -1 when latest data is requested)
 */
public class RequestModel {

    public int sol;          // holds the martian solar date to find
    public boolean latest;   // is true if the latest data is requested

    RequestModel(int sol, boolean latest){
        this.sol = sol;
        this.latest = latest;
    }
}
