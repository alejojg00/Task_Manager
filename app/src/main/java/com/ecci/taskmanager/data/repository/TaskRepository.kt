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

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: LiveData<List<Task>> = taskDao.getPendingTasks()
    val completedTasks: LiveData<List<Task>> = taskDao.getCompletedTasks()
    val overdueTasks: LiveData<List<Task>> = taskDao.getOverdueTasks()
    val todayTasks: LiveData<List<Task>> = taskDao.getTodayTasks()
    val tasksWithReminder: LiveData<List<Task>> = taskDao.getTasksWithReminder()

    val totalTasksCount: LiveData<Int> = taskDao.getTotalTasksCount()
    val completedTasksCount: LiveData<Int> = taskDao.getCompletedTasksCount()
    val pendingTasksCount: LiveData<Int> = taskDao.getPendingTasksCount()
    val overdueTasksCount: LiveData<Int> = taskDao.getOverdueTasksCount()

    suspend fun insertTask(task: Task): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!task.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El titulo de la tarea no puede estar vacio")
                )
            }

            val taskId = taskDao.insert(task)
            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertTasks(tasks: List<Task>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tasks.forEach { task ->
                if (!task.isValid()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Una o mas tareas tienen datos invalidos")
                    )
                }
            }

            taskDao.insertAll(tasks)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTaskById(taskId: Long): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(taskId)
    }

    fun getTaskByIdLive(taskId: Long): LiveData<Task?> {
        return taskDao.getTaskByIdLive(taskId)
    }

    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }

    fun getTasksByCategory(categoryId: Long): LiveData<List<Task>> {
        return taskDao.getTasksByCategory(categoryId)
    }

    fun getTasksByPriority(priority: Priority): LiveData<List<Task>> {
        return taskDao.getTasksByPriority(priority)
    }

    fun searchTasks(query: String): LiveData<List<Task>> {
        return taskDao.searchTasks(query)
    }

    fun getTasksCompletedInRange(startDate: Long, endDate: Long): LiveData<List<Task>> {
        return taskDao.getTasksCompletedInRange(startDate, endDate)
    }

    suspend fun updateTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!task.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Los datos de la tarea no son validos")
                )
            }

            taskDao.update(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                taskDao.updateTaskStatus(taskId, status)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun markTaskAsCompleted(taskId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.markAsCompleted(taskId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun deleteTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.delete(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTaskById(taskId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.deleteById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCompletedTasks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.deleteCompletedTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllTasks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.deleteAllTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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