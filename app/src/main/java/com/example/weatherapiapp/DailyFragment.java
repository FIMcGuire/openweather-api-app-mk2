package com.example.weatherapiapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class DailyFragment extends Fragment {

    TextView cityField, dateField, currentTimeField;
    RecyclerView recyclerView;
    String city;
    String[] dDate, dSunrise, dSunset, dIcon, dMin, dMax, dPop, dWindSpeed, duvindex, dDescription;
    MyDAdapter myAdapter;
    RelativeLayout background;
    ImageView dBG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dDate = new String[8];
        dSunrise = new String[8];
        dSunset = new String[8];
        dIcon = new String[8];
        dMin = new String[8];
        dMax = new String[8];
        dPop = new String[8];
        dWindSpeed = new String[8];
        duvindex = new String[8];
        dDescription = new String[8];

        cityField = view.findViewById(R.id.dCityField);
        dateField = view.findViewById(R.id.dUpdatedField);
        currentTimeField = view.findViewById(R.id.dUpdatedFieldTime);
        cityField.setText(((MainActivity) getActivity()).getCity());
        city = cityField.getText().toString();

        background = view.findViewById(R.id.dBackground);
        dBG = view.findViewById(R.id.dBG);

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

        recyclerView = view.findViewById(R.id.dRecyclerView);

        ((MainActivity) getActivity()).taskLoadUp(((MainActivity) getActivity()).getCity());

        myAdapter = new MyDAdapter(getContext(), dDate, dIcon, dSunrise, dSunset, dMin, dMax, dPop, dWindSpeed, duvindex, dDescription, this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void unPackJSON(String xml, String cityName) {
        try {
            //create new JSONObject from xml
            JSONObject json = new JSONObject(xml);

            //set textviews equal to values returned from json object
            cityField.setText(cityName);
            ((MainActivity)getActivity()).setCity(cityName);

            //if statement to determine if json is not null
            if (json != null) {
                for (int i = 0; i < dIcon.length; i++) {
                    //create new JSONObjects from data in json
                    JSONObject current = json.getJSONObject("current");
                    JSONObject daily = json.getJSONArray("daily").getJSONObject(i);
                    JSONObject weather = daily.getJSONArray("weather").getJSONObject(0);
                    JSONObject temp = daily.getJSONObject("temp");

                    //Create new Date object T from json * 1000
                    Date T = new Date((current.getLong("dt") + json.getLong("timezone_offset")) * 1000);
                    DateFormat df = DateFormat.getDateTimeInstance();
                    dateField.setText(df.format(T).substring(0, 7));
                    currentTimeField.setText(df.format(T).substring(12, 17));

                    Date S1 = new Date((daily.getLong("sunrise") + json.getLong("timezone_offset")) * 1000);
                    Date S2 = new Date((daily.getLong("sunset") + json.getLong("timezone_offset")) * 1000);
                    Date DT = new Date((daily.getLong("dt") + json.getLong("timezone_offset")) * 1000);
                    String date = df.format(DT);
                    String[] testDate = date.split(" ");
                    String sunrise = df.format(S1);
                    String[] testSunrise = sunrise.split(" ");
                    String sunset = df.format(S2);
                    String[] testSunset = sunset.split(" ");

                    dSunrise[i] = testSunrise[3].substring(0, 5);
                    dSunset[i] = testSunset[3].substring(0, 5);

                    dDate[i] = testDate[0] + " " + testDate[1];

                    dMin[i] = (String.format("%.1f", temp.getDouble("min")) + "°C");
                    dMax[i] = (String.format("%.1f", temp.getDouble("max")) + "°C");
                    dPop[i] = (daily.getString("pop"));
                    dWindSpeed[i] = daily.getString("wind_speed") + " mph";
                    duvindex[i] = daily.getString("uvi");
                    dDescription[i] = (weather.getString("description").toUpperCase(Locale.US));

                    //set weathericon equal to value returned from method sending json object values as parameters
                    dIcon[i] = weather.getString("icon");

                    //create variables equal to values return from json object
                    long sunrise1 = current.getLong("sunrise")  + json.getLong("timezone_offset") * 1000;
                    long sunset1 = current.getLong("sunset")  + json.getLong("timezone_offset") * 1000;
                    long currentTime1 = current.getLong("dt") + json.getLong("timezone_offset") * 1000;
                    int cloudiness1 = current.getInt("clouds");

                    Function.dayornight(background, dBG, sunrise1, sunset1, currentTime1, cloudiness1);
                }
                myAdapter.newAddedData(dDate, dIcon, dSunrise, dSunset, dMin, dMax, dPop, dWindSpeed, duvindex, dDescription);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //set cityField equal to "Error"
            cityField.setText("Error");
            //send toast message to the user telling them "Error" and to check city
            Toast.makeText(getContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
        }

    }

    public class MyDAdapter extends RecyclerView.Adapter<MyDAdapter.MyViewHolder> {

        Context context;
        String[] dIcon;
        String[] dDate;
        String[] dSunrise;
        String[] dSunset;
        String[] dMin;
        String[] dMax;
        String[] dPop;
        String[] dWindSpeed;
        String[] duvindex;
        String[] dDescription;
        DailyFragment fragment;

        public MyDAdapter(Context context, String[] dDate, String[] dIcon, String[] dSunrise, String[] dSunset, String[] dMin, String[] dMax, String[] dPop, String[] dDescription, String[] dWindSpeed, String[] duvindex, DailyFragment fragment) {
            this.context = context;
            this.dDate = dDate;
            this.dSunrise = dSunrise;
            this.dSunset = dSunset;
            this.dIcon = dIcon;
            this.dMin = dMin;
            this.dMax = dMax;
            this.dPop = dPop;
            this.dWindSpeed = dWindSpeed;
            this.duvindex = duvindex;
            this.dDescription = dDescription;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.daily_row, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            if (dMin[position] != null) {
                holder.icon.setImageResource(Function.setWeatherIcon(dIcon[position]));
                holder.date.setText(dDate[position]);
                holder.sunrise.setText(dSunrise[position]);
                holder.sunset.setText(dSunset[position]);
                holder.min.setText(dMin[position]);
                holder.max.setText(dMax[position]);
                holder.pop.setText(dPop[position]);
                holder.windspeed.setText(dWindSpeed[position]);
                holder.uvindex.setText(duvindex[position]);
                holder.description.setText(dDescription[position]);
            }
        }

        @Override
        public int getItemCount() {
            return dIcon.length;
        }

        public void newAddedData(String[] dDate, String[] dIcon, String[] dSunrise, String[] dSunset, String[] dMin, String[] dMax, String[] dPop, String[] dWindSpeed, String[] duvindex, String[] dDescription) {
            this.dSunrise = dSunrise;
            this.dSunset = dSunset;
            this.dDate = dDate;
            this.dIcon = dIcon;
            this.dMin = dMin;
            this.dMax = dMax;
            this.dPop = dPop;
            this.dWindSpeed = dWindSpeed;
            this.duvindex = duvindex;
            this.dDescription = dDescription;
            notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView date, sunrise, sunset, min, max, pop, uvindex, windspeed, description;
            ImageView icon;

            public MyViewHolder(View itemView) {
                super(itemView);

                if (dMin != null) {
                    date = itemView.findViewById(R.id.dDate);
                    icon = itemView.findViewById(R.id.dWeatherIcon);
                    sunrise = itemView.findViewById(R.id.dSunruse);
                    sunset = itemView.findViewById(R.id.dSunset);
                    min = itemView.findViewById(R.id.dMinTemp);
                    max = itemView.findViewById(R.id.dMaxTemp);
                    pop = itemView.findViewById(R.id.dPop);
                    uvindex = itemView.findViewById(R.id.dUvindex);
                    windspeed = itemView.findViewById(R.id.dWindspeed);
                    description = itemView.findViewById(R.id.dDescription);
                }
            }
        }
    }
}
