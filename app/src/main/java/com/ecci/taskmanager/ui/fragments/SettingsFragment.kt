package com.ecci.taskmanager.ui.fragments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.*
import com.ecci.taskmanager.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val TAG = "SettingsFragment"
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        try {
            Log.d(TAG, "Creating preferences...")

            val context = preferenceManager.context
            val screen = preferenceManager.createPreferenceScreen(context)

            // ========== DATOS ==========
            val dataCategory = PreferenceCategory(context).apply {
                key = "data_category"
                title = "Datos"
            }
            screen.addPreference(dataCategory)

            // Limpiar caché
            val clearDataPreference = Preference(context).apply {
                key = "clear_data"
                title = "Limpiar todos los datos"
                summary = "⚠️ Eliminar todas las tareas permanentemente"
                setOnPreferenceClickListener {
                    showClearDataConfirmation()
                    true
                }
            }
            dataCategory.addPreference(clearDataPreference)

            // ========== ACERCA DE ==========
            val aboutCategory = PreferenceCategory(context).apply {
                key = "about_category"
                title = "Acerca de"
            }
            screen.addPreference(aboutCategory)

            val versionPreference = Preference(context).apply {
                key = "app_version"
                title = "Versión"
                summary = "1.0.0"
                isSelectable = false
            }
            aboutCategory.addPreference(versionPreference)

            preferenceScreen = screen

            Log.d(TAG, "Preferences created successfully!")

        } catch (e: Exception) {
            Log.e(TAG, "ERROR creating preferences", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showClearDataConfirmation() {
        try {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Confirmar limpieza")
                .setMessage("¿Estás seguro de que deseas eliminar todas las tareas? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    clearAllData()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                viewModel.deleteAllTasks()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "🗑️ Todas las tareas han sido eliminadas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting all tasks", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "❌ Error al eliminar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}