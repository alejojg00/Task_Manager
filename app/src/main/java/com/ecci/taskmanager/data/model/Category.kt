package com.ecci.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa la entidad **Category** dentro de la base de datos Room.
 *
 * Cada categoría agrupa un conjunto de tareas bajo un mismo tema o propósito
 * (por ejemplo, trabajo, estudios o vida personal).
 *
 * La anotación [Entity] indica que esta clase define una tabla en la base de datos
 * llamada **"categories"**, donde cada instancia se corresponde con una fila.
 *
 * @property id Identificador único de la categoría. Se genera automáticamente.
 * @property name Nombre de la categoría (por ejemplo, “Trabajo” o “Personal”).
 * @property color Color asociado a la categoría en formato hexadecimal.
 * @property icon Nombre del ícono representativo de la categoría.
 * @property isPredefined Indica si la categoría fue creada por el sistema (true) o por el usuario (false).
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val color: String = "#1976D2",

    val icon: String = "folder",

    val isPredefined: Boolean = false
) {

    /**
     * Contiene métodos auxiliares relacionados con la creación de categorías predefinidas.
     */
    companion object {

        /**
         * Retorna una lista de categorías básicas que se insertan automáticamente
         * cuando la base de datos se crea por primera vez.
         *
         * Estas categorías sirven como estructura inicial para el usuario,
         * permitiéndole organizar sus tareas desde el inicio.
         *
         * @return Lista de categorías predefinidas.
         */
        fun getPredefinedCategories(): List<Category> {
            return listOf(
                Category(
                    id = 1,
                    name = "Trabajo",
                    color = "#1976D2",
                    icon = "work",
                    isPredefined = true
                ),
                Category(
                    id = 2,
                    name = "Personal",
                    color = "#4CAF50",
                    icon = "person",
                    isPredefined = true
                ),
                Category(
                    id = 3,
                    name = "Estudios",
                    color = "#FF9800",
                    icon = "school",
                    isPredefined = true
                )
            )
        }
    }

    /**
     * Verifica si el nombre de la categoría es válido.
     *
     * Se utiliza para asegurar que no se creen categorías vacías
     * o con nombres compuestos solo por espacios.
     *
     * @return `true` si el nombre contiene texto válido, `false` en caso contrario.
     */
    fun isValid(): Boolean {
        return name.trim().isNotEmpty()
    }
}
