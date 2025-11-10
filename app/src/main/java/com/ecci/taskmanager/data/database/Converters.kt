package com.ecci.taskmanager.data.database

import androidx.room.TypeConverter
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.data.model.Priority

class Converters {

    // --- TaskStatus ---
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    // --- Priority ---
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
}

