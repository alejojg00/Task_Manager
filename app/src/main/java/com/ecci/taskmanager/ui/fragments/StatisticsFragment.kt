package com.ecci.taskmanager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ecci.taskmanager.data.model.Priority
import com.ecci.taskmanager.databinding.FragmentStatisticsBinding
import com.ecci.taskmanager.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragmento encargado de mostrar las estadísticas generales del administrador de tareas.
 *
 * Este fragmento forma parte de la arquitectura **MVVM**, utilizando el [TaskViewModel]
 * como fuente de datos. Se encarga de observar diferentes métricas relacionadas con las tareas:
 *
 * - Total de tareas creadas.
 * - Tareas completadas y pendientes.
 * - Tareas vencidas y programadas para el día actual.
 * - Distribución de tareas según su prioridad (Alta, Media, Baja).
 * - Porcentaje de finalización general.
 *
 * El fragmento actualiza dinámicamente la interfaz de usuario mediante la observación de objetos **LiveData**
 * proporcionados por el ViewModel, garantizando así una UI reactiva y sincronizada con el estado de los datos.
 */
@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    /** Binding para acceder a los elementos del layout sin necesidad de usar findViewById. */
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    /** ViewModel que provee los datos y estadísticas de las tareas almacenadas en la base de datos. */
    private val viewModel: TaskViewModel by viewModels()

    /**
     * Infla la vista del fragmento y configura el objeto de binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta después de que la vista ha sido creada.
     * Aquí se inicializan las observaciones a los datos del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStatistics()
    }

    /**
     * Observa las distintas métricas del ViewModel y actualiza la interfaz de usuario.
     *
     * Cada LiveData observado representa un tipo de información estadística diferente.
     * A medida que cambian los datos en la base de datos, la UI se actualiza automáticamente.
     */
    private fun observeStatistics() {
        // Total de tareas creadas
        viewModel.totalTasksCount.observe(viewLifecycleOwner) { total ->
            binding.textTotalTasks.text = total.toString()
            updateCompletionRate()
        }

        // Tareas completadas
        viewModel.completedTasksCount.observe(viewLifecycleOwner) { completed ->
            binding.textCompletedTasks.text = completed.toString()
            updateCompletionRate()
        }

        // Tareas pendientes
        viewModel.pendingTasksCount.observe(viewLifecycleOwner) { pending ->
            binding.textPendingTasks.text = pending.toString()
        }

        // Tareas vencidas
        viewModel.overdueTasksCount.observe(viewLifecycleOwner) { overdue ->
            binding.textOverdueTasks.text = overdue.toString()
        }

        // Tareas programadas para el día actual
        viewModel.todayTasks.observe(viewLifecycleOwner) { todayTasks ->
            binding.textTodayTasks.text = todayTasks.size.toString()
        }

        // Conteo de tareas por prioridad
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val highCount = tasks.count { it.priority == Priority.HIGH }
            val mediumCount = tasks.count { it.priority == Priority.MEDIUM }
            val lowCount = tasks.count { it.priority == Priority.LOW }

            binding.textHighPriority.text = highCount.toString()
            binding.textMediumPriority.text = mediumCount.toString()
            binding.textLowPriority.text = lowCount.toString()
        }
    }

    /**
     * Calcula y actualiza el porcentaje de finalización de tareas.
     *
     * La tasa se determina como el cociente entre tareas completadas y el total de tareas,
     * expresada como porcentaje. Este valor se muestra en un componente **ProgressBar**
     * y un texto descriptivo en pantalla.
     */
    private fun updateCompletionRate() {
        val total = viewModel.totalTasksCount.value ?: 0
        val completed = viewModel.completedTasksCount.value ?: 0

        val percentage = if (total > 0) {
            (completed.toFloat() / total.toFloat() * 100).toInt()
        } else {
            0
        }

        binding.progressCompletion.progress = percentage
        binding.textCompletionRate.text = "$percentage%"
    }

    /**
     * Libera el binding para evitar fugas de memoria cuando la vista se destruye.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
