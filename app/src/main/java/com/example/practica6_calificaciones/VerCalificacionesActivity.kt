package com.example.practica6_calificaciones

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VerCalificacionesActivity : AppCompatActivity() {

    private lateinit var spnAlumno: Spinner
    private lateinit var btnVerCalificaciones: Button
    private lateinit var adaptadorAlumno: ArrayAdapter<String>
    private var listaAlumnos: MutableList<Alumno> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_calificaciones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spnAlumno = findViewById(R.id.spnAlumno)
        btnVerCalificaciones = findViewById(R.id.btnVerCalificaciones)

        configurarSpinner()
        llenarSpinnerAlumno()
        configurarColorSpinner()

        btnVerCalificaciones.setOnClickListener {
            mostrarCalificacionesAlumno()
        }
    }

    private fun configurarSpinner() {
        adaptadorAlumno = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        adaptadorAlumno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnAlumno.adapter = adaptadorAlumno
    }

    private fun configurarColorSpinner() {
        spnAlumno.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.let {
                    it.setTextColor(Color.BLACK)
                    it.textSize = 14f
                    it.setPadding(8, 12, 8, 12)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        spnAlumno.post {
            (spnAlumno.selectedView as? TextView)?.setTextColor(Color.BLACK)
        }
    }

    private fun llenarSpinnerAlumno() {
        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase
        var cursor = conector.rawQuery("SELECT * FROM alumnos", null)

        listaAlumnos.clear()
        adaptadorAlumno.clear()

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val alumno = Alumno(
                        cursor.getString(0), // matricula
                        cursor.getString(1), // nombre
                        cursor.getString(2), // email
                        cursor.getString(3), // carrera
                        cursor.getInt(4)     // semestre
                    )
                    listaAlumnos.add(alumno)
                    adaptadorAlumno.add("${alumno.matricula} - ${alumno.nombre}")
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar alumnos: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
        adaptadorAlumno.notifyDataSetChanged()

        spnAlumno.post {
            (spnAlumno.selectedView as? TextView)?.setTextColor(Color.BLACK)
        }
    }

    private fun mostrarCalificacionesAlumno() {
        val alumnoPos = spnAlumno.selectedItemPosition

        if (alumnoPos < 0) {
            Toast.makeText(this, "Seleccione un alumno", Toast.LENGTH_SHORT).show()
            return
        }

        val alumno = listaAlumnos[alumnoPos]
        val calificaciones = obtenerCalificacionesAlumno(alumno.matricula)

        if (calificaciones.isEmpty()) {
            Toast.makeText(this, "El alumno no tiene calificaciones registradas", Toast.LENGTH_SHORT).show()
            return
        }

        val mensaje = construirMensajeCalificaciones(alumno, calificaciones)

        // Crear un TextView personalizado para el mensaje
        val textView = TextView(this)
        textView.text = mensaje
        textView.setTextColor(Color.BLACK) // Cambiado a negro para mejor legibilidad
        textView.textSize = 14f
        textView.setPadding(32, 32, 32, 32)
        textView.setBackgroundColor(Color.WHITE)

        // Configurar scroll por si el contenido es muy largo
        val scrollView = ScrollView(this)
        scrollView.setPadding(16, 16, 16, 16)
        scrollView.setBackgroundColor(Color.WHITE)
        scrollView.addView(textView)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("📊 Calificaciones de ${alumno.nombre}")
            .setView(scrollView)
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.show()
    }

    private fun obtenerCalificacionesAlumno(matricula: String): List<CalificacionConMateria> {
        val calificaciones = mutableListOf<CalificacionConMateria>()
        val adminSQLite = AdminSQLite(this, "Escuela", 1)
        val conector = adminSQLite.readableDatabase

        val query = """
            SELECT c.*, m.nombre as nombre_materia 
            FROM calificaciones c 
            JOIN materia m ON c.codigoM = m.codigoM 
            WHERE c.matricula = ?
        """.trimIndent()

        var cursor: android.database.Cursor? = null

        try {
            cursor = conector.rawQuery(query, arrayOf(matricula))

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Verificar que las columnas existen antes de acceder a ellas
                    val calificacion = CalificacionConMateria(
                        matricula = cursor.getString(cursor.getColumnIndexOrThrow("matricula")),
                        codigoM = cursor.getString(cursor.getColumnIndexOrThrow("codigoM")),
                        unidad1 = cursor.getDouble(cursor.getColumnIndexOrThrow("unidad1")),
                        unidad2 = cursor.getDouble(cursor.getColumnIndexOrThrow("unidad2")),
                        unidad3 = cursor.getDouble(cursor.getColumnIndexOrThrow("unidad3")),
                        unidad4 = cursor.getDouble(cursor.getColumnIndexOrThrow("unidad4")),
                        nombreMateria = cursor.getString(cursor.getColumnIndexOrThrow("nombre_materia"))
                    )
                    calificaciones.add(calificacion)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener calificaciones: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
            conector.close()
        }
        return calificaciones
    }

    private fun construirMensajeCalificaciones(alumno: Alumno, calificaciones: List<CalificacionConMateria>): String {
        val builder = StringBuilder()
        builder.append("🎓 Alumno: ${alumno.nombre}\n")
        builder.append("🔢 Matrícula: ${alumno.matricula}\n")
        builder.append("📚 Carrera: ${alumno.carrera}\n")
        builder.append("📧 Email: ${alumno.email}\n\n")
        builder.append("━━━━ CALIFICACIONES ━━━━\n\n")

        var promedioGeneral = 0.0
        var materiasConCalificacion = 0

        for (cal in calificaciones) {
            val promedioMateria = (cal.unidad1 + cal.unidad2 + cal.unidad3 + cal.unidad4) / 4
            promedioGeneral += promedioMateria
            materiasConCalificacion++

            builder.append("📖 ${cal.nombreMateria}\n")
            builder.append("   │ Unidad 1: ${String.format("%.1f", cal.unidad1)}\n")
            builder.append("   │ Unidad 2: ${String.format("%.1f", cal.unidad2)}\n")
            builder.append("   │ Unidad 3: ${String.format("%.1f", cal.unidad3)}\n")
            builder.append("   │ Unidad 4: ${String.format("%.1f", cal.unidad4)}\n")
            builder.append("   │ Promedio: ${String.format("%.2f", promedioMateria)}\n")

            // Agregar estado de la materia con emojis
            val estado = when {
                promedioMateria >= 8.0 -> "🎉 Excelente"
                promedioMateria >= 6.0 -> "✅ Aprobado"
                promedioMateria >= 5.0 -> "⚠️ Regularización"
                else -> "❌ Reprobado"
            }
            builder.append("   └ Estado: $estado\n\n")
        }

        if (materiasConCalificacion > 0) {
            promedioGeneral /= materiasConCalificacion
            builder.append("━━━━ RESUMEN GENERAL ━━━━\n")
            builder.append("📊 PROMEDIO GENERAL: ${String.format("%.2f", promedioGeneral)}\n")

            val estadoGeneral = when {
                promedioGeneral >= 8.0 -> "🎊 Excelente desempeño"
                promedioGeneral >= 6.0 -> "✅ Buen desempeño"
                promedioGeneral >= 5.0 -> "⚠️ Desempeño regular"
                else -> "❌ Necesita mejorar"
            }
            builder.append("🎯 ESTADO ACADÉMICO: $estadoGeneral\n")
            builder.append("📈 MATERIAS CURSADAS: $materiasConCalificacion")
        }

        return builder.toString()
    }
}

// Clase auxiliar para manejar calificaciones con nombre de materia
data class CalificacionConMateria(
    val matricula: String,
    val codigoM: String,
    val unidad1: Double,
    val unidad2: Double,
    val unidad3: Double,
    val unidad4: Double,
    val nombreMateria: String
)