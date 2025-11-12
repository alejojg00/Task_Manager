package com.ecci.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ecci.taskmanager.data.converters.DateConverter
import java.util.Date

/**
 * Entidad que representa una **tarea** dentro de la base de datos local de la aplicación.
 *
 * Cada tarea contiene información relacionada con su descripción, prioridad,
 * estado, fechas y configuraciones opcionales como recordatorios u horarios recurrentes.
 *
 * Se utiliza la anotación [TypeConverters] para permitir que Room
 * almacene objetos de tipo [Date] en la base de datos, convirtiéndolos
 * automáticamente a valores tipo `Long` mediante la clase [DateConverter].
 *
 * @property id Identificador único de la tarea, generado automáticamente por Room.
 * @property title Título descriptivo de la tarea.
 * @property description Descripción opcional que amplía la información de la tarea.
 * @property dueDate Fecha de vencimiento o entrega de la tarea.
 * @property priority Nivel de prioridad asignado a la tarea (Alta, Media o Baja).
 * @property status Estado actual de la tarea (Pendiente, Completada o Vencida).
 * @property categoryId Identificador de la categoría asociada, si existe.
 * @property createdAt Fecha en la que fue creada la tarea (por defecto, la fecha actual).
 * @property completedAt Fecha en la que la tarea fue marcada como completada, si aplica.
 * @property hasReminder Indica si la tarea tiene activado un recordatorio.
 * @property reminderTime Hora exacta en la que debe generarse el recordatorio.
 * @property isRecurring Indica si la tarea se repite en determinados días.
 * @property recurringDays Días de repetición representados como una cadena (por ejemplo, "1,2,3" → Lunes, Martes, Miércoles).
 * @property startTime Hora de inicio del intervalo de la tarea (por ejemplo, "08:00").
 * @property endTime Hora de finalización del intervalo de la tarea (por ejemplo, "10:00").
 */
@Entity(tableName = "tasks")
@TypeConverters(DateConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val description: String? = null,

    val dueDate: Date? = null,

    val priority: Priority = Priority.MEDIUM,

    val status: TaskStatus = TaskStatus.PENDING,

    val categoryId: Long? = null,

    val createdAt: Date = Date(),

    val completedAt: Date? = null,

    val hasReminder: Boolean = false,

    val reminderTime: Date? = null,

    // NUEVAS PROPIEDADES PARA HORARIOS
    val isRecurring: Boolean = false,

    val recurringDays: String? = null, // "1,2,3" (Lunes, Martes, Miércoles)

    val startTime: String? = null, // "08:00"

    val endTime: String? = null // "10:00"
) {

    /**
     * Verifica si la tarea tiene un título válido.
     *
     * Este método evita que se creen tareas vacías o sin un nombre asignado.
     *
     * @return `true` si el título contiene texto válido, `false` en caso contrario.
     */
    fun isValid(): Boolean {
        return title.trim().isNotEmpty()
    }

    /**
     * Determina si la tarea está vencida.
     *
     * Una tarea se considera vencida si su fecha de vencimiento
     * ya ha pasado y su estado no es [TaskStatus.COMPLETED].
     *
     * @return `true` si la tarea está vencida, `false` en caso contrario.
     */
    fun isOverdue(): Boolean {
        return dueDate?.before(Date()) == true && status != TaskStatus.COMPLETED
    }

    /**
     * Marca la tarea como completada.
     *
     * Crea una nueva copia de la tarea con el estado actualizado a [TaskStatus.COMPLETED]
     * y establece la fecha de finalización ([completedAt]) en el momento actual.
     *
     * @return Una nueva instancia de [Task] marcada como completada.
     */
    fun markAsCompleted(): Task {
        return this.copy(
            status = TaskStatus.COMPLETED,
            completedAt = Date()
        )
    }
}

/**
 * Enum que define los niveles de prioridad de una tarea.
 *
 * Permite clasificar las tareas según su importancia o urgencia:
 * - [HIGH]: Alta
 * - [MEDIUM]: Media
 * - [LOW]: Baja
 *
 * @property value Valor numérico asociado a la prioridad (para ordenamiento).
 * @property displayName Nombre legible mostrado en la interfaz.
 */
enum class Priority(val value: Int, val displayName: String) {
    HIGH(3, "Alta"),
    MEDIUM(2, "Media"),
    LOW(1, "Baja");

    companion object {
        /**
         * Obtiene la prioridad correspondiente a un valor entero.
         *
         * @param value Valor numérico (1–3).
         * @return Prioridad asociada o [MEDIUM] si el valor no coincide.
         */
        fun fromValue(value: Int): Priority {
            return values().find { it.value == value } ?: MEDIUM
        }
    }
}

/**
 * Enum que representa los posibles estados de una tarea.
 *
 * Estos estados permiten realizar un seguimiento del progreso
 * y determinar si una tarea está pendiente, completada o vencida.
 *
 * @property value Cadena que identifica el estado internamente.
 */
enum class TaskStatus(val value: String) {
    PENDING("pending"),
    COMPLETED("completed"),
    OVERDUE("overdue");

    companion object {
        /**
         * Obtiene el estado de tarea correspondiente a una cadena.
         *
         * @param value Valor textual (por ejemplo, "completed").
         * @return Estado asociado o [PENDING] si no coincide.
         */
        fun fromValue(value: String): TaskStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}
