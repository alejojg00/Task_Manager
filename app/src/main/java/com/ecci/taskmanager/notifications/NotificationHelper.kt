package com.ecci.taskmanager.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ecci.taskmanager.data.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Clase encargada de programar y cancelar notificaciones asociadas a las tareas.
 *
 * Utiliza el servicio del sistema [AlarmManager] para establecer alarmas
 * que activan un [BroadcastReceiver] en el momento indicado por el recordatorio
 * de la tarea.
 *
 * Esta clase se inyecta mediante Hilt y debe usarse desde repositorios o ViewModels
 * cuando una tarea con recordatorio se crea, actualiza o elimina.
 *
 * @property context Contexto de aplicación inyectado por Hilt.
 */
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Servicio del sistema para programar y administrar alarmas. */
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Programa una notificación para una tarea que tenga un recordatorio activo.
     *
     * Si la tarea no tiene recordatorio (`hasReminder == false`) o su hora
     * de recordatorio (`reminderTime`) es nula, la función no realiza ninguna acción.
     *
     * Internamente, crea un [PendingIntent] con los datos de la tarea y
     * utiliza el [AlarmManager] para ejecutar un [BroadcastReceiver]
     * en el momento exacto del recordatorio.
     *
     * @param task La tarea que contiene la información del recordatorio.
     *
     * Ejemplo de uso:
     * ```kotlin
     * notificationHelper.scheduleNotification(task)
     * ```
     */
    fun scheduleNotification(task: Task) {
        if (!task.hasReminder || task.reminderTime == null) return

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_DESCRIPTION", task.description ?: "")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.reminderTime!!.time,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    task.reminderTime!!.time,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Cancela una notificación programada previamente para una tarea específica.
     *
     * Si existe una alarma activa para la tarea con el `taskId` indicado,
     * esta será cancelada y no se activará en el futuro.
     *
     * @param taskId Identificador único de la tarea cuya notificación se desea cancelar.
     *
     * Ejemplo de uso:
     * ```kotlin
     * notificationHelper.cancelNotification(task.id)
     * ```
     */
    fun cancelNotification(taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        alarmManager.cancel(pendingIntent)
    }
}
