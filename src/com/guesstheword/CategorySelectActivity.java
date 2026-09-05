package com.guesstheword;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectActivity extends Activity {
    private DatabaseHelper dbHelper;
    private ListView lvCategories;
    private CategoryAdapter adapter;
    private List<Category> categories;
    private Button btnAddCategory, btnStartGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        dbHelper = new DatabaseHelper(this);
        lvCategories = findViewById(R.id.lv_categories);
        btnAddCategory = findViewById(R.id.btn_add_category);
        btnStartGame = findViewById(R.id.btn_start_game);

        categories = dbHelper.getAllCategories();
        adapter = new CategoryAdapter(this, categories);
        lvCategories.setAdapter(adapter);
        lvCategories.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lvCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cb = view.findViewById(R.id.cb_category);
                cb.setChecked(!cb.isChecked());
                adapter.toggleSelection(position);
            }
        });

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
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
                            Category cat = new Category(name, false);
                            cat.setId(id);
                            categories.add(cat);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, R.string.category_added, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.category_exists, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void startGame() {
        List<Long> selectedIds = adapter.getSelectedCategoryIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("category_ids", selectedIds.toArray(new Long[0]));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}