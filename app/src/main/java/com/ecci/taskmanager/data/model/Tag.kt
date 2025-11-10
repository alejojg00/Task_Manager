package com.ecci.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val color: String = "#424242"
) {
    fun isValid(): Boolean {
        return name.trim().isNotEmpty()
    }
}

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long
)