package com.hfad.paseka;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class SchedulerListFragment extends Fragment implements SchedulerAdapter.OnItemListener {
    private SQLiteDatabase db;

    enum TasksState {Expired, Present, Completed, None}
        // expired - присутствуют просроченные задачи
        // present - присутствуют невыполненные задачи, но просроченных нет
        // completed - все задачи выполнены или отменены
        // none - задачи отсутствуют

    private TextView dateCaption;
    private ImageButton btn_previous;
    private ImageButton btn_next;
    private RecyclerView scheduler;
    private LocalDate selectedDate;
    private SchedulerAdapter.SchedulerViewHolder selected_cell;

    public SchedulerListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedDate = (LocalDate) savedInstanceState.getSerializable("selectedDate");
        }
        else {
            selectedDate = LocalDate.now();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scheduler_list, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("selectedDate", selectedDate);
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

        dateCaption = getActivity().findViewById(R.id.date_caption);
        btn_previous = getActivity().findViewById(R.id.btn_previous);
        btn_next = getActivity().findViewById(R.id.btn_next);
        scheduler = getActivity().findViewById(R.id.scheduler);

        setDateCaption();

        btn_previous.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("PASEKA", "onPreviousClick");
                selectedDate = selectedDate.minusMonths(1);
                setDateCaption();
            }
        });

        btn_next.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("PASEKA", "onNextClick");
                selectedDate = selectedDate.plusMonths(1);
                setDateCaption();
            }
        });
    }

    private void setDateCaption() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL yyyy");
        dateCaption.setText(selectedDate.format(formatter));
        ArrayList<DayTasks> monthDays = getMonthDays(selectedDate);

        // Создаём курсор с данными таблицы Tasks
        Cursor tasks_cursor = db.rawQuery(
        "SELECT T._id, T.frequency, T.start, T.timing FROM Tasks AS T " +
            "LEFT JOIN Actions AS A " +
            "ON A._id = (" +
                "SELECT _id FROM Actions " +
                "WHERE task_id = T._id " +
                "ORDER BY stamp DESC " +
                "LIMIT 1) " +
            "WHERE COALESCE(A.action_type, '') <> 'stop' " +
                "AND (" +
                    "T.frequency = 'once' AND strftime('%m', T.start) = '" + selectedDate.format(DateTimeFormatter.ofPattern("MM")) + "' OR " +
                    "T.frequency = 'daily' AND T.start < date('" + selectedDate.toString() + "', 'start of month','+1 month') OR " +
                    "T.frequency = 'weekly' AND T.start < date('" + selectedDate.toString() + "', 'start of month','+1 month') OR " +
                    "T.frequency = 'monthly' AND T.start < date('" + selectedDate.toString() + "', 'start of month','+1 month') OR " +
                    "T.frequency = 'yearly' AND T.start < date('" + selectedDate.toString() + "', 'start of month','+1 month') " +
                        "AND SUBSTR(T.timing, 1, 2) = '" + selectedDate.format(DateTimeFormatter.ofPattern("MM")) + "')", null);
        tasks_cursor.moveToFirst();
        while (tasks_cursor.isAfterLast() == false) {
            if (tasks_cursor.getString(1).equals("once")) {
                monthDays.stream().filter(t -> tasks_cursor.getString(2).substring(0, 10).equals(t.getDate() == null ? "" : t.getDate().toString()))
                        .findFirst().ifPresent(t -> t.addTask(new Task(tasks_cursor.getInt(0))));
            }
            else if (tasks_cursor.getString(1).equals("daily")) {
                LocalDate start = LocalDate.parse(tasks_cursor.getString(2).substring(0, 10));
                int timing = Integer.parseInt(tasks_cursor.getString(3));
                //Log.d("PASEKA", "test until: " + LocalDate.parse("2024-12-10").until(LocalDate.parse("2024-12-05"), ChronoUnit.DAYS) );
                for (int i = 0; i < monthDays.size();) {
                    if (monthDays.get(i).getDate() != null) {
                        long delta = start.until(monthDays.get(i).getDate(), ChronoUnit.DAYS);
                        if (monthDays.get(i).getDate().getDayOfMonth() == 1 && delta != 0 && delta % timing != 0) {
                            if (delta < 0) {
                                i += start.getDayOfMonth() - 1;
                            }
                            else {
                                i += timing - delta % timing;
                            }
                            continue;
                        }
                        if (!start.isAfter(monthDays.get(i).getDate())) {
                            //Log.d("PASEKA", "1 task day: " + monthDays.get(i).getDay() + " - " + i);
                            monthDays.get(i).addTask(new Task(tasks_cursor.getInt(0)));
                            i += timing;
                        }
                    }
                    else {
                        i++;
                    }
                }
            }
            else if (tasks_cursor.getString(1).equals("weekly")) {
                monthDays.stream().filter(t -> t.getDate() != null && !LocalDate.parse(tasks_cursor.getString(2).substring(0, 10)).isAfter(t.getDate()) &&
                        tasks_cursor.getString(3).replace('0', '7').indexOf(String.valueOf(t.getDate().getDayOfWeek().getValue())) != -1).
                    forEach(t -> t.addTask(new Task(tasks_cursor.getInt(0))));
            }
            else if (tasks_cursor.getString(1).equals("monthly")) {
                monthDays.stream().filter(t -> t.getDate() != null && !LocalDate.parse(tasks_cursor.getString(2).substring(0, 10)).isAfter(t.getDate()) &&
                        tasks_cursor.getString(3).equals(t.getDate().format(DateTimeFormatter.ofPattern("dd")))).
                    forEach(t -> t.addTask(new Task(tasks_cursor.getInt(0))));
            }
            else if (tasks_cursor.getString(1).equals("yearly")) {
                monthDays.stream().filter(t -> t.getDate() != null && !LocalDate.parse(tasks_cursor.getString(2).substring(0, 10)).isAfter(t.getDate()) &&
                        tasks_cursor.getString(3).equals(t.getDate().format(DateTimeFormatter.ofPattern("MM-dd")))).
                    forEach(t -> t.addTask(new Task(tasks_cursor.getInt(0))));
            }
            tasks_cursor.moveToNext();
        }
        tasks_cursor.close();

        // Создаём курсор с данными таблицы Actions
        Cursor actions_cursor = db.rawQuery(
        "SELECT task_id, complete_date FROM Actions " +
                "WHERE strftime('%m', complete_date) = '" + selectedDate.format(DateTimeFormatter.ofPattern("MM")) + "' " +
                "AND action_type IN ('execute', 'reject')", null);
        actions_cursor.moveToFirst();
        while (actions_cursor.isAfterLast() == false) {
            monthDays.stream().filter(t -> actions_cursor.getString(1).substring(0, 10).equals(t.getDate() == null ? "" : t.getDate().toString()))
                    .findFirst().ifPresent(t -> t.CompleteTask(actions_cursor.getInt(0)));
            actions_cursor.moveToNext();
        }
        actions_cursor.close();

        SchedulerAdapter schedulerAdapter = new SchedulerAdapter(this.selectedDate.getYear(), this.selectedDate.getMonthValue(), monthDays, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 7);
        scheduler.setLayoutManager(layoutManager);
        scheduler.setAdapter(schedulerAdapter);

    }

    private ArrayList<DayTasks> getMonthDays(LocalDate date) {
        ArrayList<DayTasks> monthDays = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        Log.d("PASEKA", "dayOfWeek" + dayOfWeek);

        for (int i = 2; i <= 43; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                monthDays.add(new DayTasks(null));
            }
            else {
                monthDays.add(new DayTasks(LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(), i - dayOfWeek)));
            }
        }

        return monthDays;
    }

    @Override
    public void onItemClick(int position, LocalDate selected_date) {
        //Log.d("PASEKA", "Selected Date " + day);
        if (selected_cell != null) {
            selected_cell.unselect();
            //Log.d("PASEKA", "Unselect Date!");
        }
        selected_cell = (SchedulerAdapter.SchedulerViewHolder)this.scheduler.findViewHolderForAdapterPosition(position);
        selected_cell.select();

        Intent intent = new Intent(this.getContext(), SchedulerActivity.class);
        intent.putExtra("selected_date", selected_date);
        startActivity(intent);
    }
}


class SchedulerAdapter extends RecyclerView.Adapter<SchedulerAdapter.SchedulerViewHolder> {
    private final ArrayList<DayTasks> monthDays;
    private final OnItemListener onItemListener;
    private final int selected_year;
    private final int selected_month;

    public SchedulerAdapter(int selected_year, int selected_month, ArrayList<DayTasks> monthDays, OnItemListener onItemListener) {
        this.selected_year = selected_year;
        this.selected_month = selected_month;
        this.monthDays = monthDays;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public SchedulerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.scheduler_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);

        return new SchedulerViewHolder(view/*, onItemListener*/);
    }

    @Override
    public int getItemCount() {
        return monthDays.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SchedulerViewHolder holder, int position) {
        holder.bind(monthDays.get(position));
    }

    public interface OnItemListener {
        void onItemClick(int position, LocalDate selected_date);
    }

    class SchedulerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final CardView border_view;
        private final CardView background_view;
        private final TextView text_view;
        private final TextView task_view;
        //private final SchedulerAdapter.OnItemListener onItemListener;
        private final LocalDate current_date;

        public SchedulerViewHolder(@NonNull View itemView/*, OnItemListener onItemListener*/) {
            super(itemView);

            border_view = itemView.findViewById(R.id.border);
            background_view = itemView.findViewById(R.id.background);
            text_view = itemView.findViewById(R.id.text);
            task_view = itemView.findViewById(R.id.task);
            //this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
            this.current_date = LocalDate.now();
        }

        public void bind(DayTasks day_tasks) {
            if (day_tasks.getDay().length() == 0) {
                border_view.setCardBackgroundColor(Colors.getContainer());
                background_view.setCardBackgroundColor(Colors.getContainer());
                border_view.setCardElevation(0);
                task_view.setBackgroundColor(Colors.getBackgroundFloating());
            }
            else {
                if (this.current_date.isEqual(day_tasks.getDate())) {
                    border_view.setCardBackgroundColor(Colors.getPrimary());
                }
                else {
                    border_view.setCardBackgroundColor(Colors.getAccent());
                }
                background_view.setCardBackgroundColor(Colors.getBackgroundFloating());
                border_view.setCardElevation(4);

                if (day_tasks.getDayTasksState() == SchedulerListFragment.TasksState.Expired) {
                    task_view.setBackgroundColor(Colors.getExpired());
                }
                else if (day_tasks.getDayTasksState() == SchedulerListFragment.TasksState.Present) {
                    task_view.setBackgroundColor(Colors.getPresent());
                }
                else if (day_tasks.getDayTasksState() == SchedulerListFragment.TasksState.Completed) {
                    task_view.setBackgroundColor(Colors.getCompleted());
                }
                else {
                    task_view.setBackgroundColor(Colors.getBackgroundFloating());
                }
            }

            text_view.setText(day_tasks.getDay());
        }

        @Override
        public void onClick(View view) {
            String day = (String)text_view.getText();
            if (!day.equals("")) {
                SchedulerAdapter.this.onItemListener.onItemClick(getAdapterPosition(),
                    LocalDate.of(SchedulerAdapter.this.selected_year, SchedulerAdapter.this.selected_month, Integer.parseInt(day)));
            }
        }

        public void select () {
            background_view.setCardBackgroundColor(Colors.getContainer());
        }

        public void unselect () {
            background_view.setCardBackgroundColor(Colors.getBackgroundFloating());
        }
    }
}


class Task {
    private int id;
    private boolean completed;

    public Task(int id) {
        this.id = id;
        this.completed = false;
    }

    public Task(int id, boolean completed) {
        this.id = id;
        this.completed = completed;
    }

    public int getId() {
        return this.id;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void Complete() {
        this.completed = true;
    }

    public String toString() { return String.valueOf(this.id); }
}


class DayTasks {
    private LocalDate date;
    private ArrayList<Task> tasks;

    public DayTasks (LocalDate date) {
        this.date = date;
        this.tasks = new ArrayList<Task>();
    }

    public String getDay() {
        return date == null ? "" : String.valueOf(this.date.getDayOfMonth());
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void CompleteTask(int task_id) {
        Optional<Task> task = this.tasks.stream().filter(t->t.getId()==task_id).findFirst();
        if (task.isPresent()) {
            task.ifPresent(t -> t.Complete());
        }
        else {
            this.tasks.add(new Task(task_id, true));
        }
    }

    public SchedulerListFragment.TasksState getDayTasksState() {
        if (this.tasks.stream().filter(t->!t.isCompleted() && this.date.isBefore(LocalDate.now())).count() != 0) {
            return SchedulerListFragment.TasksState.Expired;
        }
        else if (this.tasks.stream().filter(t->!t.isCompleted()).count() != 0) {
            return SchedulerListFragment.TasksState.Present;
        }
        else if (this.tasks.stream().filter(t->t.isCompleted()).count() != 0) {
            return SchedulerListFragment.TasksState.Completed;
        }
        else {
            return SchedulerListFragment.TasksState.None;
        }
    }
}