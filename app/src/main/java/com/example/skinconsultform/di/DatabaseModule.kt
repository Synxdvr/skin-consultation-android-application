package com.example.skinconsultform.di

import android.content.Context
import androidx.room.Room
import com.example.skinconsultform.data.db.ConsultationDao
import com.example.skinconsultform.data.db.StheticDatabase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StheticDatabase {
        return Room.databaseBuilder(
            context,
            StheticDatabase::class.java,
            "sthetic_db"
        )
            .fallbackToDestructiveMigration(false)  // safe for MVP
            .build()
    }

    @Provides
    @Singleton
    fun provideConsultationDao(
        database: StheticDatabase
    ): ConsultationDao = database.consultationDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}