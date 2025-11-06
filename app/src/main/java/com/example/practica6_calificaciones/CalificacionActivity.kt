package com.example.practica6_calificaciones

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CalificacionActivity : AppCompatActivity() {

    private lateinit var spnAlumno: Spinner
    private lateinit var spnMateria: Spinner
    private lateinit var edtUnidad1: EditText
    private lateinit var edtUnidad2: EditText
    private lateinit var edtUnidad3: EditText
    private lateinit var edtUnidad4: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnConsultar: Button
    private lateinit var btnVer: Button

    private lateinit var adaptadorAlumno: ArrayAdapter<String>
    private lateinit var adaptadorMateria: ArrayAdapter<String>

    private var listaAlumnos: MutableList<Alumno> = mutableListOf()
    private var listaMaterias: MutableList<Materia> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calificacion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()
        configurarSpinners()
        llenarSpinners()
        configurarListeners()
    }

    private fun inicializarVistas() {
        spnAlumno = findViewById(R.id.spnAlumno)
        spnMateria = findViewById(R.id.spnMateria)
        edtUnidad1 = findViewById(R.id.edtUnidad1)
        edtUnidad2 = findViewById(R.id.edtUnidad2)
        edtUnidad3 = findViewById(R.id.edtUnidad3)
        edtUnidad4 = findViewById(R.id.edtUnidad4)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnConsultar = findViewById(R.id.btnConsultar)
        btnVer = findViewById(R.id.btnVer)
    }

    private fun llenarSpinners() {
        llenarSpinnerAlumno()
        llenarSpinnerMateria()
    }

    private fun llenarSpinnerAlumno() {
        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM alumnos", null)

        listaAlumnos.clear()
        adaptadorAlumno.clear()

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
                    adaptadorAlumno.add("${alumno.matricula} - ${alumno.nombre}")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar alumnos: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
        adaptadorAlumno.notifyDataSetChanged()
    }

    private fun llenarSpinnerMateria() {
        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM materia", null)

        listaMaterias.clear()
        adaptadorMateria.clear()

        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val materia = Materia(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                    )
                    listaMaterias.add(materia)
                    adaptadorMateria.add("${materia.codigoM} - ${materia.nombre}")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar materias: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
        adaptadorMateria.notifyDataSetChanged()
    }

    private fun configurarListeners() {
        btnGuardar.setOnClickListener { guardarCalificacion() }
        btnEliminar.setOnClickListener { eliminarCalificacion() }
        btnConsultar.setOnClickListener { consultarCalificacion() }
        btnVer.setOnClickListener { consultarCalificacion() }
    }

    private fun guardarCalificacion() {
        val alumnoPos = spnAlumno.selectedItemPosition
        val materiaPos = spnMateria.selectedItemPosition

        if (alumnoPos < 0 || materiaPos < 0) {
            Toast.makeText(this, "Seleccione alumno y materia", Toast.LENGTH_SHORT).show()
            return
        }

        val unidad1 = edtUnidad1.text.toString().toDoubleOrNull() ?: 0.0
        val unidad2 = edtUnidad2.text.toString().toDoubleOrNull() ?: 0.0
        val unidad3 = edtUnidad3.text.toString().toDoubleOrNull() ?: 0.0
        val unidad4 = edtUnidad4.text.toString().toDoubleOrNull() ?: 0.0

        val alumno = listaAlumnos[alumnoPos]
        val materia = listaMaterias[materiaPos]

        val datos = ContentValues().apply {
            put("matricula", alumno.matricula)
            put("codigoM", materia.codigoM)
            put("unidad1", unidad1)
            put("unidad2", unidad2)
            put("unidad3", unidad3)
            put("unidad4", unidad4)
        }

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.writableDatabase

        try {
            // Verificar si ya existe
            val cursor = conector.rawQuery(
                "SELECT * FROM calificaciones WHERE matricula = ? AND codigoM = ?",
                arrayOf(alumno.matricula, materia.codigoM)
            )

            if (cursor != null && cursor.count > 0) {
                // Actualizar
                conector.update("calificaciones", datos, "matricula = ? AND codigoM = ?",
                    arrayOf(alumno.matricula, materia.codigoM))
                Toast.makeText(this, "Calificaciones actualizadas", Toast.LENGTH_SHORT).show()
            } else {
                // Insertar
                conector.insert("calificaciones", null, datos)
                Toast.makeText(this, "Calificaciones guardadas", Toast.LENGTH_SHORT).show()
            }
            cursor?.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            conector.close()
        }
    }

    private fun eliminarCalificacion() {
        val alumnoPos = spnAlumno.selectedItemPosition
        val materiaPos = spnMateria.selectedItemPosition

        if (alumnoPos < 0 || materiaPos < 0) {
            Toast.makeText(this, "Seleccione alumno y materia", Toast.LENGTH_SHORT).show()
            return
        }

        val alumno = listaAlumnos[alumnoPos]
        val materia = listaMaterias[materiaPos]

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.writableDatabase

        try {
            val filasEliminadas = conector.delete("calificaciones",
                "matricula = ? AND codigoM = ?",
                arrayOf(alumno.matricula, materia.codigoM))

            if (filasEliminadas > 0) {
                Toast.makeText(this, "Calificaciones eliminadas", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            } else {
                Toast.makeText(this, "No se encontraron calificaciones", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            conector.close()
        }
    }

    private fun consultarCalificacion() {
        val alumnoPos = spnAlumno.selectedItemPosition
        val materiaPos = spnMateria.selectedItemPosition

        if (alumnoPos < 0 || materiaPos < 0) {
            Toast.makeText(this, "Seleccione alumno y materia", Toast.LENGTH_SHORT).show()
            return
        }

        val alumno = listaAlumnos[alumnoPos]
        val materia = listaMaterias[materiaPos]

        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery(
            "SELECT * FROM calificaciones WHERE matricula = ? AND codigoM = ?",
            arrayOf(alumno.matricula, materia.codigoM)
        )

        limpiarCampos()

        try {
            if (cursor != null && cursor.moveToFirst()) {
                edtUnidad1.setText(cursor.getDouble(2).toString())
                edtUnidad2.setText(cursor.getDouble(3).toString())
                edtUnidad3.setText(cursor.getDouble(4).toString())
                edtUnidad4.setText(cursor.getDouble(5).toString())
                Toast.makeText(this, "Calificaciones cargadas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontraron calificaciones", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error en consulta: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
    }

    private fun limpiarCampos() {
        edtUnidad1.text.clear()
        edtUnidad2.text.clear()
        edtUnidad3.text.clear()
        edtUnidad4.text.clear()
    }
    private fun configurarSpinners() {
        adaptadorAlumno = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        adaptadorAlumno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        adaptadorMateria = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        adaptadorMateria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnAlumno.adapter = adaptadorAlumno
        spnMateria.adapter = adaptadorMateria

        // Configurar color del texto en los spinners
        configurarColorSpinners()
    }

    private fun configurarColorSpinners() {
        // Para el texto seleccionado
        spnAlumno.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        spnMateria.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }
}