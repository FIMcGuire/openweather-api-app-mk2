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

public class HourlyFragment extends Fragment {

    TextView cityField, dateField, currentTimeField;
    RecyclerView recyclerView;
    String city;
    String[] hTime, hIcon, hTemp, hfeelsLike, hPop, hWindSpeed, huvindex, hDescription;
    MyAdapter myAdapter;
    RelativeLayout background;
    ImageView hBG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hourly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hTime = new String[24];
        hIcon = new String[24];
        hTemp = new String[24];
        hfeelsLike = new String[24];
        hPop = new String[24];
        hWindSpeed = new String[24];
        huvindex = new String[24];
        hDescription = new String[24];

        cityField = view.findViewById(R.id.cityField);
        dateField = view.findViewById(R.id.updatedField);
        currentTimeField = view.findViewById(R.id.updatedFieldTime);
        cityField.setText(((MainActivity) getActivity()).getCity());
        city = cityField.getText().toString();
        background = view.findViewById(R.id.hBackground);
        hBG = view.findViewById(R.id.hBG);

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

        recyclerView = view.findViewById(R.id.recyclerView);

        ((MainActivity) getActivity()).taskLoadUp(((MainActivity) getActivity()).getCity());

        myAdapter = new MyAdapter(getContext(), hIcon, hTime, hTemp, hfeelsLike, hPop, hWindSpeed, huvindex, hDescription, this);
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
                for (int i = 0; i < hIcon.length; i++) {
                    //create new JSONObjects from data in json
                    JSONObject current = json.getJSONObject("current");
                    JSONObject hourly = json.getJSONArray("hourly").getJSONObject(i);
                    JSONObject weather = hourly.getJSONArray("weather").getJSONObject(0);

                    //Create new Date object T from json * 1000
                    Date T = new Date((current.getLong("dt") + json.getLong("timezone_offset")) * 1000);
                    DateFormat df = DateFormat.getDateTimeInstance();
                    dateField.setText(df.format(T).substring(0, 7));
                    currentTimeField.setText(df.format(T).substring(12, 17));

                    Date T2 = new Date((hourly.getLong("dt") + json.getLong("timezone_offset")) * 1000);
                    hTime[i] = (df.format(T2).substring(12, 17));

                    hTemp[i] = (String.format("%.1f", hourly.getDouble("temp")) + "°C");
                    hfeelsLike[i] = (String.format("%.1f", hourly.getDouble("feels_like")) + "°C");
                    hPop[i] = (hourly.getString("pop"));
                    hWindSpeed[i] = hourly.getString("wind_speed") + " mph";
                    huvindex[i] = hourly.getString("uvi");
                    hDescription[i] = (weather.getString("description").toUpperCase(Locale.US));

                    //set weathericon equal to value returned from method sending json object values as parameters
                    hIcon[i] = weather.getString("icon");

                    //create variables equal to values return from json object
                    long sunrise = current.getLong("sunrise") + json.getLong("timezone_offset") * 1000;
                    long sunset = current.getLong("sunset") + json.getLong("timezone_offset") * 1000;
                    long currentTime = current.getLong("dt") + json.getLong("timezone_offset") * 1000;
                    int cloudiness = current.getInt("clouds");

                    Function.dayornight(background, hBG, sunrise, sunset, currentTime, cloudiness);
                }
                myAdapter.newAddedData(hIcon, hTime, hTemp, hfeelsLike, hPop, hWindSpeed, huvindex, hDescription);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //set cityField equal to "Error"
            cityField.setText("Error");
            //send toast message to the user telling them "Error" and to check city
            Toast.makeText(getContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
        }

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        Context context;
        String[] hIcon;
        String[] hTime;
        String[] hTemp;
        String[] hfeelsLike;
        String[] hPop;
        String[] hWindSpeed;
        String[] huvindex;
        String[] hDescription;
        HourlyFragment fragment;

        public MyAdapter(Context context, String[] hIcon, String[] hTime, String[] hTemp, String[] hfeelsLike, String[] hPop, String[] hDescription, String[] hWindSpeed, String[] huvindex, HourlyFragment fragment) {
            this.context = context;
            this.hTime = hTime;
            this.hIcon = hIcon;
            this.hTemp = hTemp;
            this.hfeelsLike = hfeelsLike;
            this.hPop = hPop;
            this.hWindSpeed = hWindSpeed;
            this.huvindex = huvindex;
            this.hDescription = hDescription;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.hourly_row, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            if (hTemp[position] != null) {
                holder.icon.setImageResource(Function.setWeatherIcon(hIcon[position]));
                holder.time.setText(hTime[position]);
                holder.temp.setText(hTemp[position]);
                holder.feelsLike.setText(hfeelsLike[position]);
                holder.pop.setText(hPop[position]);
                holder.windspeed.setText(hWindSpeed[position]);
                holder.uvindex.setText(huvindex[position]);
                holder.description.setText(hDescription[position]);
            }
        }

        @Override
        public int getItemCount() {
            return hIcon.length;
        }

        public void newAddedData(String[] hIcon, String[] hTime, String[] hTemp, String[] hfeelsLike, String[] hPop, String[] hWindSpeed, String[] huvindex, String[] hDescription) {
            this.hTime = hTime;
            this.hIcon = hIcon;
            this.hTemp = hTemp;
            this.hfeelsLike = hfeelsLike;
            this.hPop = hPop;
            this.hWindSpeed = hWindSpeed;
            this.huvindex = huvindex;
            this.hDescription = hDescription;
            notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView time, temp, feelsLike, pop, uvindex, windspeed, description;
            ImageView icon;

            public MyViewHolder(View itemView) {
                super(itemView);

                if (hTemp != null) {
                    time = itemView.findViewById(R.id.hTime);
                    icon = itemView.findViewById(R.id.hweatherIcon);
                    temp = itemView.findViewById(R.id.hTemperature);
                    feelsLike = itemView.findViewById(R.id.hFeelsLike);
                    pop = itemView.findViewById(R.id.pop);
                    uvindex = itemView.findViewById(R.id.uvindex);
                    windspeed = itemView.findViewById(R.id.windspeed);
                    description = itemView.findViewById(R.id.hDescription);
                }
            }
        }
    }
}
