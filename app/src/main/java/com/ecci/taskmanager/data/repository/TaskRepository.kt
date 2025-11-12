package com.ecci.taskmanager.data.repository

import androidx.lifecycle.LiveData
import com.ecci.taskmanager.data.dao.TaskDao
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.data.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio responsable de gestionar la lógica de acceso a datos
 * relacionada con las tareas del sistema (Task).
 *
 * Su función principal es actuar como intermediario entre la capa
 * de datos (DAO) y la capa de presentación (ViewModel o UI),
 * proporcionando métodos seguros y estructurados para interactuar
 * con la base de datos Room.
 *
 * Se utiliza la anotación @Singleton para garantizar que solo exista
 * una instancia del repositorio durante todo el ciclo de vida de la aplicación.
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    // --- LiveData para observar diferentes tipos de tareas ---
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: LiveData<List<Task>> = taskDao.getPendingTasks()
    val completedTasks: LiveData<List<Task>> = taskDao.getCompletedTasks()
    val overdueTasks: LiveData<List<Task>> = taskDao.getOverdueTasks()
    val todayTasks: LiveData<List<Task>> = taskDao.getTodayTasks()
    val tasksWithReminder: LiveData<List<Task>> = taskDao.getTasksWithReminder()

    // --- Contadores observables ---
    val totalTasksCount: LiveData<Int> = taskDao.getTotalTasksCount()
    val completedTasksCount: LiveData<Int> = taskDao.getCompletedTasksCount()
    val pendingTasksCount: LiveData<Int> = taskDao.getPendingTasksCount()
    val overdueTasksCount: LiveData<Int> = taskDao.getOverdueTasksCount()

    /**
     * Inserta una nueva tarea en la base de datos.
     * Valida que el título no esté vacío antes de insertar.
     */
    suspend fun insertTask(task: Task): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!task.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El título de la tarea no puede estar vacío")
                )
            }

            val taskId = taskDao.insert(task)
            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inserta una lista de tareas, validando los datos de cada una.
     */
    suspend fun insertTasks(tasks: List<Task>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tasks.forEach { task ->
                if (!task.isValid()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Una o más tareas tienen datos inválidos")
                    )
                }
            }

            taskDao.insertAll(tasks)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene una tarea por su ID (versión suspendida).
     */
    suspend fun getTaskById(taskId: Long): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(taskId)
    }

    /**
     * Obtiene una tarea por su ID en forma de LiveData (para observación en la UI).
     */
    fun getTaskByIdLive(taskId: Long): LiveData<Task?> {
        return taskDao.getTaskByIdLive(taskId)
    }

    /**
     * Filtra las tareas según su estado (pendiente, completada, vencida).
     */
    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }

    /**
     * Obtiene todas las tareas pertenecientes a una categoría específica.
     */
    fun getTasksByCategory(categoryId: Long): LiveData<List<Task>> {
        return taskDao.getTasksByCategory(categoryId)
    }

    /**
     * Filtra las tareas por su prioridad (Alta, Media o Baja).
     */
    fun getTasksByPriority(priority: Priority): LiveData<List<Task>> {
        return taskDao.getTasksByPriority(priority)
    }

    /**
     * Busca tareas que coincidan con un texto ingresado por el usuario.
     */
    fun searchTasks(query: String): LiveData<List<Task>> {
        return taskDao.searchTasks(query)
    }

    /**
     * Obtiene las tareas completadas dentro de un rango de fechas específico.
     */
    fun getTasksCompletedInRange(startDate: Long, endDate: Long): LiveData<List<Task>> {
        return taskDao.getTasksCompletedInRange(startDate, endDate)
    }

    /**
     * Actualiza los datos de una tarea existente, validando su información.
     */
    suspend fun updateTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!task.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Los datos de la tarea no son válidos")
                )
            }

            taskDao.update(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza únicamente el estado de una tarea (por ejemplo, de pendiente a completada).
     */
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                taskDao.updateTaskStatus(taskId, status)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Marca una tarea como completada y actualiza su fecha de finalización.
     */
    suspend fun markTaskAsCompleted(taskId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.markAsCompleted(taskId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Alterna el estado de una tarea entre completada y pendiente.
     */
    suspend fun toggleTaskCompletion(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updatedTask = if (task.status == TaskStatus.COMPLETED) {
                task.copy(status = TaskStatus.PENDING, completedAt = null)
            } else {
                task.markAsCompleted()
            }

            taskDao.update(updatedTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza los recordatorios asociados a una tarea.
     */
    suspend fun updateTaskReminder(
        taskId: Long,
        hasReminder: Boolean,
        reminderTime: Long?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.updateReminder(taskId, hasReminder, reminderTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una tarea específica de la base de datos.
     */
    suspend fun deleteTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.delete(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una tarea a partir de su ID.
     */
    suspend fun deleteTaskById(taskId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.deleteById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina todas las tareas que ya han sido completadas.
     */
    suspend fun deleteCompletedTasks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.deleteCompletedTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina todas las tareas de la base de datos.
     */
    suspend fun deleteAllTasks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.deleteAllTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de las tareas vencidas (las que superaron su fecha límite).
     */
    suspend fun updateOverdueTasks(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val tasks = taskDao.getAllTasks().value ?: emptyList()

            var updatedCount = 0
            tasks.forEach { task ->
                if (task.isOverdue() && task.status == TaskStatus.PENDING) {
                    taskDao.updateTaskStatus(task.id, TaskStatus.OVERDUE)
                    updatedCount++
                }
            }

            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcula el porcentaje de tareas completadas sobre el total.
     * Retorna 0 si no hay tareas.
     */
    suspend fun getCompletionPercentage(): Float = withContext(Dispatchers.IO) {
        try {
            val total = totalTasksCount.value ?: 0
            val completed = completedTasksCount.value ?: 0

            if (total == 0) 0f else (completed.toFloat() / total.toFloat()) * 100f
        } catch (e: Exception) {
            0f
        }
    }
}
