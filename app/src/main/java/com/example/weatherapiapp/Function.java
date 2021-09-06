package com.example.weatherapiapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public abstract class Function {

    private Context context;

    public Function(Context context) {
        this.context = context;
    }

    //method to determine if network connection available
    public static boolean isNetworkAvailable(Context context) {
        //return ConnectivityManager object
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    //method to connect
    public static String excuteGet(String targetURL) {
        //create URl object
        URL url;
        //create HttpURLConnection object
        HttpURLConnection connection = null;
        //try catch block to catch exceptions
        try {
            //Create connection to targetURL
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            //set request headers
            connection.setRequestProperty("content-type", "application/json;  charset=utf-8");
            connection.setRequestProperty("Content-Language", "en-US");
            //setUseChaches to false
            connection.setUseCaches(false);
            //setDoInput to true, i.e. read data from URL connection
            connection.setDoInput(true);
            //setDoOutput to false, i.e. do not write data to URL connection
            connection.setDoOutput(false);

            //create InputStream object is to read data from opened connection
            InputStream is;
            //create int status equal to response code from URL connection
            int status = connection.getResponseCode();
            //if statement to determine if status is not equal to value HTTP_OK (i.e. status == HttpURLConnection.HTTP_Unauthorized)
            if (status != HttpURLConnection.HTTP_OK)
                //set is equal to error stream if any, null if no errors/connection not connected or no useful data
                is = connection.getErrorStream();
            else
                //set is equal to input stream
                is = connection.getInputStream();
            //create BufferedReader object rd wrapped around InputStreamReader(is) for efficiency
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            //create string line
            String line;
            //create StringBuffer response
            StringBuffer response = new StringBuffer();
            //while line is not null
            while ((line = rd.readLine()) != null) {
                //append line to StringBuffer response
                response.append(line);
                //append '\r' to StringBuffer response (carriage return)
                response.append('\r');
            }
            //call rd.close() to close stream
            rd.close();
            //return StringBuffer response as String
            return response.toString();
        } catch (Exception e) {
            //return null
            return null;
        } finally {
            //if statement to determine if connection is not null
            if (connection != null) {
                //call connection.disconnect() to close socket
                connection.disconnect();
            }
        }
    }

    //method to determine if the current location is in daylight and its cloud cover
    @SuppressLint("ResourceAsColor")
    public static void dayornight(View background, ImageView bg, double sunrise, double sunset, long currentTime, int cloudiness) {

        if (currentTime >= sunrise && currentTime <= sunset) {
            background.setBackgroundResource(R.color.colorDay);

            if (cloudiness <= 10) {
                bg.setImageResource(R.drawable.dzero);
            }
            //if statement to determine if cloudiness is greater than 10 and less than or equal to 10
            else if (cloudiness > 10 && cloudiness <= 25) {
                bg.setImageResource(R.drawable.dten);
            }
            //if statement to determine if cloudiness is greater than 25 and less than or equal to 50
            else if (cloudiness > 25 && cloudiness <= 50) {
                bg.setImageResource(R.drawable.dtwentyfive);
            }
            //if statement to determine if cloudiness is greater than 50 and less than or equal to 75
            else if (cloudiness > 50 && cloudiness <= 75) {
                bg.setImageResource(R.drawable.dfifty);
            }
            //if statement to determine if cloudiness is greater than 75 and less than or equal to 90
            else if (cloudiness > 75 && cloudiness <= 90) {
                bg.setImageResource(R.drawable.dseventyfive);
            } else {
                bg.setImageResource(R.drawable.donehundred);
            }
        } else {
            background.setBackgroundResource(R.color.colorAccent);
            if (cloudiness <= 10) {
                bg.setImageResource(R.drawable.nzero);
            }
            //if statement to determine if cloudiness is greater than 10 and less than or equal to 10
            else if (cloudiness > 10 && cloudiness <= 25) {
                bg.setImageResource(R.drawable.nten);
            }
            //if statement to determine if cloudiness is greater than 25 and less than or equal to 50
            else if (cloudiness > 25 && cloudiness <= 50) {
                bg.setImageResource(R.drawable.ntwentyfive);
            }
            //if statement to determine if cloudiness is greater than 50 and less than or equal to 75
            else if (cloudiness > 50 && cloudiness <= 75) {
                bg.setImageResource(R.drawable.nfifty);
            }
            //if statement to determine if cloudiness is greater than 75 and less than or equal to 90
            else if (cloudiness > 75 && cloudiness <= 90) {
                bg.setImageResource(R.drawable.nseventyfive);
            } else {
                bg.setImageResource(R.drawable.nonehundred);
            }
        }
    }

    //method to determine which weather icon to display
    public static int setWeatherIcon(String actualId) {

        int icon = R.drawable.day;

        //if statement to determine if actualID is equal to 800
        switch (actualId) {
            case "01d":
                icon = R.drawable.day;
                break;
            case "01n":
                icon = R.drawable.night;
                break;
            case "02d":
                icon = R.drawable.dayfewclouds;
                break;
            case "02n":
                icon = R.drawable.nightfewclouds;
                break;
            case "03d":
            case "03n":
                icon = R.drawable.scatteredclouds;
                break;
            case "04d":
            case "04n":
                icon = R.drawable.brokenclouds;
                break;
            case "09d":
            case "09n":
                icon = R.drawable.showerrain;
                break;
            case "10d":
                icon = R.drawable.dayrain;
                break;
            case "10n":
                icon = R.drawable.nightrain;
                break;
            case "11d":
            case "11n":
                icon = R.drawable.thunderstorm;
                break;
            case "13d":
            case "13n":
                icon = R.drawable.snow;
                break;
            case "50d":
            case "50n":
                icon = R.drawable.mist;
        }
        //return icon
        return icon;
    }
}