package com.example.mobileapplication;

import static Classes.ChoosePhotoButton.READ_PERMISSION_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.util.TypedValue;
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
import java.util.Objects;

import Classes.ApiManager;
import Classes.HistoryHelper;
import Classes.ImageModel;
import Classes.RecyclerAdapter;
import Classes.ResponseModel;
import Classes.ResponseModelDeserializer;
import Classes.SpinnerHandler;
import Classes.ThemeHelper;
import Classes.ToolbarHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import Classes.ChoosePhotoButton;
import Classes.Mode;

import android.app.Dialog;
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

    public Mode calculationMode;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_analyzer);
        ThemeHelper.setThemeColor(findViewById(R.id.colorBtn), getSharedPreferences("MODE", Context.MODE_PRIVATE));

        uri = getIntent().getParcelableArrayListExtra("uris");

        recyclerView = findViewById(R.id.recyclerView_Gallery_Images);
        adapter = new RecyclerAdapter(uri);
        recyclerView.setLayoutManager(new GridLayoutManager(PictureAnalyzer.this, 3));
        recyclerView.setAdapter(adapter);

        Spinner photoTypeSpinner = findViewById(R.id.photo_type_spinner);
        spinnerHandler = new SpinnerHandler(this, photoTypeSpinner);

        Button choosePicBtn = findViewById(R.id.choosePicBtn);
        choosePhotoButton = new ChoosePhotoButton(this, choosePicBtn, uri);

        getDataBtn = findViewById(R.id.getDataBtn);
        getDataBtn.setOnClickListener(getDataOnClick);

        bottomLayout = findViewById(R.id.bottomLayout);
        selectedPhotosTextView = findViewById(R.id.selected_photos);

        ImageButton helpBtn = findViewById(R.id.helpBtn);
        Button historyBtn = findViewById(R.id.historyBtn);
        ToolbarHandler toolbarHandler = new ToolbarHandler(this, helpBtn, historyBtn);

        if (uri.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            selectedPhotosTextView.setVisibility(View.GONE);
        }

        CheckPermissions();
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
                    resultList.clear();
                    analyzedImages.clear();

                    progressDialog = ProgressDialog.show(PictureAnalyzer.this, "Loading", "Analyzing your images...", true);
                    makeSequentialRequests(uri, 0);
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
                        //throw new IOException("Unexpected code " + response);
                        return;
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
                            analyzedImages.add(new ImageModel(uriList.get(index).toString(), -1, "No faces detected","", 0, 0.0, 0.0, 0.0, 0.0, 0.0, new ArrayList<String>()));
                        else if (points.length > 1 && !spinnerHandler.mode.equals("Best group photo"))
                            analyzedImages.add(new ImageModel(uriList.get(index).toString(), 0, "Select \"Best group photo\" to analyze", "", 0, 0.0, 0.0, 0.0, 0.0, 0.0, new ArrayList<String>()));
                        else if (points.length > 1 && spinnerHandler.mode.equals("Best group photo"))
                        {
                            float pointsInterim = 0;
                            String genders = "";
                            float ageInterim = 0;
                            float blurs = 0;
                            float smiles = 0;
                            float beauty = 0;
                            float happiness = 0;
                            float sadness = 0;
                            float anger = 0;
                            float disgust = 0;
                            float fear = 0;
                            float neutral = 0;
                            float surprise = 0;
                            ArrayList<String> emotions = new ArrayList<String>();

                            for (int i = 0; i < points.length; i ++)
                            {
                                pointsInterim = points[i];
                                genders += " " + responseModel.faces.get(i).attributes.gender;
                                ageInterim += responseModel.faces.get(i).attributes.age;
                                blurs += responseModel.faces.get(i).attributes.blur;
                                smiles += responseModel.faces.get(i).attributes.smile;
                                happiness += responseModel.faces.get(i).attributes.emotion.happiness;
                                sadness += responseModel.faces.get(i).attributes.emotion.sadness;
                                anger += responseModel.faces.get(i).attributes.emotion.anger;
                                disgust += responseModel.faces.get(i).attributes.emotion.disgust;
                                fear += responseModel.faces.get(i).attributes.emotion.fear;
                                neutral += responseModel.faces.get(i).attributes.emotion.neutral;
                                surprise += responseModel.faces.get(i).attributes.emotion.surprise;

                                if (Objects.equals(responseModel.faces.get(i).attributes.gender, "female")){
                                    beauty += responseModel.faces.get(i).attributes.beauty.femaleScore;
                                }
                                else{
                                    beauty += responseModel.faces.get(i).attributes.beauty.maleScore;
                            }
                            if (points.length > 2)
                            {
                                genders = "Multiple";
                            }
                            pointsInterim /= points.length;
                            ageInterim /= points.length;
                            blurs /= points.length;
                            smiles /= points.length;
                            beauty /= points.length;
                            happiness /= points.length;
                            sadness /= points.length;
                            anger /= points.length;
                            disgust /= points.length;
                            fear /= points.length;
                            neutral /= points.length;
                            surprise /= points.length;

                                if (happiness >= 10 && !emotions.contains("Happiness")) {
                                    emotions.add("Happiness");
                                }
                                if (sadness >= 10 && !emotions.contains("Sadness")) {
                                    emotions.add("Sadness");
                                }
                                if (anger >= 10 && !emotions.contains("Anger")) {
                                    emotions.add("Anger");
                                }
                                if (disgust >= 10 && !emotions.contains("Disgust")) {
                                    emotions.add("Disgust");
                                }
                                if (fear >= 10 && !emotions.contains("Fear")) {
                                    emotions.add("Fear");
                                }
                                if (neutral >= 10 && !emotions.contains("Neutral")) {
                                    emotions.add("Neutral");
                                }
                                if (surprise >= 10 && !emotions.contains("Surprise")) {
                                    emotions.add("Surprise");
                                }

                        }
                            analyzedImages.add(new ImageModel(uriList.get(index).toString(), pointsInterim, "",
                                    genders, (int) ageInterim, blurs, smiles, beauty, happiness, sadness, emotions));
                            }
                        else if (points.length == 1 && !spinnerHandler.mode.equals("Best photo")
                                && !spinnerHandler.mode.equals("Best document photo"))
                            analyzedImages.add(new ImageModel(uriList.get(index).toString(), 0, "Select \"Best photo\" to analyze", "", 0, 0.0, 0.0, 0.0, 0.0, 0.0, new ArrayList<String>()));
                        else{
                            ArrayList<String> emotions = new ArrayList<String>();
                            String genderr = "";
                            if (Objects.equals(responseModel.faces.get(0).attributes.gender, "Female")){
                                genderr = "female";
                            }
                            else genderr = "male";

                            if (responseModel.faces.get(0).attributes.emotion.happiness >= 10) {
                                emotions.add("Happiness");}
                            if (responseModel.faces.get(0).attributes.emotion.sadness >= 10) {
                                emotions.add("Sadness");}
                            if (responseModel.faces.get(0).attributes.emotion.anger >= 10) {
                                emotions.add("Anger");}
                            if (responseModel.faces.get(0).attributes.emotion.disgust >= 10) {
                                emotions.add("Disgust");}
                            if (responseModel.faces.get(0).attributes.emotion.fear >= 10) {
                                emotions.add("Fear");}
                            if (responseModel.faces.get(0).attributes.emotion.neutral >= 10) {
                                emotions.add("Neutral");}
                            if (responseModel.faces.get(0).attributes.emotion.surprise >= 10) {
                                emotions.add("Surprise");}

                            analyzedImages.add(new ImageModel(uriList.get(index).toString(), points[0], "",
                                    genderr,
                                    responseModel.faces.get(0).attributes.age,
                                    responseModel.faces.get(0).attributes.blur,
                                    responseModel.faces.get(0).attributes.smile,
                                    responseModel.faces.get(0).attributes.beauty.femaleScore,
                                    responseModel.faces.get(0).attributes.emotion.happiness,
                                    responseModel.faces.get(0).attributes.emotion.sadness,
                                    emotions));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Make next request recursively
                    makeSequentialRequests(uriList, index + 1);
                }
            });
        } else {
            // All requests completed
            progressDialog.dismiss(); // Hide loading dialog
            Intent intent = new Intent(PictureAnalyzer.this, DisplayData.class);
            intent.putExtra("mode", spinnerHandler.getMode());
            intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) analyzedImages);
            HistoryHelper.saveAnalyzedImages(this, analyzedImages);
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
        calculationMode = spinnerHandler.calculationMode;
        return calculationMode.PointCalculator(responseModel);
    }
}
