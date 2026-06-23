package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces ORDER BY createdAt DESC")
    fun getAllSpaces(): Flow<List<Space>>

    @Query("SELECT * FROM spaces WHERE id = :id")
    suspend fun getSpaceById(id: String): Space?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: Space)

    @Delete
    suspend fun deleteSpace(space: Space)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE spaceId = :spaceId AND isArchived = 0")
    fun getAccountsForSpace(spaceId: String): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Query("UPDATE accounts SET isArchived = :archived WHERE id = :id")
    suspend fun setAccountArchived(id: String, archived: Boolean)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountRaw(id: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isArchived = 0")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE spaceId = :spaceId AND isArchived = 0")
    fun getCategoriesForSpace(spaceId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("UPDATE categories SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCategory(id: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<com.example.data.model.Transaction>>

    @Query("SELECT * FROM transactions WHERE spaceId = :spaceId AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsForSpace(spaceId: String): Flow<List<com.example.data.model.Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsForAccount(accountId: String): Flow<List<com.example.data.model.Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): com.example.data.model.Transaction?

    @Query("SELECT * FROM transactions WHERE spaceId = :spaceId AND isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecentlyDeleted(spaceId: String): Flow<List<com.example.data.model.Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: com.example.data.model.Transaction)

    @Query("UPDATE transactions SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, deletedAt: Long)

    @Query("UPDATE transactions SET isDeleted = 0, deletedAt = null WHERE id = :id")
    suspend fun restoreTransaction(id: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun permanentlyDeleteTransaction(id: String)

    @Query("DELETE FROM transactions WHERE spaceId = :spaceId AND isDeleted = 1 AND deletedAt < :purgeBefore")
    suspend fun purgeRecentlyDeleted(spaceId: String, purgeBefore: Long)
}

@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules WHERE spaceId = :spaceId")
    fun getRulesForSpace(spaceId: String): Flow<List<RecurringRule>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    suspend fun getActiveRules(): List<RecurringRule>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getRuleById(id: String): RecurringRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RecurringRule)

    @Query("UPDATE recurring_rules SET isActive = :isActive WHERE id = :id")
    suspend fun updateRuleStatus(id: String, isActive: Boolean)

    @Query("UPDATE recurring_rules SET nextRunDate = :nextRunDate WHERE id = :id")
    suspend fun updateNextRunDate(id: String, nextRunDate: Long)

    @Delete
    suspend fun deleteRule(rule: RecurringRule)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE spaceId = :spaceId")
    fun getBudgetsForSpace(spaceId: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE spaceId = :spaceId")
    fun getGoalsForSpace(spaceId: String): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: String): SavingsGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal)

    @Query("UPDATE savings_goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateCurrentAmount(id: String, amount: Double)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)
}

@Dao
interface FamilyDao {
    @Query("SELECT * FROM family_members WHERE spaceId = :spaceId")
    fun getMembersForSpace(spaceId: String): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("SELECT * FROM settlements WHERE spaceId = :spaceId ORDER BY date DESC")
    fun getSettlementsForSpace(spaceId: String): Flow<List<Settlement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: Settlement)
}

@Dao
interface BusinessDao {
    // Employee Payroll
    @Query("SELECT * FROM employees WHERE spaceId = :spaceId")
    fun getEmployeesForSpace(spaceId: String): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    // Salary Payments
    @Query("SELECT * FROM salary_payments WHERE spaceId = :spaceId ORDER BY payDate DESC")
    fun getSalaryPaymentsForSpace(spaceId: String): Flow<List<SalaryPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryPayment(payment: SalaryPayment)

    // Vehicles & Fuel Log
    @Query("SELECT * FROM vehicles WHERE spaceId = :spaceId")
    fun getVehiclesForSpace(spaceId: String): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM fuel_logs WHERE spaceId = :spaceId ORDER BY date DESC")
    fun getFuelLogsForSpace(spaceId: String): Flow<List<FuelLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLog)

    // Invoicing
    @Query("SELECT * FROM invoices WHERE spaceId = :spaceId ORDER BY date DESC")
    fun getInvoicesForSpace(spaceId: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItem)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItemsOfInvoice(invoiceId: String)
}
