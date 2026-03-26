package com.example.skinconsultform.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConsultationEntity::class],
    version = 2,
    exportSchema = true
)
abstract class StheticDatabase : RoomDatabase() {
    abstract fun consultationDao(): ConsultationDao
}