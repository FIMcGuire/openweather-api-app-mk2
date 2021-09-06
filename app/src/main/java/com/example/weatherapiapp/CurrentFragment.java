package com.example.weatherapiapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentFragment extends Fragment implements SensorEventListener {

    //Declare objects/variables
    TextView cityField, detailsField, currentTemperatureField, feelsLike, humidityField, windDirectionField, cloudField, updatedField, updatedFieldTime;
    RelativeLayout background, weatherVane;
    ImageView arrow, weatherIcon, cBG;

    //ArrowCompass code
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;
    private SensorManager mSensorManager;
    private float windDirection = 0f;
    private float oldTest = 0f;
    private String city;

    //Declare SwipeRefreshLayout
    SwipeRefreshLayout pullToRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ArrowCompass Code
        arrow = getView().findViewById(R.id.windDirection);
        weatherVane = getView().findViewById(R.id.weatherVane);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        //set values of variables to equivalent xml objects
        cityField = view.findViewById(R.id.city_field);
        updatedField = view.findViewById(R.id.updated_field);
        updatedFieldTime = view.findViewById(R.id.updated_field_time);
        detailsField = view.findViewById(R.id.details_field);
        currentTemperatureField = view.findViewById(R.id.current_temperature_field);
        feelsLike = view.findViewById(R.id.feelsLike);
        humidityField = view.findViewById(R.id.humidityField);
        windDirectionField = view.findViewById(R.id.windDirectionField);
        cloudField = view.findViewById(R.id.cloudField);
        weatherIcon = view.findViewById(R.id.weather_icon);

        //set values of variables to equivalent xml objects
        background = view.findViewById(R.id.RelativeBG);
        cBG = view.findViewById(R.id.cBG);

        //call taskLoadUp method, sending city variable as parameter
        city = ((MainActivity) getActivity()).getCity();
        ((MainActivity) getActivity()).taskLoadUp(city);

        //set/create onClickListener for cityField
        cityField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setup AlertDialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                alertDialog.setTitle("Change City");
                final EditText input = new EditText(getContext());
                input.setText(city);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //set city equal value of textview
                                city = input.getText().toString();
                                //call taskLoadUp method, sending city as parameter
                                ((MainActivity) getActivity()).taskLoadUp(city);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel dialog
                                dialog.cancel();
                            }
                        });
                //show alert dialog
                alertDialog.show();
            }
        });

        //instantiate pullToRefresh
        pullToRefresh = view.findViewById(R.id.swiperefresh);
        //setup listener
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //call taskLoadUp method, sending string value of textview
                ((MainActivity) getActivity()).taskLoadUp(cityField.getText().toString());
                //set Refreshing of pulltoRefresh to false (removes the refresh icon)
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float alpha = 0.97f;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
            mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
            mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
            mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
            mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
        }

        float R[] = new float[9];
        float I[] = new float[9];
        float outR[] = new float[9];

        if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
            float orientation[] = new float[3];

            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);

            SensorManager.getOrientation(outR, orientation);
            azimuth = orientation[0];

            float degree = (float) (Math.toDegrees(azimuth) + 360) % 360;

            //azimuth = (float) Math.toDegrees(orientation[0]);
            //azimuth = (azimuth + 360) % 360;

            //float test = windDirection - degree;
            float test2 = degree - windDirection;

            Animation animArrow = new RotateAnimation(-currentAzimuth, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            currentAzimuth = degree;
            oldTest = test2;

            animArrow.setDuration(500);
            animArrow.setRepeatCount(0);
            animArrow.setFillAfter(true);

            arrow.setRotation(windDirection);
            weatherVane.startAnimation(animArrow);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void unPackJSON(String xml, String cityName) {
        //try catch block
        try {
            //create new JSONObject from xml
            JSONObject json = new JSONObject(xml);

            //if statement to determine if json is not null
            if (json != null) {
                //create new JSONObjects from data in json
                JSONObject current = json.getJSONObject("current");
                JSONObject weather = current.getJSONArray("weather").getJSONObject(0);

                //Create new Date object T from json * 1000
                Date T = new Date((current.getLong("dt") + json.getLong("timezone_offset")) * 1000);
                //Create new DateFormat
                DateFormat df = DateFormat.getDateTimeInstance();

                //set textviews equal to values returned from json object
                cityField.setText(cityName);
                ((MainActivity)getActivity()).setCity(cityField.getText().toString());
                detailsField.setText(weather.getString("description").toUpperCase(Locale.US));
                currentTemperatureField.setText(String.format("%.1f", current.getDouble("temp")) + "°C");
                feelsLike.setText(String.format("%.1f", current.getDouble("feels_like")) + "°C");
                humidityField.setText("Humidity: " + current.getString("humidity") + "%");
                windDirectionField.setText("Wind Direction: " + current.getString("wind_deg") + "°");

                windDirection = Float.parseFloat(current.getString("wind_deg"));

                cloudField.setText("Cloud Cover: " + current.getString("clouds") + "%");
                updatedField.setText(df.format(T).substring(0, 7));
                updatedFieldTime.setText(df.format(T).substring(12, 17));

                //set weathericon equal to value returned from method sending json object values as parameters
                weatherIcon.setImageResource(Function.setWeatherIcon(weather.getString("icon")));

                //create variables equal to values return from json object
                long sunrise = current.getLong("sunrise") + json.getLong("timezone_offset") * 1000;
                long sunset = current.getLong("sunset") + json.getLong("timezone_offset") * 1000;
                long currentTime = current.getLong("dt") + json.getLong("timezone_offset") * 1000;
                int cloudiness = current.getInt("clouds");

                Function.dayornight(background, cBG, sunrise, sunset, currentTime, cloudiness);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            //set cityField equal to "Error"
            cityField.setText("Error");
            //send toast message to the user telling them "Error" and to check city
            Toast.makeText(getContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
        }
    }
}
