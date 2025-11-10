package com.ecci.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val color: String = "#1976D2",

    val icon: String = "folder",

    val isPredefined: Boolean = false
) {
    companion object {
        fun getPredefinedCategories(): List<Category> {
            return listOf(
                Category(
                    id = 1,
                    name = "Trabajo",
                    color = "#1976D2",
                    icon = "work",
                    isPredefined = true
                ),
                Category(
                    id = 2,
                    name = "Personal",
                    color = "#4CAF50",
                    icon = "person",
                    isPredefined = true
                ),
                Category(
                    id = 3,
                    name = "Estudios",
                    color = "#FF9800",
                    icon = "school",
                    isPredefined = true
                )
            )
        }
    }

    fun isValid(): Boolean {
        return name.trim().isNotEmpty()
    }
}