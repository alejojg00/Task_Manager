package com.ecci.taskmanager.data.database

import androidx.room.TypeConverter
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.data.model.Priority

/**
 * Clase que contiene convertidores de tipos personalizados para la base de datos Room.
 *
 * Room solo puede almacenar tipos de datos primitivos (Int, String, Boolean, etc.),
 * por lo que se requiere esta clase para convertir objetos de tipo [TaskStatus] y [Priority]
 * a valores de texto (String) que puedan ser persistidos en la base de datos.
 *
 * Los métodos marcados con [TypeConverter] indican a Room cómo transformar los tipos
 * antes de guardarlos o leerlos desde la base de datos.x
 */
class Converters {

    // ------------------------------
    // Conversores para TaskStatus
    // ------------------------------

    /**
     * Convierte un objeto [TaskStatus] a su representación en texto.
     *
     * @param status Estado de la tarea a convertir (por ejemplo, PENDING, COMPLETED).
     * @return Una cadena de texto con el nombre del estado.
     */
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    /**
     * Convierte una cadena de texto a un objeto [TaskStatus].
     *
     * @param value Cadena que representa un estado almacenado en la base de datos.
     * @return El valor correspondiente de la enumeración [TaskStatus].
     */
    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)


    // ------------------------------
    // Conversores para Priority
    // ------------------------------

    /**
     * Convierte un objeto [Priority] a su representación en texto.
     *
     * @param priority Prioridad de la tarea a convertir (por ejemplo, HIGH, MEDIUM, LOW).
     * @return Una cadena de texto con el nombre de la prioridad.
     */
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    /**
     * Convierte una cadena de texto a un objeto [Priority].
     *
     * @param value Cadena que representa una prioridad almacenada en la base de datos.
     * @return El valor correspondiente de la enumeración [Priority].
     */
    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
}
