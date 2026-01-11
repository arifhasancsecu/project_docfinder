package com.find.doc.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.find.doc.BaseActivity;
import com.find.doc.R;
import com.find.doc.adapter.CategoryAdapter;
import com.find.doc.model.CategoryEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryListActivity extends BaseActivity {

    private RecyclerView categoryListView;
    private CategoryAdapter categoryAdapter;
    private TextInputEditText searchEditText;
    private List<CategoryEnum> allCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        setupBottomNavigation(R.id.all_doctor);

        categoryListView = findViewById(R.id.recycler_view_category);
        searchEditText = findViewById(R.id.search_doc_id_cat);

        allCategories = new ArrayList<>(Arrays.asList(CategoryEnum.values()));

        categoryAdapter = new CategoryAdapter(allCategories, true);
        categoryListView.setLayoutManager(new LinearLayoutManager(this));
        categoryListView.setAdapter(categoryAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCategories(String query) {
        List<CategoryEnum> filteredList = new ArrayList<>();
        for (CategoryEnum c : allCategories) {
            if (c.matches(query)) {
                filteredList.add(c);
            }
        }
        categoryAdapter.updateList(filteredList);
    }
}
