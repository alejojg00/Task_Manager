package com.ecci.taskmanager.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ecci.taskmanager.R
import com.ecci.taskmanager.ui.MainActivity

/**
 * [BroadcastReceiver] responsable de mostrar una notificación cuando se activa
 * un recordatorio de tarea programado por el [NotificationHelper].
 *
 * Este receptor se ejecuta en segundo plano cuando el sistema dispara la alarma
 * configurada para una tarea con recordatorio activo.
 *
 * Funcionalidades:
 * - Crea el canal de notificación si no existe (en Android 8.0+).
 * - Muestra una notificación con el título y descripción de la tarea.
 * - Permite abrir la aplicación al tocar la notificación.
 *
 * Requiere el permiso [Manifest.permission.POST_NOTIFICATIONS] en Android 13 (Tiramisu) o superior.
 */
class TaskReminderReceiver : BroadcastReceiver() {

    /**
     * Método principal que se ejecuta automáticamente cuando se recibe el evento
     * del sistema (la alarma del recordatorio de tarea).
     *
     * Recupera los datos de la tarea enviados desde el [NotificationHelper]
     * y muestra la notificación al usuario.
     *
     * @param context Contexto del sistema proporcionado por Android.
     * @param intent Intent que contiene los extras con la información de la tarea:
     *  - `"TASK_ID"`: Identificador único de la tarea.
     *  - `"TASK_TITLE"`: Título de la tarea.
     *  - `"TASK_DESCRIPTION"`: Descripción o detalles de la tarea.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val taskId = intent.getLongExtra("TASK_ID", 0)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Tarea"
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION") ?: ""

        // Crear el canal de notificación si aún no existe
        createNotificationChannel(context)

        // Mostrar la notificación con los datos de la tarea
        showNotification(context, taskId, taskTitle, taskDescription)
    }

    /**
     * Crea el canal de notificación necesario para mostrar los recordatorios de tareas.
     *
     * Este método solo se ejecuta en dispositivos con Android 8.0 (Oreo) o superior.
     * Si el canal ya existe, el sistema simplemente lo reutiliza.
     *
     * @param context Contexto de aplicación.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Tareas"
            val descriptionText = "Notificaciones para recordatorios de tareas"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una notificación personalizada con los datos de la tarea.
     *
     * Si el usuario toca la notificación, se abrirá la [MainActivity] y
     * se enviará el identificador de la tarea como extra.
     *
     * @param context Contexto necesario para construir la notificación.
     * @param taskId ID de la tarea (usado también como ID único de la notificación).
     * @param title Título de la tarea.
     * @param description Descripción o detalles de la tarea.
     *
     * @throws SecurityException Si no se cuenta con el permiso
     * [Manifest.permission.POST_NOTIFICATIONS] en Android 13 o superior.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, taskId: Long, title: String, description: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("Recordatorio: $title")
            .setContentText(description.ifEmpty { "Tienes una tarea pendiente" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // En Android 13+ es necesario el permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        }
    }

    companion object {
        /** ID del canal de notificación usado para todos los recordatorios de tareas. */
        private const val CHANNEL_ID = "task_reminders"
    }
}
