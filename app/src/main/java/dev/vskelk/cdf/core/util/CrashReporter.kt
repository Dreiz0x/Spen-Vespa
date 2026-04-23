package dev.vskelk.cdf.core.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class CrashReporter(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "crash_vespa_$timeStamp.txt"
            
            // ⚡ REDIRECCIÓN A CARPETA PÚBLICA DE DESCARGAS (DOWNLOADS)
            // Esta ruta es accesible para cualquier explorador de archivos sin restricciones de sistema.
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            
            if (downloadsDir != null) {
                val file = File(downloadsDir, fileName)
                val writer = PrintWriter(FileWriter(file))
                
                writer.println("=== VESPA FATAL CRASH REPORT ===")
                writer.println("Hora: $timeStamp")
                writer.println("Hilo: ${thread.name}")
                writer.println("Excepción: ${exception.javaClass.name}")
                writer.println("Mensaje: ${exception.message}")
                writer.println("=== STACKTRACE EXACTO ===")
                exception.printStackTrace(writer)
                writer.flush()
                writer.close()
            }
        } catch (e: Exception) {
            // Si falla el guardado por permisos de escritura, el crash original sigue su curso
        } finally {
            // Le pasamos el control al sistema para que cierre la app
            defaultHandler?.uncaughtException(thread, exception) ?: exitProcess(1)
        }
    }

    companion object {
        fun instalar(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashReporter(context))
        }
    }
}
