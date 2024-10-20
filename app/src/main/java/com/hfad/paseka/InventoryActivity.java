package com.hfad.paseka;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.*;



public class InventoryActivity extends AppCompatActivity {
    /*private static final String[][] DB_TABLES_INFO = {
            {"Locations", "address"},
            {"HiveTypes", "type"},
            {"Breeds", "breed"}
    };*/

    private SQLiteDatabase db;
    private long location_id;
    private long category_id;
    private long inventory_id;
    //private long directory_item_id;
    //private int directory_item_ix;

    private Spinner locations;
    private Spinner category;
    RecyclerView inventory;
    InventoryAdapter inventory_adapter;
    //private ListView directory;
    private ImageButton btn_add;
    private MenuItem btn_menu_edit;
    private MenuItem btn_menu_delete;
    //private EditText edit_item;


    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_ROW_ID = "row_id";
    enum Action {Add, Edit, Delete}
    //private DirectoriesActivity.Action action;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_menu, menu);

        btn_menu_edit = menu.getItem(0);
        btn_menu_delete = menu.getItem(1);

        btn_menu_edit.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_delete.getIcon().setTint(Colors.getBackgroundFloating());

        btn_menu_edit.setVisible(false);
        btn_menu_delete.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    public void setMenuBtnVisible(boolean visible) {
        btn_menu_edit.setVisible(visible);
        btn_menu_delete.setVisible(visible);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete:
                deleteInventory();
                return true;
            case R.id.edit:
                Intent intent = new Intent(this, InventoryModifyActivity.class);
                intent.putExtra(EXTRA_ACTION, Action.Edit);
                intent.putExtra(EXTRA_ROW_ID, inventory_id);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteInventory() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_inventory_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setItems(R.array.delete_reasons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("deleted", getResources().getStringArray(R.array.delete_reasons)[which]);
                        values.put("deleted_date", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())  );
                        db.update("Inventory", values,
                                "_id = ?", new String[] { Long.toString(inventory_id) });

                        // Обновление курсора directory
                        inventory_adapter.swapCursor(getInventory());
                    }
                })
                .create()
                .show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        location_id = -1;
        category_id = -1;

        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(this);
            db = db_helper.getReadableDatabase();
        }
        catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        locations = findViewById(R.id.locations);
        category = findViewById(R.id.category);
        inventory = findViewById(R.id.inventory);
        btn_add = findViewById(R.id.add);

        inventory.setLayoutManager(new LinearLayoutManager(this));
        inventory_adapter = new InventoryAdapter(this, getInventory());
        inventory.setAdapter(inventory_adapter);


        // Создаём курсор с данными таблицы Locations
        Cursor locations_cursor =
            db.rawQuery("SELECT _id, address, 2 AS sort, UPPER(address) FROM Locations UNION ALL " +
                    "SELECT 0 AS _id, 'Выбрать все' AS address, 1 AS sort, '' " +
                    "ORDER BY sort, UPPER(address)", null);

        // Создаем адаптер и слушателя выбора местоположения
        CursorAdapter locations_adapter = new SimpleCursorAdapter(this,
                //android.R.layout.simple_list_item_activated_1,
                R.layout.spinner_item_bold,
                locations_cursor,
                new String[] {"address"},
                new int[]{android.R.id.text1},
                0);
        locations.setAdapter(locations_adapter);

        locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                //Log.d("PASEKA", "createInventoryList " + location_id + ", " + inventory_id);

                location_id = selectedId;
                if (location_id != -1 && category_id != -1) {
                    inventory_adapter.swapCursor(getInventory());
                    inventory_adapter.clearSelection();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Создаём курсор с данными таблицы InventoryCategories
        Cursor category_cursor =
                db.rawQuery("SELECT _id, category FROM InventoryCategories", null);

        // Создаем адаптер и слушателя выбора инвентаря
        CursorAdapter category_adapter = new SimpleCursorAdapter(this,
                //android.R.layout.simple_list_item_activated_1,
                R.layout.spinner_item_bold,
                category_cursor,
                new String[] {"category"},
                new int[]{android.R.id.text1},
                0);
        category.setAdapter(category_adapter);

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                //Log.d("PASEKA", "createInventoryList " + location_id + ", " + inventory_id);

                category_id = selectedId;
                if (location_id != -1 && category_id != -1) {
                    inventory_adapter.swapCursor(getInventory());
                    inventory_adapter.clearSelection();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /*@Override
    protected void onRestart() {
        super.onRestart();

        inventory_adapter.swapCursor(getInventory());
        inventory_adapter.clearSelection();
    }*/

    private Cursor getInventory() {
        return db.rawQuery(
            "SELECT I._id, I.category_id, I.number, DATE(I.made) AS made, I.size, I.description, L.address, T.type FROM Inventory AS I " +
                    "LEFT JOIN Locations AS L ON L._id = I.location_id " +
                    "LEFT JOIN HiveTypes AS T ON T._id = I.type_id " +
                    "WHERE deleted_date IS NULL AND category_id = " + category_id +
                    (location_id == 0 ? "" : " AND location_id = " + location_id) +
                    " ORDER BY L.address, I.number", null);
    }

    public void onAddClick(View view) {
        /*Intent intent = new Intent(this, InventoryModifyActivity.class);
        intent.putExtra(EXTRA_ACTION, Action.Add);
        intent.putExtra(EXTRA_ROW_ID, 0L);
        startActivityForResult(intent, 1);*/

        modify_launcher.launch(new InventoryModifyExtras(Action.Add, 0L));
    }

    public void setInventoryId(long id) {
        this.inventory_id = id;
    }

    class InventoryModifyExtras {
        public Action action;
        public Long inventory_id;

        public InventoryModifyExtras(Action action, Long inventory_id) {
            this.action = action;
            this.inventory_id = inventory_id;
        }
    }

    class InventoryModifyContract extends ActivityResultContract<InventoryModifyExtras, Long> {
        public Intent createIntent(Context context, InventoryModifyExtras extras) {
            Intent intent = new Intent(context, InventoryModifyActivity.class);
            intent.putExtra(EXTRA_ACTION, extras.action);
            intent.putExtra(EXTRA_ROW_ID, extras.inventory_id);
            return intent;
        }

        public Long parseResult(int resultCode, Intent intent) {
            return resultCode == Activity.RESULT_OK ?
                (long) intent.getSerializableExtra(InventoryActivity.EXTRA_ROW_ID) : null;
        }
    }

    ActivityResultLauncher<InventoryModifyExtras> modify_launcher = registerForActivityResult(
        new InventoryModifyContract(),
        new ActivityResultCallback<Long>() {
            @Override
            public void onActivityResult(Long inventory_id) {
                Log.d("PASEKA", "modify_launcher " + inventory_id);
                //inventory_adapter.cursor.moveToPosition(Math.toIntExact(inventory_id));
                //inventory_adapter.setHasStableIds(true);
                //((InventoryAdapter.InventoryHiveViewHolder)inventory.findViewHolderForItemId(inventory_id)).setSelection();
                //Log.d("PASEKA", inventory.findViewHolderForItemId(inventory_id).toString());

                /*CompletableFuture<Void> asyncOp = CompletableFuture.runAsync(
                    () -> inventory_adapter.swapCursor(getInventory())
                );
                asyncOp.join();
                inventory_adapter.setSelectedByRowId(inventory_id);*/

                //inventory_adapter.swapCursor(getInventory());
                //inventory_adapter.setSelectedByRowId(inventory_id);

                inventory_adapter.swapCursor(getInventory());
                //inventory_adapter.
                //inventory.getLayoutManager().scrollToPosition();
            }
        });
}


class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private final Context context;
    private Cursor cursor;
    //private ArrayList<InventoryHiveViewHolder> view_holders;
    private InventoryViewHolder selected_item;

    //enum Type {Common, Selected}

    public InventoryAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        //this.view_holders = new ArrayList<>();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.inventory_list_item, parent, false);
        InventoryHiveViewHolder vh = new InventoryHiveViewHolder(view);
        //this.view_holders.add(vh);
        return vh;
    }

    /*public void setSelectedByRowId(long id) {
        for (InventoryHiveViewHolder vh : view_holders) {
            if (vh.row_id == id) {
                vh.setSelection();
                Log.d("PASEKA", "setSelectedByRowId FIND");
            }
            else {
                Log.d("PASEKA", "setSelectedByRowId vh id: " + vh.row_id);
            }
        }
    }*/

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor new_cursor) {
        if (cursor != null) { cursor.close(); }
        cursor = new_cursor;
        if (new_cursor != null) { notifyDataSetChanged(); }
    }

    /*@Override
    public int getItemViewType(int position) {
        if (cursor.moveToPosition(position) &&
                cursor.getInt(cursor.getColumnIndexOrThrow("_id")) == context.selected_item_id) {
            Log.d("PASEKA", "getItemViewType 1");
            return 1;
        }
        else {
            Log.d("PASEKA", "getItemViewType 0");
            return 0;
        }
    }*/

    public void clearSelection() {
        if (selected_item != null) {
            selected_item.clearSelection();
        }
    }

    abstract class InventoryViewHolder extends RecyclerView.ViewHolder {
        protected View layout;
        protected View details;

        protected long row_id;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract public void bind(int position);

        public void clearSelection() {
            if (selected_item != null) {
                selected_item.layout.setBackgroundColor(Colors.getContainer());

                selected_item.details.setVisibility(View.GONE);
                ((InventoryActivity)InventoryAdapter.this.context).setMenuBtnVisible(false);
                ((InventoryActivity)InventoryAdapter.this.context).setInventoryId(0);

                selected_item = null;
            }
        }

        public void setSelection() {
            if (selected_item != this) {
                this.layout.setBackgroundColor(Colors.getAccent());
                clearSelection();
                selected_item = this;

                details.setVisibility(View.VISIBLE);
                ((InventoryActivity)InventoryAdapter.this.context).setMenuBtnVisible(true);
                ((InventoryActivity)InventoryAdapter.this.context).setInventoryId(row_id);
            }
            else {
                clearSelection();
            }
        }
    }

    public class InventoryHiveViewHolder extends InventoryViewHolder {
        private final TextView number_text_view;
        private final TextView type_text_view;
        private final TextView address_text_view;

        private final TextView description_text_view;
        private final TextView made_text_view;

        public InventoryHiveViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView;
            number_text_view = itemView.findViewById(R.id.hive_number);
            type_text_view = itemView.findViewById(R.id.hive_type);
            address_text_view = itemView.findViewById(R.id.hive_address);

            details = itemView.findViewById(R.id.details);
            description_text_view = itemView.findViewById(R.id.hive_description);
            made_text_view = itemView.findViewById(R.id.hive_made);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.d("PASEKA", String.valueOf(getAdapterPosition()));

                    if (!cursor.moveToPosition(getAdapterPosition())) { return; }
                    //context.selected_item_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    setSelection();
                }
            });
        }

        public void bind(int position) {
            if (!cursor.moveToPosition(position)) { return; }

            row_id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

            int category_id = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
            if (category_id == 2 || category_id == 3) {
                number_text_view.setVisibility(View.GONE);
            }
            else {
                number_text_view.setVisibility(View.VISIBLE);
                int hive_number = cursor.getInt(cursor.getColumnIndexOrThrow("number"));
                number_text_view.setText(String.valueOf(hive_number));
            }

            String hive_type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String hive_size = cursor.getString(cursor.getColumnIndexOrThrow("size"));
            String hive_address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

            String hive_description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String hive_made = cursor.getString(cursor.getColumnIndexOrThrow("made"));


            type_text_view.setText(hive_type + " (" + hive_size + ")");
            address_text_view.setText(hive_address);

            description_text_view.setText(hive_description);
            made_text_view.setText(hive_made);
        }
    }


}