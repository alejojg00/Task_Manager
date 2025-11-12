package com.ecci.taskmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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

/**
 * Clase principal que representa la base de datos local de la aplicación TaskManager.
 *
 * Se implementa utilizando la librería Room, la cual simplifica el manejo de bases de datos SQLite.
 * Aquí se definen las entidades, los DAO y los conversores utilizados para el almacenamiento y
 * recuperación de datos.
 *
 * @Database define las entidades que formarán las tablas, la versión de la base de datos y si
 * se exportará el esquema.
 */
@Database(
    entities = [
        Task::class,
        Category::class,
        Tag::class,
        TaskTagCrossRef::class
    ],
    version = 2, // Versión actual de la base de datos (incrementar en caso de cambios estructurales)
    exportSchema = false
)
@TypeConverters(Converters::class) // Conversor para manejar enums TaskStatus y Priority
abstract class AppDatabase : RoomDatabase() {

    /** DAO para la gestión de tareas (Task). */
    abstract fun taskDao(): TaskDao

    /** DAO para la gestión de categorías (Category). */
    abstract fun categoryDao(): CategoryDao

    /** DAO para la gestión de etiquetas (Tag). */
    abstract fun tagDao(): TagDao

    companion object {

        /** Instancia única de la base de datos (Singleton). */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene una instancia única de la base de datos.
         *
         * Si la base de datos aún no existe, se crea utilizando Room.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_manager_database" // Nombre del archivo físico de la base de datos
                )
                    .addCallback(DatabaseCallback()) // Inicializa datos al crear la BD
                    .fallbackToDestructiveMigration() // Elimina y recrea la BD si hay cambios sin migración definida
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback que se ejecuta cuando la base de datos es creada por primera vez.
         * Permite inicializar datos predeterminados.
         */
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

        /**
         * Inserta datos iniciales en la base de datos cuando se crea por primera vez.
         *
         * Se agregan categorías predefinidas y etiquetas de ejemplo para facilitar
         * la experiencia inicial del usuario.
         *
         * @param database Instancia de la base de datos.
         */
        private suspend fun populateDatabase(database: AppDatabase) {
            val categoryDao = database.categoryDao()

            // Verificar si ya existen categorías
            val existingCategories = categoryDao.getAllCategories()

            // Insertar categorías predefinidas si no existen
            val predefinedCategories = Category.getPredefinedCategories()
            categoryDao.insertAll(predefinedCategories)

            // Insertar etiquetas de ejemplo
            val tagDao = database.tagDao()
            val exampleTags = listOf(
                Tag(name = "Urgente", color = "#F44336"),
                Tag(name = "Importante", color = "#FF9800"),
                Tag(name = "Reunión", color = "#2196F3"),
                Tag(name = "Proyecto", color = "#4CAF50")
            )
            tagDao.insertTags(exampleTags)
        }

        /**
         * Destruye la instancia actual de la base de datos.
         *
         * Útil en pruebas o reinicios controlados.
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}