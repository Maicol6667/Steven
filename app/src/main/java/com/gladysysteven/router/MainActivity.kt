package com.gladysysteven.router

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Color
import android.content.Intent
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import java.net.Socket
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var output: TextView
    private lateinit var scanButton: Button
    private val routerIp = "192.168.18.1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(Color.rgb(12, 18, 30))
        }
        val title = TextView(this).apply {
            text = "ROUTER GLADYS Y STEVEN"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Diagnóstico de acceso · Huawei EG8145V5"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }
        scanButton = Button(this).apply {
            text = "COMPROBAR ACCESO AL MÓDEM"
            setOnClickListener { runDiagnostic() }
        }
        val openButton = Button(this).apply {
            text = "ABRIR PANEL DEL ROUTER"
            setOnClickListener { openRouter() }
        }
        output = TextView(this).apply {
            text = "Router detectado como dirección configurada: $routerIp\n\nPulsa «Comprobar acceso» para revisar los puertos web habituales.\n\nEsta herramienta solo diagnostica la conexión; no descubre ni modifica contraseñas."
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(4, 24, 4, 16)
        }
        root.addView(title)
        root.addView(subtitle)
        root.addView(scanButton)
        root.addView(openButton)
        val scroll = ScrollView(this)
        scroll.addView(output)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun runDiagnostic() {
        scanButton.isEnabled = false
        output.text = "Comprobando conexión con $routerIp…\nEsto puede tardar unos segundos."
        executor.execute {
            val report = StringBuilder()
            report.append("DIAGNÓSTICO DE ROUTER\nDirección: $routerIp\n\n")
            val ports = listOf(80, 443, 8080, 8443)
            var reachable = false
            for (port in ports) {
                var open = false
                try {
                    Socket().use { socket ->
                        socket.connect(java.net.InetSocketAddress(routerIp, port), 1800)
                        open = true
                    }
                } catch (_: Exception) { }
                report.append("Puerto $port: ").append(if (open) "ACCESIBLE" else "sin respuesta").append("\n")
                if (open) {
                    reachable = true
                    val scheme = if (port == 443 || port == 8443) "https" else "http"
                    report.append("Probando página $scheme://$routerIp:$port …\n")
                    try {
                        val connection = URL("$scheme://$routerIp:$port").openConnection() as HttpURLConnection
                        connection.connectTimeout = 2500
                        connection.readTimeout = 2500
                        connection.instanceFollowRedirects = false
                        connection.requestMethod = "GET"
                        val code = connection.responseCode
                        report.append("Respuesta web: HTTP ").append(code).append("\n")
                        connection.disconnect()
                    } catch (e: Exception) {
                        report.append("El puerto responde, pero no se pudo leer la página: ")
                            .append(e.javaClass.simpleName).append("\n")
                    }
                }
            }
            report.append("\nRESULTADO\n")
            if (!reachable) {
                report.append("El teléfono no obtuvo respuesta en los cuatro puertos revisados. Esto puede deberse a que el router bloquea el acceso local, usa otro puerto, o el teléfono no está conectado a esa red Wi‑Fi. No significa que el módem esté dañado.")
            } else {
                report.append("Se encontró al menos un puerto web accesible. Usa «Abrir panel del router» para intentar entrar desde el navegador. Si aparece inicio de sesión, utiliza únicamente las credenciales impresas/proporcionadas por tu operador.")
            }
            runOnUiThread {
                output.text = report.toString()
                scanButton.isEnabled = true
            }
        }
    }

    private fun openRouter() {
        val candidates = listOf("http://$routerIp", "https://$routerIp")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(candidates[0])))
        } catch (_: Exception) {
            output.append("\nNo se pudo abrir el navegador.")
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
