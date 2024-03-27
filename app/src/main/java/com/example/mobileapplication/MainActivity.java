package com.example.mobileapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import Classes.ChoosePhotoButton;
import Classes.SpinnerHandler;

public class MainActivity extends AppCompatActivity {
    Button choosePicBtn;
    SpinnerHandler spinnerHandler;
    ArrayList<Uri> uri = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosePicBtn = findViewById(R.id.choosePicBtn);
        ChoosePhotoButton choosePhotoButton = new ChoosePhotoButton(this, choosePicBtn, uri);
        choosePhotoButton.setBackgroundResource(R.drawable.primary_button);

        Spinner photoTypeSpinner = findViewById(R.id.photo_type_spinner);
        spinnerHandler = new SpinnerHandler(this, photoTypeSpinner);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            if(data.getClipData() != null) {
                int x = data.getClipData().getItemCount();

                for (int i = 0; i < x; i++){
                    uri.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null){
                Uri imageUrl = data.getData();
                uri.add(imageUrl);
            }
        }

        Intent analyzerIntent = new Intent(MainActivity.this, PictureAnalyzer.class);
        analyzerIntent.putParcelableArrayListExtra("uris", uri);
        startActivity(analyzerIntent);
    }
}