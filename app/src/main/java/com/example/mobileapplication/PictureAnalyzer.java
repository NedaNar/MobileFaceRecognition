package com.example.mobileapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Classes.ImageModel;
import Classes.RecyclerAdapter;
import Classes.ResponseModel;
import Classes.ResponseModelDeserializer;
import Classes.SpinnerHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import Classes.ChoosePhotoButton;

public class PictureAnalyzer extends AppCompatActivity {
    RecyclerView recyclerView;
    Button choosePicBtn;
    Button getDataBtn;
    LinearLayout bottomLayout;
    TextView selectedPhotosTextView;
    ArrayList<Uri> uri = new ArrayList<>();
    ArrayList<ResponseModel> resultList = new ArrayList<>();
    ArrayList<ImageModel> analyzedImages = new ArrayList<>();
    RecyclerAdapter adapter;
    private static final int Read_Permission = 101;
    private static final int Network_Permission = 102;
    public float[] points;
    SpinnerHandler spinnerHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_analyzer);

        uri = getIntent().getParcelableArrayListExtra("uris");

        recyclerView = findViewById(R.id.recyclerView_Gallery_Images);
        adapter = new RecyclerAdapter(uri);
        recyclerView.setLayoutManager(new GridLayoutManager(PictureAnalyzer.this, 3));
        recyclerView.setAdapter(adapter);
        if(ContextCompat.checkSelfPermission(PictureAnalyzer.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PictureAnalyzer.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);
        }

        if(ContextCompat.checkSelfPermission(PictureAnalyzer.this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PictureAnalyzer.this, new String[]{
                    Manifest.permission.ACCESS_NETWORK_STATE}, Network_Permission);
        }

        Spinner photoTypeSpinner = findViewById(R.id.photo_type_spinner);
        spinnerHandler = new SpinnerHandler(this, photoTypeSpinner);

        choosePicBtn = findViewById(R.id.choosePicBtn);
        ChoosePhotoButton choosePhotoButton = new ChoosePhotoButton(this, choosePicBtn, uri);
        choosePhotoButton.setBackgroundResource(R.drawable.secondary_button);

        getDataBtn = findViewById(R.id.getDataBtn);
        getDataBtn.setOnClickListener(getDataOnClick);

        bottomLayout = findViewById(R.id.bottomLayout);
        selectedPhotosTextView = findViewById(R.id.selected_photos);

        if (uri.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            selectedPhotosTextView.setVisibility(View.GONE);
        }
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Read_Permission) {
            // Check if the READ_EXTERNAL_STORAGE permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with your logic
                // (e.g., loading images from external storage)
                // loadImages();
            } else {
                // Permission denied, handle accordingly (e.g., show a message, disable functionality)
                finishAffinity();
                System.exit(0);
            }
        } else if (requestCode == Network_Permission) {
            // Check if the ACCESS_NETWORK_STATE permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with your logic
                // (e.g., check network status)
                // checkNetworkStatus();
            } else {
                // Permission denied, handle accordingly (e.g., show a message, disable functionality)
                finishAffinity();
                System.exit(0);
            }
        }
    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener getDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (uri.size()!=0) {
                if (!isInternetAvailable()){
                    showToast("Please turn on your internet");
                }
                else{
                    for(Uri imageUri : uri){
                        makeSequentialRequests(uri, 0);
                    }
                }
            }
            else {
                Log.e("getDataOnClick", "No image selected");
            }
        }
    };

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

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

        uri.clear();
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

        if (uri.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            selectedPhotosTextView.setVisibility(View.GONE);
        } else {
            bottomLayout.setVisibility(View.VISIBLE);
            selectedPhotosTextView.setVisibility(View.VISIBLE);
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

                        points = CalculatePoints(responseModel);

                        if (points == null)
                            analyzedImages.add(new ImageModel(uriList.get(index), -1, "No faces detected","", 0));
                        else if (points.length > 1)
                            analyzedImages.add(new ImageModel(uriList.get(index), 0, "Select \"Group Photo\" to analyze", "", 0));
                        else
                            analyzedImages.add(new ImageModel(uriList.get(index), points[0], "",
                                    responseModel.faces.get(0).attributes.gender.toString(),
                                    responseModel.faces.get(0).attributes.age));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Make next request recursively
                    makeSequentialRequests(uriList, index + 1);
                }
            });
        } else {
            // All requests completed
            Intent intent = new Intent(PictureAnalyzer.this, DisplayData.class);
            intent.putExtra("mode", spinnerHandler.getMode());
            intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) analyzedImages);
            startActivity(intent);
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


    //Calculates points of a face
    //Returns an array of points
    public float[] CalculatePoints(ResponseModel responseModel){
        int sizeArray = responseModel.faces.size();

        if (sizeArray == 0)
            return null;

        float[] pointsArray = new float[sizeArray];
        float points = 0;
        for (int i = 0; i < sizeArray; i++)
        {
            ResponseModel.Attributes attributes = responseModel.faces.get(i).attributes;
            if ((attributes.eyeStatus.leftEyeStatus.noGlassEyeOpen >
                 attributes.eyeStatus.leftEyeStatus.noGlassEyeClose) ||
                (attributes.eyeStatus.leftEyeStatus.normalGlassEyeOpen >
                 attributes.eyeStatus.leftEyeStatus.normalGlassEyeClose))
            {
                if (attributes.eyeStatus.leftEyeStatus.noGlassEyeOpen >
                        attributes.eyeStatus.leftEyeStatus.normalGlassEyeOpen)
                {
                    points += attributes.eyeStatus.leftEyeStatus.noGlassEyeOpen / 100;
                }
                else points += attributes.eyeStatus.leftEyeStatus.normalGlassEyeOpen / 100;
            }
            else
            {
                if(attributes.eyeStatus.leftEyeStatus.noGlassEyeClose >
                   attributes.eyeStatus.leftEyeStatus.normalGlassEyeClose)
                {
                    points -= attributes.eyeStatus.leftEyeStatus.noGlassEyeClose /100;
                }
                else points -= attributes.eyeStatus.leftEyeStatus.normalGlassEyeClose /100;
            }

            if ((attributes.eyeStatus.rightEyeStatus.noGlassEyeOpen >
                    attributes.eyeStatus.rightEyeStatus.noGlassEyeClose) ||
                    (attributes.eyeStatus.rightEyeStatus.normalGlassEyeOpen >
                            attributes.eyeStatus.rightEyeStatus.normalGlassEyeClose))
            {
                if (attributes.eyeStatus.rightEyeStatus.noGlassEyeOpen >
                        attributes.eyeStatus.rightEyeStatus.normalGlassEyeOpen)
                {
                    points += attributes.eyeStatus.rightEyeStatus.noGlassEyeOpen / 100;
                }
                else points += attributes.eyeStatus.rightEyeStatus.normalGlassEyeOpen / 100;
            }
            else
            {
                if(attributes.eyeStatus.rightEyeStatus.noGlassEyeClose >
                        attributes.eyeStatus.rightEyeStatus.normalGlassEyeClose)
                {
                    points -= attributes.eyeStatus.rightEyeStatus.noGlassEyeClose /100;
                }
                else points -= attributes.eyeStatus.rightEyeStatus.normalGlassEyeClose /100;
            }

            String gender = attributes.gender;
            if (Objects.equals(gender, "Male"))
            {
                points += attributes.beauty.maleScore / 100;
            }
            else
            {
                points += attributes.beauty.femaleScore / 100;
            }
            points += attributes.smile / 100 - attributes.blur / 100 + attributes.faceQuality / 100;
            pointsArray[i] = points;
        }
        return pointsArray;
    }
}
