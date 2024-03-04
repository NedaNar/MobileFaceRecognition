package com.example.mobileapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import Classes.ImageModel;
import Classes.RecyclerAdapter;
import Classes.RecyclerAdapterList;

public class DisplayData extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerAdapterList adapter;
    private ArrayList<ImageModel> images;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        //  Retrieve image URIs from intent
        images = getIntent().<ImageModel>getParcelableArrayListExtra("images");
        mode = getIntent().getStringExtra("mode");

        //  Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set adapter
        adapter = new RecyclerAdapterList(images);
        recyclerView.setAdapter(adapter);

        TextView modeTextView = findViewById(R.id.textViewTitle);
        modeTextView.setText("Mode: " + mode);
    }
}