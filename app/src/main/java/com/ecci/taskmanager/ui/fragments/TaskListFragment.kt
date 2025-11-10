package com.ecci.taskmanager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecci.taskmanager.R
import com.ecci.taskmanager.databinding.FragmentTaskListBinding
import com.ecci.taskmanager.ui.adapters.TaskAdapter
import com.ecci.taskmanager.ui.viewmodel.TaskViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        observeViewModel()
        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // TODO: Navegar a detalle cuando lo implementemos
                Snackbar.make(binding.root, "Tarea: ${task.title}", Snackbar.LENGTH_SHORT).show()
            },
            onTaskCheckChanged = { task, isChecked ->
                viewModel.toggleTaskCompletion(task)

                // Forzar actualización de la lista después de un pequeño delay
                view?.postDelayed({
                    viewModel.applyFilter(viewModel.activeFilter.value ?: com.ecci.taskmanager.ui.viewmodel.TaskFilter.ALL)
                }, 100)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val checkedChipId = checkedIds[0]
            val checkedChip = group.findViewById<Chip>(checkedChipId)

            when (checkedChip.id) {
                R.id.chip_all -> {
                    viewModel.applyFilter(com.ecci.taskmanager.ui.viewmodel.TaskFilter.ALL)
                }
                R.id.chip_today -> {
                    viewModel.applyFilter(com.ecci.taskmanager.ui.viewmodel.TaskFilter.TODAY)
                }
                R.id.chip_pending -> {
                    viewModel.applyFilter(com.ecci.taskmanager.ui.viewmodel.TaskFilter.PENDING)
                }
                R.id.chip_completed -> {
                    viewModel.applyFilter(com.ecci.taskmanager.ui.viewmodel.TaskFilter.COMPLETED)
                }
                R.id.chip_overdue -> {
                    viewModel.applyFilter(com.ecci.taskmanager.ui.viewmodel.TaskFilter.OVERDUE)
                }
            }
        }

        binding.chipAll.isChecked = true
    }

    private fun observeViewModel() {
        // Observar todas las tareas primero
        viewModel.allTasks.observe(viewLifecycleOwner) { allTasks ->
            // Este observe asegura que siempre tengamos datos actualizados
        }

        // Observar filtro activo y actualizar lista
        viewModel.activeFilter.observe(viewLifecycleOwner) { filter ->
            when (filter) {
                com.ecci.taskmanager.ui.viewmodel.TaskFilter.ALL -> {
                    viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
                        taskAdapter.submitList(tasks)
                        updateEmptyState(tasks.isEmpty())
                    }
                }
                com.ecci.taskmanager.ui.viewmodel.TaskFilter.PENDING -> {
                    viewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->
                        taskAdapter.submitList(tasks)
                        updateEmptyState(tasks.isEmpty())
                    }
                }
                com.ecci.taskmanager.ui.viewmodel.TaskFilter.COMPLETED -> {
                    viewModel.completedTasks.observe(viewLifecycleOwner) { tasks ->
                        taskAdapter.submitList(tasks)
                        updateEmptyState(tasks.isEmpty())
                    }
                }
                com.ecci.taskmanager.ui.viewmodel.TaskFilter.OVERDUE -> {
                    viewModel.overdueTasks.observe(viewLifecycleOwner) { tasks ->
                        taskAdapter.submitList(tasks)
                        updateEmptyState(tasks.isEmpty())
                    }
                }
                com.ecci.taskmanager.ui.viewmodel.TaskFilter.TODAY -> {
                    viewModel.todayTasks.observe(viewLifecycleOwner) { tasks ->
                        taskAdapter.submitList(tasks)
                        updateEmptyState(tasks.isEmpty())
                    }
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }

        viewModel.totalTasksCount.observe(viewLifecycleOwner) { total ->
            binding.textTotalTasks.text = "Total: $total"
        }

        viewModel.completedTasksCount.observe(viewLifecycleOwner) { completed ->
            binding.textCompletedTasks.text = "Completadas: $completed"
        }

        viewModel.pendingTasksCount.observe(viewLifecycleOwner) { pending ->
            binding.textPendingTasks.text = "Pendientes: $pending"
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.currentList[position]

                viewModel.deleteTask(task)

                Snackbar.make(binding.root, "Tarea eliminada", Snackbar.LENGTH_LONG)
                    .setAction("DESHACER") {
                        viewModel.createTask(task)
                    }
                    .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTasks)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerViewTasks.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.recyclerViewTasks.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}