package com.example.skinconsultform.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consultation: ConsultationEntity): Long

    @Update
    suspend fun update(consultation: ConsultationEntity)

    @Query("SELECT * FROM consultations ORDER BY submittedAt DESC")
    fun getAllConsultations(): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultations WHERE id = :id")
    suspend fun getById(id: Long): ConsultationEntity?

    @Query("SELECT * FROM consultations ORDER BY submittedAt DESC LIMIT 1")
    suspend fun getLatest(): ConsultationEntity?

    @Delete
    suspend fun delete(consultation: ConsultationEntity)

    @Query("SELECT COUNT(*) FROM consultations")
    fun getTotalCount(): Flow<Int>
}