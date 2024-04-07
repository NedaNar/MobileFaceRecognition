package com.example.mobileapplication;

import static Classes.ChoosePhotoButton.READ_PERMISSION_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import Classes.ChoosePhotoButton;
import Classes.SpinnerHandler;

public class MainActivity extends AppCompatActivity {
    ChoosePhotoButton choosePhotoButton;
    SpinnerHandler spinnerHandler;
    ArrayList<Uri> uri = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button choosePicBtn = findViewById(R.id.choosePicBtn);
        choosePhotoButton = new ChoosePhotoButton(this, choosePicBtn, uri);
        choosePhotoButton.setBackgroundResource(R.drawable.primary_button);

        Spinner photoTypeSpinner = findViewById(R.id.photo_type_spinner);
        spinnerHandler = new SpinnerHandler(this, photoTypeSpinner);

        TextView modeTextView = findViewById(R.id.app_name);
        modeTextView.setTextColor(Color.parseColor("#B5FFD2"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhotoButton.openGallery();
            } else {
            }
        }
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