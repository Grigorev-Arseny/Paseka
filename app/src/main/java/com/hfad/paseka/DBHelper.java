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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
