package Classes;

import java.util.Objects;

public class BestPhoto implements Mode {
    @Override
    public float [] PointCalculator(ResponseModel responseModel)
    {
        int sizeArray = responseModel.faces.size();

        if (sizeArray == 0)
            return null;

        float[] pointsArray = new float[sizeArray];
        float points = 0;
        for (int i = 0; i < sizeArray; i++) {
            ResponseModel.Attributes attributes = responseModel.faces.get(i).attributes;
            if ((attributes.eyeStatus.leftEyeStatus.noGlassEyeOpen >
                    attributes.eyeStatus.leftEyeStatus.noGlassEyeClose) ||
                    (attributes.eyeStatus.leftEyeStatus.normalGlassEyeOpen >
                            attributes.eyeStatus.leftEyeStatus.normalGlassEyeClose)) {
                if (attributes.eyeStatus.leftEyeStatus.noGlassEyeOpen >
                        attributes.eyeStatus.leftEyeStatus.normalGlassEyeOpen) {
                    points += attributes.eyeStatus.leftEyeStatus.noGlassEyeOpen / 100;
                } else points += attributes.eyeStatus.leftEyeStatus.normalGlassEyeOpen / 100;
            } else {
                if (attributes.eyeStatus.leftEyeStatus.noGlassEyeClose >
                        attributes.eyeStatus.leftEyeStatus.normalGlassEyeClose) {
                    points -= attributes.eyeStatus.leftEyeStatus.noGlassEyeClose / 100;
                } else points -= attributes.eyeStatus.leftEyeStatus.normalGlassEyeClose / 100;
            }

            if ((attributes.eyeStatus.rightEyeStatus.noGlassEyeOpen >
                    attributes.eyeStatus.rightEyeStatus.noGlassEyeClose) ||
                    (attributes.eyeStatus.rightEyeStatus.normalGlassEyeOpen >
                            attributes.eyeStatus.rightEyeStatus.normalGlassEyeClose)) {
                if (attributes.eyeStatus.rightEyeStatus.noGlassEyeOpen >
                        attributes.eyeStatus.rightEyeStatus.normalGlassEyeOpen) {
                    points += attributes.eyeStatus.rightEyeStatus.noGlassEyeOpen / 100;
                } else points += attributes.eyeStatus.rightEyeStatus.normalGlassEyeOpen / 100;
            } else {
                if (attributes.eyeStatus.rightEyeStatus.noGlassEyeClose >
                        attributes.eyeStatus.rightEyeStatus.normalGlassEyeClose) {
                    points -= attributes.eyeStatus.rightEyeStatus.noGlassEyeClose / 100;
                } else points -= attributes.eyeStatus.rightEyeStatus.normalGlassEyeClose / 100;
            }

            String gender = attributes.gender;
            if (Objects.equals(gender, "Male")) {
                points += attributes.beauty.maleScore / 100;
            } else {
                points += attributes.beauty.femaleScore / 100;
            }
            points += attributes.smile / 100 - attributes.blur / 100 + attributes.faceQuality / 100;
            points += (1 / responseModel.faces.get(i).attributes.headPose.pitchAngle) +
                    (1 / responseModel.faces.get(i).attributes.headPose.yawAngle) +
                    (1 / responseModel.faces.get(i).attributes.headPose.rollAngle);

            pointsArray[i] = points;

            //Fail safes
            if(pointsArray[i] > 5)
            {
                pointsArray[i] = 5;
            }
            if(pointsArray[i] < 0)
            {
                pointsArray[i] = 0;
            }
        }
        return pointsArray;
    }
}
