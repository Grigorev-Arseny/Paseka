package com.hfad.paseka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventoryModifyActivity extends AppCompatActivity {
    private SQLiteDatabase db;

    private InventoryActivity.Action action;
    private long row_id;
    private Spinner category;
    private TextView inventory_number;
    private TextView number;
    private Spinner hive_type;
    private TextView size;
    private Spinner location;
    private TextView description;
    private Switch broken;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_modify);

        Intent intent = getIntent();
        action = (InventoryActivity.Action) intent.getSerializableExtra(InventoryActivity.EXTRA_ACTION);
        row_id = (long) intent.getSerializableExtra(InventoryActivity.EXTRA_ROW_ID);
        //Log.d("PASEKA", "EXTRA_CATEGORY: " + row_id + action);


        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(this);
            db = db_helper.getReadableDatabase();
        }
        catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        category = findViewById(R.id.category);
        inventory_number = findViewById(R.id.inventory_number);
        number = findViewById(R.id.number);
        hive_type = findViewById(R.id.hive_type);
        size = findViewById(R.id.size);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        broken = findViewById(R.id.broken);
        save = findViewById(R.id.save);

        if (action == InventoryActivity.Action.Add) { save.setText(getString(R.string.action_Add)); }
        if (action == InventoryActivity.Action.Edit) { save.setText(getString(R.string.action_Edit)); }

        // Создаём курсор с данными таблицы InventoryCategories
        Cursor category_cursor =
                db.rawQuery("SELECT _id, category FROM InventoryCategories", null);

        // Создаем адаптер и слушателя выбора инвентаря
        CursorAdapter category_adapter = new ExtCursorAdapter(this,
                android.R.layout.simple_list_item_activated_1,
                //R.layout.spinner_item_bold,
                category_cursor,
                new String[] {"category"},
                new int[]{android.R.id.text1},
                0,
                getString(R.string.inventory_modify_category));
        category.setAdapter(category_adapter);

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                //Log.d("PASEKA", "Inventory Category: " + selectedItemPosition + ", " + selectedId);

                /*category_id = selectedId;
                if (location_id != -1 && category_id != -1) {
                    inventory_adapter.swapCursor(getInventory());
                    inventory_adapter.clearSelection();
                }*/
            }
            public void onNothingSelected(AdapterView<?> parent) {
                 parent.getSelectedItem();
            }
        });


        // Создаём курсор с данными таблицы HiveTypes
        Cursor hive_type_cursor =
                db.rawQuery("SELECT _id, type FROM HiveTypes", null);

        // Создаем адаптер и слушателя выбора типа улья
        CursorAdapter hive_type_adapter = new ExtCursorAdapter(this,
                android.R.layout.simple_list_item_activated_1,
                //R.layout.spinner_item_bold,
                hive_type_cursor,
                new String[] {"type"},
                new int[] {android.R.id.text1},
                0,
                getString(R.string.inventory_modify_hive_type));
        hive_type.setAdapter(hive_type_adapter);

        hive_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                //Log.d("PASEKA", "Hive type: " + selectedItemPosition + ", " + selectedId);

                /*category_id = selectedId;
                if (location_id != -1 && category_id != -1) {
                    inventory_adapter.swapCursor(getInventory());
                    inventory_adapter.clearSelection();
                }*/
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // Создаём курсор с данными таблицы Locations
        Cursor location_cursor =
                db.rawQuery("SELECT _id, address FROM Locations", null);

        // Создаем адаптер и слушателя выбора местоположения
        CursorAdapter location_adapter = new ExtCursorAdapter(this,
                android.R.layout.simple_list_item_activated_1,
                //R.layout.spinner_item_bold,
                location_cursor,
                new String[] {"address"},
                new int[] {android.R.id.text1},
                0,
                getString(R.string.inventory_modify_location));
        location.setAdapter(location_adapter);

        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                //Log.d("PASEKA", "Location: " + selectedItemPosition + ", " + selectedId);

                /*category_id = selectedId;
                if (location_id != -1 && category_id != -1) {
                    inventory_adapter.swapCursor(getInventory());
                    inventory_adapter.clearSelection();
                }*/
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Заполним поля формы данными для action = Edit
        if (action == InventoryActivity.Action.Edit) {
            // Создаём курсор с данными таблицы Inventory
            Cursor cursor = db.rawQuery("SELECT _id, category_id, inventory_number, number, type_id, size, location_id, description, broken FROM Inventory WHERE _id = " + row_id, null);
            cursor.moveToFirst();

            if (cursor.getInt(cursor.getColumnIndexOrThrow("category_id")) != 1) {
                number.setVisibility(View.GONE);
            }

            category.setSelection(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
            inventory_number.setText(cursor.getString(cursor.getColumnIndexOrThrow("inventory_number")));
            number.setText(cursor.getString(cursor.getColumnIndexOrThrow("number")));
            hive_type.setSelection(cursor.getInt(cursor.getColumnIndexOrThrow("type_id")));
            size.setText(cursor.getString(cursor.getColumnIndexOrThrow("size")));
            location.setSelection(cursor.getInt(cursor.getColumnIndexOrThrow("location_id")));
            description.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            broken.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow("broken")) > 0);
        }
    }

    public void onSaveClick(View view) {
        if(!inventory_number.getText().toString().isEmpty() && !number.getText().toString().isEmpty() && !size.getText().toString().isEmpty()
                && category.getSelectedItemId() != 0 && hive_type.getSelectedItemId() != 0 && location.getSelectedItemId() != 0) {
            if (action == InventoryActivity.Action.Add) {
                System.out.println("Add");
                ContentValues values = new ContentValues();
                values.put("category_id", category.getSelectedItemId());
                values.put("inventory_number", inventory_number.getText().toString());
                values.put("number", number.getText().toString());
                values.put("type_id", hive_type.getSelectedItemId());
                values.put("size", size.getText().toString());
                values.put("location_id", location.getSelectedItemId());
                values.put("description", description.getText().toString());
                values.put("broken", broken.isChecked());
                values.put("made", LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS")));

                row_id = db.insert("Inventory", null, values);
            }

            /*
            1. Обновить курсор
            2. Закрыть активность
            3. Проверить дубль инв. номер (сортировать по номеру улья, автоматом подставить номер улья)
            4. Проверить дубль номера улья
            5. При добавлении корпуса, магазина, ?ловушки? убрать номер (новая активность)
            */


            if (action == InventoryActivity.Action.Edit) {
                System.out.println("Edit");
                ContentValues values = new ContentValues();
                values.put("category_id", category.getSelectedItemId());
                values.put("inventory_number", inventory_number.getText().toString());
                //values.put("number", number.getText().toString());
                values.put("type_id", hive_type.getSelectedItemId());
                values.put("size", size.getText().toString());
                values.put("location_id", location.getSelectedItemId());
                values.put("description", description.getText().toString());
                values.put("broken", broken.isChecked());
                //values.put("made", LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS")));

                row_id = db.update("Inventory", values, "number = ?", new String[] {number.getText().toString()});
            }

            // Передадим id созданной/измененной строки в родительскую активность
            Intent intent = new Intent();
            intent.putExtra(InventoryActivity.EXTRA_ROW_ID, row_id);
            setResult(RESULT_OK, intent);
            finish();

        }

        else {
        (Toast.makeText(this, "Не все поля заполнены!", Toast.LENGTH_LONG)).show();
        }
    }
}


class ExtCursorAdapter extends SimpleCursorAdapter {
    private LayoutInflater layoutInflater;
    private String caption;

    public ExtCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags, String caption) {
        super(context, layout, cursor, from, to, flags);

        this.layoutInflater = LayoutInflater.from(context);
        this.caption = caption;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            TextView prompt = (TextView)layoutInflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
            prompt.setText(this.caption);
            prompt.setTextColor(Colors.getTextAppearanceListItemSecondary());
            return prompt;
        }
        return super.getView(position - 1, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            TextView prompt = (TextView) layoutInflater.inflate(R.layout.spinner_row_nothing_selected, parent, false);
            prompt.setText(this.caption);
            return prompt;
        }
        return super.getDropDownView(position - 1, null, parent);
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : super.getItem(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position > 0 ? super.getItemId(position - 1) : 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0; // Don't allow the 'nothing selected' item to be picked.
    }
}