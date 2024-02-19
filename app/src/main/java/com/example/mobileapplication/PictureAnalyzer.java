package com.example.mobileapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import Classes.ResponseModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PictureAnalyzer extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    Button choosePicBtn;
    Button getDataBtn;
    ImageView imageView;
    String selectedImageUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_analyzer);

        choosePicBtn = findViewById(R.id.choosePicBtn);
        choosePicBtn.setOnClickListener(choosePicOnClick);

        getDataBtn = findViewById(R.id.getDataBtn);
        getDataBtn.setOnClickListener(getDataOnClick);

        imageView = findViewById(R.id.imageView);
    }

    View.OnClickListener choosePicOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Request permission to read external storage
            requestStoragePermission();
        }
    };

    View.OnClickListener getDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!selectedImageUri.equals("")) {
                makeRequest();
            } else {
                Log.e("getDataOnClick", "No image selected");
            }
        }
    };

    // Request permission to read external storage
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, open image picker
            openImagePicker();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open image picker
                openImagePicker();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle activity result for image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                selectedImageUri = cursor.getString(columnIndex);
                cursor.close();
                imageView.setImageURI(imageUri); // Display the selected image in the ImageView
            }
        }
    }

    // Method to make HTTP request
    private void makeRequest() {
        OkHttpClient client = new OkHttpClient();

        // Prepare the image file
        File imageFile = new File(selectedImageUri);
//        resizeImage(imageFile);

        // Create multipart request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", "UjQQW6FjvINv3nT6El9OR8WMMrNTlrhL")
                .addFormDataPart("api_secret", "9ptGFtbx5TOSAc0xXaMKTVZXgvM0Op5F")
                .addFormDataPart("image_file", imageFile.getName(), RequestBody.create(imageFile, MediaType.parse("image/*")))
                .addFormDataPart("return_attributes", "gender,age")
                .build();

        // Create the request
        Request request = new Request.Builder()
                .url("https://api-us.faceplusplus.com/facepp/v3/detect")
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseData = response.body().string();
                Gson gson = new Gson();
                ResponseModel responseModel = gson.fromJson(responseData, ResponseModel.class);
            }
        });
    }
}
