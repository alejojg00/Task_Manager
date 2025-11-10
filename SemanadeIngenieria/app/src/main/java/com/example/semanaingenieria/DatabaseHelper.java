package com.example.semanaingenieria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eventos.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla y columnas
    private static final String TABLE_EVENTOS = "eventos";
    private static final String COL_COD = "cod";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_DESCRIP = "descrip";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_EVENTOS + "("
                + COL_COD + " INTEGER PRIMARY KEY,"
                + COL_NOMBRE + " TEXT,"
                + COL_DESCRIP + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTOS);
        onCreate(db);
    }

    // Insertar evento
    public boolean insertarEvento(int cod, String nombre, String descrip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COD, cod);
        values.put(COL_NOMBRE, nombre);
        values.put(COL_DESCRIP, descrip);

        long result = db.insert(TABLE_EVENTOS, null, values);
        return result != -1;
    }

    // Listar todos los eventos
    public List<Evento> listarEventos() {
        List<Evento> eventos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTOS, null);

        if (cursor.moveToFirst()) {
            do {
                Evento evento = new Evento();
                evento.setCod(cursor.getInt(0));
                evento.setNombre(cursor.getString(1));
                evento.setDescrip(cursor.getString(2));
                eventos.add(evento);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventos;
    }

    // Consultar evento por código
    public Evento consultarEvento(int cod) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTOS, null, COL_COD + "=?",
                new String[]{String.valueOf(cod)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Evento evento = new Evento();
            evento.setCod(cursor.getInt(0));
            evento.setNombre(cursor.getString(1));
            evento.setDescrip(cursor.getString(2));
            cursor.close();
            return evento;
        }
        return null;
    }

    // Actualizar evento
    public boolean actualizarEvento(int cod, String nombre, String descrip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, nombre);
        values.put(COL_DESCRIP, descrip);

        int result = db.update(TABLE_EVENTOS, values, COL_COD + "=?",
                new String[]{String.valueOf(cod)});
        return result > 0;
    }

    // Eliminar evento
    public boolean eliminarEvento(int cod) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENTOS, COL_COD + "=?",
                new String[]{String.valueOf(cod)});
        return result > 0;
    }
}

// Clase modelo Evento
class Evento {
    private int cod;
    private String nombre;
    private String descrip;

    public Evento() {}

    public Evento(int cod, String nombre, String descrip) {
        this.cod = cod;
        this.nombre = nombre;
        this.descrip = descrip;
    }

    public int getCod() { return cod; }
    public void setCod(int cod) { this.cod = cod; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescrip() { return descrip; }
    public void setDescrip(String descrip) { this.descrip = descrip; }

    @Override
    public String toString() {
        return "Código: " + cod + "\nNombre: " + nombre + "\nDescripción: " + descrip + "\n\n";
    }
}