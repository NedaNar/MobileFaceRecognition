package Classes;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResponseModel {
    @SerializedName("request_id")
    public String requestId;

    @SerializedName("time_used")
    public int timeUsed;

    @SerializedName("faces")
    public List<Face> faces;

    @SerializedName("image_id")
    public String imageId;

    @SerializedName("face_num")
    public int faceNum;

    public static class Face {
        @SerializedName("face_token")
        public String faceToken;

        @SerializedName("face_rectangle")
        public FaceRectangle faceRectangle;

        @SerializedName("attributes")
        public Attributes attributes;
    }

    public static class FaceRectangle {
        public int top;
        public int left;
        public int width;
        public int height;
    }

    public static class Attributes {
        public Gender gender;
        public Age age;
    }

    public static class Gender {
        public String value;
    }

    public static class Age {
        public int value;
    }
}
