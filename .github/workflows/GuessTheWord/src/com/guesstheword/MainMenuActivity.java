package com.guesstheword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends Activity {
    private Button btnNewGame, btnCategories, btnImportExport, btnSettings, btnHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnNewGame = findViewById(R.id.btn_new_game);
        btnCategories = findViewById(R.id.btn_categories);
        btnImportExport = findViewById(R.id.btn_import_export);
        btnSettings = findViewById(R.id.btn_settings);
        btnHelp = findViewById(R.id.btn_help);

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenuActivity.this, CategorySelectActivity.class));
            }
        });

        btnCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenuActivity.this, CategorySelectActivity.class));
            }
        });

        btnImportExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenuActivity.this, ImportExportActivity.class));
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });
    }

    private void showHelpDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.help_title)
                .setMessage(R.string.help_text)
                .setPositiveButton(R.string.dialog_ok, null)
                .show();
    }
}