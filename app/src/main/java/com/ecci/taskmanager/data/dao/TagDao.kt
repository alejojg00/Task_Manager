package com.ecci.taskmanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ecci.taskmanager.data.model.Tag
import com.ecci.taskmanager.data.model.TaskTagCrossRef
import com.ecci.taskmanager.data.model.Task

/**
 * Interfaz que define las operaciones de acceso a datos (DAO)
 * relacionadas con la entidad [Tag] y su relación con [Task].
 *
 * Esta interfaz permite manejar etiquetas (tags) asociadas a tareas,
 * incluyendo la gestión de su relación muchos a muchos mediante
 * la tabla intermedia [TaskTagCrossRef].
 *
 * Proporciona operaciones CRUD y consultas personalizadas para obtener
 * tanto las etiquetas de una tarea específica como las tareas asociadas
 * a una etiqueta determinada.
 */
@Dao
interface TagDao {

    /**
     * Inserta una nueva etiqueta en la base de datos.
     * Si ya existe una con el mismo ID, se reemplaza.
     *
     * @param tag Objeto [Tag] que se desea insertar.
     * @return El identificador único ([Long]) generado o reemplazado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    /**
     * Inserta una lista de etiquetas en la base de datos.
     * Si alguna ya existe, se reemplaza.
     *
     * @param tags Lista de objetos [Tag] que se desean insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    /**
     * Obtiene todas las etiquetas registradas en la base de datos,
     * ordenadas alfabéticamente por nombre.
     *
     * @return Un objeto [LiveData] con la lista de etiquetas actualizadas automáticamente.
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>

    /**
     * Obtiene una etiqueta específica según su identificador.
     *
     * @param tagId Identificador único de la etiqueta.
     * @return El objeto [Tag] correspondiente, o `null` si no existe.
     */
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    /**
     * Actualiza los datos de una etiqueta existente.
     *
     * @param tag Objeto [Tag] con los datos actualizados.
     */
    @Update
    suspend fun updateTag(tag: Tag)

    /**
     * Elimina una etiqueta específica de la base de datos.
     *
     * @param tag Objeto [Tag] que se desea eliminar.
     */
    @Delete
    suspend fun deleteTag(tag: Tag)

    /**
     * Elimina una etiqueta según su identificador único.
     *
     * @param tagId Identificador de la etiqueta a eliminar.
     */
    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)

    /**
     * Inserta una relación entre una tarea y una etiqueta
     * en la tabla intermedia [TaskTagCrossRef].
     * Si la relación ya existe, se reemplaza.
     *
     * @param crossRef Objeto que representa la relación entre una tarea y una etiqueta.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTagCrossRef(crossRef: TaskTagCrossRef)

    /**
     * Elimina una relación existente entre una tarea y una etiqueta.
     *
     * @param crossRef Objeto [TaskTagCrossRef] que representa la relación a eliminar.
     */
    @Delete
    suspend fun deleteTaskTagCrossRef(crossRef: TaskTagCrossRef)

    /**
     * Obtiene todas las etiquetas asociadas a una tarea específica.
     *
     * Utiliza una unión interna entre las tablas `tags` y `task_tag_cross_ref`
     * para identificar las etiquetas vinculadas con el ID de la tarea.
     *
     * @param taskId Identificador de la tarea.
     * @return Un objeto [LiveData] con la lista de etiquetas relacionadas.
     */
    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN task_tag_cross_ref ON tags.id = task_tag_cross_ref.tagId
        WHERE task_tag_cross_ref.taskId = :taskId
        ORDER BY tags.name ASC
    """)
    fun getTagsForTask(taskId: Long): LiveData<List<Tag>>

    /**
     * Obtiene todas las tareas asociadas a una etiqueta específica.
     *
     * Utiliza una unión interna entre las tablas `tasks` y `task_tag_cross_ref`
     * para identificar las tareas vinculadas al ID de la etiqueta.
     *
     * @param tagId Identificador de la etiqueta.
     * @return Un objeto [LiveData] con la lista de tareas asociadas.
     */
    @Query("""
        SELECT tasks.* FROM tasks
        INNER JOIN task_tag_cross_ref ON tasks.id = task_tag_cross_ref.taskId
        WHERE task_tag_cross_ref.tagId = :tagId
        ORDER BY tasks.createdAt DESC
    """)
    fun getTasksWithTag(tagId: Long): LiveData<List<Task>>

    /**
     * Elimina todas las relaciones de etiquetas asociadas a una tarea específica.
     * Esto no elimina las etiquetas en sí, solo sus vínculos con la tarea.
     *
     * @param taskId Identificador de la tarea cuyos vínculos se eliminarán.
     */
    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun deleteAllTagsFromTask(taskId: Long)

    /**
     * Obtiene el número de tareas asociadas a una etiqueta específica.
     *
     * @param tagId Identificador de la etiqueta.
     * @return Un objeto [LiveData] con el conteo de tareas relacionadas.
     */
    @Query("SELECT COUNT(*) FROM task_tag_cross_ref WHERE tagId = :tagId")
    fun getTaskCountForTag(tagId: Long): LiveData<Int>
}
