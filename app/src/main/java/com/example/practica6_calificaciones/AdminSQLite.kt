package com.example.practica6_calificaciones

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AdminSQLite(context: Context, bdName: String, version: Int): SQLiteOpenHelper(context, bdName, null, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Tabla Alumno
        db?.execSQL("CREATE TABLE alumnos(matricula TEXT PRIMARY KEY, nombre TEXT, carrera TEXT, email TEXT, avatar INTEGER)")

        // Tabla Materia
        db?.execSQL("CREATE TABLE materia(codigoM TEXT PRIMARY KEY, nombre TEXT, creditos TEXT)")

        // Tabla Calificaciones
        db?.execSQL("CREATE TABLE calificaciones(" +
                "matricula TEXT, " +
                "codigoM TEXT, " +
                "unidad1 REAL, " +
                "unidad2 REAL, " +
                "unidad3 REAL, " +
                "unidad4 REAL, " +
                "FOREIGN KEY(matricula) REFERENCES alumnos(matricula), " +
                "FOREIGN KEY(codigoM) REFERENCES materia(codigoM))")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS calificaciones")
        db?.execSQL("DROP TABLE IF EXISTS alumnos")
        db?.execSQL("DROP TABLE IF EXISTS materia")
        onCreate(db)
    }
}