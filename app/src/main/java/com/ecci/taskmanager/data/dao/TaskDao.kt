package com.ecci.taskmanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.data.model.Priority

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskByIdLive(taskId: Long): LiveData<Task?>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getTasksByCategory(categoryId: Long): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    fun getTasksByPriority(priority: Priority): LiveData<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :searchQuery || '%' 
        OR description LIKE '%' || :searchQuery || '%'
        ORDER BY createdAt DESC
    """)
    fun searchTasks(searchQuery: String): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY dueDate ASC, priority DESC")
    fun getPendingTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): LiveData<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE status != 'COMPLETED' 
        AND dueDate < :currentDate 
        ORDER BY dueDate ASC
    """)
    fun getOverdueTasks(currentDate: Long = System.currentTimeMillis()): LiveData<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE date(dueDate/1000, 'unixepoch') = date(:today/1000, 'unixepoch')
        AND status != 'COMPLETED'
        ORDER BY priority DESC
    """)
    fun getTodayTasks(today: Long = System.currentTimeMillis()): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE hasReminder = 1 AND status != 'COMPLETED'")
    fun getTasksWithReminder(): LiveData<List<Task>>

    @Update
    suspend fun update(task: Task)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus)

    @Query("""
        UPDATE tasks 
        SET status = 'COMPLETED', completedAt = :completedAt 
        WHERE id = :taskId
    """)
    suspend fun markAsCompleted(taskId: Long, completedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE tasks 
        SET hasReminder = :hasReminder, reminderTime = :reminderTime 
        WHERE id = :taskId
    """)
    suspend fun updateReminder(taskId: Long, hasReminder: Boolean, reminderTime: Long?)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("DELETE FROM tasks WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedTasks()

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTasksCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED'")
    fun getCompletedTasksCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'PENDING'")
    fun getPendingTasksCount(): LiveData<Int>

    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE status != 'COMPLETED' 
        AND dueDate < :currentDate
    """)
    fun getOverdueTasksCount(currentDate: Long = System.currentTimeMillis()): LiveData<Int>

    @Query("""
        SELECT * FROM tasks 
        WHERE status = 'COMPLETED' 
        AND completedAt BETWEEN :startDate AND :endDate
        ORDER BY completedAt DESC
    """)
    fun getTasksCompletedInRange(startDate: Long, endDate: Long): LiveData<List<Task>>
    class TaskStatusConverter {
        @TypeConverter
        fun fromStatus(status: TaskStatus): String = status.name

        @TypeConverter
        fun toStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
    }

}