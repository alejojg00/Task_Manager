package com.ecci.taskmanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ecci.taskmanager.data.model.Category

/**
 * Interfaz que define las operaciones de acceso a datos (DAO)
 * para la entidad [Category] dentro de la base de datos Room.
 *
 * Esta interfaz permite realizar operaciones CRUD (crear, leer, actualizar y eliminar)
 * sobre las categorías que se utilizan en la aplicación.
 *
 * Las consultas están definidas mediante anotaciones de Room, como:
 * - @Insert para insertar registros.
 * - @Update para modificarlos.
 * - @Delete para eliminarlos.
 * - @Query para ejecutar consultas personalizadas.
 */
@Dao
interface CategoryDao {

    /**
     * Inserta una nueva categoría en la base de datos.
     * Si ya existe una categoría con el mismo ID, la reemplaza.
     *
     * @param category Objeto [Category] que se desea insertar.
     * @return El ID (tipo [Long]) de la categoría insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    /**
     * Inserta una lista de categorías en la base de datos.
     * Si alguna categoría ya existe, será reemplazada.
     *
     * @param categories Lista de objetos [Category] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    /**
     * Obtiene todas las categorías almacenadas en la base de datos,
     * ordenadas alfabéticamente por nombre.
     *
     * @return Un objeto [LiveData] que contiene una lista de categorías.
     * La lista se actualiza automáticamente cuando cambian los datos.
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    /**
     * Busca una categoría específica según su identificador único.
     *
     * @param categoryId Identificador numérico de la categoría.
     * @return El objeto [Category] correspondiente al ID proporcionado,
     * o `null` si no se encuentra.
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    /**
     * Obtiene todas las categorías predefinidas (isPredefined = 1),
     * ordenadas alfabéticamente.
     *
     * @return Un objeto [LiveData] con la lista de categorías predefinidas.
     */
    @Query("SELECT * FROM categories WHERE isPredefined = 1 ORDER BY name ASC")
    fun getPredefinedCategories(): LiveData<List<Category>>

    /**
     * Obtiene todas las categorías personalizadas creadas por el usuario
     * (isPredefined = 0), ordenadas alfabéticamente.
     *
     * @return Un objeto [LiveData] con la lista de categorías personalizadas.
     */
    @Query("SELECT * FROM categories WHERE isPredefined = 0 ORDER BY name ASC")
    fun getCustomCategories(): LiveData<List<Category>>

    /**
     * Busca categorías cuyo nombre contenga una cadena de texto específica.
     *
     * @param searchQuery Cadena que se desea buscar dentro de los nombres.
     * @return Un objeto [LiveData] con la lista de categorías coincidentes.
     */
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchCategories(searchQuery: String): LiveData<List<Category>>

    /**
     * Actualiza los datos de una categoría existente en la base de datos.
     *
     * @param category Objeto [Category] con los datos actualizados.
     */
    @Update
    suspend fun update(category: Category)

    /**
     * Elimina una categoría específica de la base de datos.
     *
     * @param category Objeto [Category] que se desea eliminar.
     */
    @Delete
    suspend fun delete(category: Category)

    /**
     * Elimina una categoría por su identificador, solo si no es predefinida.
     *
     * @param categoryId Identificador de la categoría a eliminar.
     */
    @Query("DELETE FROM categories WHERE id = :categoryId AND isPredefined = 0")
    suspend fun deleteById(categoryId: Long)

    /**
     * Obtiene la cantidad de tareas asociadas a una categoría específica.
     *
     * @param categoryId Identificador de la categoría a consultar.
     * @return Un objeto [LiveData] que contiene el número de tareas relacionadas.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    fun getTaskCountByCategory(categoryId: Long): LiveData<Int>
}
