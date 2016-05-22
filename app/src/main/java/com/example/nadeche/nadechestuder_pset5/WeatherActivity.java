package com.example.nadeche.nadechestuder_pset5;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nadeche Studer
 *
 * This activity displays the latest martian weather data sent to earth by curiosity.
 * On start of the activity the latest data is automatically loaded.
 * True the calender icon in the actionbar the user can request weather data from any martian solar day,
 * since curiosity's landing on mars. Solar day 0 signifies the landing day of curiosity.
 * Since weather data started to come in form solar day 15 the user can search from this day on.
 * From day 15 on curiosity didn't send back data every day, so it could be that there is no weather data
 * for the solar day the user searched for. In that case the previous data stays on screen.
 * To go back to the latest weather data (or check if there are new latest data)
 * the user can tap on the home icon in the actionbar.
 *
 * Weather data is gathered true the API from: marsweather.ingenology.com
 * */

public class WeatherActivity extends AppCompatActivity {

    private TextView dateOfDataTextView;        // holds the earth date of the data
    private TextView solTextView;               // holds the martian solar day of the data
    private TextView minCelsiusTextView;        // holds the minimum temperature in C
    private TextView maxCelsiusTextView;        // holds the maximum temperature in C
    private TextView windSpeedDataTextView;     // holds the wind speed (unknown scale)
    private TextView opacityDataTextView;       // holds the status of the weather
    private TextView seasonDataTextView;        // holds the martian season
    private TextView lastUpdateTextView;        // holds the date and time of the last update
    private WeatherDataModel weatherData;       // holds the weather data send back by the API
    private int latestSol;                      // holds the latest solar day number
    private Dialog dialog;                      // holds the dialog to search for a differed solar day

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar actionBar = (Toolbar)findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);

        // initialize fields
        dateOfDataTextView = (TextView)findViewById(R.id.dateTextView);
        solTextView = (TextView)findViewById(R.id.solTextView);
        minCelsiusTextView = (TextView)findViewById(R.id.minCelsiusTextView);
        maxCelsiusTextView = (TextView)findViewById(R.id.maxCelsiusTextView);
        windSpeedDataTextView = (TextView)findViewById(R.id.windDataTextView);
        opacityDataTextView = (TextView)findViewById(R.id.opacityDataTextView);
        seasonDataTextView = (TextView)findViewById(R.id.seasonDataTextView);
        lastUpdateTextView = (TextView)findViewById(R.id.lastUpdateTextView);
        dialog = new Dialog(WeatherActivity.this);

        // when the activity runs for the first time get the latest weather data
        if(savedInstanceState == null){
            getApiData(-1, true);
        }
        // when the activity has already run, restore the last requested weather data
        else {
            latestSol = savedInstanceState.getInt("latestSol");
            weatherData = (WeatherDataModel) savedInstanceState.getSerializable("weatherModel");
            setDataToView(weatherData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dateRange:
                // let the user pick a different solar day
                showChooseNewSolDialog();
                return true;
            case R.id.home:
                // get latest weather data
                getApiData(-1, true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("latestSol", latestSol);
        outState.putSerializable("weatherModel", weatherData);
    }

    /**
     * This class runs in the background of the activity to get the weather data from the api.
     * When called it needs to be passed a Request model to tell what kind of data to get.
     * While fetching data it displays a progress dialog to the user.
     * The data is saved in a WeatherDataModel from where the data is displayed on screen.
     * */
    public class FetchData extends AsyncTask<RequestModel, Void, WeatherDataModel> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(WeatherActivity.this);
            progressDialog.setMessage(getText(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected WeatherDataModel doInBackground(RequestModel... requestModels) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // base url
            String get = "http://marsweather.ingenology.com/v1/";
            if(requestModels[0].latest){
                // create url to get latest data
                get  += "latest/?format=json";
            }
            else {
                // create url to get data about the requested solar day
                get += "archive/?sol=" + requestModels[0].sol + "&format=json";
            }

            try {
                URL url = new URL(get);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream stream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                // convert received data to a string
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                // convert complete data to Json object
                JSONObject reportJsonObject = new JSONObject(buffer.toString());

                // when the returned Json object of a requested solar day is empty quit this action
                if(!requestModels[0].latest && reportJsonObject.getInt("count") == 0) {
                    return null;
                }

                JSONObject weatherDataJsonObj;
                // when the latest data was requested there is no extra Json array
                if(requestModels[0].latest) {
                    weatherDataJsonObj = reportJsonObject.getJSONObject("report");
                }
                // when a particular solar day was requested there is an extra Json array to get
                else {
                    JSONArray resultArrayJsonObject = reportJsonObject.getJSONArray("results");
                    weatherDataJsonObj = resultArrayJsonObject.getJSONObject(0);
                }

                weatherData = new WeatherDataModel();

                // convert the returned terrestrial date to a EU date format and save it in weatherDataModel
                SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = originalDateFormat.parse(weatherDataJsonObj.getString("terrestrial_date"));
                weatherData.setTerrestrial_date(dateFormat.format(date));

                // save the other returned data to in a weatherDataModel
                weatherData.setSol(weatherDataJsonObj.getLong("sol"));
                weatherData.setMax_temp(weatherDataJsonObj.getLong("max_temp"));
                weatherData.setMin_temp(weatherDataJsonObj.getLong("min_temp"));
                weatherData.setAtmo_opacity(weatherDataJsonObj.getString("atmo_opacity"));
                weatherData.setWind_speed(weatherDataJsonObj.optLong("wind_speed"));
                weatherData.setSeason(weatherDataJsonObj.getString("season"));

                // when the latest data are requested save the solar date(for max numberPicker)
                if(requestModels[0].latest) {
                    latestSol = (int) weatherData.getSol();
                }

                return weatherData;

            } catch (IOException | JSONException | ParseException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(WeatherDataModel weatherData) {
            super.onPostExecute(weatherData);
            progressDialog.dismiss();

            // when doInBackground has quit because there was no data, let the user know
            if(weatherData == null){
                Toast.makeText(WeatherActivity.this, getText(R.string.toast_message), Toast.LENGTH_SHORT).show();
                return;
            }
            // display fetched weather data
            setDataToView(weatherData);
        }
    }

    /**
     * This method displays the receivedWeather data to the screen.
     * It checks whether the weather status has a value.
     * It gets the current date and time to display as update time.
     * */
    public void setDataToView (WeatherDataModel weatherData) {

        solTextView.setText(String.valueOf(weatherData.getSol()));
        dateOfDataTextView.setText(weatherData.getTerrestrial_date());

        // set a degrees celsius behind the temperatures values
        maxCelsiusTextView.setText(String.valueOf(weatherData.getMax_temp())+ (char) 0x00B0 + "C");
        minCelsiusTextView.setText(String.valueOf(weatherData.getMin_temp()) + (char) 0x00B0 + "C");

        // display "no data" when there is no weather status
        if(weatherData.getAtmo_opacity().equals("null")) {
            opacityDataTextView.setText(getText(R.string.no_data));
        }
        else {
            opacityDataTextView.setText(weatherData.getAtmo_opacity());
        }

        windSpeedDataTextView.setText(String.valueOf(weatherData.getWind_speed()));
        seasonDataTextView.setText(weatherData.getSeason());

        // get and display the current time and date as last update time
        SimpleDateFormat dateFormatUpdate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String currentDateAndTime = dateFormatUpdate.format(new Date());
        lastUpdateTextView.setText(getText(R.string.last_update) + currentDateAndTime);
    }

    /**
     * This method displays a dialog where the user can choose
     * a solar day to see the weather data from.
     * When a day is confirmed a the data is fetched directly.
     * */
    public void showChooseNewSolDialog() {

        dialog.setContentView(R.layout.change_sol_dialog);
        dialog.setTitle(getText(R.string.dialog_title));

        // initialize number picker with the latest solar day as a maximum
        final NumberPicker numberPicker = (NumberPicker)dialog.findViewById(R.id.solNumberPicker);
        numberPicker.setMaxValue(latestSol);
        numberPicker.setMinValue(15);
        numberPicker.setWrapSelectorWheel(true);

        Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
        Button getButton = (Button)dialog.findViewById(R.id.getButton);

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the data from the entered solar day, not the latest data
                getApiData(numberPicker.getValue(), false);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * This method calls the AsyncTask thread to get data from the API.
     * Sol for a particular solar day (-1 for the latest data)
     * Set latest to true when latest data is requested
     * */
    public void getApiData(int sol, boolean latest) {
        RequestModel request = new RequestModel(sol, latest);
        new FetchData().execute(request);
    }
}
