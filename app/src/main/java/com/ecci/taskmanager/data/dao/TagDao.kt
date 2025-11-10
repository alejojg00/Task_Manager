package com.ecci.taskmanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ecci.taskmanager.data.model.Tag
import com.ecci.taskmanager.data.model.TaskTagCrossRef
import com.ecci.taskmanager.data.model.Task

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTagCrossRef(crossRef: TaskTagCrossRef)

    @Delete
    suspend fun deleteTaskTagCrossRef(crossRef: TaskTagCrossRef)

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN task_tag_cross_ref ON tags.id = task_tag_cross_ref.tagId
        WHERE task_tag_cross_ref.taskId = :taskId
        ORDER BY tags.name ASC
    """)
    fun getTagsForTask(taskId: Long): LiveData<List<Tag>>

    @Query("""
        SELECT tasks.* FROM tasks
        INNER JOIN task_tag_cross_ref ON tasks.id = task_tag_cross_ref.taskId
        WHERE task_tag_cross_ref.tagId = :tagId
        ORDER BY tasks.createdAt DESC
    """)
    fun getTasksWithTag(tagId: Long): LiveData<List<Task>>

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun deleteAllTagsFromTask(taskId: Long)

    @Query("SELECT COUNT(*) FROM task_tag_cross_ref WHERE tagId = :tagId")
    fun getTaskCountForTag(tagId: Long): LiveData<Int>
}
