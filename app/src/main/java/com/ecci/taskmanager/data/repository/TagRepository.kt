package com.ecci.taskmanager.data.repository

import androidx.lifecycle.LiveData
import com.ecci.taskmanager.data.dao.TagDao
import com.ecci.taskmanager.data.model.Tag
import com.ecci.taskmanager.data.model.Task
import com.ecci.taskmanager.data.model.TaskTagCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {

    val allTags: LiveData<List<Tag>> = tagDao.getAllTags()

    suspend fun insertTag(tag: Tag): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!tag.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El nombre de la etiqueta no puede estar vacio")
                )
            }

            val tagId = tagDao.insertTag(tag)
            Result.success(tagId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertTags(tags: List<Tag>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tagDao.insertTags(tags)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTagById(tagId: Long): Tag? = withContext(Dispatchers.IO) {
        tagDao.getTagById(tagId)
    }

    suspend fun updateTag(tag: Tag): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!tag.isValid()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Los datos de la etiqueta no son validos")
                )
            }

            tagDao.updateTag(tag)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTag(tag: Tag): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tagDao.deleteTag(tag)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTagById(tagId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tagDao.deleteTagById(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTagToTask(taskId: Long, tagId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val crossRef = TaskTagCrossRef(taskId, tagId)
                tagDao.insertTaskTagCrossRef(crossRef)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun removeTagFromTask(taskId: Long, tagId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val crossRef = TaskTagCrossRef(taskId, tagId)
                tagDao.deleteTaskTagCrossRef(crossRef)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateTaskTags(taskId: Long, tagIds: List<Long>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tagDao.deleteAllTagsFromTask(taskId)

                tagIds.forEach { tagId ->
                    val crossRef = TaskTagCrossRef(taskId, tagId)
                    tagDao.insertTaskTagCrossRef(crossRef)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getTagsForTask(taskId: Long): LiveData<List<Tag>> {
        return tagDao.getTagsForTask(taskId)
    }

    fun getTasksWithTag(tagId: Long): LiveData<List<Task>> {
        return tagDao.getTasksWithTag(tagId)
    }

    fun getTaskCountForTag(tagId: Long): LiveData<Int> {
        return tagDao.getTaskCountForTag(tagId)
    }

    suspend fun removeAllTagsFromTask(taskId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tagDao.deleteAllTagsFromTask(taskId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}