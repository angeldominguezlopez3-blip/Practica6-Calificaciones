package com.example.practica6_calificaciones

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlumnoActivity : AppCompatActivity() {
    private lateinit var edtMatricula: EditText
    private lateinit var edtNombre: EditText
    private lateinit var edtCarrera: EditText
    private lateinit var edtEmail: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnConsultar: Button
    private lateinit var btnVer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alumno)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()

        btnGuardar.setOnClickListener { guardarAlumno() }
        btnEliminar.setOnClickListener { eliminarAlumno() }
        btnConsultar.setOnClickListener { consultarAlumno() }
        btnVer.setOnClickListener { mostrarListaAlumnos() }
    }

    private fun inicializarVistas() {
        edtMatricula = findViewById(R.id.edtMatricula)
        edtNombre = findViewById(R.id.edtNombre)
        edtCarrera = findViewById(R.id.edtCarrera)
        edtEmail = findViewById(R.id.edtEmail)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnConsultar = findViewById(R.id.btnConsultar)
        btnVer = findViewById(R.id.btnVer)
    }

    private fun guardarAlumno() {
        val matricula = edtMatricula.text.toString()
        val nombre = edtNombre.text.toString()
        val carrera = edtCarrera.text.toString()
        val email = edtEmail.text.toString()

        if (matricula.isBlank() || nombre.isBlank() || carrera.isBlank() || email.isBlank()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = ContentValues().apply {
            put("matricula", matricula)
            put("nombre", nombre)
            put("carrera", carrera)
            put("email", email)
            put("avatar", R.drawable.user_default) // Avatar por defecto
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.writableDatabase

        try {
            conector.insert("alumnos", null, datos)
            Toast.makeText(this, "Alumno guardado", Toast.LENGTH_SHORT).show()
            limpiarCampos()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            conector.close()
        }
    }

    private fun eliminarAlumno() {
        val matricula = edtMatricula.text.toString()

        if (matricula.isBlank()) {
            Toast.makeText(this, "Ingrese una matrícula", Toast.LENGTH_SHORT).show()
            return
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.writableDatabase

        try {
            val filasEliminadas = conector.delete("alumnos", "matricula = ?", arrayOf(matricula))
            if (filasEliminadas > 0) {
                Toast.makeText(this, "Alumno eliminado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            } else {
                Toast.makeText(this, "No se encontró el alumno", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            conector.close()
        }
    }

    private fun consultarAlumno() {
        val matricula = edtMatricula.text.toString()

        if (matricula.isBlank()) {
            Toast.makeText(this, "Ingrese una matrícula", Toast.LENGTH_SHORT).show()
            return
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM alumnos WHERE matricula = ?", arrayOf(matricula))

        edtNombre.text.clear()
        edtCarrera.text.clear()
        edtEmail.text.clear()

        try {
            if (cursor != null && cursor.moveToFirst()) {
                edtNombre.setText(cursor.getString(1))
                edtCarrera.setText(cursor.getString(2))
                edtEmail.setText(cursor.getString(3))
                Toast.makeText(this, "Alumno encontrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Alumno no encontrado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error en consulta: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
    }

    private fun mostrarListaAlumnos() {
        val alumnos = consultarTodosAlumnos()
        val nombres = alumnos.map { "${it.matricula} - ${it.nombre}" }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alumnos Registrados")
            .setItems(nombres) { _, which ->
                val alumnoSeleccionado = alumnos[which]
                edtMatricula.setText(alumnoSeleccionado.matricula)
                edtNombre.setText(alumnoSeleccionado.nombre)
                edtCarrera.setText(alumnoSeleccionado.carrera)
                edtEmail.setText(alumnoSeleccionado.email)
                Toast.makeText(this, "Seleccionado: ${alumnoSeleccionado.nombre}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cerrar", null)
        builder.show()
    }

    private fun consultarTodosAlumnos(): MutableList<Alumno> {
        val listaAlumnos: MutableList<Alumno> = mutableListOf()
        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM alumnos", null)

        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val alumno = Alumno(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4)
                    )
                    listaAlumnos.add(alumno)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
        return listaAlumnos
    }

    private fun limpiarCampos() {
        edtMatricula.text.clear()
        edtNombre.text.clear()
        edtCarrera.text.clear()
        edtEmail.text.clear()
    }
}