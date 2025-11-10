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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }
}