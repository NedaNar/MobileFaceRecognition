package Classes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RecyclerAdapterList extends RecyclerView.Adapter<RecyclerAdapterList.ViewHolder> {
    private ArrayList<ImageModel> images;
    private String recyclerViewId;

    public RecyclerAdapterList(ArrayList<ImageModel> images, String recyclerViewId) {
        ArrayList<ImageModel> updatedImages = new ArrayList<>();

        this.recyclerViewId = recyclerViewId;
        Collections.sort(images, new Comparator<ImageModel>() {
            @Override
            public int compare(ImageModel o1, ImageModel o2) {
                return Float.compare(o2.Points, o1.Points);
            }
        });

        if (recyclerViewId.equals("recyclerViewBestImage")) {
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
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_item, parent, false);

        LinearLayout parentLayout = view.findViewById(R.id.itemLayout);
        if (recyclerViewId == "recyclerViewBestImage"){
            parentLayout.setBackgroundResource(R.drawable.list_item_background);
        } else {
            parentLayout.setBackground(null);
        }

        Button moreButton = (Button) view.findViewById(R.id.moreButton);
        moreButton.setPaintFlags(moreButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterList.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.imageView.setImageURI(images.get(position).ImageUri);

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

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
        } else {
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
                showCustomDialog(v.getContext(), finalPointsText, images.get(position).ImageUri);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
            textPoints = itemView.findViewById(R.id.textPoints);
            textGender = itemView.findViewById(R.id.textGender);
            textAge = itemView.findViewById(R.id.textAge);
            textMessage = itemView.findViewById(R.id.textMessage);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }

    private void showCustomDialog(Context context, String points, Uri imageUri) {
        Dialog dialog = new Dialog(context, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.dialog_layout);

        ImageView closeButton = dialog.findViewById(R.id.closeButton);
        TextView pointsText = dialog.findViewById(R.id.dialogText);
        ImageView imageView = dialog.findViewById(R.id.dialogImage);

        pointsText.setText(FormatText("Total points: ", points));
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
}
