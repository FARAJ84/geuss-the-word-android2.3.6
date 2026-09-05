package com.guesstheword;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class ImportExportActivity extends Activity {
    private DatabaseHelper dbHelper;
    private Button btnExportWords, btnExportCategories, btnExportAll;
    private Button btnImportWords, btnImportCategories, btnImportAll, btnUpdateList;
    private Button btnAddWord, btnAddCategory;

    private static final int REQUEST_EXPORT = 1;
    private static final int REQUEST_IMPORT = 2;
    private long targetCategoryId = -1;
    private int importMode = 0; // 0=words, 1=categories, 2=all, 3=update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        dbHelper = new DatabaseHelper(this);

        btnExportWords = findViewById(R.id.btn_export_words);
        btnExportCategories = findViewById(R.id.btn_export_categories);
        btnExportAll = findViewById(R.id.btn_export_all);
        btnImportWords = findViewById(R.id.btn_import_words);
        btnImportCategories = findViewById(R.id.btn_import_categories);
        btnImportAll = findViewById(R.id.btn_import_all);
        btnUpdateList = findViewById(R.id.btn_update_list);
        btnAddWord = findViewById(R.id.btn_add_word);
        btnAddCategory = findViewById(R.id.btn_add_category);

        btnExportWords.setOnClickListener(v -> selectCategoryForExport());
        btnExportCategories.setOnClickListener(v -> exportCategories());
        btnExportAll.setOnClickListener(v -> exportAll());
        btnImportWords.setOnClickListener(v -> { importMode = 0; selectCategoryForImport(); });
        btnImportCategories.setOnClickListener(v -> { importMode = 1; pickFile(); });
        btnImportAll.setOnClickListener(v -> { importMode = 2; pickFile(); });
        btnUpdateList.setOnClickListener(v -> { importMode = 3; pickFile(); });
        btnAddWord.setOnClickListener(v -> showAddWordDialog());
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void selectCategoryForExport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category to Export");

        DatabaseHelper db = new DatabaseHelper(this);
        final List<Category> cats = db.getAllCategories();
        db.close();

        String[] names = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) names[i] = cats.get(i).getName();

        builder.setItems(names, (dialog, which) -> {
            targetCategoryId = cats.get(which).getId();
            exportWords(targetCategoryId);
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    private void selectCategoryForImport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category to Import Into");

        DatabaseHelper db = new DatabaseHelper(this);
        final List<Category> cats = db.getAllCategories();
        db.close();

        String[] names = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) names[i] = cats.get(i).getName();

        builder.setItems(names, (dialog, which) -> {
            targetCategoryId = cats.get(which).getId();
            pickFile();
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    private void exportWords(long categoryId) {
        File file = new File(Environment.getExternalStorageDirectory(), "guesstheword_words_" + categoryId + ".json");
        if (dbHelper.exportWords(file, categoryId)) {
            Toast.makeText(this, getString(R.string.file_saved, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCategories() {
        File file = new File(Environment.getExternalStorageDirectory(), "guesstheword_categories.json");
        if (dbHelper.exportCategories(file)) {
            Toast.makeText(this, getString(R.string.file_saved, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportAll() {
        File file = new File(Environment.getExternalStorageDirectory(), "guesstheword_all.json");
        if (dbHelper.exportAll(file)) {
            Toast.makeText(this, getString(R.string.file_saved, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            File file = new File(uri.getPath());
            performImport(file);
        }
    }

    private void performImport(File file) {
        boolean success = false;
        switch (importMode) {
            case 0: success = dbHelper.importWords(file, targetCategoryId); break;
            case 1: success = dbHelper.importCategories(file); break;
            case 2: success = dbHelper.importAll(file); break;
            case 3: success = dbHelper.updateWordList(file); break;
        }
        if (success) {
            Toast.makeText(this, getString(R.string.file_loaded, file.getName()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.error_invalid_format, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddWordDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Word (3-7 letters, use space for multi-part)");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_word);
        builder.setView(input);

        // Category selection
        final DatabaseHelper db = new DatabaseHelper(this);
        final List<Category> cats = db.getAllCategories();
        db.close();

        String[] catNames = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) catNames[i] = cats.get(i).getName();
        final int[] selectedCat = {0};

        builder.setSingleChoiceItems(catNames, 0, (dialog, which) -> selectedCat[0] = which);
        builder.setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
            String text = input.getText().toString().trim().toUpperCase();
            if (text.length() >= 3 && text.replace(" ", "").length() <= 7) {
                boolean multi = text.contains(" ");
                long catId = cats.get(selectedCat[0]).getId();
                if (!dbHelper.wordExists(text, catId)) {
                    dbHelper.addWord(text, catId, multi);
                    Toast.makeText(this, R.string.word_added, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.word_exists, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Word must be 3-7 letters", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    private void showAddCategoryDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Category name");

        new AlertDialog.Builder(this)
                .setTitle(R.string.enter_category_name)
                .setView(input)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        long id = dbHelper.addCategory(name);
                        if (id > 0) {
                            Toast.makeText(this, R.string.category_added, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.category_exists, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}