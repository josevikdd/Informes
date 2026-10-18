package com.josevicentesanchez.ra5generarinformes

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.josevicentesanchez.ra5generarinformes.ui.theme.RA5GenerarInformesTheme
import org.json.JSONArray
import android.graphics.Color
import android.graphics.Paint
import android.widget.EditText
import android.widget.Toast
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private lateinit var spinnerCiudad: Spinner
    private lateinit var botonGenerarInforme: Button
    private lateinit var botonExportarPdf: Button
    private lateinit var contenedorInforme: LinearLayout
    private lateinit var textoCiudadSeleccionada: TextView
    private lateinit var textoNumeroOfertas: TextView
    private lateinit var textoSalarioTotal: TextView
    private lateinit var textoSalarioMedio: TextView
    private lateinit var textoSalarioMaximo: TextView
    private lateinit var textoSalarioMinimo: TextView
    private lateinit var barraValencia: ProgressBar
    private lateinit var barraMadrid: ProgressBar
    private lateinit var barraAlicante: ProgressBar

    private lateinit var barraSegorbe: ProgressBar
    private lateinit var textoCantidadValencia: TextView
    private lateinit var textoCantidadMadrid: TextView
    private lateinit var textoCantidadAlicante: TextView
    private lateinit var textoCantidadSegorbe: TextView
    private lateinit var contenedorOfertas: LinearLayout
    private val ofertas = ArrayList<Oferta>()
    private lateinit var salarioMinimo: EditText
    private lateinit var textoMejorOferta: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        spinnerCiudad = findViewById(R.id.spinnerCiudad)
        botonGenerarInforme = findViewById(R.id.botonGenerarInforme)
        botonExportarPdf = findViewById(R.id.botonExportarPdf)
        contenedorInforme = findViewById(R.id.contenedorInforme)
        textoCiudadSeleccionada = findViewById(R.id.textoCiudadSeleccionada)
        textoNumeroOfertas = findViewById(R.id.textoNumeroOfertas)
        textoSalarioTotal = findViewById(R.id.textoSalarioTotal)
        textoSalarioMedio = findViewById(R.id.textoSalarioMedio)
        textoSalarioMaximo = findViewById(R.id.textoSalarioMaximo)
        textoSalarioMinimo = findViewById(R.id.textoSalarioMinimo)
        barraValencia = findViewById(R.id.barraValencia)
        barraMadrid = findViewById(R.id.barraMadrid)
        barraAlicante = findViewById(R.id.barraAlicante)
        barraSegorbe = findViewById(R.id.barraSegorbe)
        textoCantidadValencia = findViewById(R.id.textoCantidadValencia)
        textoCantidadMadrid = findViewById(R.id.textoCantidadMadrid)
        textoCantidadAlicante = findViewById(R.id.textoCantidadAlicante)
        textoCantidadSegorbe = findViewById(R.id.textoCantidadSegorbe)
        contenedorOfertas = findViewById(R.id.contenedorOfertas)
        salarioMinimo = findViewById(R.id.salarioMinimo)
        textoMejorOferta = findViewById(R.id.mejorOferta)

        configurarSpinner()

        ofertas.addAll(cargarOfertas())

        botonGenerarInforme.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val minimo = salarioMinimo.text.toString().toIntOrNull() ?: 0
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada).filter {
                oferta -> oferta.salario >= minimo
            }
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
        }

        botonExportarPdf.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
            contenedorInforme.post {
                abrirSelectorParaGuardarPdf()
            }
        }

    }

    private fun configurarSpinner() {
        val ciudades = arrayOf("Todas", "Valencia", "Madrid", "Alicante", "Segorbe")
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ciudades
        )
        adaptador.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spinnerCiudad.adapter = adaptador
    }

    private fun cargarOfertas(): ArrayList<Oferta> {
        val listaOfertas = ArrayList<Oferta>()
        val textoJson = assets.open("ofertas.json")
            .bufferedReader()
            .use { it.readText() }
        val arrayJson = JSONArray(textoJson)
        for (i in 0 until arrayJson.length()) {
            val objetoJson = arrayJson.getJSONObject(i)
            val oferta = Oferta(
                objetoJson.getInt("id"),
                objetoJson.getString("puesto"),
                objetoJson.getString("empresa"),
                objetoJson.getString("ciudad"),
                objetoJson.getInt("salario"),
                objetoJson.getString("modalidad")
            )
            listaOfertas.add(oferta)
        }
        return listaOfertas
    }

    private fun filtrarOfertasPorCiudad(ciudad: String): List<Oferta> {
        val resultado = if (ciudad == "Todas") {
            ofertas
        } else {
            ofertas.filter {
                it.ciudad == ciudad
            }
        }
        return resultado
    }

    private fun mostrarResultado (ciudad: String, ofertasFiltradas: List<Oferta>) {
        val numeroOfertas = ofertasFiltradas.size
        val salarioTotal = ofertasFiltradas.sumOf { it.salario }
        val salarioMedio = if (ofertasFiltradas.size > 0) {
            ofertasFiltradas.map { it.salario }.average()
        } else {
            0.0
        }
        val salarioMaximo = ofertasFiltradas.maxOfOrNull { it.salario } ?: 0
        val salarioMinimo = ofertasFiltradas.minOfOrNull { it.salario } ?: 0

        val ofertaMaxSalario: Oferta? = ofertasFiltradas.maxByOrNull { it.salario }

        textoCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        textoNumeroOfertas.text = "Numero de ofertas: $numeroOfertas"
        textoSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        textoSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        textoSalarioMaximo.text = "Salario maximo: ${formatearSalario(salarioMaximo)}"
        textoSalarioMinimo.text = "Salario mínimo filtrado: ${formatearSalario(salarioMinimo)}"
        textoMejorOferta.text = if (ofertaMaxSalario != null) {
            "La oferta mejor pagada del filtro es: " +
                    "${ofertaMaxSalario.puesto} en ${ofertaMaxSalario.empresa}, " +
                    "en ${ofertaMaxSalario.ciudad}, con un salario de " +
                    "${formatearSalario(ofertaMaxSalario.salario)} €"

        } else {
            "La oferta mejor pagada del filtro es: -"
        }

        contenedorOfertas.removeAllViews()

        mostrarGrafico(ofertasFiltradas)
    }

    private fun formatearSalario(salario: Int): String {
        return "%,d".format(salario).replace(",", ".") + " €"
    }


    private fun mostrarGrafico(ofertasFiltradas: List<Oferta>) {
        val ofertasValencia = ofertasFiltradas.count {
            it.ciudad == "Valencia"
        }
        val ofertasMadrid = ofertasFiltradas.count {
            it.ciudad == "Madrid"
        }
        val ofertasAlicante = ofertasFiltradas.count {
            it.ciudad == "Alicante"
        }
        val ofertasSegorbe = ofertasFiltradas.count {
            it.ciudad == "Segorbe"
        }

        val valorMaximo = maxOf(
            ofertasValencia,
            ofertasMadrid,
            ofertasAlicante,
            ofertasSegorbe,
            1
        )

        barraValencia.max = valorMaximo
        barraMadrid.max = valorMaximo
        barraAlicante.max = valorMaximo
        barraSegorbe.max = valorMaximo

        barraValencia.progress = ofertasValencia
        barraMadrid.progress = ofertasMadrid
        barraAlicante.progress = ofertasAlicante
        barraSegorbe.progress = ofertasSegorbe

        textoCantidadValencia.text = ofertasValencia.toString()
        textoCantidadMadrid.text = ofertasMadrid.toString()
        textoCantidadAlicante.text = ofertasAlicante.toString()
        textoCantidadSegorbe.text = ofertasSegorbe.toString()
    }

    private val codigoCrearPdf = 100
    private fun abrirSelectorParaGuardarPdf() {
        val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_TITLE, "informe_${ciudadSeleccionada}.pdf")
        startActivityForResult(intent, codigoCrearPdf)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == codigoCrearPdf && resultCode == RESULT_OK) {
            val uriPdf = data?.data
            if (uriPdf != null) {
                exportarInformePdf(uriPdf)
            }
        }
    }

    private fun exportarInformePdf(uriPdf: Uri) {
        val documentoPdf = PdfDocument()
        val pagina = PdfDocument.PageInfo.Builder(
            595,
            842,
            1
        ).create()
        val paginaPdf = documentoPdf.startPage(pagina)
        val canvas = paginaPdf.canvas

        val pinturaTitulo = Paint()
        pinturaTitulo.color = Color.BLACK
        pinturaTitulo.textSize = 24f
        pinturaTitulo.isFakeBoldText = true
        canvas.drawColor(Color.WHITE)
        canvas.drawText(
            "Informe de ofertas",
            32f,
            48f,
            pinturaTitulo
        )

        val anchoDisponible = 531f
        val altoDisponible = 760f
        val anchoInforme = contenedorInforme.width
        val altoInforme = contenedorInforme.height
        if (anchoInforme > 0 && altoInforme > 0) {
            val escala = min(
                anchoDisponible / anchoInforme,
                altoDisponible / altoInforme
            )
            canvas.save()
            canvas.translate(32f, 72f)
            canvas.scale(escala, escala)
            contenedorInforme.draw(canvas)
            canvas.restore()
        }

        documentoPdf.finishPage(paginaPdf)
        val salida = contentResolver.openOutputStream(uriPdf)
        if (salida != null) {
            salida.use {
                documentoPdf.writeTo(salida)
            }
            Toast.makeText(
                this,
                "PDF guardado",
                Toast.LENGTH_LONG
            ).show()
        }
        documentoPdf.close()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RA5GenerarInformesTheme {
        Greeting("Android")
    }
}