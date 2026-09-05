package com.guesstheword;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;
    private SeekBar sbMinLength, sbMaxLength;
    private TextView tvMinLength, tvMaxLength;
    private CheckBox cbMultiPart, cbSound, cbVibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("GuessTheWordPrefs", MODE_PRIVATE);

        sbMinLength = findViewById(R.id.sb_min_length);
        sbMaxLength = findViewById(R.id.sb_max_length);
        tvMinLength = findViewById(R.id.tv_min_length);
        tvMaxLength = findViewById(R.id.tv_max_length);
        cbMultiPart = findViewById(R.id.cb_multi_part);
        cbSound = findViewById(R.id.cb_sound);
        cbVibration = findViewById(R.id.cb_vibration);

        // Load settings
        int minLen = prefs.getInt("min_length", 3);
        int maxLen = prefs.getInt("max_length", 7);
        boolean multiPart = prefs.getBoolean("multi_part", true);
        boolean sound = prefs.getBoolean("sound", true);
        boolean vibration = prefs.getBoolean("vibration", true);

        sbMinLength.setProgress(minLen - 3);
        sbMaxLength.setProgress(maxLen - 3);
        tvMinLength.setText(String.valueOf(minLen));
        tvMaxLength.setText(String.valueOf(maxLen));
        cbMultiPart.setChecked(multiPart);
        cbSound.setChecked(sound);
        cbVibration.setChecked(vibration);

        sbMinLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress + 3;
                tvMinLength.setText(String.valueOf(val));
                if (val > sbMaxLength.getProgress() + 3) {
                    sbMaxLength.setProgress(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbMaxLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress + 3;
                tvMaxLength.setText(String.valueOf(val));
                if (val < sbMinLength.getProgress() + 3) {
                    sbMinLength.setProgress(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("min_length", sbMinLength.getProgress() + 3);
        editor.putInt("max_length", sbMaxLength.getProgress() + 3);
        editor.putBoolean("multi_part", cbMultiPart.isChecked());
        editor.putBoolean("sound", cbSound.isChecked());
        editor.putBoolean("vibration", cbVibration.isChecked());
        editor.apply();
    }
}