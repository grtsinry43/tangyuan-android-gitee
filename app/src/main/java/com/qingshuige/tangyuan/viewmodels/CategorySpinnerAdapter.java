package com.qingshuige.tangyuan.viewmodels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.network.Category;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Category> {
    private Context context;
    private List<Category> items;

    public CategorySpinnerAdapter(@NonNull Context context, int resource, @NonNull List<Category> objects) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    private View createCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_spinner_category, parent, false);

        TextView name = view.findViewById(R.id.textCategoryName);
        TextView disc = view.findViewById(R.id.textCategoryDisc);

        name.setText(items.get(position).baseName);
        disc.setText(items.get(position).baseDescription);

        return view;
    }
}
