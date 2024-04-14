package com.example.mobileapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import Classes.ImageModel;
import Classes.RecyclerAdapterList;
import Classes.RecyclerViewItemDecoration;
import Classes.ThemeHelper;
import Classes.ToolbarHandler;

public class DisplayData extends AppCompatActivity {

    private RecyclerView imagesView;
    private RecyclerView bestView;
    private RecyclerAdapterList imagesAdapter;
    private RecyclerAdapterList bestAdapter;
    private ImageButton backButton;
    private ArrayList<ImageModel> images;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);
        ThemeHelper.setThemeColor(findViewById(R.id.colorBtn), getSharedPreferences("MODE", Context.MODE_PRIVATE));

        //  Retrieve image URIs from intent
        images = getIntent().<ImageModel>getParcelableArrayListExtra("images");
        mode = getIntent().getStringExtra("mode");

        //  Initialize recycler view of all images
        imagesView = findViewById(R.id.recyclerViewImages);
        imagesView.setLayoutManager(new LinearLayoutManager(this));
        imagesAdapter = new RecyclerAdapterList(images, "recyclerViewImages", mode);
        imagesView.setAdapter(imagesAdapter);
        RecyclerViewItemDecoration dividerItemDecoration = new RecyclerViewItemDecoration(this, R.drawable.separator);
        imagesView.addItemDecoration(dividerItemDecoration);

        if (!mode.equals("History")){
            bestView = findViewById(R.id.recyclerViewBestImage);
            bestView.setLayoutManager(new LinearLayoutManager(this));
            bestAdapter = new RecyclerAdapterList(images, "recyclerViewBestImage", mode);
            bestView.setAdapter(bestAdapter);
        }

        TextView modeTextView = findViewById(R.id.textViewTitle);
        modeTextView.setText(mode.toUpperCase());

        TextView resultsTextView = findViewById(R.id.resultsTitle);
        if (mode.equals("History")) resultsTextView.setVisibility(View.GONE);

        backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayData.this, PictureAnalyzer.class);
                intent.putParcelableArrayListExtra("uris", new ArrayList<Uri>());
                startActivity(intent);
            }
        });

        ImageButton helpBtn = findViewById(R.id.helpBtn);
        Button historyBtn = findViewById(R.id.historyBtn);
        ToolbarHandler toolbarHandler = new ToolbarHandler(this, helpBtn, historyBtn);
    }

    private void showHelpDialog() {

        // Create dialog
        final Dialog helpDialog = new Dialog(this);
        helpDialog.setContentView(R.layout.help_layout);
        helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        helpDialog.setCancelable(true);

        // Find "Close" button in dialog layout
        ImageView btnClose = helpDialog.findViewById(R.id.closeButton);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                helpDialog.dismiss();
            }
        });

        // Show dialog
        helpDialog.show();
    }
}