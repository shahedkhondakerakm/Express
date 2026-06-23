package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.PreferenceManager
import com.example.data.repository.ExpenseRepository

class ExpenseApp : Application() {
    
    lateinit var database: AppDatabase
    lateinit var repository: ExpenseRepository
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "expense_management_tracker_db"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = ExpenseRepository(database)
        preferenceManager = PreferenceManager(applicationContext)
    }

    companion object {
        lateinit var instance: ExpenseApp
            private set
    }
}
