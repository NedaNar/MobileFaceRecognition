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

public class MainActivity extends AppCompatActivity {
    Button choosePicBtn;
    public Spinner photoTypeSpinner;
    public String mode;
    ArrayList<Uri> uri = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosePicBtn = findViewById(R.id.choosePicBtn);
        choosePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select picture"), 1);
            }
        });

        photoTypeSpinner = findViewById(R.id.photo_type_spinner);
        photoTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mode = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                mode = "Best photo";
            }
        });

        List<String> photoTypes = new ArrayList<>();
        photoTypes.add("Best photo");
        photoTypes.add("Best group photo");
        photoTypes.add("Best document photo");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, photoTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        photoTypeSpinner.setAdapter(adapter);
    }

    // Handle activity result for image selection
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