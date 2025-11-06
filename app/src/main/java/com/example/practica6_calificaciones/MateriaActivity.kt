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

class MateriaActivity : AppCompatActivity() {
    private lateinit var edtCodigo: EditText
    private lateinit var edtNombre: EditText
    private lateinit var edtCreditos: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnConsultar: Button
    private lateinit var btnVer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_materia)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()

        btnGuardar.setOnClickListener { guardarMateria() }
        btnEliminar.setOnClickListener { eliminarMateria() }
        btnConsultar.setOnClickListener { consultarMateria() }
        btnVer.setOnClickListener { mostrarListaMaterias() }
    }

    private fun inicializarVistas() {
        edtCodigo = findViewById(R.id.edtCodigo)
        edtNombre = findViewById(R.id.edtNombre)
        edtCreditos = findViewById(R.id.edtCreditos)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnConsultar = findViewById(R.id.btnConsultar)
        btnVer = findViewById(R.id.btnVer)
    }

    private fun guardarMateria() {
        val codigo = edtCodigo.text.toString()
        val nombre = edtNombre.text.toString()
        val creditos = edtCreditos.text.toString()

        if (codigo.isBlank() || nombre.isBlank() || creditos.isBlank()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = ContentValues().apply {
            put("codigoM", codigo)
            put("nombre", nombre)
            put("creditos", creditos)
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.writableDatabase

        try {
            conector.insert("materia", null, datos)
            Toast.makeText(this, "Materia guardada", Toast.LENGTH_SHORT).show()
            limpiarCampos()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            conector.close()
        }
    }

    private fun eliminarMateria() {
        val codigo = edtCodigo.text.toString()

        if (codigo.isBlank()) {
            Toast.makeText(this, "Ingrese un código", Toast.LENGTH_SHORT).show()
            return
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.writableDatabase

        try {
            val filasEliminadas = conector.delete("materia", "codigoM = ?", arrayOf(codigo))
            if (filasEliminadas > 0) {
                Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            } else {
                Toast.makeText(this, "No se encontró la materia", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            conector.close()
        }
    }

    private fun consultarMateria() {
        val codigo = edtCodigo.text.toString()

        if (codigo.isBlank()) {
            Toast.makeText(this, "Ingrese un código", Toast.LENGTH_SHORT).show()
            return
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM materia WHERE codigoM = ?", arrayOf(codigo))

        edtNombre.text.clear()
        edtCreditos.text.clear()

        try {
            if (cursor != null && cursor.moveToFirst()) {
                edtNombre.setText(cursor.getString(1))
                edtCreditos.setText(cursor.getString(2))
                Toast.makeText(this, "Materia encontrada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Materia no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error en consulta: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
    }

    private fun mostrarListaMaterias() {
        val materias = consultarTodasMaterias()
        val nombres = materias.map { "${it.codigoM} - ${it.nombre}" }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Materias Registradas")
            .setItems(nombres) { _, which ->
                val materiaSeleccionada = materias[which]
                edtCodigo.setText(materiaSeleccionada.codigoM)
                edtNombre.setText(materiaSeleccionada.nombre)
                edtCreditos.setText(materiaSeleccionada.creditos)
                Toast.makeText(this, "Seleccionada: ${materiaSeleccionada.nombre}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cerrar", null)
        builder.show()
    }

    private fun consultarTodasMaterias(): MutableList<Materia> {
        val listaMaterias: MutableList<Materia> = mutableListOf()
        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM materia", null)

        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val materia = Materia(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                    )
                    listaMaterias.add(materia)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
        return listaMaterias
    }

    private fun limpiarCampos() {
        edtCodigo.text.clear()
        edtNombre.text.clear()
        edtCreditos.text.clear()
    }
}