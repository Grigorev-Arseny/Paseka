package com.hfad.paseka;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "paseka";
    public static final int DB_VERSION = 1;
    private final Context context;

    DBHelper (Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // СПРАВОЧНИКИ
        db.execSQL("CREATE TABLE Locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, address TEXT);");
        db.execSQL("CREATE TABLE HiveTypes (_id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT);");
        db.execSQL("CREATE TABLE Breeds (_id INTEGER PRIMARY KEY AUTOINCREMENT, breed TEXT);");

        // Добавляем информацию в таблицу HiveTypes
        String[] types = this.context.getResources().getStringArray(R.array.hive_types);
        for(String type : types) {
            ContentValues values = new ContentValues();
            values.put("type", type);
            db.insert("HiveTypes", null, values);
        }

        // Добавляем информацию в таблицу Breeds
        String[] breeds = this.context.getResources().getStringArray(R.array.breeds);
        for(String breed : breeds) {
            ContentValues values = new ContentValues();
            values.put("breed", breed);
            db.insert("Breeds", null, values);
        }

        // ОСНОВНЫЕ ТАБЛИЦЫ
        /*db.execSQL("CREATE TABLE Hives (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "number INTEGER, made NUMERIC, type_id INTEGER, size INTEGER, location_id INTEGER, description TEXT);");
        db.execSQL("CREATE TABLE HiveBoxes (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "made NUMERIC, type_id INTEGER, size INTEGER, location_id INTEGER, description TEXT, hive_id INTEGER);");
        db.execSQL("CREATE TABLE Traps (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "number INTEGER, made NUMERIC, type_id INTEGER, size INTEGER, location_id INTEGER, description TEXT);");*/

        db.execSQL("CREATE TABLE InventoryCategories (_id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT);");
        // Добавляем информацию в таблицу InventoryCategories
        String[] inventory_categories = this.context.getResources().getStringArray(R.array.inventory_categories);
        for(String category : inventory_categories) {
            ContentValues values = new ContentValues();
            values.put("category", category);
            db.insert("InventoryCategories", null, values);
        }

        db.execSQL("CREATE TABLE Inventory (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "category_id INTEGER, inventory_number INTEGER, number INTEGER, made NUMERIC, type_id INTEGER, size INTEGER, frames INTEGER, location_id INTEGER, description TEXT, hive_id INTEGER, deleted TEXT, deleted_date NUMERIC, broken NUMERIC);");

        db.execSQL("CREATE TABLE Bees (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "breed_id INTEGER, bought NUMERIC, queen_birth NUMERIC, hive_id INTEGER, deleted TEXT, deleted_date NUMERIC, description TEXT);");

        db.execSQL("CREATE TABLE Tasks (_id INTEGER PRIMARY KEY AUTOINCREMENT, stamp NUMERIC DEFAULT CURRENT_TIMESTAMP, " +
                "task TEXT, frequency TEXT, start NUMERIC, timing TEXT, inventory_id INTEGER, ask_status INTEGER DEFAULT 0, ask_comment INTEGER  DEFAULT 0, ask_total INTEGER  DEFAULT 0);");

        db.execSQL("CREATE TABLE Actions (_id INTEGER PRIMARY KEY AUTOINCREMENT, stamp NUMERIC DEFAULT CURRENT_TIMESTAMP, " +
                "task_id INTEGER, complete_date NUMERIC, action_type TEXT, action_status INTEGER, comment TEXT, total REAL);");
        // action_type:
        //  start - добавлена новая задача
        //  execute - задача выполнена
        //  reject - выполнение задачи отменено в какую-либо дату
        //  stop - задача остановлена


        // ДАЛЕЕ КОД НАПОЛНЕНИЯ БД ДЛЯ ТЕСТОВ - УДАЛИТЬ!
        ContentValues values;

        values = new ContentValues();
        values.put("address", "дер. Боровиково");
        db.insert("Locations", null, values);

        values = new ContentValues();
        values.put("address", "дер. Погорелка");
        db.insert("Locations", null, values);

        values = new ContentValues();
        values.put("category_id", 1);
        values.put("inventory_number", "1000");
        values.put("number", 1);
        values.put("made", "2023-02-18 00:00:00.000");
        values.put("type_id", 1);
        values.put("size", 12);
        values.put("frames", 12);
        values.put("location_id", 1);
        values.put("description", "test");
        values.put("broken", 1);
        db.insert("Inventory", null, values);

        values = new ContentValues();
        values.put("category_id", 1);
        values.put("inventory_number", "2000");
        values.put("number", 999);
        values.put("made", "2023-02-18 00:00:00.000");
        values.put("type_id", 1);
        values.put("size", 12);
        values.put("frames", 12);
        values.put("location_id", 1);
        values.put("description", "test");
        values.put("broken", 0);
        db.insert("Inventory", null, values);

        values = new ContentValues();
        values.put("category_id", 1);
        values.put("inventory_number", "3000");
        values.put("number", 2);
        values.put("made", "2023-02-19 00:00:00.000");
        values.put("type_id", 2);
        values.put("size", 12);
        values.put("location_id", 2);
        values.put("description", "test");
        values.put("broken", 0);
        db.insert("Inventory", null, values);

        values = new ContentValues();
        values.put("category_id", 2);
        values.put("inventory_number", "К1000");
        values.put("made", "2023-02-19 00:00:00.000");
        values.put("type_id", 2);
        values.put("size", 12);
        values.put("frames", 6);
        values.put("location_id", 2);
        values.put("description", "test");
        values.put("hive_id", 1);
        values.put("broken", 0);
        db.insert("Inventory", null, values);

        values = new ContentValues();
        values.put("category_id", 3);
        values.put("inventory_number", "М1000");
        values.put("made", "2023-02-19 00:00:00.000");
        values.put("type_id", 2);
        values.put("size", 12);
        values.put("frames", 7);
        values.put("location_id", 2);
        values.put("description", "test");
        values.put("hive_id", 2);
        values.put("broken", 0);
        db.insert("Inventory", null, values);

        values = new ContentValues();
        values.put("category_id", 3);
        values.put("inventory_number", "М2000");
        values.put("made", "2023-02-19 00:00:00.000");
        values.put("type_id", 2);
        values.put("size", 12);
        values.put("frames", 8);
        values.put("location_id", 2);
        values.put("description", "test");
        values.put("hive_id", 2);
        values.put("broken", 0);
        db.insert("Inventory", null, values);

        values = new ContentValues();
        values.put("breed_id", 1);
        values.put("bought", "2020-01-19 00:00:00.000");
        values.put("queen_birth", "2021-01-19 00:00:00.000");
        values.put("hive_id", 1);
        values.put("description", "test1");
        db.insert("Bees", null, values);

        values = new ContentValues();
        values.put("breed_id", 1);
        values.put("bought", "2020-02-19 00:00:00.000");
        values.put("queen_birth", "2021-02-19 00:00:00.000");
        values.put("hive_id", 2);
        values.put("description", "test2");
        db.insert("Bees", null, values);

        values = new ContentValues();
        values.put("task", "task1");
        values.put("frequency", "once");
        values.put("start", "2024-12-01 00:00:00.000");
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task2");
        values.put("frequency", "once");
        values.put("start", "2024-12-15 00:00:00.000");
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task3");
        values.put("frequency", "daily");
        values.put("start", "2024-11-27 00:00:00.000");
        values.put("timing", "4");
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task4");
        values.put("frequency", "weekly");
        values.put("start", "2024-11-30 00:00:00.000");
        values.put("timing", "3,4");
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task5");
        values.put("frequency", "monthly");
        values.put("start", "2024-12-25 00:00:00.000");
        values.put("timing", "15");
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task6");
        values.put("frequency", "yearly");
        values.put("start", "2025-02-25 00:00:00.000");
        values.put("timing", "01-10");
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task7");
        values.put("frequency", "once");
        values.put("start", "2024-12-01 00:00:00.000");
        values.put("ask_status", 1);
        values.put("ask_total", 1);
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task8");
        values.put("frequency", "once");
        values.put("start", "2024-12-01 00:00:00.000");
        values.put("ask_status", 1);
        values.put("ask_comment", 1);
        values.put("ask_total", 1);
        db.insert("Tasks", null, values);

        values = new ContentValues();
        values.put("task", "task9");
        values.put("frequency", "once");
        values.put("start", "2024-12-01 00:00:00.000");
        values.put("ask_status", 1);
        values.put("ask_comment", 1);
        values.put("ask_total", 1);
        db.insert("Tasks", null, values);



        values = new ContentValues();
        values.put("task_id", 1);
        values.put("complete_date", "2024-12-01 00:00:00.000");
        values.put("action_type", "execute");
        db.insert("Actions", null, values);

        values = new ContentValues();
        values.put("task_id", 2);
        values.put("complete_date", "2024-12-15 00:00:00.000");
        values.put("action_type", "execute");
        db.insert("Actions", null, values);

        values = new ContentValues();
        values.put("task_id", 3);
        values.put("complete_date", "2024-12-05 00:00:00.000");
        values.put("action_type", "execute");
        db.insert("Actions", null, values);

        values = new ContentValues();
        values.put("task_id", 3);
        values.put("complete_date", "2024-12-06 00:00:00.000");
        values.put("action_type", "execute");
        db.insert("Actions", null, values);

        values = new ContentValues();
        values.put("task_id", 4);
        values.put("complete_date", "2024-12-01 00:00:00.000");
        values.put("action_type", "reject");
        db.insert("Actions", null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
