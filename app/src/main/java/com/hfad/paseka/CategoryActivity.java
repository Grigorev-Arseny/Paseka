package com.hfad.paseka;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

public class CategoryActivity extends AppCompatActivity {

    private MainActivity.Categories category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Intent intent = getIntent();
        category = (MainActivity.Categories) intent.getSerializableExtra(MainActivity.EXTRA_CATEGORY);

        Log.d("PASEKA", "EXTRA_CATEGORY: " + category);
    }

    public void onAddClick(View view) {

    }

    public void onEditClick(View view) {

    }

    public void onDeleteClick(View view) {

    }
}