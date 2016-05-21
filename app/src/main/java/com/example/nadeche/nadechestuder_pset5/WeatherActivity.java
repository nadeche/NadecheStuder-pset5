package com.example.nadeche.nadechestuder_pset5;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherActivity extends AppCompatActivity {

    private TextView dateOfDataTextView;
    private TextView minCelsiusTextView;
    private TextView maxCelsiusTextView;
    private TextView windSpeedDataTextView;
    private TextView opacityDataTextView;
    private TextView lastUpdateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        dateOfDataTextView = (TextView)findViewById(R.id.dateTextView);
        minCelsiusTextView = (TextView)findViewById(R.id.minCelsiusTextView);
        maxCelsiusTextView = (TextView)findViewById(R.id.maxCelsiusTextView);
        windSpeedDataTextView = (TextView)findViewById(R.id.windDataTextView);
        opacityDataTextView = (TextView)findViewById(R.id.opacityDataTextView);
        lastUpdateTextView = (TextView)findViewById(R.id.lastUpdateTextView);

        URL url = null;
        try {
            url = new URL("http://marsweather.ingenology.com/v1/latest/?format=json");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new FetchData().execute(url);

    }

    public class FetchData extends AsyncTask<URL, String, WeatherDataModel> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(WeatherActivity.this);
            progressDialog.setMessage(getText(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected WeatherDataModel doInBackground(URL... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                urlConnection = (HttpURLConnection) params[0].openConnection();
                urlConnection.connect();

                InputStream stream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String completeJsonString = buffer.toString();
                Log.d("Return", completeJsonString);

                JSONObject reportJsonObject = new JSONObject(completeJsonString);
                Log.d("reportJsonObject", reportJsonObject.toString());

                JSONObject weatherDataJsonObj = reportJsonObject.getJSONObject("report");

                WeatherDataModel weatherData = new WeatherDataModel();

                SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = originalDateFormat.parse(weatherDataJsonObj.getString("terrestrial_date"));

                weatherData.setTerrestrial_date(dateFormat.format(date));
                weatherData.setMax_temp(weatherDataJsonObj.getLong("max_temp"));
                weatherData.setMin_temp(weatherDataJsonObj.getLong("min_temp"));
                weatherData.setAtmo_opacity(weatherDataJsonObj.getString("atmo_opacity"));
                weatherData.setWind_speed(weatherDataJsonObj.optLong("wind_speed"));

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

            dateOfDataTextView.append(weatherData.getTerrestrial_date());
            maxCelsiusTextView.setText(String.valueOf(weatherData.getMax_temp())+ (char) 0x00B0 + "C");
            minCelsiusTextView.setText(String.valueOf(weatherData.getMin_temp()) + (char) 0x00B0 + "C");
            opacityDataTextView.setText(weatherData.getAtmo_opacity());
            windSpeedDataTextView.setText(String.valueOf(weatherData.getWind_speed()));
            SimpleDateFormat dateFormatUpdate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String currentDateAndTime = dateFormatUpdate.format(new Date());
            lastUpdateTextView.append(currentDateAndTime);
        }
    }
}
