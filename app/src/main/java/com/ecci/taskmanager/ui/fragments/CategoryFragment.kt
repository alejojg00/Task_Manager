package com.ecci.taskmanager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecci.taskmanager.databinding.FragmentCategoryBinding
import com.ecci.taskmanager.ui.viewmodel.CategoryViewModel
import com.ecci.taskmanager.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCategories()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(context)
    }

    private fun observeCategories() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val workCategory = categories.find { it.name == "Trabajo" }
            val personalCategory = categories.find { it.name == "Personal" }
            val studiesCategory = categories.find { it.name == "Estudios" }

            workCategory?.let { category ->
                taskViewModel.getTasksByCategory(category.id).observe(viewLifecycleOwner) { tasks ->
                    binding.textWorkCount.text = "${tasks.size} tareas"
                }
            }

            personalCategory?.let { category ->
                taskViewModel.getTasksByCategory(category.id).observe(viewLifecycleOwner) { tasks ->
                    binding.textPersonalCount.text = "${tasks.size} tareas"
                }
            }

            studiesCategory?.let { category ->
                taskViewModel.getTasksByCategory(category.id).observe(viewLifecycleOwner) { tasks ->
                    binding.textStudiesCount.text = "${tasks.size} tareas"
                }
            }

            val customCategories = categories.filter { !it.isPredefined }
            if (customCategories.isEmpty()) {
                binding.emptyStateCategories.visibility = View.VISIBLE
                binding.recyclerViewCategories.visibility = View.GONE
            } else {
                binding.emptyStateCategories.visibility = View.GONE
                binding.recyclerViewCategories.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}