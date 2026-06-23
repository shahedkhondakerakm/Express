package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExpenseRepository(private val db: AppDatabase) {

    // DAOs refer
    private val spaceDao = db.spaceDao()
    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()
    private val recurringRuleDao = db.recurringRuleDao()
    private val budgetDao = db.budgetDao()
    private val savingsGoalDao = db.savingsGoalDao()
    private val familyDao = db.familyDao()
    private val businessDao = db.businessDao()

    // ==========================================
    // SPACES METHODS
    // ==========================================
    fun getAllSpaces(): Flow<List<Space>> = spaceDao.getAllSpaces()
    
    suspend fun getSpaceById(id: String): Space? = spaceDao.getSpaceById(id)
    
    suspend fun insertSpace(space: Space) = spaceDao.insertSpace(space)
    
    suspend fun deleteSpace(space: Space) = spaceDao.deleteSpace(space)

    // ==========================================
    // ACCOUNTS METHODS
    // ==========================================
    fun getAccountsForSpace(spaceId: String): Flow<List<Account>> = accountDao.getAccountsForSpace(spaceId)
    
    suspend fun getAccountById(id: String): Account? = accountDao.getAccountById(id)
    
    suspend fun insertAccount(account: Account) = accountDao.insertAccount(account)
    
    suspend fun archiveAccount(id: String, archived: Boolean) = accountDao.setAccountArchived(id, archived)
    
    suspend fun deleteAccountRaw(id: String) = accountDao.deleteAccountRaw(id)

    // ==========================================
    // CATEGORIES METHODS
    // ==========================================
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesForSpace(spaceId: String): Flow<List<Category>> = categoryDao.getCategoriesForSpace(spaceId)
    
    suspend fun getCategoryById(id: String): Category? = categoryDao.getCategoryById(id)
    
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    
    suspend fun archiveCategory(id: String) = categoryDao.archiveCategory(id)

    // ==========================================
    // TRANSACTIONS METHODS
    // ==========================================
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsForSpace(spaceId: String): Flow<List<Transaction>> = transactionDao.getTransactionsForSpace(spaceId)
    
    fun getTransactionsForAccount(accountId: String): Flow<List<Transaction>> = transactionDao.getTransactionsForAccount(accountId)
    
    suspend fun getTransactionById(id: String): Transaction? = transactionDao.getTransactionById(id)
    
    fun getRecentlyDeleted(spaceId: String): Flow<List<Transaction>> = transactionDao.getRecentlyDeleted(spaceId)
    
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    
    suspend fun softDeleteTransaction(id: String) = transactionDao.softDeleteTransaction(id, System.currentTimeMillis())
    
    suspend fun restoreTransaction(id: String) = transactionDao.restoreTransaction(id)
    
    suspend fun permanentlyDeleteTransaction(id: String) = transactionDao.permanentlyDeleteTransaction(id)
    
    suspend fun purgeRecentlyDeleted(spaceId: String, olderThanDays: Int = 30) {
        val purgeBefore = System.currentTimeMillis() - (olderThanDays * 24L * 60L * 60L * 1000L)
        transactionDao.purgeRecentlyDeleted(spaceId, purgeBefore)
    }

    // ==========================================
    // RECURRING RULES METHODS
    // ==========================================
    fun getRulesForSpace(spaceId: String): Flow<List<RecurringRule>> = recurringRuleDao.getRulesForSpace(spaceId)
    
    suspend fun getActiveRules(): List<RecurringRule> = recurringRuleDao.getActiveRules()
    
    suspend fun getRuleById(id: String): RecurringRule? = recurringRuleDao.getRuleById(id)
    
    suspend fun insertRule(rule: RecurringRule) = recurringRuleDao.insertRule(rule)
    
    suspend fun updateRuleStatus(id: String, isActive: Boolean) = recurringRuleDao.updateRuleStatus(id, isActive)
    
    suspend fun updateNextRunDate(id: String, nextRunDate: Long) = recurringRuleDao.updateNextRunDate(id, nextRunDate)
    
    suspend fun deleteRule(rule: RecurringRule) = recurringRuleDao.deleteRule(rule)

    // ==========================================
    // BUDGETS METHODS
    // ==========================================
    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetsForSpace(spaceId: String): Flow<List<Budget>> = budgetDao.getBudgetsForSpace(spaceId)
    
    suspend fun getBudgetById(id: String): Budget? = budgetDao.getBudgetById(id)
    
    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)
    
    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)

    // ==========================================
    // SAVINGS GOALS METHODS
    // ==========================================
    fun getGoalsForSpace(spaceId: String): Flow<List<SavingsGoal>> = savingsGoalDao.getGoalsForSpace(spaceId)
    
    suspend fun getGoalById(id: String): SavingsGoal? = savingsGoalDao.getGoalById(id)
    
    suspend fun insertGoal(goal: SavingsGoal) = savingsGoalDao.insertGoal(goal)
    
    suspend fun updateGoalAmount(id: String, amount: Double) = savingsGoalDao.updateCurrentAmount(id, amount)
    
    suspend fun deleteGoal(goal: SavingsGoal) = savingsGoalDao.deleteGoal(goal)

    // ==========================================
    // FAMILY METHODS
    // ==========================================
    fun getMembersForSpace(spaceId: String): Flow<List<FamilyMember>> = familyDao.getMembersForSpace(spaceId)
    
    suspend fun insertMember(member: FamilyMember) = familyDao.insertMember(member)
    
    suspend fun deleteMember(member: FamilyMember) = familyDao.deleteMember(member)
    
    fun getSettlementsForSpace(spaceId: String): Flow<List<Settlement>> = familyDao.getSettlementsForSpace(spaceId)
    
    suspend fun insertSettlement(settlement: Settlement) = familyDao.insertSettlement(settlement)

    // ==========================================
    // BUSINESS METHODS
    // ==========================================
    fun getEmployeesForSpace(spaceId: String): Flow<List<Employee>> = businessDao.getEmployeesForSpace(spaceId)
    
    suspend fun getEmployeeById(id: String): Employee? = businessDao.getEmployeeById(id)
    
    suspend fun insertEmployee(employee: Employee) = businessDao.insertEmployee(employee)
    
    fun getSalaryPaymentsForSpace(spaceId: String): Flow<List<SalaryPayment>> = businessDao.getSalaryPaymentsForSpace(spaceId)
    
    suspend fun insertSalaryPayment(payment: SalaryPayment) = businessDao.insertSalaryPayment(payment)
    
    fun getVehiclesForSpace(spaceId: String): Flow<List<Vehicle>> = businessDao.getVehiclesForSpace(spaceId)
    
    suspend fun getVehicleById(id: String): Vehicle? = businessDao.getVehicleById(id)
    
    suspend fun insertVehicle(vehicle: Vehicle) = businessDao.insertVehicle(vehicle)
    
    fun getFuelLogsForSpace(spaceId: String): Flow<List<FuelLog>> = businessDao.getFuelLogsForSpace(spaceId)
    
    suspend fun insertFuelLog(log: FuelLog) = businessDao.insertFuelLog(log)
    
    fun getInvoicesForSpace(spaceId: String): Flow<List<Invoice>> = businessDao.getInvoicesForSpace(spaceId)
    
    suspend fun getInvoiceById(id: String): Invoice? = businessDao.getInvoiceById(id)
    
    suspend fun insertInvoice(invoice: Invoice) = businessDao.insertInvoice(invoice)
    
    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItem> = businessDao.getInvoiceItems(invoiceId)
    
    suspend fun insertInvoiceItem(item: InvoiceItem) = businessDao.insertInvoiceItem(item)
    
    suspend fun deleteInvoiceItemsOfInvoice(invoiceId: String) = businessDao.deleteInvoiceItemsOfInvoice(invoiceId)
}
