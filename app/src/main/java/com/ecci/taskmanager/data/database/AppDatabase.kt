package com.ecci.taskmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ecci.taskmanager.data.converters.DateConverter
import com.ecci.taskmanager.data.dao.CategoryDao
import com.ecci.taskmanager.data.dao.TagDao
import com.ecci.taskmanager.data.dao.TaskDao
import com.ecci.taskmanager.data.model.Category
import com.ecci.taskmanager.data.model.Tag
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskTagCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Task::class,
        Category::class,
        Tag::class,
        TaskTagCrossRef::class
    ],
    version = 2, // ← CAMBIAR DE 1 A 2
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_manager_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // ← AGREGAR ESTA LÍNEA
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val categoryDao = database.categoryDao()

            // Verificar si ya existen categorías
            val existingCategories = categoryDao.getAllCategories()

            // Si no hay categorías, insertar las predefinidas
            val predefinedCategories = Category.getPredefinedCategories()
            categoryDao.insertAll(predefinedCategories)

            // Insertar etiquetas de ejemplo
            val tagDao = database.tagDao()
            val exampleTags = listOf(
                Tag(name = "Urgente", color = "#F44336"),
                Tag(name = "Importante", color = "#FF9800"),
                Tag(name = "Reunion", color = "#2196F3"),
                Tag(name = "Proyecto", color = "#4CAF50")
            )
            tagDao.insertTags(exampleTags)
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}