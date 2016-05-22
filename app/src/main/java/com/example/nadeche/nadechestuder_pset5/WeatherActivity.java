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
            RequestModel request = new RequestModel(-1, true);
            new FetchData().execute(request);
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
                showChooseNewSolDialog();
                return true;
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

    public class FetchData extends AsyncTask<RequestModel, String, WeatherDataModel> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(WeatherActivity.this);
            progressDialog.setMessage(getText(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected WeatherDataModel doInBackground(RequestModel... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String get = "http://marsweather.ingenology.com/v1/";
            if(params[0].latest){
                get  += "latest/?format=json";
            }
            else {
                get += "archive/?sol=" + params[0].sol + "&format=json";
            }

            try {
                URL url = new URL(get);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream stream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String completeJsonString = buffer.toString();

                JSONObject reportJsonObject = new JSONObject(completeJsonString);

                if(!params[0].latest && reportJsonObject.getInt("count") == 0) {
                    return null;
                }

                JSONObject weatherDataJsonObj;
                if(params[0].latest) {
                    weatherDataJsonObj = reportJsonObject.getJSONObject("report");
                }
                else {
                    JSONArray resultArrayJsonObject = reportJsonObject.getJSONArray("results");
                    weatherDataJsonObj = resultArrayJsonObject.getJSONObject(0);
                }

                weatherData = new WeatherDataModel();

                SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = originalDateFormat.parse(weatherDataJsonObj.getString("terrestrial_date"));

                weatherData.setTerrestrial_date(dateFormat.format(date));
                weatherData.setSol(weatherDataJsonObj.getLong("sol"));
                weatherData.setMax_temp(weatherDataJsonObj.getLong("max_temp"));
                weatherData.setMin_temp(weatherDataJsonObj.getLong("min_temp"));
                weatherData.setAtmo_opacity(weatherDataJsonObj.getString("atmo_opacity"));
                weatherData.setWind_speed(weatherDataJsonObj.optLong("wind_speed"));
                weatherData.setSeason(weatherDataJsonObj.getString("season"));

                if(params[0].latest) {
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

            if(weatherData == null){
                Toast.makeText(WeatherActivity.this, getText(R.string.toast_message), Toast.LENGTH_SHORT).show();
                return;
            }
            setDataToView(weatherData);
        }
    }

    public void setDataToView (WeatherDataModel weatherData) {
        solTextView.setText(String.valueOf(weatherData.getSol()));
        dateOfDataTextView.setText(weatherData.getTerrestrial_date());
        maxCelsiusTextView.setText(String.valueOf(weatherData.getMax_temp())+ (char) 0x00B0 + "C");
        minCelsiusTextView.setText(String.valueOf(weatherData.getMin_temp()) + (char) 0x00B0 + "C");
        if(weatherData.getAtmo_opacity().equals("null")) {
            opacityDataTextView.setText(getText(R.string.no_data));
        }
        else {
            opacityDataTextView.setText(weatherData.getAtmo_opacity());
        }
        windSpeedDataTextView.setText(String.valueOf(weatherData.getWind_speed()));
        seasonDataTextView.setText(weatherData.getSeason());
        SimpleDateFormat dateFormatUpdate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String currentDateAndTime = dateFormatUpdate.format(new Date());
        lastUpdateTextView.setText(getText(R.string.last_update) + currentDateAndTime);
    }

    public void showChooseNewSolDialog() {

        dialog.setContentView(R.layout.change_sol_dialog);
        dialog.setTitle(getText(R.string.dialog_title));
        final NumberPicker numberPicker = (NumberPicker)dialog.findViewById(R.id.solNumberPicker);
        numberPicker.setMaxValue(latestSol);
        numberPicker.setMinValue(15);
        numberPicker.setWrapSelectorWheel(true);

        Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
        Button getButton = (Button)dialog.findViewById(R.id.getButton);

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestModel request = new RequestModel(numberPicker.getValue(), false);
                new FetchData().execute(request);
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
}
