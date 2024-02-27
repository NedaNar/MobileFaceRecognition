package Classes;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ResponseModelDeserializer implements JsonDeserializer<ResponseModel> {

    @Override
    public ResponseModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ResponseModel responseModel = new ResponseModel();

        JsonArray facesArray = jsonObject.getAsJsonArray("faces");
        List<ResponseModel.Face> faces = new ArrayList<>();
        for (JsonElement faceElement : facesArray) {
            ResponseModel.Face face = new ResponseModel.Face();
            JsonObject faceObject = faceElement.getAsJsonObject();

            JsonObject faceRectangleObject = faceObject.getAsJsonObject("face_rectangle");
            ResponseModel.FaceRectangle faceRectangle = new ResponseModel.FaceRectangle();
            faceRectangle.top = faceRectangleObject.get("top").getAsInt();
            faceRectangle.left = faceRectangleObject.get("left").getAsInt();
            faceRectangle.width = faceRectangleObject.get("width").getAsInt();
            faceRectangle.height = faceRectangleObject.get("height").getAsInt();
            face.faceRectangle = faceRectangle;

            JsonObject attributesObject = faceObject.getAsJsonObject("attributes");
            ResponseModel.Attributes attributes = new ResponseModel.Attributes();
            attributes.gender = attributesObject.getAsJsonObject("gender").get("value").getAsString();
            attributes.age = attributesObject.getAsJsonObject("age").get("value").getAsInt();
            attributes.smile = attributesObject.getAsJsonObject("smile").get("value").getAsDouble();
            attributes.headPose = context.deserialize(attributesObject.getAsJsonObject("headpose"), ResponseModel.HeadPose.class);
            attributes.blur = attributesObject.getAsJsonObject("blur").getAsJsonObject("blurness").get("value").getAsDouble();
            attributes.eyeStatus = context.deserialize(attributesObject.getAsJsonObject("eyestatus"), ResponseModel.EyeStatus.class);
            attributes.faceQuality = attributesObject.getAsJsonObject("facequality").get("value").getAsDouble();
            attributes.emotion = context.deserialize(attributesObject.getAsJsonObject("emotion"), ResponseModel.Emotion.class);
            attributes.beauty = context.deserialize(attributesObject.getAsJsonObject("beauty"), ResponseModel.Beauty.class);

            face.attributes = attributes;
            faces.add(face);
        }
        responseModel.faces = faces;
        responseModel.faceNum = jsonObject.get("face_num").getAsInt();

        return responseModel;
    }
}