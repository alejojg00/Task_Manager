/**
 * Archivo: TaskDao.kt
 * Define las operaciones DAO (Data Access Object) para la gestión
 * de tareas dentro de la base de datos Room.
 *
 * Permite realizar operaciones CRUD sobre la entidad [Task],
 * así como consultas avanzadas filtradas por estado, categoría,
 * prioridad, fecha de vencimiento o recordatorios.
 */

package com.ecci.taskmanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.data.model.Priority

/**
 * Interfaz que define las operaciones de acceso y manipulación de datos
 * para las tareas ([Task]) en la base de datos Room.
 *
 * A través de anotaciones como @Insert, @Update, @Delete y @Query,
 * se gestionan las operaciones de inserción, actualización, eliminación
 * y consulta.
 *
 * Las tareas almacenan información como su estado ([TaskStatus]),
 * prioridad ([Priority]), fechas de creación y vencimiento, y recordatorios.
 */
@Dao
interface TaskDao {

    /**
     * Inserta una nueva tarea en la base de datos.
     * Si ya existe una con el mismo ID, será reemplazada.
     *
     * @param task Objeto [Task] que se desea insertar.
     * @return El identificador único generado o reemplazado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    /**
     * Inserta una lista de tareas en la base de datos.
     *
     * @param tasks Lista de objetos [Task] que se desean insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    /**
     * Obtiene todas las tareas almacenadas, ordenadas por fecha de creación descendente.
     *
     * @return [LiveData] que contiene la lista de todas las tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): LiveData<List<Task>>

    /**
     * Busca una tarea específica por su ID.
     *
     * @param taskId Identificador único de la tarea.
     * @return El objeto [Task] correspondiente, o `null` si no existe.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    /**
     * Obtiene una tarea específica por su ID, con actualización en tiempo real.
     *
     * @param taskId Identificador de la tarea.
     * @return [LiveData] que contiene la tarea o `null` si no existe.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskByIdLive(taskId: Long): LiveData<Task?>

    /**
     * Obtiene todas las tareas filtradas por su estado.
     *
     * @param status Estado de la tarea (PENDING, COMPLETED, etc.).
     * @return [LiveData] con la lista de tareas filtradas.
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>>

    /**
     * Obtiene las tareas pertenecientes a una categoría específica.
     *
     * @param categoryId ID de la categoría.
     * @return [LiveData] con las tareas filtradas por categoría.
     */
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getTasksByCategory(categoryId: Long): LiveData<List<Task>>

    /**
     * Obtiene las tareas según su nivel de prioridad.
     *
     * @param priority Nivel de prioridad (ALTA, MEDIA, BAJA).
     * @return [LiveData] con las tareas correspondientes.
     */
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    fun getTasksByPriority(priority: Priority): LiveData<List<Task>>

    /**
     * Busca tareas cuyo título o descripción contengan una palabra clave.
     *
     * @param searchQuery Texto de búsqueda.
     * @return [LiveData] con las tareas que coinciden con la búsqueda.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :searchQuery || '%' 
        OR description LIKE '%' || :searchQuery || '%'
        ORDER BY createdAt DESC
    """)
    fun searchTasks(searchQuery: String): LiveData<List<Task>>

    /**
     * Obtiene las tareas pendientes ordenadas por fecha de vencimiento
     * (más próximas primero) y prioridad descendente.
     *
     * @return [LiveData] con la lista de tareas pendientes.
     */
    @Query("SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY dueDate ASC, priority DESC")
    fun getPendingTasks(): LiveData<List<Task>>

    /**
     * Obtiene las tareas completadas, ordenadas por fecha de finalización.
     *
     * @return [LiveData] con la lista de tareas completadas.
     */
    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): LiveData<List<Task>>

    /**
     * Obtiene las tareas vencidas que aún no se han completado.
     *
     * @param currentDate Fecha actual en milisegundos.
     * @return [LiveData] con la lista de tareas vencidas.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE status != 'COMPLETED' 
        AND dueDate < :currentDate 
        ORDER BY dueDate ASC
    """)
    fun getOverdueTasks(currentDate: Long = System.currentTimeMillis()): LiveData<List<Task>>

    /**
     * Obtiene las tareas programadas para el día actual.
     *
     * @param today Fecha actual en milisegundos.
     * @return [LiveData] con las tareas del día.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE date(dueDate/1000, 'unixepoch') = date(:today/1000, 'unixepoch')
        AND status != 'COMPLETED'
        ORDER BY priority DESC
    """)
    fun getTodayTasks(today: Long = System.currentTimeMillis()): LiveData<List<Task>>

    /**
     * Obtiene las tareas con recordatorios activos y no completadas.
     *
     * @return [LiveData] con las tareas que tienen recordatorio.
     */
    @Query("SELECT * FROM tasks WHERE hasReminder = 1 AND status != 'COMPLETED'")
    fun getTasksWithReminder(): LiveData<List<Task>>

    /**
     * Actualiza los datos de una tarea específica.
     *
     * @param task Objeto [Task] con la información actualizada.
     */
    @Update
    suspend fun update(task: Task)

    /**
     * Actualiza el estado de una tarea según su ID.
     *
     * @param taskId Identificador de la tarea.
     * @param status Nuevo estado de la tarea.
     */
    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus)

    /**
     * Marca una tarea como completada y actualiza la fecha de finalización.
     *
     * @param taskId Identificador de la tarea.
     * @param completedAt Fecha de finalización en milisegundos.
     */
    @Query("""
        UPDATE tasks 
        SET status = 'COMPLETED', completedAt = :completedAt 
        WHERE id = :taskId
    """)
    suspend fun markAsCompleted(taskId: Long, completedAt: Long = System.currentTimeMillis())

    /**
     * Actualiza el estado del recordatorio de una tarea.
     *
     * @param taskId ID de la tarea.
     * @param hasReminder Indica si la tarea tiene recordatorio activo.
     * @param reminderTime Hora programada del recordatorio (puede ser nula).
     */
    @Query("""
        UPDATE tasks 
        SET hasReminder = :hasReminder, reminderTime = :reminderTime 
        WHERE id = :taskId
    """)
    suspend fun updateReminder(taskId: Long, hasReminder: Boolean, reminderTime: Long?)

    /**
     * Elimina una tarea específica de la base de datos.
     *
     * @param task Objeto [Task] que se desea eliminar.
     */
    @Delete
    suspend fun delete(task: Task)

    /**
     * Elimina una tarea por su ID.
     *
     * @param taskId Identificador de la tarea a eliminar.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    /**
     * Elimina todas las tareas que ya fueron completadas.
     */
    @Query("DELETE FROM tasks WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedTasks()

    /**
     * Elimina todas las tareas existentes en la base de datos.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    /**
     * Obtiene el número total de tareas registradas.
     *
     * @return [LiveData] con el conteo total.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTasksCount(): LiveData<Int>

    /**
     * Obtiene la cantidad de tareas completadas.
     *
     * @return [LiveData] con el conteo de tareas completadas.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED'")
    fun getCompletedTasksCount(): LiveData<Int>

    /**
     * Obtiene la cantidad de tareas pendientes.
     *
     * @return [LiveData] con el conteo de tareas pendientes.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'PENDING'")
    fun getPendingTasksCount(): LiveData<Int>

    /**
     * Obtiene la cantidad de tareas vencidas no completadas.
     *
     * @param currentDate Fecha actual en milisegundos.
     * @return [LiveData] con el conteo de tareas vencidas.
     */
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE status != 'COMPLETED' 
        AND dueDate < :currentDate
    """)
    fun getOverdueTasksCount(currentDate: Long = System.currentTimeMillis()): LiveData<Int>

    /**
     * Obtiene las tareas completadas dentro de un rango de fechas.
     *
     * @param startDate Fecha de inicio del rango en milisegundos.
     * @param endDate Fecha de finalización del rango en milisegundos.
     * @return [LiveData] con la lista de tareas completadas en ese período.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE status = 'COMPLETED' 
        AND completedAt BETWEEN :startDate AND :endDate
        ORDER BY completedAt DESC
    """)
    fun getTasksCompletedInRange(startDate: Long, endDate: Long): LiveData<List<Task>>

    /**
     * Clase interna que define los convertidores para transformar
     * los valores del enumerado [TaskStatus] en cadenas y viceversa,
     * permitiendo su almacenamiento en la base de datos.
     */
    class TaskStatusConverter {

        /**
         * Convierte un valor de tipo [TaskStatus] a una cadena.
         *
         * @param status Estado de la tarea.
         * @return Representación en texto del estado.
         */
        @TypeConverter
        fun fromStatus(status: TaskStatus): String = status.name

        /**
         * Convierte una cadena de texto al tipo [TaskStatus].
         *
         * @param value Texto que representa un estado.
         * @return Valor correspondiente del enumerado [TaskStatus].
         */
        @TypeConverter
        fun toStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
    }
}
