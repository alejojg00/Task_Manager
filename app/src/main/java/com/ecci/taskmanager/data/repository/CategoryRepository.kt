package com.ecci.taskmanager.data.repository

import androidx.lifecycle.LiveData
import com.ecci.taskmanager.data.dao.CategoryDao
import com.ecci.taskmanager.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de gestionar las operaciones relacionadas con las categorías.
 *
 * Esta clase actúa como intermediario entre la capa de datos (DAO) y la capa de
 * presentación, garantizando que las operaciones de acceso a la base de datos
 * se ejecuten en un hilo de entrada/salida (IO) mediante **corrutinas**.
 *
 * Además, encapsula la lógica de validación de categorías y el manejo de errores
 * devolviendo resultados seguros mediante el tipo [Result].
 *
 * @property categoryDao Objeto de acceso a datos (DAO) inyectado mediante Hilt.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    /** Lista en tiempo real de todas las categorías almacenadas. */
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    /** Lista en tiempo real de las categorías predefinidas. */
    val predefinedCategories: LiveData<List<Category>> = categoryDao.getPredefinedCategories()

    /** Lista en tiempo real de las categorías personalizadas por el usuario. */
    val customCategories: LiveData<List<Category>> = categoryDao.getCustomCategories()

    /**
     * Inserta una nueva categoría en la base de datos.
     *
     * Valida que la categoría tenga un nombre válido antes de realizar la inserción.
     * Devuelve un [Result] que puede contener el ID generado o una excepción si ocurre un error.
     *
     * @param category Objeto [Category] a insertar.
     * @return [Result] con el ID generado en caso de éxito o una excepción en caso de error.
     */
    suspend fun insertCategory(category: Category): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!category.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El nombre de la categoría no puede estar vacío")
                )
            }

            val categoryId = categoryDao.insert(category)
            Result.success(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inserta una lista de categorías en la base de datos.
     *
     * Ideal para inicializar categorías predefinidas o importar datos.
     *
     * @param categories Lista de objetos [Category] a insertar.
     * @return [Result] que indica éxito o error durante la operación.
     */
    suspend fun insertCategories(categories: List<Category>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                categoryDao.insertAll(categories)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Obtiene una categoría específica por su ID.
     *
     * @param categoryId Identificador único de la categoría.
     * @return Objeto [Category] si se encuentra, o `null` si no existe.
     */
    suspend fun getCategoryById(categoryId: Long): Category? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(categoryId)
    }

    /**
     * Busca categorías cuyo nombre contenga el texto ingresado.
     *
     * Utiliza una consulta SQL con la cláusula LIKE.
     *
     * @param query Texto a buscar en los nombres de las categorías.
     * @return Lista reactiva ([LiveData]) con los resultados filtrados.
     */
    fun searchCategories(query: String): LiveData<List<Category>> {
        return categoryDao.searchCategories(query)
    }

    /**
     * Obtiene la cantidad de tareas asociadas a una categoría específica.
     *
     * @param categoryId Identificador de la categoría.
     * @return [LiveData] con el número total de tareas vinculadas.
     */
    fun getTaskCountByCategory(categoryId: Long): LiveData<Int> {
        return categoryDao.getTaskCountByCategory(categoryId)
    }

    /**
     * Actualiza una categoría existente en la base de datos.
     *
     * Verifica que la categoría sea válida y no sea una categoría predefinida.
     *
     * @param category Categoría con los nuevos datos a actualizar.
     * @return [Result] que indica el éxito o error de la operación.
     */
    suspend fun updateCategory(category: Category): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!category.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Los datos de la categoría no son válidos")
                )
            }

            if (category.isPredefined) {
                return@withContext Result.failure(
                    IllegalStateException("No se pueden modificar categorías predefinidas")
                )
            }

            categoryDao.update(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una categoría de la base de datos.
     *
     * Antes de eliminar, se valida que la categoría **no sea predefinida**.
     *
     * @param category Categoría que se desea eliminar.
     * @return [Result] que indica el éxito o error de la operación.
     */
    suspend fun deleteCategory(category: Category): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (category.isPredefined) {
                return@withContext Result.failure(
                    IllegalStateException("No se pueden eliminar categorías predefinidas")
                )
            }

            categoryDao.delete(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una categoría por su identificador, siempre que no sea predefinida.
     *
     * @param categoryId ID de la categoría a eliminar.
     * @return [Result] que indica el resultado de la operación.
     */
    suspend fun deleteCategoryById(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            categoryDao.deleteById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
