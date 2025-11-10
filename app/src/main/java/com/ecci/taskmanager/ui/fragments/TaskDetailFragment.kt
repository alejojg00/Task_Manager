package com.ecci.taskmanager.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ecci.taskmanager.R
import com.ecci.taskmanager.data.model.Priority
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.databinding.FragmentTaskDetailBinding
import com.ecci.taskmanager.ui.viewmodel.CategoryViewModel
import com.ecci.taskmanager.ui.viewmodel.TaskViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private var selectedDueDate: Date? = null
    private var selectedReminderTime: Date? = null
    private var selectedCategoryId: Long? = null
    private var currentTask: Task? = null
    private var isEditMode = false
    private var taskId: Long = 0L

    private var selectedStartTime: String = "08:00"
    private var selectedEndTime: String = "10:00"
    private val selectedDays = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskId = arguments?.getLong("taskId") ?: 0L

        setupPrioritySpinner()
        setupCategorySpinner()
        setupDatePickers()
        setupButtons()
        setupRecurringSchedule()

        if (taskId != 0L) {
            isEditMode = true
            loadTaskData(taskId)
        }
    }

    private fun setupPrioritySpinner() {
        val priorities = Priority.values().map { it.displayName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            priorities
        )
        binding.spinnerPriority.setAdapter(adapter)

        // Seleccionar Media por defecto
        if (!isEditMode) {
            binding.spinnerPriority.setText(Priority.MEDIUM.displayName, false)
        }
    }
    private fun setupCategorySpinner() {
        // Forzar la carga de categorías
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            if (categories.isEmpty()) {
                // Si no hay categorías, no podemos hacer nada
                binding.spinnerCategory.isEnabled = false
                binding.spinnerCategory.hint = "Cargando categorías..."
                return@observe
            }

            // Habilitar el spinner
            binding.spinnerCategory.isEnabled = true

            // Crear lista de nombres
            val categoryNames = categories.map { it.name }

            // Crear y configurar el adapter
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
            )

            // Aplicar el adapter
            binding.spinnerCategory.setAdapter(adapter)

            // Forzar que muestre el primer elemento por defecto
            if (selectedCategoryId == null && !isEditMode) {
                val firstCategory = categories[0]
                selectedCategoryId = firstCategory.id
                binding.spinnerCategory.setText(firstCategory.name, false)
            }

            // Si estamos en modo edición, cargar la categoría actual
            if (isEditMode && selectedCategoryId != null) {
                val category = categories.find { it.id == selectedCategoryId }
                category?.let {
                    binding.spinnerCategory.setText(it.name, false)
                }
            }

            // Listener para cuando el usuario selecciona manualmente
            binding.spinnerCategory.setOnItemClickListener { parent, view, position, id ->
                selectedCategoryId = categories[position].id
            }
        }
    }

    private fun setupDatePickers() {
        binding.buttonSelectDueDate.setOnClickListener {
            showDatePicker { date ->
                selectedDueDate = date
                updateDueDateDisplay()
            }
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.chipGroupReminder.apply {
                for (i in 0 until childCount) {
                    getChildAt(i).isEnabled = isChecked
                }
            }

            if (isChecked) {
                binding.textReminderPreview.visibility = View.VISIBLE
            } else {
                selectedReminderTime = null
                binding.textReminderPreview.visibility = View.GONE
                updateReminderDisplay()
            }
        }

        binding.chip15min.setOnClickListener {
            setQuickReminder(15)
        }

        binding.chip30min.setOnClickListener {
            setQuickReminder(30)
        }

        binding.chip1hour.setOnClickListener {
            setQuickReminder(60)
        }

        binding.chipTomorrow9am.setOnClickListener {
            setTomorrowReminder(9, 0)
        }

        binding.chipCustom.setOnClickListener {
            showDateTimePicker { dateTime ->
                selectedReminderTime = dateTime
                updateReminderDisplay()
            }
        }
    }

    private fun setQuickReminder(minutesFromNow: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutesFromNow)
        selectedReminderTime = calendar.time
        updateReminderDisplay()
    }

    private fun setTomorrowReminder(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        selectedReminderTime = calendar.time
        updateReminderDisplay()
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            if (validateForm()) {
                saveTask()
            }
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        if (isEditMode) {
            binding.buttonDelete.visibility = View.VISIBLE
            binding.buttonDelete.setOnClickListener {
                showDeleteConfirmationDialog()
            }
        }
    }

    private fun loadTaskData(taskId: Long) {
        taskViewModel.getTaskById(taskId).observe(viewLifecycleOwner) { task ->
            task?.let {
                currentTask = it
                populateForm(it)
            }
        }
    }

    private fun populateForm(task: Task) {
        binding.apply {
            editTextTitle.setText(task.title)
            editTextDescription.setText(task.description)

            spinnerPriority.setText(task.priority.displayName, false)

            selectedDueDate = task.dueDate
            updateDueDateDisplay()

            if (task.hasReminder && task.reminderTime != null) {
                switchReminder.isChecked = true
                selectedReminderTime = task.reminderTime
                updateReminderDisplay()
            }

            selectedCategoryId = task.categoryId
        }
    }

    private fun validateForm(): Boolean {
        val title = binding.editTextTitle.text.toString().trim()

        if (title.isEmpty()) {
            binding.editTextTitle.error = "El titulo es obligatorio"
            return false
        }

        return true
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val priorityText = binding.spinnerPriority.text.toString()
        val priority = Priority.values().find { it.displayName == priorityText } ?: Priority.MEDIUM

        categoryViewModel.allCategories.value?.let { categories ->
            val categoryText = binding.spinnerCategory.text.toString()
            val selectedCategory = categories.find { it.name == categoryText }
            selectedCategoryId = selectedCategory?.id
        }

        val task = if (isEditMode && currentTask != null) {
            currentTask!!.copy(
                title = title,
                description = description.ifBlank { null },
                priority = priority,
                dueDate = selectedDueDate,
                categoryId = selectedCategoryId,
                hasReminder = binding.switchReminder.isChecked,
                reminderTime = if (binding.switchReminder.isChecked) selectedReminderTime else null,
                isRecurring = binding.switchRecurring.isChecked,
                recurringDays = if (binding.switchRecurring.isChecked) selectedDays.sorted().joinToString(",") else null,
                startTime = if (binding.switchRecurring.isChecked) selectedStartTime else null,
                endTime = if (binding.switchRecurring.isChecked) selectedEndTime else null
            )
        } else {
            Task(
                title = title,
                description = description.ifBlank { null },
                priority = priority,
                dueDate = selectedDueDate,
                categoryId = selectedCategoryId,
                hasReminder = binding.switchReminder.isChecked,
                reminderTime = if (binding.switchReminder.isChecked) selectedReminderTime else null,
                isRecurring = binding.switchRecurring.isChecked,
                recurringDays = if (binding.switchRecurring.isChecked) selectedDays.sorted().joinToString(",") else null,
                startTime = if (binding.switchRecurring.isChecked) selectedStartTime else null,
                endTime = if (binding.switchRecurring.isChecked) selectedEndTime else null
            )
        }

        if (isEditMode) {
            taskViewModel.updateTask(task)
        } else {
            taskViewModel.createTask(task)
        }

        // Programar notificacion si tiene recordatorio
        if (task.hasReminder && task.reminderTime != null) {
            val notificationHelper = com.ecci.taskmanager.notifications.NotificationHelper(requireContext())
            notificationHelper.scheduleNotification(task)
            Snackbar.make(binding.root, "Recordatorio programado", Snackbar.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar tarea")
            .setMessage("Estas seguro de que deseas eliminar esta tarea?")
            .setPositiveButton("Eliminar") { _, _ ->
                currentTask?.let { task ->
                    taskViewModel.deleteTask(task)
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        selectedDueDate?.let { calendar.time = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()

        // Primero seleccionar fecha
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                // Luego seleccionar hora
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        onDateTimeSelected(calendar.time)

                        Snackbar.make(
                            binding.root,
                            "Recordatorio configurado para: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun updateDueDateDisplay() {
        selectedDueDate?.let { date ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.buttonSelectDueDate.text = dateFormat.format(date)
        } ?: run {
            binding.buttonSelectDueDate.text = "Seleccionar fecha limite"
        }
    }

    private fun updateReminderDisplay() {
        selectedReminderTime?.let { time ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedTime = dateFormat.format(time)

            val now = Calendar.getInstance()
            val reminderCal = Calendar.getInstance().apply { this.time = time }

            val diffMillis = reminderCal.timeInMillis - now.timeInMillis
            val diffMinutes = diffMillis / (1000 * 60)
            val diffHours = diffMinutes / 60
            val diffDays = diffHours / 24

            val relativeTime = when {
                diffMinutes < 60 -> "En $diffMinutes minutos"
                diffHours < 24 -> "En $diffHours horas"
                diffDays == 1L -> "Mañana"
                else -> "En $diffDays días"
            }

            binding.textReminderPreview.apply {
                visibility = View.VISIBLE
                text = "⏰ Recordatorio: $formattedTime\n($relativeTime)"
            }
        } ?: run {
            binding.textReminderPreview.apply {
                visibility = if (binding.switchReminder.isChecked) View.VISIBLE else View.GONE
                text = "No configurado"
            }
        }
    }
    private fun setupRecurringSchedule() {
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.chipGroupDays.apply {
                for (i in 0 until childCount) {
                    getChildAt(i).isEnabled = isChecked
                }
            }
            binding.buttonStartTime.isEnabled = isChecked
            binding.buttonEndTime.isEnabled = isChecked

            if (!isChecked) {
                selectedDays.clear()
                binding.chipGroupDays.clearCheck()
            }
        }

        // Configurar chips de días
        binding.chipMonday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(1) else selectedDays.remove(1)
        }
        binding.chipTuesday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(2) else selectedDays.remove(2)
        }
        binding.chipWednesday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(3) else selectedDays.remove(3)
        }
        binding.chipThursday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(4) else selectedDays.remove(4)
        }
        binding.chipFriday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(5) else selectedDays.remove(5)
        }
        binding.chipSaturday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(6) else selectedDays.remove(6)
        }
        binding.chipSunday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedDays.add(7) else selectedDays.remove(7)
        }

        // Selectores de hora
        binding.buttonStartTime.setOnClickListener {
            showTimePicker("Hora de inicio", selectedStartTime) { time ->
                selectedStartTime = time
                binding.buttonStartTime.text = time
            }
        }

        binding.buttonEndTime.setOnClickListener {
            showTimePicker("Hora de fin", selectedEndTime) { time ->
                selectedEndTime = time
                binding.buttonEndTime.text = time
            }
        }
    }

    private fun showTimePicker(title: String, currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(timeString)
            },
            hour,
            minute,
            true
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}