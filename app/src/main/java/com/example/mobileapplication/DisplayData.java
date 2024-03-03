package com.example.mobileapplication;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import Classes.RecyclerAdapter;

public class DisplayData extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private ArrayList<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        //  Retrieve image URIs from intent
        imageUris = getIntent().getParcelableArrayListExtra("imageUris");

        //  Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set adapter
        adapter = new RecyclerAdapter(imageUris);
        recyclerView.setAdapter(adapter);
    }
}