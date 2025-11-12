package com.ecci.taskmanager.data.repository

import androidx.lifecycle.LiveData
import com.ecci.taskmanager.data.dao.TagDao
import com.ecci.taskmanager.data.model.Tag
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskTagCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio responsable de gestionar las operaciones relacionadas con las etiquetas (tags)
 * dentro de la aplicación.
 *
 * Actúa como una capa intermedia entre la base de datos (DAO) y la capa de presentación,
 * implementando el patrón **Repository**, que promueve una arquitectura limpia y desacoplada.
 *
 * Todas las operaciones de acceso a datos se ejecutan en un contexto de entrada/salida (IO)
 * utilizando **corrutinas** para evitar bloqueos en el hilo principal.
 *
 * @property tagDao Objeto de acceso a datos (DAO) inyectado mediante Hilt para manipular las etiquetas.
 */
@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {

    /** Lista reactiva con todas las etiquetas almacenadas en la base de datos. */
    val allTags: LiveData<List<Tag>> = tagDao.getAllTags()

    /**
     * Inserta una nueva etiqueta en la base de datos, validando su nombre antes de hacerlo.
     *
     * @param tag Objeto [Tag] que se desea insertar.
     * @return [Result] que contiene el ID de la etiqueta creada en caso de éxito o una excepción en caso de error.
     */
    suspend fun insertTag(tag: Tag): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!tag.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El nombre de la etiqueta no puede estar vacío")
                )
            }

            val tagId = tagDao.insertTag(tag)
            Result.success(tagId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inserta una lista de etiquetas en la base de datos.
     *
     * Esta función se utiliza principalmente al inicializar datos o importar múltiples etiquetas.
     *
     * @param tags Lista de objetos [Tag] a insertar.
     * @return [Result] indicando el éxito o fallo de la operación.
     */
    suspend fun insertTags(tags: List<Tag>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tagDao.insertTags(tags)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene una etiqueta específica a partir de su identificador único.
     *
     * @param tagId ID de la etiqueta a consultar.
     * @return Objeto [Tag] si se encuentra, o `null` si no existe.
     */
    suspend fun getTagById(tagId: Long): Tag? = withContext(Dispatchers.IO) {
        tagDao.getTagById(tagId)
    }

    /**
     * Actualiza los datos de una etiqueta existente.
     *
     * Valida que el nombre sea válido antes de realizar la actualización.
     *
     * @param tag Objeto [Tag] con los nuevos valores.
     * @return [Result] que indica el resultado de la operación.
     */
    suspend fun updateTag(tag: Tag): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!tag.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Los datos de la etiqueta no son válidos")
                )
            }

            tagDao.updateTag(tag)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una etiqueta específica de la base de datos.
     *
     * @param tag Etiqueta que se desea eliminar.
     * @return [Result] que indica el éxito o error de la operación.
     */
    suspend fun deleteTag(tag: Tag): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tagDao.deleteTag(tag)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una etiqueta de la base de datos a partir de su identificador.
     *
     * @param tagId ID de la etiqueta a eliminar.
     * @return [Result] que indica si la operación fue exitosa o fallida.
     */
    suspend fun deleteTagById(tagId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tagDao.deleteTagById(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Asocia una etiqueta con una tarea mediante una relación cruzada (N:M).
     *
     * @param taskId ID de la tarea.
     * @param tagId ID de la etiqueta.
     * @return [Result] que indica el resultado de la operación.
     */
    suspend fun addTagToTask(taskId: Long, tagId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val crossRef = TaskTagCrossRef(taskId, tagId)
                tagDao.insertTaskTagCrossRef(crossRef)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Elimina la relación entre una tarea y una etiqueta específica.
     *
     * @param taskId ID de la tarea.
     * @param tagId ID de la etiqueta.
     * @return [Result] que indica si la operación fue exitosa o fallida.
     */
    suspend fun removeTagFromTask(taskId: Long, tagId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val crossRef = TaskTagCrossRef(taskId, tagId)
                tagDao.deleteTaskTagCrossRef(crossRef)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Actualiza todas las etiquetas asociadas a una tarea.
     *
     * Elimina las relaciones existentes y crea nuevas según la lista de IDs recibida.
     *
     * @param taskId ID de la tarea.
     * @param tagIds Lista de IDs de etiquetas que deben quedar asociadas.
     * @return [Result] indicando éxito o error.
     */
    suspend fun updateTaskTags(taskId: Long, tagIds: List<Long>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tagDao.deleteAllTagsFromTask(taskId)

                tagIds.forEach { tagId ->
                    val crossRef = TaskTagCrossRef(taskId, tagId)
                    tagDao.insertTaskTagCrossRef(crossRef)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Obtiene todas las etiquetas asociadas a una tarea específica.
     *
     * @param taskId ID de la tarea.
     * @return [LiveData] con la lista de etiquetas relacionadas.
     */
    fun getTagsForTask(taskId: Long): LiveData<List<Tag>> {
        return tagDao.getTagsForTask(taskId)
    }

    /**
     * Obtiene todas las tareas asociadas a una etiqueta determinada.
     *
     * @param tagId ID de la etiqueta.
     * @return [LiveData] con la lista de tareas relacionadas.
     */
    fun getTasksWithTag(tagId: Long): LiveData<List<Task>> {
        return tagDao.getTasksWithTag(tagId)
    }

    /**
     * Obtiene la cantidad total de tareas vinculadas a una etiqueta específica.
     *
     * @param tagId ID de la etiqueta.
     * @return [LiveData] con el número total de tareas asociadas.
     */
    fun getTaskCountForTag(tagId: Long): LiveData<Int> {
        return tagDao.getTaskCountForTag(tagId)
    }

    /**
     * Elimina todas las etiquetas asociadas a una tarea específica.
     *
     * @param taskId ID de la tarea.
     * @return [Result] indicando si la eliminación fue exitosa.
     */
    suspend fun removeAllTagsFromTask(taskId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tagDao.deleteAllTagsFromTask(taskId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
