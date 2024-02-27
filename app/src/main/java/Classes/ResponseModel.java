package Classes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseModel {
    @SerializedName("faces")
    public List<Face> faces;

    @SerializedName("face_num")
    public int faceNum;

    public static class Face {
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
        public String gender;
        public int age;
        public double smile;
        @SerializedName("headpose")
        public HeadPose headPose;
        public double blur;
        @SerializedName("eyestatus")
        public EyeStatus eyeStatus;
        public Emotion emotion;
        @SerializedName("facequality")
        public double faceQuality;
        public Beauty beauty;
    }

    public static class HeadPose {
        @SerializedName("pitch_angle")
        public double pitchAngle;
        @SerializedName("roll_angle")
        public double rollAngle;
        @SerializedName("yaw_angle")
        public double yawAngle;
    }

    public static class EyeStatus {
        @SerializedName("left_eye_status")
        public Eye leftEyeStatus;
        @SerializedName("right_eye_status")
        public Eye rightEyeStatus;

        public static class Eye {
            @SerializedName("no_glass_eye_open")
            public double noGlassEyeOpen;
            @SerializedName("no_glass_eye_close")
            public double noGlassEyeClose;
            @SerializedName("normal_glass_eye_open")
            public double normalGlassEyeOpen;
            @SerializedName("normal_glass_eye_close")
            public double normalGlassEyeClose;
            @SerializedName("dark_glasses")
            public double darkGlasses;
            public double occlusion;
        }
    }

    public static class Emotion {
        public double anger;
        public double disgust;
        public double fear;
        public double happiness;
        public double neutral;
        public double sadness;
        public double surprise;
    }

    public static class Beauty {
        @SerializedName("male_score")
        public double maleScore;
        @SerializedName("female_score")
        public double femaleScore;
    }
}
