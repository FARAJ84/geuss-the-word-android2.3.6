package com.guesstheword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {
    private List<Category> categories;
    private List<Boolean> selected;
    private Context context;

    public CategoryAdapter(Context context, List<Category> categories) {
        super(context, R.layout.list_item_category, categories);
        this.context = context;
        this.categories = categories;
        this.selected = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            selected.add(i == 0); // "All" selected by default
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_category, parent, false);
            holder = new ViewHolder();
            holder.cbCategory = convertView.findViewById(R.id.cb_category);
            holder.tvCategoryName = convertView.findViewById(R.id.tv_category_name);
            holder.tvWordCount = convertView.findViewById(R.id.tv_word_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category cat = categories.get(position);
        holder.tvCategoryName.setText(cat.getName());

        DatabaseHelper db = new DatabaseHelper(context);
        int count = db.getWordCount(cat.getId());
        db.close();

        holder.tvWordCount.setText(count + " words");
        holder.cbCategory.setChecked(selected.get(position));
        holder.cbCategory.setTag(position);

        return convertView;
    }

    public void toggleSelection(int position) {
        selected.set(position, !selected.get(position));
        notifyDataSetChanged();
    }

    public List<Long> getSelectedCategoryIds() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i)) {
                ids.add(categories.get(i).getId());
            }
        }
        return ids;
    }

    static class ViewHolder {
        CheckBox cbCategory;
        TextView tvCategoryName;
        TextView tvWordCount;
    }
}