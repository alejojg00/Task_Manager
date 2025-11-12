package com.ecci.taskmanager.di

import android.content.Context
import com.ecci.taskmanager.data.database.AppDatabase
import com.ecci.taskmanager.data.dao.CategoryDao
import com.ecci.taskmanager.data.dao.TagDao
import com.ecci.taskmanager.data.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de dependencias de la aplicación.
 *
 * Este archivo utiliza **Dagger Hilt**, un framework de inyección de dependencias
 * que simplifica la gestión de instancias en Android.
 *
 * Su objetivo es proporcionar (a través de anotaciones) los objetos y dependencias
 * que se utilizarán en toda la aplicación, garantizando que existan instancias únicas
 * y centralizadas para los componentes principales, como la base de datos y los DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Proporciona una instancia única de la base de datos Room.
     *
     * La anotación @Singleton garantiza que solo se cree una instancia
     * de la base de datos durante todo el ciclo de vida de la aplicación.
     *
     * @param context Contexto de la aplicación (inyectado automáticamente por Hilt)
     * @return Objeto de tipo AppDatabase
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * Proporciona el acceso al DAO (Data Access Object) de tareas.
     *
     * Los DAOs permiten realizar operaciones CRUD (crear, leer, actualizar, eliminar)
     * sobre las tablas correspondientes en la base de datos Room.
     *
     * @param database Instancia de AppDatabase
     * @return Objeto de tipo TaskDao
     */
    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    /**
     * Proporciona el DAO de categorías.
     * Permite gestionar las categorías asociadas a las tareas.
     *
     * @param database Instancia de AppDatabase
     * @return Objeto de tipo CategoryDao
     */
    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    /**
     * Proporciona el DAO de etiquetas (tags).
     * Permite gestionar las etiquetas asociadas a las tareas.
     *
     * @param database Instancia de AppDatabase
     * @return Objeto de tipo TagDao
     */
    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }
}
