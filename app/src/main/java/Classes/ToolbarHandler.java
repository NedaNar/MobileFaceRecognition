package Classes;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.mobileapplication.DisplayData;
import com.example.mobileapplication.R;

public class ToolbarHandler {
    private Context context;
    private ImageButton helpBtn;
    private Button historyBtn;

    public ToolbarHandler(Context context, ImageButton helpBtn, Button historyBtn) {
        this.context = context;
        this.helpBtn = helpBtn;
        this.historyBtn = historyBtn;

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog(context);
            }
        });

        historyBtn.setPaintFlags(historyBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayData.class);
                intent.putExtra("mode", "History");
                intent.putParcelableArrayListExtra("images", HistoryHelper.loadAnalyzedImages(context));
                context.startActivity(intent);
            }
        });
    }

    private void showHelpDialog(Context context) {
        final Dialog helpDialog = new Dialog(context);
        helpDialog.setContentView(R.layout.help_layout);
        helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        helpDialog.setCancelable(true);

        ImageView btnClose = helpDialog.findViewById(R.id.closeButton);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });

        helpDialog.show();
    }
}