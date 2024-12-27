package com.hfad.paseka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.time.LocalDate;

public class TaskExecuteActivity extends AppCompatActivity {
    private boolean ask_status;
    private boolean ask_comment;
    private boolean ask_total;

    private EditText view_status;
    private EditText view_comment;
    private EditText view_total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_execute);

        Intent intent = getIntent();
        ask_status = intent.getBooleanExtra("ask_status", true);
        ask_comment = intent.getBooleanExtra("ask_comment", true);
        ask_total = intent.getBooleanExtra("ask_total", true);

        view_status = findViewById(R.id.status);
        view_comment = findViewById(R.id.comment);
        view_total = findViewById(R.id.total);

        if (!ask_status) { view_status.setVisibility(View.GONE); }
        if (!ask_comment) { view_comment.setVisibility(View.GONE); }
        if (!ask_total) { view_total.setVisibility(View.GONE); }
    }
}