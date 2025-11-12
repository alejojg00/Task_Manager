package com.ecci.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa la entidad **Tag** dentro de la base de datos Room.
 *
 * Una etiqueta (Tag) permite clasificar las tareas con palabras clave o categorías adicionales
 * (por ejemplo, "Urgente", "Reunión", "Proyecto"), lo que facilita su búsqueda y filtrado.
 *
 * Cada etiqueta tiene un nombre y un color distintivo que ayuda a su identificación visual
 * dentro de la interfaz de usuario.
 *
 * @property id Identificador único de la etiqueta. Se genera automáticamente.
 * @property name Nombre de la etiqueta (por ejemplo, “Importante” o “Urgente”).
 * @property color Color visual asociado a la etiqueta en formato hexadecimal.
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val color: String = "#424242"
) {
    /**
     * Verifica si el nombre de la etiqueta es válido.
     *
     * Se utiliza para evitar la creación de etiquetas vacías o con espacios en blanco.
     *
     * @return `true` si el nombre contiene texto válido, `false` en caso contrario.
     */
    fun isValid(): Boolean {
        return name.trim().isNotEmpty()
    }
}

/**
 * Entidad intermedia que representa la relación **muchos a muchos**
 * entre las tablas [Task] y [Tag].
 *
 * Una tarea puede tener múltiples etiquetas, y una etiqueta puede
 * estar asociada a varias tareas.
 *
 * Room maneja esta relación mediante una tabla de referencia cruzada,
 * definida por esta clase, que utiliza como claves primarias combinadas
 * los identificadores de la tarea y de la etiqueta.
 *
 * @property taskId Identificador de la tarea relacionada.
 * @property tagId Identificador de la etiqueta asociada.
 */
@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long
)
