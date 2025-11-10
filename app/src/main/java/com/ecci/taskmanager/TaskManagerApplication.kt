package com.ecci.taskmanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TaskManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar Timber para logging
        Timber.plant(Timber.DebugTree())

        Timber.d("TaskManager Application iniciada")
    }
}