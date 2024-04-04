package com.example.mobileapplication;

import static Classes.ChoosePhotoButton.READ_PERMISSION_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import Classes.ApiManager;
import Classes.ImageModel;
import Classes.RecyclerAdapter;
import Classes.ResponseModel;
import Classes.ResponseModelDeserializer;
import Classes.SpinnerHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import Classes.ChoosePhotoButton;
import Classes.PointCalculator;
import Classes.Mode;
import Classes.DocumentPhoto;
import Classes.BestPhoto;
import android.app.Dialog;
import android.widget.ImageButton;
import android.widget.ImageView;
public class PictureAnalyzer extends AppCompatActivity {
    RecyclerView recyclerView;
    ChoosePhotoButton choosePhotoButton;
    Button getDataBtn;
    LinearLayout bottomLayout;
    TextView selectedPhotosTextView;
    ArrayList<Uri> uri = new ArrayList<>();
    ArrayList<ResponseModel> resultList = new ArrayList<>();
    ArrayList<ImageModel> analyzedImages = new ArrayList<>();
    RecyclerAdapter adapter;
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

        Spinner photoTypeSpinner = findViewById(R.id.photo_type_spinner);
        spinnerHandler = new SpinnerHandler(this, photoTypeSpinner);

        Button choosePicBtn = findViewById(R.id.choosePicBtn);
        choosePhotoButton = new ChoosePhotoButton(this, choosePicBtn, uri);
        choosePhotoButton.setBackgroundResource(R.drawable.secondary_button);

        getDataBtn = findViewById(R.id.getDataBtn);
        getDataBtn.setOnClickListener(getDataOnClick);

        bottomLayout = findViewById(R.id.bottomLayout);
        selectedPhotosTextView = findViewById(R.id.selected_photos);


        // Find HelpBtn and set OnClickListener
        ImageButton helpBtn = findViewById(R.id.helpBtn);
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show help dialog
                showHelpDialog();
            }
        });
        if (uri.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            selectedPhotosTextView.setVisibility(View.GONE);
        }

        CheckPermissions();
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

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhotoButton.openGallery();
            } else {
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

    private void CheckPermissions(){
        if(ContextCompat.checkSelfPermission(PictureAnalyzer.this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PictureAnalyzer.this, new String[]{
                    Manifest.permission.ACCESS_NETWORK_STATE}, Network_Permission);
        }
    }

    private void makeSequentialRequests(ArrayList<Uri> uriList, int index) {
        if (index < uriList.size()) {
            ApiManager.makeRequest(this, uriList.get(index), new Callback() {
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

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    private float[] CalculatePoints(ResponseModel responseModel) {
        return PointCalculator.CalculatePoints(responseModel);
    }
}
