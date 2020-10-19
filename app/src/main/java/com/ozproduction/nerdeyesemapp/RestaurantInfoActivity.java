package com.ozproduction.nerdeyesemapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class RestaurantInfoActivity extends AppCompatActivity {

    TextView nametv,addresstv,genretv,phonetv,pricerangetv;
    String name, address, genre, phone, pricerange, latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurantinfo);

        setTitle("Restoran Detayı");

        nametv = (TextView)findViewById(R.id.restaurantname);
        addresstv = (TextView)findViewById(R.id.address);
        genretv = (TextView)findViewById(R.id.genre);
        phonetv = (TextView)findViewById(R.id.phone);
        pricerangetv = (TextView)findViewById(R.id.price);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        genre = intent.getStringExtra("genre");
        phone = intent.getStringExtra("phone");
        pricerange = intent.getStringExtra("pricerange");
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");

        if(name != null)
        {
            nametv.setText(name);
        }
        if(address != null)
        {
            addresstv.setText(address);
        }
        if(genre != null)
        {
            genretv.setText(genre);
        }
        if(phone != null)
        {
            phonetv.setText(phone);
        }
        if(pricerange != null)
        {
            pricerangetv.setText(pricerange);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void Back(View view)
    {
        Intent intent = new Intent(RestaurantInfoActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}