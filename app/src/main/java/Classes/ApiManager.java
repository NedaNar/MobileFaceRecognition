package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiManager {

    public static void makeRequest(Context context, Uri imageUri, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                String base64Image = rescaleImage(inputStream);

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

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String rescaleImage(InputStream inputStream) {
        try {
            // Decode input stream into Bitmap
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            // Log the size of the original Bitmap
            int originalSize = originalBitmap.getByteCount();
            Log.d("ImageRescaler", "Original image size: " + originalSize + " bytes");

            // Calculate desired dimensions to fit within 2MB
            int maxWidth = originalBitmap.getWidth();
            int maxHeight = originalBitmap.getHeight();
            int maxSizeInBytes =  2 * 1024 * 1024; // 2MB
            float scale = (float) Math.sqrt((double) maxSizeInBytes / originalSize);
            int newWidth = Math.round(maxWidth * scale);
            int newHeight = Math.round(maxHeight * scale);

            // Scale the Bitmap
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

            // Log the size of the rescaled Bitmap
            int rescaledSize = scaledBitmap.getByteCount();
            Log.d("ImageRescaler", "Rescaled image size: " + rescaledSize + " bytes");

            // Compress the Bitmap to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // Adjust quality as needed

            // Convert compressed byte array to Base64 string
            byte[] compressedByteArray = outputStream.toByteArray();
            return Base64.encodeToString(compressedByteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
