package com.wordbook.data.repository

import com.wordbook.data.database.daos.LabelDao
import com.wordbook.domain.model.Label
import com.wordbook.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LabelRepositoryImpl @Inject constructor(
    private val labelDao: LabelDao
) : LabelRepository {

    override fun getAllLabels(): Flow<List<Label>> =
        labelDao.getAllLabels().map { list -> list.map { it.toDomain() } }

    override suspend fun getLabelById(id: Long): Label? =
        labelDao.getLabelById(id)?.toDomain()

    override suspend fun insertLabel(label: Label): Long =
        labelDao.insertLabel(label.toEntity())

    override suspend fun updateLabel(label: Label) =
        labelDao.updateLabel(label.toEntity())

    override suspend fun deleteLabel(id: Long) =
        labelDao.deleteLabel(id)
}
