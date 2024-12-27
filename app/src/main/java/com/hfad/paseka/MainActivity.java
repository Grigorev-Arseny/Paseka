package com.hfad.paseka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "category";
    enum Categories {Bees, Hives, Traps}

    public SQLiteDatabase db;

    private TabLayout main_tabs;
    private ViewPager main_pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Colors.init(this);

        main_tabs = findViewById(R.id.main_tabs);
        main_pager = findViewById(R.id.main_pager);
        main_tabs.setupWithViewPager(main_pager);

        MainVPAdapter mainVPAdapter = new MainVPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mainVPAdapter.addFragment(new SchedulerListFragment(), "Scheduler");
        mainVPAdapter.addFragment(new BeesListFragment(), "Bees");
        //mainVPAdapter.addFragment(new HivesListFragment(), "Hives");
        mainVPAdapter.addFragment(new TrapsListFragment(), "Traps");
        main_pager.setAdapter(mainVPAdapter);

        // Подключение к БД
        try {
            SQLiteOpenHelper db_helper = new DBHelper(this);
            db = db_helper.getWritableDatabase();
        }
        catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_inventory:
                startActivity(new Intent(this, InventoryActivity.class));
                return true;
            case R.id.menu_directories:
                startActivity(new Intent(this, DirectoriesActivity.class));
                return true;
            case R.id.menu_delete_db:
                deleteDB();
                return true;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteDB() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_db_title)
            .setMessage(R.string.delete_db_confirm)
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    getApplicationContext().deleteDatabase(DBHelper.DB_NAME);
                    Toast.makeText(getApplicationContext(), R.string.delete_db_result, Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }



    /*
    public void onBeesClick(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(EXTRA_CATEGORY, Categories.Bees);
        startActivity(intent);
    }

    public void onHivesClick(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(EXTRA_CATEGORY, Categories.Hives);
        startActivity(intent);
    }

    public void onTrapsClick(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(EXTRA_CATEGORY, Categories.Traps);
        startActivity(intent);
    }
     */
}