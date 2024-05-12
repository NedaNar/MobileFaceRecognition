package Classes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobileapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RecyclerAdapterList extends RecyclerView.Adapter<RecyclerAdapterList.ViewHolder> {
    private ArrayList<ImageModel> images;
    private String recyclerViewId;
    public RecyclerAdapterList(ArrayList<ImageModel> images, String recyclerViewId, String mode) {
        ArrayList<ImageModel> updatedImages = new ArrayList<>();

        this.recyclerViewId = recyclerViewId;

        Collections.sort(images, new Comparator<ImageModel>() {
            @Override
            public int compare(ImageModel o1, ImageModel o2) {
                return Float.compare(o2.Points, o1.Points);
            }
        });

        if (mode.equals("History")){
            updatedImages.addAll(images);
        }
        else if (recyclerViewId.equals("recyclerViewBestImage")) {
            if (!images.isEmpty()) {
                updatedImages.add(images.get(0));
            }
        } else {
            if (images.size() > 1) {
                updatedImages.addAll(images.subList(1, images.size()));
            }
        }

        this.images = updatedImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (recyclerViewId == "recyclerViewBestImage"){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.best_photo_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_item, parent, false);
        }

        Button moreButton = (Button) view.findViewById(R.id.moreButton);
        moreButton.setPaintFlags(moreButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterList.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(holder.itemView.getContext())
                .load(images.get(position).ImageUri)
                .into(holder.imageView);

        String pointsText = "";
        if (images.get(position).Points != 0 && images.get(position).Points != -1) {
            pointsText = String.valueOf(Math.round(images.get(position).Points * 10.0) / 10.0) + "/5";
            holder.textPoints.setText(FormatText("Points: ", pointsText));
        } else {
            holder.textPoints.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(images.get(position).Gender)) {
            holder.textGender.setText("Gender: " + images.get(position).Gender);
        } else {
            holder.textGender.setVisibility(View.GONE);
        }

        if (images.get(position).Age != 0) {
            holder.textAge.setText("Age: " + images.get(position).Age);
        }
        else {
            holder.textAge.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(images.get(position).Message)) {
            holder.textMessage.setText(images.get(position).Message);
            holder.moreButton.setVisibility(View.GONE);
        } else {
            holder.textMessage.setVisibility(View.GONE);
        }

        String finalPointsText = pointsText;
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog(v.getContext(), finalPointsText, Uri.parse(images.get(position).ImageUri), position);
            }
        });

        holder.instagramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstagram(v.getContext(), Uri.parse(images.get(position).ImageUri));
            }
        });
        holder.facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFacebook(v.getContext(), Uri.parse(images.get(position).ImageUri));
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textPoints;
        TextView textGender;
        TextView textAge;
        TextView textMessage;
        Button moreButton;
        ImageButton instagramButton;
        ImageButton facebookButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
            textPoints = itemView.findViewById(R.id.textPoints);
            textGender = itemView.findViewById(R.id.textGender);
            textAge = itemView.findViewById(R.id.textAge);
            textMessage = itemView.findViewById(R.id.textMessage);
            moreButton = itemView.findViewById(R.id.moreButton);
            instagramButton = itemView.findViewById(R.id.instaBtn);
            facebookButton = itemView.findViewById(R.id.fbBtn);
        }
    }

    private void showCustomDialog(Context context, String points, Uri imageUri, int position) {
        Dialog dialog = new Dialog(context, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.dialog_layout);

        ImageView closeButton = dialog.findViewById(R.id.closeButton);
        TextView pointsText = dialog.findViewById(R.id.dialogText);
        ImageView imageView = dialog.findViewById(R.id.dialogImage);

        TextView firstText = dialog.findViewById(R.id.dialogGender);
        TextView secondText = dialog.findViewById(R.id.dialogAge);
        TextView smilePoints = dialog.findViewById(R.id.dialogSmile);
        TextView beautyPoints = dialog.findViewById(R.id.dialogBeauty);
        TextView emotionsList = dialog.findViewById(R.id.dialogEmotions);

        pointsText.setText(FormatText("Total points: ", points));
        firstText.setText(FormatText("Gender: ", images.get(position).Gender));
        secondText.setText(FormatText("Age: ", String.valueOf(images.get(position).Age)));

        StringBuilder emotionsBuilder = new StringBuilder();
        ArrayList<String> emotions = images.get(position).Emotions; // Assuming you have a list of emotions in your ImageModel
        for (String emotion : emotions) {
            emotionsBuilder.append(emotion).append(", ");
        }
        // Remove the trailing comma and space
        if (emotionsBuilder.length() > 0) {
            emotionsBuilder.setLength(emotionsBuilder.length() - 2);
        }

        // Set the string representation of emotions to the TextView
        emotionsList.setText("Detected emotions: " + emotionsBuilder.toString());

        if (images.get(position).Beauty <= 25){
            beautyPoints.setText(FormatText("Beauty score: ", "low"));
        }
        else if (images.get(position).Beauty > 25 && images.get(position).Beauty < 60){
            beautyPoints.setText(FormatText("Beauty score: ", "average"));
        }
        else{
            beautyPoints.setText(FormatText("Beauty score: ", "high"));
        }

        if (images.get(position).Smile <= 2){
            smilePoints.setText(FormatText("Smile: ", "no smile"));
        }
        else if (images.get(position).Smile > 2 && images.get(position).Smile < 50){
            smilePoints.setText(FormatText("Smile: ", "small smile"));
        }
        else if (images.get(position).Smile >= 50 && images.get(position).Smile < 90){
            smilePoints.setText(FormatText("Smile: ", "average smile"));
        }
        else if (images.get(position).Smile >= 90){
            smilePoints.setText(FormatText("Smile: ", "big smile"));
        }

        imageView.setImageURI(imageUri);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private SpannableStringBuilder FormatText(String labelText, String valueText) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        SpannableString pointsLabelSpannable = new SpannableString(labelText);
        pointsLabelSpannable.setSpan(new StyleSpan(Typeface.NORMAL), 0, labelText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.append(pointsLabelSpannable);

        stringBuilder.append(valueText);
        stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), stringBuilder.length() - valueText.length(), stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return stringBuilder;
    }

    private void openInstagram(Context context, Uri imageUri) {
        String instagramPackageName = "com.instagram.android";

        try {
            // Open Instagram with the photo URL
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.setPackage(instagramPackageName);
            context.startActivity(intent);
        } catch (Exception e) {
            // Instagram app not found, open Instagram website
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com")));
        }
    }

    private void openFacebook(Context context, Uri imageUri) {
        String facebookPackageName = "com.facebook.katana";

        try {
            // Open Facebook with the photo URL
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.setPackage(facebookPackageName);
            context.startActivity(intent);
        } catch (Exception e) {
            // Facebook app not found, open Facebook website
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com")));
        }
    }
}
