package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Space::class,
        Account::class,
        Category::class,
        Transaction::class,
        RecurringRule::class,
        Budget::class,
        SavingsGoal::class,
        FamilyMember::class,
        Settlement::class,
        Employee::class,
        SalaryPayment::class,
        Vehicle::class,
        FuelLog::class,
        Invoice::class,
        InvoiceItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun familyDao(): FamilyDao
    abstract fun businessDao(): BusinessDao
}
