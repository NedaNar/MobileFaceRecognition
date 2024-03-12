package Classes;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;

import java.util.ArrayList;

public class RecyclerAdapterList extends RecyclerView.Adapter<RecyclerAdapterList.ViewHolder> {
    private ArrayList<ImageModel> images;

    public RecyclerAdapterList(ArrayList<ImageModel> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_single_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterList.ViewHolder holder, int position) {
        holder.imageView.setImageURI(images.get(position).ImageUri);

        String message = images.get(position).Message;

        if (message.isEmpty())
            holder.textView.setText("Points: " + Math.round(images.get(position).Points * 10.0) / 10.0 + "/5");
        else
            holder.textView.setText(images.get(position).Message);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);

            textView = itemView.findViewById(R.id.textPoints);
        }
    }
}
