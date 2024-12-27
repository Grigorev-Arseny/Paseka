package com.hfad.paseka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class SchedulerActivity extends AppCompatActivity {

    protected SQLiteDatabase db;

    private TextView view_task;
    private TextView view_date;
    private Spinner view_status;
    private RecyclerView view_tasks;
    private TasksAdapter tasks_adapter;
    private ImageButton btn_add;
    private MenuItem btn_menu_execute;
    private MenuItem btn_menu_reject;
    private MenuItem btn_menu_edit;
    private MenuItem btn_menu_stop;

    private String task;
    private String date;
    private String status;

    private LocalDate selected_date;
    private boolean day_is_passed;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shceduler_menu, menu);

        btn_menu_execute = menu.getItem(0);
        btn_menu_reject = menu.getItem(1);
        btn_menu_edit = menu.getItem(2);
        btn_menu_stop = menu.getItem(3);

        btn_menu_execute.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_reject.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_edit.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_stop.getIcon().setTint(Colors.getBackgroundFloating());

        setMenuBtnVisible(false, false, false, false);

        return super.onCreateOptionsMenu(menu);
    }

    public void setMenuBtnVisible(Boolean execute, Boolean reject, Boolean edit, Boolean stop) {
        if (execute != null) { btn_menu_execute.setVisible(execute); }
        if (reject != null) { btn_menu_reject.setVisible(reject); }
        if (edit != null) { btn_menu_edit.setVisible(edit); }
        if (stop != null) { btn_menu_stop.setVisible(stop); }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.execute:
                tasks_adapter.executeTask();
                return true;
            case R.id.reject:
                tasks_adapter.rejectTask();
                return true;
            case R.id.stop:
                tasks_adapter.stopTask();
                return true;
            /*case R.id.delete:
            deleteInventory();
            return true;
            case R.id.edit:
                Intent intent = new Intent(this, InventoryModifyActivity.class);
                intent.putExtra(EXTRA_ACTION, InventoryActivity.Action.Edit);
                intent.putExtra(EXTRA_ROW_ID, inventory_id);
                startActivity(intent);
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        Intent intent = getIntent();
        selected_date = (LocalDate) intent.getSerializableExtra("selected_date");
        day_is_passed = selected_date.isBefore(LocalDate.now());

        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(this);
            db = db_helper.getReadableDatabase();
        }
        catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        view_task = findViewById(R.id.filter_task);
        view_date = findViewById(R.id.filter_date);
        view_status = findViewById(R.id.filter_status);
        view_tasks = findViewById(R.id.tasks);
        btn_add = findViewById(R.id.add);

        view_date.setText(selected_date.toString());
        view_tasks.setLayoutManager(new LinearLayoutManager(this));
        tasks_adapter = new TasksAdapter(this, getTasks());
        view_tasks.setAdapter(tasks_adapter);

    }

    private Cursor getTasks() {
        String date = selected_date.toString();
        return db.rawQuery(
            "SELECT T._id, T.task, T.frequency, T.start, T.timing, T.ask_status, T.ask_comment, T.ask_total, A.action_type, " +
                    "C.category || ' (' || CASE WHEN I.number IS NULL THEN I.inventory_number ELSE I.number END || ')' AS inventory, " +
                    "CASE WHEN A.stamp IS NOT NULL THEN 1 ELSE 0 END AS completed, " +
                    "CASE WHEN S.action_type = 'stop' THEN 1 ELSE 0 END AS stopped " +
                "FROM Tasks AS T " +
                "LEFT JOIN Inventory AS I " +
                "ON I._id = T.inventory_id " +
                "LEFT JOIN InventoryCategories AS C " +
                "ON C._id = I.category_id " +
                "LEFT JOIN Actions AS A " +
                "ON T._id = A.task_id " +
                    "AND A.action_type IN ('execute', 'reject') " +
                    "AND date(A.complete_date) = date('" + date + "') " +
                "LEFT JOIN Actions AS S " +
                "ON S._id = (" +
                    "SELECT _id FROM Actions " +
                    "WHERE task_id = T._id " +
                    "ORDER BY stamp DESC " +
                    "LIMIT 1) " +
                "WHERE A.stamp IS NOT NULL OR (stopped = 0 AND (" +
                    "T.frequency = 'once' AND date(T.start) = date('" + date + "') OR " +
                    "T.frequency = 'daily' AND date(T.start) <= date('" + date + "') AND (strftime(\"%J\", '" + date + "') - strftime(\"%J\", T.start)) % T.timing = 0 OR " +
                    "T.frequency = 'weekly' AND date(T.start) <= date('" + date + "') AND T.timing LIKE '%' || strftime(\"%w\", '" + date + "') || '%' OR " +
                    "T.frequency = 'monthly' AND date(T.start) <= date('" + date + "') AND strftime(\"%d\", '" + date + "') = T.timing OR " +
                    "T.frequency = 'yearly' AND date(T.start) <= date('" + date + "') AND strftime(\"%m\", '" + date + "') = substr(T.timing, 1, 2) AND strftime(\"%d\", '" + date + "') = substr(T.timing, 4, 2)))", null);
    }

    public boolean dayIsPassed() { return day_is_passed; }

    public LocalDate getSelectedDate() { return  selected_date; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}


class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {
    private final SchedulerActivity context;
    ArrayList<TaskRow> tasks = new ArrayList<>();
    private Integer selected_position;
    private TasksAdapter.TasksViewHolder selected_holder;

    public TasksAdapter(SchedulerActivity context, Cursor cursor) {
        this.context = context;

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            tasks.add(new TaskRow(
                    cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("task")),
                    cursor.getString(cursor.getColumnIndexOrThrow("frequency")),
                    LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("start")).substring(0, 10)),
                    cursor.getString(cursor.getColumnIndexOrThrow("timing")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("ask_status")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("ask_comment")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("ask_total")),
                    cursor.getString(cursor.getColumnIndexOrThrow("action_type")),
                    cursor.getString(cursor.getColumnIndexOrThrow("inventory")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("completed")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("stopped"))
            ));
            cursor.moveToNext();
        }
        cursor.close();
    }

    @NonNull
    @Override
    public TasksAdapter.TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.task_list_item, parent, false);
        TasksAdapter.TasksViewHolder vh = new TasksAdapter.TasksViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TasksAdapter.TasksViewHolder holder, int position) {
        holder.bind(position);
        //Log.d("PASEKA", "ON BIND ViewHolder: " + position + holder.row_id + holder.completed + holder.stopped);
    }

    @Override
    public int getItemCount() { return tasks.size(); }

    public void executeTask() {
        TaskRow selected_task = tasks.get(selected_position);
        boolean ask_status = selected_task.askStatus();
        boolean ask_comment = selected_task.askComment();
        boolean ask_total = selected_task.askTotal();
        if (!ask_status && !ask_comment && !ask_total) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.task_execute_title)
                    .setMessage(R.string.task_execute_confirm)
                    .setPositiveButton(R.string.task_button_execute, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ContentValues values = new ContentValues();
                            values.put("task_id", selected_task.getId());
                            values.put("action_type", "execute");
                            values.put("complete_date", context.getSelectedDate().toString());
                            context.db.insert("Actions", null, values);

                            selected_task.setActionType("execute");
                            selected_task.setCompleted(true);
                            notifyItemChanged(selected_position);
                            TasksAdapter.this.context.setMenuBtnVisible(false, false, null, null);

                            Toast.makeText(context, R.string.task_execute_result, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            Intent intent = new Intent(this.context, TaskExecuteActivity.class);
            intent.putExtra("ask_status", ask_status);
            intent.putExtra("ask_comment", ask_comment);
            intent.putExtra("ask_total", ask_total);
            this.context.startActivity(intent);
        }
    }

    public void rejectTask() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.task_reject_title)
                .setMessage(R.string.task_reject_confirm)
                .setPositiveButton(R.string.task_button_reject, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("task_id", tasks.get(selected_position).getId());
                        values.put("action_type", "reject");
                        values.put("complete_date", context.getSelectedDate().toString());
                        context.db.insert("Actions", null, values);

                        tasks.get(selected_position).setActionType("reject");
                        tasks.get(selected_position).setCompleted(true);
                        notifyItemChanged(selected_position);
                        TasksAdapter.this.context.setMenuBtnVisible(false, false, null, null);

                        Toast.makeText(context, R.string.task_reject_result, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void stopTask() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.task_stop_title)
                .setMessage(R.string.task_stop_confirm)
                .setPositiveButton(R.string.task_button_stop, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("task_id", tasks.get(selected_position).getId());
                        values.put("action_type", "stop");
                        context.db.insert("Actions", null, values);

                        if (tasks.get(selected_position).isCompleted()) {
                            tasks.get(selected_position).setStopped(true);
                            TasksAdapter.this.context.setMenuBtnVisible(null, null, null, false);
                        }
                        else {
                            tasks.remove((int)selected_position);
                            notifyItemRemoved(selected_position);
                            selected_position = null;
                            selected_holder = null;
                            TasksAdapter.this.context.setMenuBtnVisible(false, false, false, false);
                        }

                        Toast.makeText(context, R.string.task_stop_result, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public class TasksViewHolder extends RecyclerView.ViewHolder {
        protected View layout;
        private final ImageView icon_image_view;
        private final TextView task_text_view;
        private final TextView inventory_text_view;

        public TasksViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView;
            icon_image_view = itemView.findViewById(R.id.icon);
            task_text_view = itemView.findViewById(R.id.task);
            inventory_text_view = itemView.findViewById(R.id.inventory);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if (!cursor.moveToPosition(getAdapterPosition())) { return; }
                    setSelection();
                }
            });
        }

        public void bind(int position) {
            if ("execute".equals(tasks.get(position).getActionType())) {
                icon_image_view.setImageResource(R.drawable.check);
                icon_image_view.setColorFilter(Colors.getCompleted());
            }
            else if ("reject".equals(tasks.get(position).getActionType())) {
                icon_image_view.setImageResource(R.drawable.cancel);
                icon_image_view.setColorFilter(Colors.getCompleted());
            }
            else {
                icon_image_view.setImageResource(R.drawable.schedule);
                if (TasksAdapter.this.context.dayIsPassed()) {
                    icon_image_view.setColorFilter(Colors.getExpired());
                }
                else {
                    icon_image_view.setColorFilter(Colors.getPresent());
                }
            }

            if (selected_position != null) {
                if (selected_position == position) {
                    this.layout.setBackgroundColor(Colors.getAccent());
                    selected_holder = this;
                }
                else {
                    this.layout.setBackgroundColor(Colors.getContainer());

                    if (selected_holder == this) { selected_holder = null; }
                }
            }

            task_text_view.setText(tasks.get(position).getTask());
            inventory_text_view.setText(tasks.get(position).getInventory());
        }

        public void clearSelection() {
            if (selected_position != null) {
                selected_position = null;
                if (selected_holder != null) {
                    selected_holder.layout.setBackgroundColor(Colors.getContainer());
                    selected_holder = null;
                }

                TasksAdapter.this.context.setMenuBtnVisible(false, false, false, false);
            }
        }

        public void setSelection() {
            if (selected_position == null || selected_position != getAdapterPosition()) {
                this.layout.setBackgroundColor(Colors.getAccent());
                clearSelection();
                selected_position = getAdapterPosition();
                selected_holder = this;

                TasksAdapter.this.context.setMenuBtnVisible(!tasks.get(selected_position).isCompleted(), !tasks.get(selected_position).isCompleted(), true, !tasks.get(selected_position).isStopped());
            }
            else {
                clearSelection();
            }
        }
    }
}


class TaskRow {
    private long id;
    private String task;
    private String frequency;
    private LocalDate start;
    private String timing;
    private int ask_status;
    private int ask_comment;
    private int ask_total;
    private String action_type;
    private String inventory;
    private int completed;
    private int stopped;

    public TaskRow(long id, String task, String frequency, LocalDate start, String timing,
                   int ask_status, int ask_comment, int ask_total,
               String action_type, String inventory, int completed, int stopped) {
        this.id = id;
        this.task = task;
        this.frequency = frequency;
        this.start = start;
        this.timing = timing;
        this.ask_status = ask_status;
        this.ask_comment = ask_comment;
        this.ask_total = ask_total;
        this.action_type = action_type;
        this.inventory = inventory;
        this.completed = completed;
        this.stopped = stopped;
    }

    public long getId() { return id; }
    public String getTask() { return task; }
    public boolean askStatus() { return ask_status == 1 ? true : false; };
    public boolean askComment() { return ask_comment == 1 ? true : false; };
    public boolean askTotal() { return ask_total == 1 ? true : false; };
    public String getActionType() { return action_type; }
    public String getInventory() { return inventory; }
    public boolean isCompleted() { return completed == 1; }
    public boolean isStopped() { return stopped == 1; }

    public void setActionType(String type) { action_type = type; }
    public void setCompleted(boolean completed) { this.completed = completed ? 1 : 0; }
    public void setStopped(boolean stopped) { this.stopped = stopped ? 1 : 0; }
}










/*
public class SchedulerActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private String task;
    private String date;
    private String status;

    private TextView filter_task;
    private TextView filter_date;
    private Spinner filter_status;
    RecyclerView tasks;
    TasksAdapter tasks_adapter;
    private ImageButton btn_add;
    private MenuItem btn_menu_execute;
    private MenuItem btn_menu_reject;
    private MenuItem btn_menu_edit;
    private MenuItem btn_menu_stop;

    private LocalDate selected_date;
    private boolean day_is_passed;
    private long selected_task_id;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shceduler_menu, menu);

        btn_menu_execute = menu.getItem(0);
        btn_menu_reject = menu.getItem(1);
        btn_menu_edit = menu.getItem(2);
        btn_menu_stop = menu.getItem(3);

        btn_menu_execute.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_reject.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_edit.getIcon().setTint(Colors.getBackgroundFloating());
        btn_menu_stop.getIcon().setTint(Colors.getBackgroundFloating());

        btn_menu_execute.setVisible(false);
        btn_menu_reject.setVisible(false);
        btn_menu_edit.setVisible(false);
        btn_menu_stop.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    public void setMenuBtnVisible(boolean execute, boolean reject, boolean edit, boolean stop) {
        btn_menu_execute.setVisible(execute);
        btn_menu_reject.setVisible(reject);
        btn_menu_edit.setVisible(edit);
        btn_menu_stop.setVisible(stop);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.stop:
                stopTask();
                return true;
            case R.id.reject:
                rejectTask();
                return true;
            ////case R.id.delete:
                deleteInventory();
                return true;
            case R.id.edit:
                Intent intent = new Intent(this, InventoryModifyActivity.class);
                intent.putExtra(EXTRA_ACTION, InventoryActivity.Action.Edit);
                intent.putExtra(EXTRA_ROW_ID, inventory_id);
                startActivity(intent);
                return true;////
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        Intent intent = getIntent();
        selected_date = (LocalDate) intent.getSerializableExtra("selected_date");
        day_is_passed = selected_date.isBefore(LocalDate.now());
        selected_task_id = 0;

        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(this);
            db = db_helper.getReadableDatabase();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        filter_task = findViewById(R.id.filter_task);
        filter_date = findViewById(R.id.filter_date);
        filter_status = findViewById(R.id.filter_status);
        tasks = findViewById(R.id.tasks);
        btn_add = findViewById(R.id.add);

        tasks.setLayoutManager(new LinearLayoutManager(this));
        tasks_adapter = new TasksAdapter(this, getTasks());
        tasks.setAdapter(tasks_adapter);

        ((EditText) findViewById(R.id.filter_date)).setText(selected_date.toString());
    }

    private Cursor getTasks() {
        String date = selected_date.toString();
        return db.rawQuery(
                "SELECT T._id, T.task, T.frequency, T.start, T.timing, T.ask_status, T.ask_comment, T.ask_total, A.action_type, " +
                        "C.category || ' (' || CASE WHEN I.number IS NULL THEN I.inventory_number ELSE I.number END || ')' AS inventory, " +
                        "CASE WHEN A.stamp IS NOT NULL THEN 1 ELSE 0 END AS completed, " +
                        "CASE WHEN S.action_type = 'stop' THEN 1 ELSE 0 END AS stopped " +
                    "FROM Tasks AS T " +
                    "LEFT JOIN Inventory AS I " +
                    "ON I._id = T.inventory_id " +
                    "LEFT JOIN InventoryCategories AS C " +
                    "ON C._id = I.category_id " +
                    "LEFT JOIN Actions AS A " +
                    "ON T._id = A.task_id " +
                        "AND A.action_type IN ('execute', 'reject') " +
                        "AND date(A.complete_date) = date('" + date + "') " +
                    "LEFT JOIN Actions AS S " +
                    "ON S._id = (" +
                        "SELECT _id FROM Actions " +
                        "WHERE task_id = T._id " +
                        "ORDER BY stamp DESC " +
                        "LIMIT 1) " +
                    "WHERE A.stamp IS NOT NULL OR (stopped = 0 AND (" +
                        "T.frequency = 'once' AND date(T.start) = date('" + date + "') OR " +
                        "T.frequency = 'daily' AND date(T.start) <= date('" + date + "') AND (strftime(\"%J\", '" + date + "') - strftime(\"%J\", T.start)) % T.timing = 0 OR " +
                        "T.frequency = 'weekly' AND date(T.start) <= date('" + date + "') AND T.timing LIKE '%' || strftime(\"%w\", '" + date + "') || '%' OR " +
                        "T.frequency = 'monthly' AND date(T.start) <= date('" + date + "') AND strftime(\"%d\", '" + date + "') = T.timing OR " +
                        "T.frequency = 'yearly' AND date(T.start) <= date('" + date + "') AND strftime(\"%m\", '" + date + "') = substr(T.timing, 1, 2) AND strftime(\"%d\", '" + date + "') = substr(T.timing, 4, 2)))", null);
    }

    private void stopTask() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.task_stop_title)
                .setMessage(R.string.task_stop_confirm)
                .setPositiveButton(R.string.task_button_stop, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("task_id", selected_task_id);
                        values.put("action_type", "stop");
                        db.insert("Actions", null, values);

                        Toast.makeText(getApplicationContext(), R.string.task_stop_result, Toast.LENGTH_LONG).show();
                        ////tasks_adapter.swapCursor(getTasks());
                        if (tasks_adapter.selected_item.completed) {
                            //setMenuBtnVisible(!tasks_adapter.selected_item.completed, !tasks_adapter.selected_item.completed, true, !tasks_adapter.selected_item.stopped);
                        }
                        else { tasks_adapter.selected_item.clearSelection(); }////
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void rejectTask() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.task_reject_title)
                .setMessage(R.string.task_reject_confirm)
                .setPositiveButton(R.string.task_button_reject, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("task_id", selected_task_id);
                        values.put("action_type", "reject");
                        values.put("complete_date", selected_date.toString());
                        db.insert("Actions", null, values);

                        Toast.makeText(getApplicationContext(), R.string.task_reject_result, Toast.LENGTH_LONG).show();
                        ////tasks_adapter.selected_item = null; ????????????
                        tasks_adapter.swapCursor(getTasks());////
                        //Log.d("PASEKA", "REJECT TASK: " + tasks_adapter.selected_item.row_id + tasks_adapter.selected_item.completed + tasks_adapter.selected_item.stopped);
                        Log.d("PASEKA", "REJECT TASK: " + tasks_adapter.selected_item.getAdapterPosition());
                        //tasks_adapter.notifyItemChanged(tasks_adapter.selected_item.getAdapterPosition(), new Integer(10));

                        tasks_adapter.cursor. notifyItemRemoved(tasks_adapter.selected_item.getAdapterPosition());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {
        private final Context context;
        private Cursor cursor;
        private TasksAdapter.TasksViewHolder selected_item;

        public TasksAdapter(Context context, Cursor cursor) {
            this.context = context;
            this.cursor = cursor;
            //this.view_holders = new ArrayList<>();
        }

        @NonNull
        @Override
        public TasksAdapter.TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.task_list_item, parent, false);
            TasksAdapter.TasksViewHolder vh = new TasksAdapter.TasksViewHolder(view);
            //this.view_holders.add(vh);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull TasksAdapter.TasksViewHolder holder, int position) {
            holder.bind(position);
            Log.d("PASEKA", "ON BIND ViewHolder: " + position + holder.row_id + holder.completed + holder.stopped);
        }

        ////@Override
        public void onBindViewHolder(@NonNull TasksAdapter.TasksViewHolder holder, int position, List<Object> payload) {
            holder.bind(position);
            Log.d("PASEKA", "ON BIND VH Payload: " + position + holder.row_id + holder.completed + holder.stopped + "--" + payload.size());
        }////

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        public void swapCursor(Cursor new_cursor) {
            if (cursor != null) { cursor.close(); }
            cursor = new_cursor;
            if (new_cursor != null) { notifyDataSetChanged(); }
        }


        public class TasksViewHolder extends RecyclerView.ViewHolder {
            protected View layout;
            private final ImageView icon_image_view;
            private final TextView task_text_view;
            private final TextView inventory_text_view;

            private long row_id;
            private boolean stopped;
            private boolean completed;

            public TasksViewHolder(@NonNull View itemView) {
                super(itemView);

                layout = itemView;
                icon_image_view = itemView.findViewById(R.id.icon);
                task_text_view = itemView.findViewById(R.id.task);
                inventory_text_view = itemView.findViewById(R.id.inventory);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!cursor.moveToPosition(getAdapterPosition())) { return; }
                        setSelection();
                    }
                });
            }

            public void bind(int position) {
                if (!cursor.moveToPosition(position)) {
                    return;
                }

                row_id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                stopped = cursor.getInt(cursor.getColumnIndexOrThrow("stopped")) == 0 ? false : true;
                completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 0 ? false : true;

                if ("execute".equals(cursor.getString(cursor.getColumnIndexOrThrow("action_type")))) {
                    icon_image_view.setImageResource(R.drawable.check);
                    icon_image_view.setColorFilter(Colors.getCompleted());
                }
                else if ("reject".equals(cursor.getString(cursor.getColumnIndexOrThrow("action_type")))) {
                    icon_image_view.setImageResource(R.drawable.cancel);
                    icon_image_view.setColorFilter(Colors.getCompleted());
                }
                else {
                    icon_image_view.setImageResource(R.drawable.schedule);
                    if (day_is_passed) {
                        icon_image_view.setColorFilter(Colors.getExpired());
                    }
                    else {
                        icon_image_view.setColorFilter(Colors.getPresent());
                    }
                }

                task_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow("task")));
                inventory_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow("inventory")));

                ////if (row_id == selected_task_id) {
                    setSelection();
                }???????????? пропадает меню////
            }

            public void clearSelection() {
                if (selected_item != null) {
                    selected_item.layout.setBackgroundColor(Colors.getContainer());

                    ((SchedulerActivity)TasksAdapter.this.context).setMenuBtnVisible(false, false, false, false);
                    selected_task_id = 0;

                    selected_item = null;
                }
            }

            public void setSelection() {
                if (selected_item != this) {
                    this.layout.setBackgroundColor(Colors.getAccent());
                    clearSelection();
                    selected_item = this;

                    ((SchedulerActivity)TasksAdapter.this.context).setMenuBtnVisible(!completed, !completed, true, !stopped);
                    selected_task_id = row_id;

                }
                else {
                    clearSelection();
                }
            }
        }
    }
}
*/