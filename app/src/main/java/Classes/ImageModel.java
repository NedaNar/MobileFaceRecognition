package Classes;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.ArrayList;
public class ImageModel implements Parcelable {


    public ImageModel(String imageUri, float points, String message, String gender, int age, double blur,
                      double smile, double beauty, double happiness, double sadness, List<String> emotions) {
        ImageUri = imageUri;
        Points = points;
        Message = message;
        Gender = gender;
        Age = age;
        Blur = blur;
        Smile = smile;
        Beauty = beauty;
        Happiness = happiness;
        Sadness = sadness;
        Emotions = new ArrayList<>(emotions);
    }

    public String ImageUri;
    public float Points;
    public String Gender;
    public int Age;
    public String Message;
    public double Blur;
    public double Smile;
    public double Beauty;
    public double Happiness;
    public double Sadness;

    public ArrayList<String> Emotions;
    protected ImageModel(Parcel in) {
        ImageUri = in.readString();
        Points = in.readFloat();
        Message = in.readString();
        Gender = in.readString();
        Age = in.readInt();
        Blur = in.readDouble();
        Smile = in.readDouble();
        Beauty = in.readDouble();
        Happiness = in.readDouble();
        Sadness = in.readDouble();
        Emotions = in.createStringArrayList();
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
        dest.writeString(ImageUri);
        dest.writeFloat(Points);
        dest.writeString(Message);
        dest.writeString(Gender);
        dest.writeInt(Age);
        dest.writeDouble(Blur);
        dest.writeDouble(Smile);
        dest.writeDouble(Beauty);
        dest.writeDouble(Happiness);
        dest.writeDouble(Sadness);
        dest.writeStringList(Emotions);
    }
}
