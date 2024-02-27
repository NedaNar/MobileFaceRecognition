package com.example.mobileapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import Classes.RecyclerAdapter;
import Classes.ResponseModel;
import Classes.ResponseModelDeserializer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PictureAnalyzer extends AppCompatActivity {
    RecyclerView recyclerView;
    Button choosePicBtn;
    Button getDataBtn;
    ArrayList<Uri> uri = new ArrayList<>();
    ArrayList<ResponseModel> resultList = new ArrayList<>();
    RecyclerAdapter adapter;
    private static final int Read_Permission = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_analyzer);

        recyclerView = findViewById(R.id.recyclerView_Gallery_Images);
        adapter = new RecyclerAdapter(uri);
        recyclerView.setLayoutManager(new GridLayoutManager(PictureAnalyzer.this, 4));
        recyclerView.setAdapter(adapter);
        if(ContextCompat.checkSelfPermission(PictureAnalyzer.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PictureAnalyzer.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);
        }

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

        getDataBtn = findViewById(R.id.getDataBtn);
        getDataBtn.setOnClickListener(getDataOnClick);
    }

    View.OnClickListener getDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (uri.size()!=0) {
                for(Uri imageUri : uri){
                    makeSequentialRequests(uri, 0);
                }
            } else {
                Log.e("getDataOnClick", "No image selected");
            }
        }
    };

    private String getImagePath(Uri uri) {
        String path = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    path = cursor.getString(index);
                }
            } catch (Exception e) {
                Log.e("getImagePath", "Error retrieving image path: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }
        return path;
    }

    // Handle activity result for image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getDataBtn.setVisibility(View.VISIBLE);
        choosePicBtn.setVisibility(View.GONE);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            if(data.getClipData() != null) {
                int x = data.getClipData().getItemCount();

                for (int i = 0; i < x; i++){
                    uri.add(data.getClipData().getItemAt(i).getUri());
                }
                adapter.notifyDataSetChanged();
            } else if (data.getData() != null){
                Uri imageUrl = data.getData();
                uri.add(imageUrl);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void makeSequentialRequests(ArrayList<Uri> uriList, int index) {
        if (index < uriList.size()) {
            makeRequest(uriList.get(index), new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    // Handle failure
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseData = response.body().string();
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ResponseModel.class, new ResponseModelDeserializer())
                            .create();
                    try {
                        ResponseModel responseModel = gson.fromJson(responseData, ResponseModel.class);
                        resultList.add(responseModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Make next request recursively
                    makeSequentialRequests(uriList, index + 1);
                }
            });
        } else {
            // All requests completed
            // Do something with resultList
        }
    }

    private void makeRequest(Uri imageUri, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                byte[] imageBytes = readBytes(inputStream);
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("api_key", "UjQQW6FjvINv3nT6El9OR8WMMrNTlrhL")
                        .addFormDataPart("api_secret", "9ptGFtbx5TOSAc0xXaMKTVZXgvM0Op5F")
                        .addFormDataPart("image_base64", base64Image)
                        .addFormDataPart("return_attributes", "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,beauty")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api-us.faceplusplus.com/facepp/v3/detect")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(callback);
            } else {
                Log.e("makeRequest", "InputStream is null for URI: " + imageUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
