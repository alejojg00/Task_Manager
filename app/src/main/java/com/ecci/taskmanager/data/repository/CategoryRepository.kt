package com.ecci.taskmanager.data.repository

import androidx.lifecycle.LiveData
import com.ecci.taskmanager.data.dao.CategoryDao
import com.ecci.taskmanager.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    val predefinedCategories: LiveData<List<Category>> = categoryDao.getPredefinedCategories()
    val customCategories: LiveData<List<Category>> = categoryDao.getCustomCategories()

    suspend fun insertCategory(category: Category): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!category.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El nombre de la categoria no puede estar vacio")
                )
            }

            val categoryId = categoryDao.insert(category)
            Result.success(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertCategories(categories: List<Category>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                categoryDao.insertAll(categories)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCategoryById(categoryId: Long): Category? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(categoryId)
    }

    fun searchCategories(query: String): LiveData<List<Category>> {
        return categoryDao.searchCategories(query)
    }

    fun getTaskCountByCategory(categoryId: Long): LiveData<Int> {
        return categoryDao.getTaskCountByCategory(categoryId)
    }

    suspend fun updateCategory(category: Category): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!category.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Los datos de la categoria no son validos")
                )
            }

            if (category.isPredefined) {
                return@withContext Result.failure(
                    IllegalStateException("No se pueden modificar categorias predefinidas")
                )
            }

            categoryDao.update(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(category: Category): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (category.isPredefined) {
                return@withContext Result.failure(
                    IllegalStateException("No se pueden eliminar categorias predefinidas")
                )
            }

            categoryDao.delete(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryById(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            categoryDao.deleteById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}