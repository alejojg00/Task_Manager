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

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val taskId = intent.getLongExtra("TASK_ID", 0)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Tarea"
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION") ?: ""


    }

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

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

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
        private const val CHANNEL_ID = "task_reminders"
    }
}