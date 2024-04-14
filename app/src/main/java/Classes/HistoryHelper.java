package Classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HistoryHelper {
    public static final String ANALYZED_IMAGES_PREF = "analyzed_images";
    public static final String ANALYZED_IMAGES_KEY = "analyzed_images_key";

    public static ArrayList<ImageModel> loadAnalyzedImages(Context context) {
        ArrayList<ImageModel> images = new ArrayList<>();
        String json = context.getSharedPreferences(ANALYZED_IMAGES_PREF, MODE_PRIVATE)
                .getString(ANALYZED_IMAGES_KEY, null);

        if (json != null) {

            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<ArrayList<ImageModel>>() {}.getType();
            ArrayList<ImageModel> analyzedImages = gson.fromJson(json, type);
            if (analyzedImages != null) {
                images.addAll(analyzedImages);
            }
        }

        return images;
    }

    public static void saveAnalyzedImages(Context context, ArrayList<ImageModel> newImages) {
        ArrayList<ImageModel> existingImages = loadAnalyzedImages(context);

        ArrayList<ImageModel> filteredImages = new ArrayList<>();
        for (ImageModel image : newImages) {
            boolean uriExists = false;
            for (ImageModel existingImage : existingImages) {
                if (existingImage.ImageUri.equals(image.ImageUri)) {
                    uriExists = true;
                    break;
                }
            }
            if (!uriExists && image.Points != 0 && image.Points != -1) {
                filteredImages.add(image);
            }
        }

        existingImages.addAll(filteredImages);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(existingImages);
        context.getSharedPreferences(ANALYZED_IMAGES_PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(ANALYZED_IMAGES_KEY, json)
                .apply();
    }

}
