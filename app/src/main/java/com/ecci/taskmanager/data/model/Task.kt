package com.ecci.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ecci.taskmanager.data.converters.DateConverter
import java.util.Date
@Entity(tableName = "tasks")
@TypeConverters(DateConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val description: String? = null,

    val dueDate: Date? = null,

    val priority: Priority = Priority.MEDIUM,

    val status: TaskStatus = TaskStatus.PENDING,

    val categoryId: Long? = null,

    val createdAt: Date = Date(),

    val completedAt: Date? = null,

    val hasReminder: Boolean = false,

    val reminderTime: Date? = null,

    // NUEVAS PROPIEDADES PARA HORARIOS
    val isRecurring: Boolean = false,

    val recurringDays: String? = null, // "1,2,3" (Lunes, Martes, Miércoles)

    val startTime: String? = null, // "08:00"

    val endTime: String? = null // "10:00"
) {
    fun isValid(): Boolean {
        return title.trim().isNotEmpty()
    }

    fun isOverdue(): Boolean {
        return dueDate?.before(Date()) == true && status != TaskStatus.COMPLETED
    }

    fun markAsCompleted(): Task {
        return this.copy(
            status = TaskStatus.COMPLETED,
            completedAt = Date()
        )
    }
}

enum class Priority(val value: Int, val displayName: String) {
    HIGH(3, "Alta"),
    MEDIUM(2, "Media"),
    LOW(1, "Baja");

    companion object {
        fun fromValue(value: Int): Priority {
            return values().find { it.value == value } ?: MEDIUM
        }
    }
}

enum class TaskStatus(val value: String) {
    PENDING("pending"),
    COMPLETED("completed"),
    OVERDUE("overdue");

    companion object {
        fun fromValue(value: String): TaskStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}