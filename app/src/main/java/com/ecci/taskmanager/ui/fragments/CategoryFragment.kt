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

/**
 * Fragmento que muestra las categorías de tareas dentro de la aplicación.
 *
 * Este fragmento se encarga de:
 * - Mostrar las categorías predefinidas ("Trabajo", "Personal", "Estudios") y sus conteos de tareas.
 * - Observar los cambios en las categorías almacenadas en la base de datos.
 * - Mostrar un estado vacío cuando no existen categorías personalizadas.
 *
 * Implementa la arquitectura MVVM utilizando los ViewModels [CategoryViewModel] y [TaskViewModel],
 * además de la inyección de dependencias con Hilt.
 */
@AndroidEntryPoint
class CategoryFragment : Fragment() {

    /** Binding para acceder a las vistas del layout XML de este fragmento. */
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    /** ViewModel encargado de manejar la lógica relacionada con las categorías. */
    private val categoryViewModel: CategoryViewModel by viewModels()

    /** ViewModel encargado de manejar las tareas y su relación con las categorías. */
    private val taskViewModel: TaskViewModel by viewModels()

    /**
     * Infla el layout del fragmento y configura el binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la vista una vez creada, incluyendo el RecyclerView y la observación de categorías.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeCategories()
    }

    /**
     * Inicializa el RecyclerView con un [LinearLayoutManager].
     * Este componente mostrará las categorías personalizadas si existen.
     */
    private fun setupRecyclerView() {
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(context)
    }

    /**
     * Observa los cambios en la lista de categorías desde [CategoryViewModel].
     * También actualiza los contadores de tareas para cada categoría predefinida.
     */
    private fun observeCategories() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            // Buscar las categorías predefinidas
            val workCategory = categories.find { it.name == "Trabajo" }
            val personalCategory = categories.find { it.name == "Personal" }
            val studiesCategory = categories.find { it.name == "Estudios" }

            // Observa las tareas de la categoría "Trabajo"
            workCategory?.let { category ->
                taskViewModel.getTasksByCategory(category.id).observe(viewLifecycleOwner) { tasks ->
                    binding.textWorkCount.text = "${tasks.size} tareas"
                }
            }

            // Observa las tareas de la categoría "Personal"
            personalCategory?.let { category ->
                taskViewModel.getTasksByCategory(category.id).observe(viewLifecycleOwner) { tasks ->
                    binding.textPersonalCount.text = "${tasks.size} tareas"
                }
            }

            // Observa las tareas de la categoría "Estudios"
            studiesCategory?.let { category ->
                taskViewModel.getTasksByCategory(category.id).observe(viewLifecycleOwner) { tasks ->
                    binding.textStudiesCount.text = "${tasks.size} tareas"
                }
            }

            // Filtra las categorías personalizadas creadas por el usuario
            val customCategories = categories.filter { !it.isPredefined }

            // Muestra estado vacío si no hay categorías personalizadas
            if (customCategories.isEmpty()) {
                binding.emptyStateCategories.visibility = View.VISIBLE
                binding.recyclerViewCategories.visibility = View.GONE
            } else {
                binding.emptyStateCategories.visibility = View.GONE
                binding.recyclerViewCategories.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Libera los recursos del binding cuando la vista es destruida
     * para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
