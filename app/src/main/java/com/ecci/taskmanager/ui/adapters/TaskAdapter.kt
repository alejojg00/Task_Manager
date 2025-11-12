package com.ecci.taskmanager.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecci.taskmanager.R
import com.ecci.taskmanager.data.model.Priority
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskStatus
import com.ecci.taskmanager.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptador para mostrar una lista de [Task] en un [RecyclerView].
 *
 * Este adaptador utiliza [ListAdapter] con [DiffUtil] para optimizar actualizaciones
 * en la lista y mejorar el rendimiento al renderizar elementos.
 *
 * Permite:
 * - Mostrar título, descripción, fecha de vencimiento y prioridad de cada tarea.
 * - Reflejar el estado de completado mediante tachado de texto y opacidad.
 * - Mostrar un ícono de recordatorio si la tarea tiene uno activo.
 * - Detectar clics y cambios en el estado de la tarea (checkbox).
 *
 * @param onTaskClick Callback que se ejecuta al hacer clic en una tarea.
 * @param onTaskCheckChanged Callback que se ejecuta cuando se marca o desmarca una tarea como completada.
 */
class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCheckChanged: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    /**
     * Crea un nuevo [TaskViewHolder] inflando el layout XML correspondiente al ítem de tarea.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    /**
     * Asigna los datos de una tarea específica al ViewHolder correspondiente.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder interno que representa una tarea individual en la lista.
     * Encapsula la lógica de presentación y vinculación de datos.
     */
    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de una [Task] a los elementos de la interfaz de usuario.
         *
         * Se encarga de:
         * - Mostrar título y descripción (oculta si está vacía).
         * - Manejar el estado del checkbox (completado o no).
         * - Aplicar efectos visuales según el estado.
         * - Mostrar prioridad, fecha de vencimiento y recordatorio.
         *
         * @param task Objeto [Task] que contiene la información de la tarea.
         */
        fun bind(task: Task) {
            binding.apply {
                // Título
                textTaskTitle.text = task.title

                // Descripción (oculta si está vacía)
                if (task.description.isNullOrBlank()) {
                    textTaskDescription.visibility = View.GONE
                } else {
                    textTaskDescription.visibility = View.VISIBLE
                    textTaskDescription.text = task.description
                }

                // Evitar llamadas dobles al listener del checkbox
                checkboxTask.setOnCheckedChangeListener(null)
                checkboxTask.isChecked = task.status == TaskStatus.COMPLETED
                checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                    onTaskCheckChanged(task, isChecked)
                }

                // Estilos visuales según el estado de la tarea
                if (task.status == TaskStatus.COMPLETED) {
                    textTaskTitle.paintFlags = textTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textTaskTitle.alpha = 0.5f
                    if (!task.description.isNullOrBlank()) {
                        textTaskDescription.alpha = 0.5f
                    }
                } else {
                    textTaskTitle.paintFlags = textTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textTaskTitle.alpha = 1.0f
                    if (!task.description.isNullOrBlank()) {
                        textTaskDescription.alpha = 1.0f
                    }
                }

                // Indicador de prioridad (color)
                setPriorityIndicator(task.priority)

                // Fecha de vencimiento (con color según estado)
                task.dueDate?.let { dueDate ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    textDueDate.text = dateFormat.format(dueDate)
                    textDueDate.visibility = View.VISIBLE

                    if (task.isOverdue()) {
                        textDueDate.setTextColor(
                            ContextCompat.getColor(root.context, R.color.error)
                        )
                    } else {
                        textDueDate.setTextColor(
                            ContextCompat.getColor(root.context, R.color.on_surface_variant)
                        )
                    }
                } ?: run {
                    textDueDate.visibility = View.GONE
                }

                // Mostrar ícono si tiene recordatorio activo
                iconReminder.visibility = if (task.hasReminder) View.VISIBLE else View.GONE

                // Clic sobre el elemento
                root.setOnClickListener {
                    onTaskClick(task)
                }
            }
        }

        /**
         * Cambia el color del indicador de prioridad según el nivel asignado.
         *
         * @param priority Nivel de prioridad de la tarea ([Priority.HIGH], [Priority.MEDIUM], [Priority.LOW]).
         */
        private fun setPriorityIndicator(priority: Priority) {
            val color = when (priority) {
                Priority.HIGH -> ContextCompat.getColor(binding.root.context, R.color.priority_high)
                Priority.MEDIUM -> ContextCompat.getColor(binding.root.context, R.color.priority_medium)
                Priority.LOW -> ContextCompat.getColor(binding.root.context, R.color.priority_low)
            }
            binding.priorityIndicator.setBackgroundColor(color)
        }
    }

    /**
     * Implementación de [DiffUtil.ItemCallback] para detectar eficientemente
     * los cambios entre listas de tareas y actualizar solo los elementos necesarios.
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        /** Compara si dos tareas representan el mismo elemento (por ID). */
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        /** Compara si el contenido completo de dos tareas es el mismo. */
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
