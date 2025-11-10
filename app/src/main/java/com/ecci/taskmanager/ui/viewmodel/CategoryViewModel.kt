package com.ecci.taskmanager.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecci.taskmanager.data.model.Category
import com.ecci.taskmanager.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val allCategories: LiveData<List<Category>> = categoryRepository.allCategories
    val predefinedCategories: LiveData<List<Category>> = categoryRepository.predefinedCategories
    val customCategories: LiveData<List<Category>> = categoryRepository.customCategories

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    fun createCategory(category: Category) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = categoryRepository.insertCategory(category)

            result.onSuccess {
                _successMessage.value = "Categoria creada exitosamente"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al crear la categoria"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    suspend fun getCategoryById(categoryId: Long): Category? {
        return categoryRepository.getCategoryById(categoryId)
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = categoryRepository.updateCategory(category)

            result.onSuccess {
                _successMessage.value = "Categoria actualizada"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al actualizar la categoria"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = categoryRepository.deleteCategory(category)

            result.onSuccess {
                _successMessage.value = "Categoria eliminada"
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error al eliminar la categoria"
                _successMessage.value = null
            }

            _isLoading.value = false
        }
    }

    fun searchCategories(query: String): LiveData<List<Category>> {
        return categoryRepository.searchCategories(query)
    }

    fun getTaskCountByCategory(categoryId: Long): LiveData<Int> {
        return categoryRepository.getTaskCountByCategory(categoryId)
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun clearSelectedCategory() {
        _selectedCategory.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
