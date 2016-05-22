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

public class WeatherActivity extends AppCompatActivity {

    private TextView dateOfDataTextView;
    private TextView solTextView;
    private TextView minCelsiusTextView;
    private TextView maxCelsiusTextView;
    private TextView windSpeedDataTextView;
    private TextView opacityDataTextView;
    private TextView seasonDataTextView;
    private TextView lastUpdateTextView;
    private WeatherDataModel weatherData;
    private int latestSol;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar actionBar = (Toolbar)findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);

        dateOfDataTextView = (TextView)findViewById(R.id.dateTextView);
        solTextView = (TextView)findViewById(R.id.solTextView);
        minCelsiusTextView = (TextView)findViewById(R.id.minCelsiusTextView);
        maxCelsiusTextView = (TextView)findViewById(R.id.maxCelsiusTextView);
        windSpeedDataTextView = (TextView)findViewById(R.id.windDataTextView);
        opacityDataTextView = (TextView)findViewById(R.id.opacityDataTextView);
        seasonDataTextView = (TextView)findViewById(R.id.seasonDataTextView);
        lastUpdateTextView = (TextView)findViewById(R.id.lastUpdateTextView);
        dialog = new Dialog(WeatherActivity.this);

        if(savedInstanceState == null){
            RequestModel request = new RequestModel(-1, true);
            new FetchData().execute(request);
        }
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
