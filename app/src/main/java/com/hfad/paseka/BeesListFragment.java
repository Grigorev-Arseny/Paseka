package com.hfad.paseka;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BeesListFragment extends Fragment {
    private SQLiteDatabase db;

    private long location_id;
    private long bees_id;

    private Spinner locations;
    RecyclerView bees;
    BeesAdapter bees_adapter;


    public BeesListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bees_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(getActivity().getApplicationContext());
            db = db_helper.getReadableDatabase();
        }
        catch(SQLiteException e) {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        locations = getActivity().findViewById(R.id.locations);
        bees = getActivity().findViewById(R.id.bees);

        bees.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        bees_adapter = new BeesAdapter(getActivity().getApplicationContext(), getBees());
        bees.setAdapter(bees_adapter);

        // Создаём курсор с данными таблицы Locations
        Cursor locations_cursor =
                db.rawQuery("SELECT _id, address, 2 AS sort, UPPER(address) FROM Locations UNION ALL " +
                        "SELECT 0 AS _id, 'Выбрать все' AS address, 1 AS sort, '' " +
                        "ORDER BY sort, UPPER(address)", null);

        // Создаем адаптер и слушателя выбора местоположения
        //Log.d("PASEKA", "create location adapter " + getActivity().getApplicationContext().toString());
        CursorAdapter locations_adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
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
                if (location_id != -1) {
                    bees_adapter.swapCursor(getBees());
                    bees_adapter.clearSelection();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private Cursor getBees() {
        return db.rawQuery(
                "SELECT B._id, I.number, L.address, R.breed, DATE(B.queen_birth) AS queen_birth, T.type, I.size, B.description, " +
                        "SUM(CASE WHEN H.category_id = 2 THEN 1 ELSE 0 END) + 1 AS qty_hives, " +
                        "SUM(CASE WHEN H.category_id = 3 THEN 1 ELSE 0 END) AS qty_shops " +
                    "FROM Bees AS B " +
                    "LEFT JOIN Inventory AS I " +
                    "ON I._id = B.hive_id " +
                    "LEFT JOIN Locations AS L " +
                    "ON L._id = I.location_id " +
                    "LEFT JOIN Breeds AS R " +
                    "ON R._id = B.breed_id " +
                    "LEFT JOIN HiveTypes AS T " +
                    "ON T._id = I.type_id " +
                    "LEFT JOIN Inventory AS H " +
                    "ON H.hive_id = I._id " +
                    "WHERE B.deleted_date IS NULL " +
                    (location_id == 0 ? "" : "AND I.location_id = " + location_id) +
                    " GROUP BY B._id, I.number, L.address, R.breed, DATE(B.queen_birth), T.type, I.size, B.description" +
                    " ORDER BY I.location_id, I.number", null);
    }
}


class BeesAdapter extends RecyclerView.Adapter<BeesAdapter.BeesViewHolder> {
    private final Context context;
    private Cursor cursor;
    private BeesViewHolder selected_item;

    //enum Type {Common, Selected}

    public BeesAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public BeesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.bees_list_item, parent, false);
        return new BeesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeesViewHolder holder, int position) {
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

    class BeesViewHolder extends RecyclerView.ViewHolder {
        protected View layout;
        protected View details;

        protected long row_id;

        private final TextView number_text_view;
        private final TextView qty_hives_text_view;
        private final TextView qty_shops_text_view;
        private final TextView address_text_view;

        private final TextView breed_text_view;
        private final TextView type_text_view;
        private final TextView description_text_view;

        public BeesViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView;
            number_text_view = itemView.findViewById(R.id.hive_number);
            qty_hives_text_view = itemView.findViewById(R.id.qty_hives);
            qty_shops_text_view = itemView.findViewById(R.id.qty_shops);
            address_text_view = itemView.findViewById(R.id.hive_address);

            details = itemView.findViewById(R.id.details);
            breed_text_view = itemView.findViewById(R.id.breed);
            type_text_view = itemView.findViewById(R.id.hive_type);
            description_text_view = itemView.findViewById(R.id.bees_description);

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

            String hive_number = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("number")));
            String qty_hives = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("qty_hives")));
            String qty_shops = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("qty_shops")));
            String hive_address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

            String breed = cursor.getString(cursor.getColumnIndexOrThrow("breed"));
            String queen_birth = cursor.getString(cursor.getColumnIndexOrThrow("queen_birth"));
            String hive_type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String hive_size = cursor.getString(cursor.getColumnIndexOrThrow("size"));
            String bees_description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

            number_text_view.setText(hive_number);
            qty_hives_text_view.setText(qty_hives);
            qty_shops_text_view.setText(qty_shops);
            address_text_view.setText(hive_address);

            breed_text_view.setText(breed + " (" + queen_birth + ")");
            type_text_view.setText(hive_type + " (" + hive_size + ")");
            description_text_view.setText(bees_description);
        }

        public void clearSelection() {
            if (selected_item != null) {
                selected_item.layout.setBackgroundColor(Colors.getContainer());

                selected_item.details.setVisibility(View.GONE);
                //((MainActivity)BeesAdapter.this.context).setMenuBtnVisible(false);
                //((MainActivity)BeesAdapter.this.context).setInventoryId(0);

                selected_item = null;
            }
        }

        public void setSelection() {
            if (selected_item != this) {
                this.layout.setBackgroundColor(Colors.getAccent());
                clearSelection();
                selected_item = this;

                details.setVisibility(View.VISIBLE);
                //((MainActivity)BeesAdapter.this.context).setMenuBtnVisible(true);
                //((MainActivity)BeesAdapter.this.context).setInventoryId(row_id);
            }
            else {
                clearSelection();
            }
        }
    }




}