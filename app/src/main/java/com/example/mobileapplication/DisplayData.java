package com.example.mobileapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import Classes.ImageModel;
import Classes.RecyclerAdapter;
import Classes.RecyclerAdapterList;
import Classes.RecyclerViewItemDecoration;

public class DisplayData extends AppCompatActivity {

    private RecyclerView imagesView;
    private RecyclerView bestView;
    private RecyclerAdapterList imagesAdapter;
    private RecyclerAdapterList bestAdapter;
    private ImageButton backButton;
    private ArrayList<ImageModel> images;
    private String mode;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        //  Retrieve image URIs from intent
        images = getIntent().<ImageModel>getParcelableArrayListExtra("images");
        mode = getIntent().getStringExtra("mode");

        //  Initialize recycler view of all images
        imagesView = findViewById(R.id.recyclerViewImages);
        imagesView.setLayoutManager(new LinearLayoutManager(this));
        imagesAdapter = new RecyclerAdapterList(images, "recyclerViewImages");
        imagesView.setAdapter(imagesAdapter);
        RecyclerViewItemDecoration dividerItemDecoration = new RecyclerViewItemDecoration(this, R.drawable.separator);
        imagesView.addItemDecoration(dividerItemDecoration);

        //  Initialize recycler view of best image
        bestView = findViewById(R.id.recyclerViewBestImage);
        bestView.setLayoutManager(new LinearLayoutManager(this));
        bestAdapter = new RecyclerAdapterList(images, "recyclerViewBestImage");
        bestView.setAdapter(bestAdapter);

        TextView modeTextView = findViewById(R.id.textViewTitle);
        modeTextView.setText(mode.toUpperCase());

        backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayData.this, PictureAnalyzer.class);
                intent.putParcelableArrayListExtra("uris", new ArrayList<Uri>());
                startActivity(intent);
            }
        });
    }
}