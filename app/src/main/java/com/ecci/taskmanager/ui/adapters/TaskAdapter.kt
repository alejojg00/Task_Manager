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

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCheckChanged: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                textTaskTitle.text = task.title

                if (task.description.isNullOrBlank()) {
                    textTaskDescription.visibility = View.GONE
                } else {
                    textTaskDescription.visibility = View.VISIBLE
                    textTaskDescription.text = task.description
                }

                // IMPORTANTE: Desactivar listener antes de cambiar el estado
                checkboxTask.setOnCheckedChangeListener(null)
                checkboxTask.isChecked = task.status == TaskStatus.COMPLETED
                checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                    onTaskCheckChanged(task, isChecked)
                }

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

                setPriorityIndicator(task.priority)

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

                if (task.hasReminder) {
                    iconReminder.visibility = View.VISIBLE
                } else {
                    iconReminder.visibility = View.GONE
                }

                root.setOnClickListener {
                    onTaskClick(task)
                }
            }
        }

        private fun setPriorityIndicator(priority: Priority) {
            val color = when (priority) {
                Priority.HIGH -> ContextCompat.getColor(binding.root.context, R.color.priority_high)
                Priority.MEDIUM -> ContextCompat.getColor(binding.root.context, R.color.priority_medium)
                Priority.LOW -> ContextCompat.getColor(binding.root.context, R.color.priority_low)
            }
            binding.priorityIndicator.setBackgroundColor(color)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}