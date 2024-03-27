package Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class SpinnerHandler {
    private static final String MODE_KEY = "mode";
    private Context mContext;
    private Spinner mSpinner;
    private String mode = "Best photo";

    public SpinnerHandler(Context context, Spinner spinner) {
        mContext = context;
        mSpinner = spinner;
        loadModeFromPreferences(); // Load mode from SharedPreferences

        List<String> photoTypes = new ArrayList<>();
        photoTypes.add("Best photo");
        photoTypes.add("Best group photo");
        photoTypes.add("Best document photo");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, photoTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set initial selection based on the loaded mode
        int position = photoTypes.indexOf(mode);
        if (position >= 0) {
            spinner.setSelection(position);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mode = parentView.getItemAtPosition(position).toString();
                saveModeToPreferences(); // Save mode to SharedPreferences
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                mode = "Best photo";
            }
        });
    }

    // Load mode from SharedPreferences
    private void loadModeFromPreferences() {
        SharedPreferences preferences = mContext.getSharedPreferences("SpinnerHandlerPrefs", Context.MODE_PRIVATE);
        mode = preferences.getString(MODE_KEY, mode);
    }

    // Save mode to SharedPreferences
    private void saveModeToPreferences() {
        SharedPreferences preferences = mContext.getSharedPreferences("SpinnerHandlerPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MODE_KEY, mode);
        editor.apply();
    }

    // Getter method to retrieve the current mode
    public String getMode() {
        return mode;
    }
}