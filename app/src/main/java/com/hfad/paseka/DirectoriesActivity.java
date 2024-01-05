package com.hfad.paseka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class DirectoriesActivity extends AppCompatActivity {
    private static final String[][] DB_TABLES_INFO = {
            {"Locations", "address"},
            {"HiveTypes", "type"},
            {"Breeds", "breed"}
    };

    private SQLiteDatabase db;
    private int directory_id;
    private long directory_item_id;
    private int directory_item_ix;

    private Spinner directories;
    private ListView directory;
    private ImageButton btn_add;
    private ImageButton btn_edit;
    private ImageButton btn_delete;
    private EditText edit_item;

    enum Action {Add, Edit, Delete}
    private Action action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directories);

        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(this);
            db = db_helper.getReadableDatabase();
        }
        catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        directories = findViewById(R.id.directories);
        directory = findViewById(R.id.directory);
        btn_add = findViewById(R.id.add);
        btn_edit = findViewById(R.id.edit);
        btn_delete = findViewById(R.id.delete);
        //edit_item = findViewById(R.id.edit_item);
        edit_item = new AppCompatEditText(this) {
            @Override
            public boolean onKeyPreIme(int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    //Log.d("PASEKA", "onKeyPreIme KEYCODE_BACK");
                    showFooterEdit(false);
                }
                return super.onKeyPreIme(keyCode, event);
            }
        };
        edit_item.setInputType(1);
        edit_item.setVisibility(View.GONE);
        edit_item.setLayoutParams(new LinearLayout.LayoutParams(0, 48*(int)this.getResources().getDisplayMetrics().density, 1));
        edit_item.setPadding(16*(int)this.getResources().getDisplayMetrics().density, 0, 16*(int)this.getResources().getDisplayMetrics().density, 0);

        LinearLayout footer = findViewById(R.id.footer);
        footer.addView(edit_item);

        // Создаем адаптер и слушателя выбора справочника
        ArrayAdapter<?> directories_adapter =
            ArrayAdapter.createFromResource(this, R.array.directories, R.layout.spinner_item_bold /*android.R.layout.simple_spinner_item*/);
        //directories_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directories.setAdapter(directories_adapter);

        directories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                //Log.d("PASEKA", "onItemSelected");
                //Toast.makeText(getApplicationContext(),"Ваш выбор: " + getResources().getStringArray(R.array.directories)[selectedItemPosition], Toast.LENGTH_LONG).show();

                directory_item_id = 0;
                directory_item_ix = -1;
                directory_id = selectedItemPosition;

                btn_edit.setEnabled(false);
                btn_delete.setEnabled(false);

                showFooterEdit(false);

                // Создаём курсор с соответствующими данными
                Cursor cursor = db.query(DB_TABLES_INFO[directory_id][0],
                    new String[] {"_id", DB_TABLES_INFO[directory_id][1]},
                    null, null, null, null, null);

                // Закрываем курсор открытого ранее справочника
                if(directory.getAdapter() != null) {
                    ((CursorAdapter)directory.getAdapter()).getCursor().close();
                }

                // Заполняем ListView id - directory
                directory.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                CursorAdapter directory_adapter = new SimpleCursorAdapter(getApplicationContext(),
                    android.R.layout.simple_list_item_activated_1,
                    cursor,
                    new String[] {DB_TABLES_INFO[directory_id][1]},
                    new int[]{android.R.id.text1},
                    0);
                directory.setAdapter(directory_adapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Создаем слушателя выбора записи справочника
        directory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                directory_item_id = id;
                directory_item_ix = position;

                btn_edit.setEnabled(true);
                btn_delete.setEnabled(true);
            }
        });

        // Создаем слушателя ввода нового значения справочника
        edit_item.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Log.d("PASEKA", "onEditorAction IME_ACTION_DONE");
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    showFooterEdit(false);

                    if (action == Action.Add) {
                        ContentValues values = new ContentValues();
                        values.put(DB_TABLES_INFO[directory_id][1], edit_item.getText().toString());
                        db.insert(DB_TABLES_INFO[directory_id][0], null, values);

                        // Обновление курсора directory
                        ((CursorAdapter)directory.getAdapter()).changeCursor(
                                db.query(DB_TABLES_INFO[directory_id][0],
                                        new String[] {"_id", DB_TABLES_INFO[directory_id][1]},
                                        null, null, null, null, null)
                        );

                        directory.clearChoices();
                        directory_item_id = 0;
                        directory_item_ix = -1;
                        btn_edit.setEnabled(false);
                        btn_delete.setEnabled(false);

                        Toast.makeText(getApplicationContext(),"Запись добавлена.", Toast.LENGTH_LONG).show();
                    }

                    if (action == Action.Edit) {
                        ContentValues values = new ContentValues();
                        values.put(DB_TABLES_INFO[directory_id][1], edit_item.getText().toString());
                        db.update(DB_TABLES_INFO[directory_id][0], values,
                                "_id = ?", new String[] { Long.toString(directory_item_id) });

                        // Обновление курсора directory
                        ((CursorAdapter)directory.getAdapter()).changeCursor(
                                db.query(DB_TABLES_INFO[directory_id][0],
                                        new String[] {"_id", DB_TABLES_INFO[directory_id][1]},
                                        null, null, null, null, null)
                        );

                        directory.clearChoices();
                        directory_item_id = 0;
                        directory_item_ix = -1;
                        btn_edit.setEnabled(false);
                        btn_delete.setEnabled(false);

                        Toast.makeText(getApplicationContext(),"Запись изменена.", Toast.LENGTH_LONG).show();
                    }

                }
                return false;
            }
        });

    }

    private void showFooterEdit(boolean show) {
        if (!show) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_item.getWindowToken(), 0);
        }

        btn_add.setVisibility(show ? View.GONE : View.VISIBLE);
        btn_edit.setVisibility(show ? View.GONE : View.VISIBLE);
        btn_delete.setVisibility(show ? View.GONE : View.VISIBLE);
        edit_item.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void onAddClick(View view) {
        action = Action.Add;
        showFooterEdit(true);
        edit_item.setText("");

        if (edit_item.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit_item, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void onEditClick(View view) {
        action = Action.Edit;
        showFooterEdit(true);
        edit_item.setText(((TextView)directory.getChildAt(directory_item_ix)).getText().toString());
        edit_item.setSelection(edit_item.getText().length());

        if (edit_item.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit_item, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void onDeleteClick(View view) {
        new AlertDialog.Builder(this)
            //.setTitle("Delete entry")
            .setMessage(R.string.delete_row_confirm)
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    db.delete(DB_TABLES_INFO[directory_id][0], "_id = ?", new String[] {Long.toString(directory_item_id)});

                    // Обновление курсора directory
                    ((CursorAdapter)directory.getAdapter()).changeCursor(
                        db.query(DB_TABLES_INFO[directory_id][0],
                            new String[] {"_id", DB_TABLES_INFO[directory_id][1]},
                            null, null, null, null, null)
                    );

                    directory_item_id = 0;
                    directory_item_ix = -1;
                    btn_edit.setEnabled(false);
                    btn_delete.setEnabled(false);

                    Toast.makeText(getApplicationContext(),"Запись удалена.", Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            //.setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}