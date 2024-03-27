package Classes;

// ChoosePhotoButton.java

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ChoosePhotoButton {
    private Activity activity;
    private Button choosePicBtn;
    private ArrayList<Uri> uriList;
    public static final int READ_PERMISSION_REQUEST_CODE = 101;

    public ChoosePhotoButton(Activity activity, Button choosePicBtn, ArrayList<Uri> uriList) {
        this.activity = activity;
        this.choosePicBtn = choosePicBtn;
        this.uriList = uriList;
        initialize();
    }

    public void setBackgroundResource(int resId) {
        choosePicBtn.setBackgroundResource(resId);
    }

    private void initialize() {
        choosePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST_CODE);
                        return;
                    }
                }
                openGallery();
            }
        });
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select picture"), 1);
    }
}

