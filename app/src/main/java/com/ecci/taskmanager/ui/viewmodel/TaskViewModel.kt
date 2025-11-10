package com.ecci.taskmanager.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.data.model.Priority
import com.ecci.taskmanager.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val allTasks: LiveData<List<Task>> = taskRepository.allTasks
    val pendingTasks: LiveData<List<Task>> = taskRepository.pendingTasks
    val completedTasks: LiveData<List<Task>> = taskRepository.completedTasks
    val overdueTasks: LiveData<List<Task>> = taskRepository.overdueTasks
    val todayTasks: LiveData<List<Task>> = taskRepository.todayTasks

    val totalTasksCount: LiveData<Int> = taskRepository.totalTasksCount
    val completedTasksCount: LiveData<Int> = taskRepository.completedTasksCount
    val pendingTasksCount: LiveData<Int> = taskRepository.pendingTasksCount
    val overdueTasksCount: LiveData<Int> = taskRepository.overdueTasksCount

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> = _selectedTask

    private val _activeFilter = MutableLiveData<TaskFilter>(TaskFilter.ALL)
    val activeFilter: LiveData<TaskFilter> = _activeFilter

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Task>>()
    val searchResults: LiveData<List<Task>> = _searchResults

    fun createTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = taskRepository.insertTask(task)

            result.onSuccess {
                _successMessage.value = "Tarea creada exitosamente"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al crear la tarea"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun getTaskById(taskId: Long): LiveData<Task?> {
        return taskRepository.getTaskByIdLive(taskId)
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = taskRepository.updateTask(task)

            result.onSuccess {
                _successMessage.value = "Tarea actualizada exitosamente"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al actualizar la tarea"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = taskRepository.deleteTask(task)

            result.onSuccess {
                _successMessage.value = "Tarea eliminada"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al eliminar la tarea"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun deleteTaskById(taskId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = taskRepository.deleteTaskById(taskId)

            result.onSuccess {
                _successMessage.value = "Tarea eliminada"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al eliminar la tarea"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val result = taskRepository.toggleTaskCompletion(task)

            result.onSuccess {
                val message = if (task.status == TaskStatus.COMPLETED) {
                    "Tarea marcada como pendiente"
                } else {
                    "Tarea completada!"
                }
                _successMessage.value = message
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al actualizar la tarea"
            }
        }
    }

    fun updateTaskReminder(taskId: Long, hasReminder: Boolean, reminderTime: Long?) {
        viewModelScope.launch {
            val result = taskRepository.updateTaskReminder(taskId, hasReminder, reminderTime)

            result.onSuccess {
                _successMessage.value = "Recordatorio configurado"
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al configurar recordatorio"
            }
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = taskRepository.deleteCompletedTasks()

            result.onSuccess {
                _successMessage.value = "Tareas completadas eliminadas"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al eliminar tareas"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun applyFilter(filter: TaskFilter) {
        _activeFilter.value = filter
    }

    fun getFilteredTasks(): LiveData<List<Task>> {
        return when (_activeFilter.value) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> pendingTasks
            TaskFilter.COMPLETED -> completedTasks
            TaskFilter.OVERDUE -> overdueTasks
            TaskFilter.TODAY -> todayTasks
            else -> allTasks
        }
    }

    fun searchTasks(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            taskRepository.searchTasks(query).observeForever { results ->
                _searchResults.value = results
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun getTasksByCategory(categoryId: Long): LiveData<List<Task>> {
        return taskRepository.getTasksByCategory(categoryId)
    }

    fun getTasksByPriority(priority: Priority): LiveData<List<Task>> {
        return taskRepository.getTasksByPriority(priority)
    }

    fun selectTask(task: Task) {
        _selectedTask.value = task
    }

    fun clearSelectedTask() {
        _selectedTask.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun updateOverdueTasks() {
        viewModelScope.launch {
            taskRepository.updateOverdueTasks()
        }
    }

    suspend fun getCompletionPercentage(): Float {
        return taskRepository.getCompletionPercentage()
    }
}

enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED,
    OVERDUE,
    TODAY
}