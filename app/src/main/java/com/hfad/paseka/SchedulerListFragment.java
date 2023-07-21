package com.hfad.paseka;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SchedulerListFragment extends Fragment implements SchedulerAdapter.OnItemListener {
    private TextView dateCaption;
    private ImageButton btn_previous;
    private ImageButton btn_next;
    private RecyclerView scheduler;
    private LocalDate selectedDate;

    public SchedulerListFragment() {
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
        return inflater.inflate(R.layout.fragment_scheduler_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        dateCaption = getActivity().findViewById(R.id.date_caption);
        btn_previous = getActivity().findViewById(R.id.btn_previous);
        btn_next = getActivity().findViewById(R.id.btn_next);
        scheduler = getActivity().findViewById(R.id.scheduler);

        selectedDate = LocalDate.now();
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        dateCaption.setText(selectedDate.format(formatter));
        ArrayList<String> monthDays = daysInMonthArray(selectedDate);

        SchedulerAdapter schedulerAdapter = new SchedulerAdapter(monthDays, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 7);
        scheduler.setLayoutManager(layoutManager);
        scheduler.setAdapter(schedulerAdapter);

    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        Log.d("PASEKA", "dayOfWeek" + dayOfWeek);

        for (int i = 2; i <= 43; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            }
            else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }

        return daysInMonthArray;
    }

    @Override
    public void onItemClick(int position, String day) {
        if (!day.equals("")) {
            Log.d("PASEKA", "Selected Date " + day);
        }
    }
}


class SchedulerAdapter extends RecyclerView.Adapter<SchedulerAdapter.SchedulerViewHolder> {
    private ArrayList<String> monthDays;
    private OnItemListener onItemListener;

    public SchedulerAdapter(ArrayList<String> monthDays, OnItemListener onItemListener) {
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

        return new SchedulerViewHolder(view, onItemListener);
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
        void onItemClick(int position, String day);
    }

    class SchedulerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView dayCell;
        private SchedulerAdapter.OnItemListener onItemListener;

        public SchedulerViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);

            dayCell = itemView.findViewById(R.id.day_cell);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        public void bind(String day) {
            dayCell.setText(day);
        }

        @Override
        public void onClick(View view) {
            onItemListener.onItemClick(getAdapterPosition(), (String)dayCell.getText());
        }
    }
}