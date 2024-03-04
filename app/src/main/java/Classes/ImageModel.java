package Classes;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ImageModel implements Parcelable {

    public ImageModel(Uri imageUri, float points) {
        ImageUri = imageUri;
        Points = points;
    }
    public Uri ImageUri;

    public float Points;

    protected ImageModel(Parcel in) {
        ImageUri = in.readParcelable(Uri.class.getClassLoader());
        Points = in.readFloat();
    }

    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(ImageUri, flags);
        dest.writeFloat(Points);
    }
}
