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

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeStatistics()
    }

    private fun observeStatistics() {
        viewModel.totalTasksCount.observe(viewLifecycleOwner) { total ->
            binding.textTotalTasks.text = total.toString()
            updateCompletionRate()
        }

        viewModel.completedTasksCount.observe(viewLifecycleOwner) { completed ->
            binding.textCompletedTasks.text = completed.toString()
            updateCompletionRate()
        }

        viewModel.pendingTasksCount.observe(viewLifecycleOwner) { pending ->
            binding.textPendingTasks.text = pending.toString()
        }

        viewModel.overdueTasksCount.observe(viewLifecycleOwner) { overdue ->
            binding.textOverdueTasks.text = overdue.toString()
        }

        viewModel.todayTasks.observe(viewLifecycleOwner) { todayTasks ->
            binding.textTodayTasks.text = todayTasks.size.toString()
        }

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val highCount = tasks.count { it.priority == Priority.HIGH }
            val mediumCount = tasks.count { it.priority == Priority.MEDIUM }
            val lowCount = tasks.count { it.priority == Priority.LOW }

            binding.textHighPriority.text = highCount.toString()
            binding.textMediumPriority.text = mediumCount.toString()
            binding.textLowPriority.text = lowCount.toString()
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}