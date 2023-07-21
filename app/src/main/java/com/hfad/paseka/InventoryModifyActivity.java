package com.hfad.paseka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class InventoryModifyActivity extends AppCompatActivity {
    private InventoryActivity.Action action;
    private long row_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_modify);

        Intent intent = getIntent();
        action = (InventoryActivity.Action) intent.getSerializableExtra(InventoryActivity.EXTRA_ACTION);
        row_id = (long) intent.getSerializableExtra(InventoryActivity.EXTRA_ROW_ID);
        Log.d("PASEKA", "EXTRA_CATEGORY: " + row_id + action);
    }
}