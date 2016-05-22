package com.example.nadeche.nadechestuder_pset5;

/**
 * Created by Nadeche Studer
 *
 * This class contains information about the type of date request to be done.
 * It contains whether the latest data is requested or which martian solar day is requested.
 * The constructor with sol requests a particular solar day and
 * the constructor without arguments requests the latest solar day.
 */
class RequestModel {

    public int sol;          // holds the martian solar date to find
    public boolean latest;   // is true if the latest data is requested

    RequestModel(int sol){
        this.sol = sol;
        this.latest = false;
    }

    RequestModel() {
        this.sol = -1;
        this.latest = true;
    }
}
