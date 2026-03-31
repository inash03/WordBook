package com.wordbook.domain.repository

import com.wordbook.domain.model.Label
import kotlinx.coroutines.flow.Flow

interface LabelRepository {
    fun getAllLabels(): Flow<List<Label>>
    suspend fun getLabelById(id: Long): Label?
    suspend fun insertLabel(label: Label): Long
    suspend fun updateLabel(label: Label)
    suspend fun deleteLabel(id: Long)
}
