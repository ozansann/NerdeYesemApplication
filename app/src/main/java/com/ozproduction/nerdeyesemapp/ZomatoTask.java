package com.ozproduction.nerdeyesemapp;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ZomatoTask extends AsyncTask<Double, Void, Restaurant[]>{
    private final String userKey = "7b0b1388c340444fd2191f79f4a54643";
    private final String nearbyUrl = "https://developers.zomato.com/api/v2.1/geocode?";
    MainActivityInterface listener;

    ZomatoTask(MainActivityInterface listener)
    {
        this.listener = listener;
    }

    @Override
    protected Restaurant[] doInBackground(Double...params)
    {
        double latitude = params[0];
        double longitude = params[1];
        InputStream stream = null;
        HttpURLConnection urlConn = null;

        try {
            // Construct the request
            URL url = new URL(nearbyUrl + "lat=" + latitude + "&lon=" + longitude);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("user-key", userKey);
            urlConn.setRequestMethod("GET");
            urlConn.setReadTimeout(10000);
            urlConn.setConnectTimeout(15000);
            urlConn.setDoInput(true);

            // Send the request
            urlConn.connect();

            // Get results
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                // Process and return results
                stream = urlConn.getInputStream();
                return processNearbyJSON(convertStreamToString(stream));
            }
        } catch (IOException | JSONException e) {
            Log.e("ZomatoTask", e.getMessage());
        } finally {
            // Make sure to close stream and HTTP connection
            if (stream != null)
            {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("ZomatoTask", e.getMessage());
                }
            }
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
        return null;
    }

    private Restaurant[] processNearbyJSON(String response) throws JSONException {
        JSONObject totalResponse = new JSONObject(response);
        // Get only the nearby restaurants, no other data
        JSONArray restoArray = totalResponse.getJSONArray("nearby_restaurants");
        Restaurant[] list = new Restaurant[restoArray.length()];

        // Build a restaurant with each JSON object
        for(int i = 0; i < restoArray.length(); i++)
        {
            JSONObject obj = restoArray.getJSONObject(i).getJSONObject("restaurant");
            list[i] = buildRestaurant(obj);
        }

        return list;
    }

    private String convertStreamToString(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null)
        {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    private Restaurant buildRestaurant(JSONObject obj) throws JSONException {
        Restaurant resto = new Restaurant();

        if (obj.has("id"))
        {
            resto.setZomatoId(obj.getInt("id"));
        }

        // Verify each key exists before saving it to the Restaurant
        if (obj.has("name"))
        {
            resto.setName(obj.getString("name"));
        }

        // location is container for more fields
        if (obj.has("location"))
        {
            JSONObject sub = obj.getJSONObject("location");

            if (sub.has("address"))
            {
                if (sub.has("zipcode"))
                {
                    resto.setAddress(sub.getString("address") + " " + sub.getString("zipcode"));
                }
                else
                {
                    resto.setAddress(sub.getString("address"));
                }
            }

            if (sub.has("latitude"))
            {
                resto.setLatitude(sub.getDouble("latitude"));
            }
            if (sub.has("longitude"))
            {
                resto.setLongitude(sub.getDouble("longitude"));
            }
        }

        if (obj.has("cuisines"))
        {
            resto.setGenre(obj.getString("cuisines"));
        }

        if (obj.has("price_range"))
        {
            resto.setPriceRange(obj.getInt("price_range"));
        }

        // user_rating is a container for more fields
        if (obj.has("user_rating"))
        {
            JSONObject sub = obj.getJSONObject("user_rating");

            if (sub.has("aggregate_rating"))
            {
                resto.setStarRating(sub.getDouble("aggregate_rating"));
            }
        }

        if (obj.has("phone_numbers"))
        {
            resto.setPhone(obj.getString("phone_numbers"));
        }

        resto.setSource(1);
        resto.setDbId(-1);
        resto.setHerokuId(-1);

        Log.i("ZomatoTask", "Built restaurant: " + resto.getName());
        return resto;
    }

    @Override
    protected void onPostExecute(Restaurant[] list)
    {
        if(list != null)
        {
            listener.SetRestaurantList(list);
        }
        else if(list == null)
        {
            listener.RestaurantNotFound();
        }
        listener.CloseLoadingDialog();
    }
}