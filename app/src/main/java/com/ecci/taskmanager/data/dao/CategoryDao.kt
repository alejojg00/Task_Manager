package com.ecci.taskmanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ecci.taskmanager.data.model.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("SELECT * FROM categories WHERE isPredefined = 1 ORDER BY name ASC")
    fun getPredefinedCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE isPredefined = 0 ORDER BY name ASC")
    fun getCustomCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchCategories(searchQuery: String): LiveData<List<Category>>

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :categoryId AND isPredefined = 0")
    suspend fun deleteById(categoryId: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    fun getTaskCountByCategory(categoryId: Long): LiveData<Int>
}