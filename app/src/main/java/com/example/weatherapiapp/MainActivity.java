package com.example.weatherapiapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    Menu menu;
    private int menuID = 0;
    private CurrentFragment currentFragment;
    private HourlyFragment hourlyFragment;
    private DailyFragment dailyFragment;
    //Declare ArrayList containing type String
    ArrayList<String> savedlocations = new ArrayList<>();
    //Declare DatabaseManipulator
    DatabaseManipulator dm;

    DownloadWeather downloadWeather;

    SubMenu sub;

    //Default Location
    String city = "Galashiels";

    //Declare LocationManager and Listener objects
    private LocationManager locationManager;
    private LocationListener locationListener;
    //Declare int
    private static final int ACCESS_REQUEST_LOCATION = 0;

    FloatingActionButton fabLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        downloadWeather = new DownloadWeather();

        currentFragment = new CurrentFragment();
        hourlyFragment = new HourlyFragment();
        dailyFragment = new DailyFragment();

        setCity(city);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        sub = menu.addSubMenu(1, Menu.NONE, Menu.NONE, "Saved Locations");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            fragmentManager("Current", currentFragment);
            navigationView.setCheckedItem(R.id.nav_current);
        }

        //Instantiate DatabaseManipulator dm
        dm = new DatabaseManipulator(this);

        //Create list of String arrays testers, equal to value returned from dm.selectAll() method
        List<String[]> database = dm.selectAll();

        //foreach loop that runs through every string array in testers
        for (String[] name : database) {
            //add item at position 1 in current string array from testers into savedlocations ArrayList of Strings
            savedlocations.add(name[1]);
        }

        for (String name : savedlocations) {
            sub.add(1, menuID, Menu.CATEGORY_ALTERNATIVE, name);
            MenuItem test = sub.getItem(menuID);

            ImageButton iButton = new ImageButton(MainActivity.this);
            //iButton.setLayoutParams(new DrawerLayout.LayoutParams(165, 165));
            iButton.setBackgroundColor(Color.TRANSPARENT);
            iButton.setImageResource(R.drawable.ic_delete);
            iButton.setId(menuID);
            iButton.setOnClickListener(MainActivity.this);

            test.setActionView(iButton);

            menuID++;
        }

        fabLocation = findViewById(R.id.fabLocation);

        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = getCity();

                if (!savedlocations.contains(cityName)) {
                    //add current city to array
                    savedlocations.add(cityName);
                    //instantiate DatabaseManipulator
                    dm = new DatabaseManipulator(MainActivity.this);
                    //insert current city into database
                    dm.insert(cityName);

                    sub.add(1, menuID, Menu.CATEGORY_ALTERNATIVE, cityName);
                    MenuItem test = sub.getItem(menuID);

                    ImageButton iButton = new ImageButton(MainActivity.this);
                    //iButton.setLayoutParams(new DrawerLayout.LayoutParams(165, 165));
                    iButton.setBackgroundColor(Color.TRANSPARENT);
                    iButton.setImageResource(R.drawable.ic_delete);
                    iButton.setId(menuID);
                    iButton.setOnClickListener(MainActivity.this);

                    test.setActionView(iButton);

                    menuID++;
                }

            }
        });

        //setup location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    //create coordinate variables
                    Double lat = location.getLatitude();
                    Double lon = location.getLongitude();

                    //call taskLoadUp sending coords as parameter
                    taskLoadUp(lat, lon);

                    fabLocation.setClickable(true);

                    //stop locationManager from searching for current location
                    locationManager.removeUpdates(this);
                }
            }

            //unused methods
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        //if statement to determine if the app doesnt have the correct permissions from user
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_REQUEST_LOCATION);
        } else {
            //call setLocationUpdateFunction() method
            setLocationUpdateFunction();
        }

    }

    //setLocationUpdateFunction that runs when users location is updated/location button is clicked
    //@SuppressLint("MissingPermission")
    private void setLocationUpdateFunction() {
        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create boolean isGPSEnabled equal to result returned from isProviderEnabled() method, sending locationManager.GPS_PROVIDER as paremeter
                boolean isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
                //create boolean isNetworkEnabled equal to result returned from isProviderEnabled() method, sending locationManager.NETWORK_PROVIDER as paremeter
                boolean isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

                //if statement to determine if both booleans are false
                if (!isGPSEnabled && !isNetworkEnabled && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //toast message to tell the user that network and location are not available/enabled
                    Toast.makeText(MainActivity.this, "Network and Location not Available", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //create Criteria object critera
                    Criteria criteria = new Criteria();

                    //string provider equal to value returned from method
                    String provider = locationManager.getBestProvider(criteria, true);

                    fabLocation.setClickable(false);

                    //call locationupdater
                    locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                }
            }
        });
    }

    //method runs on first app launch after user has selected allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //switch
        switch (requestCode) {
            case ACCESS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    Log.i(getResources().getString(R.string.app_name),
                            "Location Permission granted by user.");
                    setLocationUpdateFunction();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Log.e(getResources().getString(R.string.app_name),
                            "No Location Permission granted by user.");
                    //toast message telling the user to enable location permissions
                    Toast.makeText(this, "You have not enabled Location services for this app, please enable them in this apps settings", Toast.LENGTH_LONG);
                }
                return;
            }

        }
    }

    @Override
    public void onClick(final View v) {
        MenuItem test = sub.getItem(v.getId());
        final String test2 = String.valueOf(test.getTitle());

        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dm.deletecity(test2);
                        savedlocations.remove(test2);
                        sub.removeItem(v.getId());
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //method to determine which nav option was selected
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_current:
                fragmentManager("Current", currentFragment);
                break;
            case R.id.nav_hourly:
                fragmentManager("Hourly", hourlyFragment);
                break;
            case R.id.nav_daily:
                fragmentManager("Daily", dailyFragment);
                break;
            default:
                taskLoadUp(item.getTitle().toString());
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Method to handle fragment switching
    @SuppressLint("ResourceType")
    public void fragmentManager(String tag, Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();

        fm.findFragmentByTag(tag);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.fragment_container, fragment, tag);
        ft.commit();

        getSupportActionBar().setTitle(tag + " Forecast");
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //method taking doubles parameters
    public void taskLoadUp(Double lat, Double lon) {
        //if statement to determine if the user has an internet connection
        if (Function.isNetworkAvailable(this)) {
            //execute API request
            downloadWeather.execute(String.valueOf(lat), String.valueOf(lon));
        } else {
            //toast message to tell the user that they dont have an internet connection
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    //method taking String parameter
    public void taskLoadUp(String query) {
        //if statement to determine if the user has an internet connection
        if (Function.isNetworkAvailable(this)) {
            //execute API request
            downloadWeather.execute(query);
        } else {
            //toast message to tell the user that they dont have an internet connection
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    //class DownloadWeather
    class DownloadWeather extends AsyncTask<String, Void, String[]> {

        //OpenWeatherMap API key
        String OPEN_WEATHER_MAP_API = "8ee3713483d0844cffe6a2689db6b17f";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //method call API and return JSON as a string
        protected String[] doInBackground(String... args) {
            //Create string xml
            String xml;
            String cityName = "Custom Location";
            //if statement to determine if city name or coords
            if (args.length == 1) {
                //set xml to String returned from Function.executeGet() method sending API request to http address as parameter
                xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                        "&units=metric&appid=" + OPEN_WEATHER_MAP_API);

                try {
                    JSONObject json = new JSONObject(xml);
                    JSONObject coord = json.getJSONObject("coord");
                    String lat = coord.getString("lat");
                    String lon = coord.getString("lon");
                    cityName = json.getString("name");

                    xml = Function.excuteGet("https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=minutely&units=metric&appid=" + OPEN_WEATHER_MAP_API);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

                xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?lat=" + args[0] + "&lon=" + args[1] +
                        "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
                try {

                    JSONObject json = new JSONObject(xml);
                    cityName = json.getString("name");

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //set xml to String returned from Function.executeGet() method sending API request to http address as parameter
                xml = Function.excuteGet("https://api.openweathermap.org/data/2.5/onecall?lat=" + args[0] +
                        "&lon=" + args[1] + "&exclude=minutely&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            }

            String[] json = {xml, cityName};

            return json;
        }

        //method called after above methods returns string
        @Override
        protected void onPostExecute(String[] xml) {

            setCity(xml[1]);

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (fragment instanceof CurrentFragment)
                currentFragment.unPackJSON(xml[0], xml[1]);
            if (fragment instanceof HourlyFragment)
                hourlyFragment.unPackJSON(xml[0], xml[1]);
            if (fragment instanceof DailyFragment)
                dailyFragment.unPackJSON(xml[0], xml[1]);
        }

    }
}


