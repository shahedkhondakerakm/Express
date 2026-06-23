package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ExpenseApp
import com.example.data.local.PreferenceManager
import com.example.data.model.*
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository = (application as ExpenseApp).repository
    private val prefManager: PreferenceManager = (application as ExpenseApp).preferenceManager

    // ==========================================
    // DATASTORE FLOWS
    // ==========================================
    val username = prefManager.usernameFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val activeSpaceId = prefManager.activeSpaceIdFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val baseCurrency = prefManager.baseCurrencyFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "USD")
    val currentLanguage = prefManager.languageFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "English")
    val themeMode = prefManager.themeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "SYSTEM")
    val biometricEnabled = prefManager.biometricLockFlow.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val pinLockCode = prefManager.pinLockFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val isOnboardingComplete = prefManager.onboardingCompleteFlow.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val exchangeRates = prefManager.exchangeRatesFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "{}")

    // ==========================================
    // ROOM DATABASE STATE SEGMENTS
    // ==========================================
    val allSpaces = repository.getAllSpaces().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBudgets = repository.getAllBudgets().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransactions = repository.getAllTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCategories = repository.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSpace = MutableStateFlow<Space?>(null)
    val activeSpace: StateFlow<Space?> = _activeSpace.asStateFlow()

    // Main collection sets scoped to the active space
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _recentlyDeleted = MutableStateFlow<List<Transaction>>(emptyList())
    val recentlyDeleted: StateFlow<List<Transaction>> = _recentlyDeleted.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()

    private val _rules = MutableStateFlow<List<RecurringRule>>(emptyList())
    val rules: StateFlow<List<RecurringRule>> = _rules.asStateFlow()

    // Family-space additions
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    val settlements: StateFlow<List<Settlement>> = _settlements.asStateFlow()

    // Business-space additions
    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _salaryPayments = MutableStateFlow<List<SalaryPayment>>(emptyList())
    val salaryPayments: StateFlow<List<SalaryPayment>> = _salaryPayments.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _fuelLogs = MutableStateFlow<List<FuelLog>>(emptyList())
    val fuelLogs: StateFlow<List<FuelLog>> = _fuelLogs.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    // Celeb Animation State Trigger
    private val _goalCelebratedSignal = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val goalCelebratedSignal = _goalCelebratedSignal.asSharedFlow()

    // Filter states
    val searchText = MutableStateFlow("")
    val filterType = MutableStateFlow<TransactionType?>(null)
    val filterAccount = MutableStateFlow<String?>(null)
    val filterCategory = MutableStateFlow<String?>(null)
    val filterIsPaid = MutableStateFlow<Boolean?>(null) // Billing Accounts Payable

    // Google Play Ads Compliance & Rewards Sandbox variables
    private val _personalizedAdsConsent = MutableStateFlow(true)
    val personalizedAdsConsent = _personalizedAdsConsent.asStateFlow()

    private val _adShowcaseRewardTokens = MutableStateFlow(3)
    val adShowcaseRewardTokens = _adShowcaseRewardTokens.asStateFlow()

    private val _isAdFreeModeActive = MutableStateFlow(false)
    val isAdFreeModeActive = _isAdFreeModeActive.asStateFlow()

    private val _adFreeExpirationTime = MutableStateFlow<Long?>(null)
    val adFreeExpirationTime = _adFreeExpirationTime.asStateFlow()

    fun setPersonalizedAdsConsent(consent: Boolean) {
        _personalizedAdsConsent.value = consent
    }

    fun earnRewardToken() {
        _adShowcaseRewardTokens.value += 1
    }

    fun spendRewardToken() {
        if (_adShowcaseRewardTokens.value > 0) {
            _adShowcaseRewardTokens.value -= 1
        }
    }

    fun activateAdFree24Hours(): Boolean {
        if (_adShowcaseRewardTokens.value >= 1) {
            _adShowcaseRewardTokens.value -= 1
            _isAdFreeModeActive.value = true
            _adFreeExpirationTime.value = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
            return true
        }
        return false
    }

    init {
        // Observe space changes and bind dependent flow sets
        viewModelScope.launch {
            activeSpaceId.collect { spaceId ->
                if (spaceId != null) {
                    val sp = repository.getSpaceById(spaceId)
                    _activeSpace.value = sp
                    
                    // Bind Room database flows scoped dynamically to active space
                    launch { repository.getAccountsForSpace(spaceId).collect { _accounts.value = it } }
                    launch { repository.getCategoriesForSpace(spaceId).collect { _categories.value = it } }
                    launch { repository.getTransactionsForSpace(spaceId).collect { _transactions.value = it } }
                    launch { repository.getRecentlyDeleted(spaceId).collect { _recentlyDeleted.value = it } }
                    launch { repository.getBudgetsForSpace(spaceId).collect { _budgets.value = it } }
                    launch { repository.getGoalsForSpace(spaceId).collect { _savingsGoals.value = it } }
                    launch { repository.getRulesForSpace(spaceId).collect { _rules.value = it } }
                    
                    // Family Scope additions
                    launch { repository.getMembersForSpace(spaceId).collect { _familyMembers.value = it } }
                    launch { repository.getSettlementsForSpace(spaceId).collect { _settlements.value = it } }

                    // Business Scope additions
                    launch { repository.getEmployeesForSpace(spaceId).collect { _employees.value = it } }
                    launch { repository.getSalaryPaymentsForSpace(spaceId).collect { _salaryPayments.value = it } }
                    launch { repository.getVehiclesForSpace(spaceId).collect { _vehicles.value = it } }
                    launch { repository.getFuelLogsForSpace(spaceId).collect { _fuelLogs.value = it } }
                    launch { repository.getInvoicesForSpace(spaceId).collect { _invoices.value = it } }

                    // Fire recurring auto-log worker simulation
                    processDueRecurringRules(spaceId)
                }
            }
        }
    }

    // ==========================================
    // ONBOARDING & SEEDING PROCEDURES
    // ==========================================
    fun completeOnboarding(
        userNameInput: String,
        baseCurrencyCode: String,
        initialCashBalance: Double,
        appLanguage: String,
        useBiometrics: Boolean,
        pinLock: String?
    ) {
        viewModelScope.launch {
            val personalSpaceId = "personal_default"
            val personalSpace = Space(
                id = personalSpaceId,
                name = "Personal Space",
                type = SpaceType.PERSONAL,
                currency = baseCurrencyCode
            )
            repository.insertSpace(personalSpace)

            // Seed initial account
            val cashAccount = Account(
                id = "account_cash",
                spaceId = personalSpaceId,
                name = "Cash Wallet",
                type = AccountType.CASH,
                currency = baseCurrencyCode,
                initialBalance = initialCashBalance
            )
            repository.insertAccount(cashAccount)

            // Seed Categories in personal space
            val defaultCategories = listOf(
                Category("cat_food", personalSpaceId, "Food & Dining", "Restaurant", 0xFFE57373.toInt(), false),
                Category("cat_transport", personalSpaceId, "Transport", "DirectionsCar", 0xFF64B5F6.toInt(), false),
                Category("cat_shopping", personalSpaceId, "Shopping", "LocalMall", 0xFFFFB74D.toInt(), false),
                Category("cat_bills", personalSpaceId, "Bills & Utilities", "Receipt", 0xFF81C784.toInt(), false),
                Category("cat_health", personalSpaceId, "Health & Care", "LocalHospital", 0xFF4DB6AC.toInt(), false),
                Category("cat_entertainment", personalSpaceId, "Entertainment", "SportsEsports", 0xFFBA68C8.toInt(), false),
                Category("cat_education", personalSpaceId, "Education", "School", 0xFF90A4AE.toInt(), false),
                Category("cat_housing", personalSpaceId, "Housing", "Home", 0xFFA1887F.toInt(), false),
                Category("cat_personal", personalSpaceId, "Personal Care", "Face", 0xFFF06292.toInt(), false),
                Category("cat_income", personalSpaceId, "Salary / Income", "AttachMoney", 0xFFAED581.toInt(), true)
            )
            defaultCategories.forEach { repository.insertCategory(it) }

            // Persist Datastore details
            prefManager.setUsername(userNameInput)
            prefManager.setBaseCurrency(baseCurrencyCode)
            prefManager.setActiveSpaceId(personalSpaceId)
            prefManager.setLanguage(appLanguage)
            prefManager.setBiometricLock(useBiometrics)
            prefManager.setPinLock(pinLock)
            prefManager.setOnboardingComplete(true)
        }
    }

    // ==========================================
    // CONFIG / PREFERENCES WRITE OPERATIONS
    // ==========================================
    fun setAppTheme(theme: String) {
        viewModelScope.launch { prefManager.setTheme(theme) }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch { prefManager.setLanguage(lang) }
    }

    fun switchActiveSpace(spaceId: String) {
        viewModelScope.launch { prefManager.setActiveSpaceId(spaceId) }
    }

    fun registerBiometrics(enabled: Boolean) {
        viewModelScope.launch { prefManager.setBiometricLock(enabled) }
    }

    fun registerPinLock(code: String?) {
        viewModelScope.launch { prefManager.setPinLock(code) }
    }

    fun updateBaseCurrency(currency: String) {
        viewModelScope.launch { prefManager.setBaseCurrency(currency) }
    }

    fun updateManualExchangeRate(currency: String, rate: Double) {
        viewModelScope.launch {
            val currentRates = JSONObject(exchangeRates.value)
            currentRates.put(currency, rate)
            prefManager.saveExchangeRates(currentRates.toString(), System.currentTimeMillis())
        }
    }

    // ==========================================
    // SPACE MANAGEMENT (CRUD)
    // ==========================================
    fun createSpace(name: String, type: SpaceType, currency: String) {
        viewModelScope.launch {
            val spaceId = "space_${UUID.randomUUID()}"
            val newSpace = Space(id = spaceId, name = name, type = type, currency = currency)
            repository.insertSpace(newSpace)

            // Auto seed categories depending on space type
            if (type == SpaceType.BUSINESS) {
                // Seed business categories
                val bizCats = listOf(
                    Category("biz_sales_${spaceId}", spaceId, "Sales Revenue", "TrendingUp", 0xFF81C784.toInt(), true),
                    Category("biz_payroll_${spaceId}", spaceId, "Payroll", "Payments", 0xFFE57373.toInt(), false),
                    Category("biz_fuel_${spaceId}", spaceId, "Vehicle & Fuel", "LocalGasStation", 0xFFFFB74D.toInt(), false),
                    Category("biz_office_${spaceId}", spaceId, "Office Supplies", "Inventory2", 0xFF64B5F6.toInt(), false),
                    Category("biz_taxes_${spaceId}", spaceId, "Tax Expense", "AccountBalance", 0xFF90A4AE.toInt(), false)
                )
                bizCats.forEach { repository.insertCategory(it) }
            } else if (type == SpaceType.FAMILY) {
                // Seed family categories
                val famCats = listOf(
                    Category("fam_groceries_${spaceId}", spaceId, "Groceries", "ShoppingBasket", 0xFF81C784.toInt(), false),
                    Category("fam_kids_${spaceId}", spaceId, "Kids & School", "ChildCare", 0xFFBA68C8.toInt(), false),
                    Category("fam_rent_${spaceId}", spaceId, "House Rent", "Home", 0xFFA1887F.toInt(), false),
                    Category("fam_bills_${spaceId}", spaceId, "Utilities", "Power", 0xFFFFB74D.toInt(), false)
                )
                famCats.forEach { repository.insertCategory(it) }

                // Seed Default Pool Account
                val poolAccount = Account(
                    id = "account_pool_${spaceId}",
                    spaceId = spaceId,
                    name = "Shared Family Pool",
                    type = AccountType.BANK,
                    currency = currency,
                    initialBalance = 0.0
                )
                repository.insertAccount(poolAccount)

                // Seed default admin member
                val adminMember = FamilyMember(
                    id = "member_admin_${spaceId}",
                    spaceId = spaceId,
                    name = username.value ?: "Owner/Admin",
                    role = "ADMIN"
                )
                repository.insertMember(adminMember)
            }

            // Quick select this new space
            prefManager.setActiveSpaceId(spaceId)
        }
    }

    // ==========================================
    // ACCOUNT SERVICES (CRUD)
    // ==========================================
    fun createAccount(name: String, type: AccountType, currency: String, initialBalance: Double) {
        val currentSpaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val accountId = "acc_${UUID.randomUUID()}"
            val newAcc = Account(
                id = accountId,
                spaceId = currentSpaceId,
                name = name,
                type = type,
                currency = currency,
                initialBalance = initialBalance
            )
            repository.insertAccount(newAcc)
        }
    }

    fun archiveAccount(accountId: String) {
        viewModelScope.launch { repository.archiveAccount(accountId, true) }
    }

    fun archiveCategory(categoryId: String) {
        viewModelScope.launch { repository.archiveCategory(categoryId) }
    }

    // ==========================================
    // CATEGORY MANAGEMENT (CRUD)
    // ==========================================
    fun createCategory(name: String, icon: String, color: Int, isIncome: Boolean, parentId: String? = null) {
        val currentSpaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val catId = "cat_${UUID.randomUUID()}"
            val newCat = Category(
                id = catId,
                spaceId = currentSpaceId,
                name = name,
                icon = icon,
                color = color,
                isIncome = isIncome,
                parentCategoryId = parentId
            )
            repository.insertCategory(newCat)
        }
    }

    // ==========================================
    // TRANSACTION WORKFLOWS & CALCULATIONS
    // ==========================================
    fun insertTransaction(
        accountId: String,
        amount: Double,
        type: TransactionType,
        categoryId: String?,
        note: String = "",
        toAccountId: String? = null,
        transferFee: Double? = null,
        date: Long = System.currentTimeMillis(),
        refundOfId: String? = null,
        attachmentPath: String? = null,
        paidByMemberId: String? = null,
        splitType: String? = null,
        splitDetailsJson: String? = null,
        vendorName: String? = null,
        invoiceNumber: String? = null,
        taxAmount: Double? = null,
        taxRate: Double? = null,
        isPaid: Boolean = true
    ) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "txn_${UUID.randomUUID()}"
            val transaction = Transaction(
                id = id,
                spaceId = spaceId,
                accountId = accountId,
                toAccountId = toAccountId,
                amount = amount,
                type = type,
                categoryId = categoryId,
                note = note,
                date = date,
                refundOfTransactionId = refundOfId,
                attachmentPath = attachmentPath,
                transferFee = transferFee,
                paidByMemberId = paidByMemberId,
                splitType = splitType,
                splitDetailsJson = splitDetailsJson,
                vendorName = vendorName,
                invoiceNumber = invoiceNumber,
                taxAmount = taxAmount,
                taxRate = taxRate,
                isPaid = isPaid
            )
            repository.insertTransaction(transaction)

            // If a transfer, check to build destination atomic legs internally
            // But having a single transaction item with toAccountId is fully self-contained as defined by schema, 
            // since running balances deduct from source (accountId) and add to destination(toAccountId) onTRANSFER type.
            
            // Re-evaluate budgets for alert triggering
            categoryId?.let { checkBudgetAlerts(it, amount) }
        }
    }

    fun softDeleteTransaction(id: String) {
        viewModelScope.launch { repository.softDeleteTransaction(id) }
    }

    fun restoreTransaction(id: String) {
        viewModelScope.launch { repository.restoreTransaction(id) }
    }

    fun permanentlyDeleteTransaction(id: String) {
        viewModelScope.launch { repository.permanentlyDeleteTransaction(id) }
    }

    fun clearTrash() {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            // Delete all soft-deleted items in the current space
            recentlyDeleted.value.forEach {
                repository.permanentlyDeleteTransaction(it.id)
            }
        }
    }

    // ==========================================
    // BUDGET CHECKS & PROGRESS
    // ==========================================
    fun createBudget(categoryId: String?, limit: Double, period: String, alertPercent: List<Double>) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "budget_${UUID.randomUUID()}"
            val thresholdsStr = JSONArray(alertPercent).toString()
            val newBudget = Budget(
                id = id,
                spaceId = spaceId,
                categoryId = categoryId,
                limitAmount = limit,
                period = period,
                alertThresholdsJson = thresholdsStr
            )
            repository.insertBudget(newBudget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { repository.deleteBudget(budget) }
    }

    private suspend fun checkBudgetAlerts(categoryId: String, purchaseAmount: Double) {
        val spaceId = activeSpaceId.value ?: return
        val currentSpaceBudgets = budgets.value.filter { it.categoryId == categoryId }
        val allTxnsInSpace = transactions.value.filter { it.categoryId == categoryId && it.type == TransactionType.EXPENSE }
        
        // Sum expenses in space
        val currentSpending = allTxnsInSpace.sumOf { it.amount } + purchaseAmount

        for (budget in currentSpaceBudgets) {
            val limit = budget.limitAmount
            val thresholds = parseDoubleList(budget.alertThresholdsJson)
            val fired = parseStringList(budget.firedThresholdsJson).toMutableSet()

            for (t in thresholds) {
                val margin = limit * t
                if (currentSpending >= margin && !fired.contains(t.toString())) {
                    fired.add(t.toString())
                    // Trigger notification simulated or saved back
                    val updatedBudget = budget.copy(firedThresholdsJson = JSONArray(fired.toList()).toString())
                    repository.insertBudget(updatedBudget)
                    // We can also post a system notification or local state banner alert
                }
            }
        }
    }

    fun calculateAccountBalance(account: Account): Double {
        val allTxns = transactions.value
        var balance = account.initialBalance
        allTxns.forEach { t ->
            if (t.accountId == account.id) {
                when (t.type) {
                    TransactionType.EXPENSE -> balance -= t.amount
                    TransactionType.INCOME -> balance += t.amount
                    TransactionType.TRANSFER -> {
                        balance -= t.amount
                        t.transferFee?.let { balance -= it }
                    }
                }
            }
            if (t.toAccountId == account.id && t.type == TransactionType.TRANSFER) {
                balance += t.amount
            }
        }
        return balance
    }

    fun calculateNetWorth(): Double {
        val rates = JSONObject(exchangeRates.value)
        val spaceBase = activeSpace.value?.currency ?: return 0.0
        
        var total = 0.0
        accounts.value.forEach { acc ->
            val bal = calculateAccountBalance(acc)
            val baseVal = convertCurrency(bal, acc.currency, spaceBase, rates)
            total += baseVal
        }
        return total
    }

    private fun convertCurrency(amount: Double, from: String, to: String, rates: JSONObject): Double {
        if (from == to) return amount
        val fromRate = rates.optDouble(from, 1.0)
        val toRate = rates.optDouble(to, 1.0)
        if (fromRate == 0.0) return amount
        // Convert to USD first (base of standard rate cards), then to target
        val amountInUSD = amount / fromRate
        return amountInUSD * toRate
    }

    // ==========================================
    // RECURRING WORKER ENGINE (ON TIME TRIGGER)
    // ==========================================
    fun createRecurringRule(
        amount: Double,
        type: TransactionType,
        categoryId: String?,
        accountId: String,
        frequency: String,
        autoLog: Boolean,
        note: String
    ) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "rule_${UUID.randomUUID()}"
            val newRule = RecurringRule(
                id = id,
                spaceId = spaceId,
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                frequency = frequency,
                startDate = System.currentTimeMillis(),
                nextRunDate = System.currentTimeMillis() + getFrequencyOffset(frequency),
                autoLog = autoLog,
                note = note
            )
            repository.insertRule(newRule)
        }
    }

    private suspend fun processDueRecurringRules(spaceId: String) {
        val activeRules = repository.getActiveRules()
        val now = System.currentTimeMillis()
        activeRules.forEach { rule ->
            if (rule.spaceId == spaceId && rule.nextRunDate <= now) {
                if (rule.autoLog) {
                    // Automatically log the transaction
                    insertTransaction(
                        accountId = rule.accountId,
                        amount = rule.amount,
                        type = rule.type,
                        categoryId = rule.categoryId,
                        note = "Auto-Recurring: " + rule.note,
                        toAccountId = rule.toAccountId,
                        date = now
                    )
                }
                // Advance next run date
                val nextRun = now + getFrequencyOffset(rule.frequency)
                repository.updateNextRunDate(rule.id, nextRun)
            }
        }
    }

    private fun getFrequencyOffset(freq: String): Long {
        val day = 24L * 60L * 60L * 1000L
        return when (freq) {
            "DAILY" -> day
            "WEEKLY" -> day * 7
            "MONTHLY" -> day * 30
            "YEARLY" -> day * 365
            else -> day * 30
        }
    }

    // ==========================================
    // SAVINGS GOAL METRICS
    // ==========================================
    fun createSavingsGoal(name: String, target: Double, targetDate: Long?, icon: String, color: Int) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "goal_${UUID.randomUUID()}"
            val newGoal = SavingsGoal(
                id = id,
                spaceId = spaceId,
                name = name,
                targetAmount = target,
                targetDate = targetDate,
                icon = icon,
                color = color
            )
            repository.insertGoal(newGoal)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    fun contributeToGoal(goal: SavingsGoal, contribution: Double) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + contribution
            repository.updateGoalAmount(goal.id, newAmount)
            if (newAmount >= goal.targetAmount && !goal.isCompleted) {
                _goalCelebratedSignal.emit(goal.name)
                // Mark completed
                repository.insertGoal(goal.copy(currentAmount = newAmount, isCompleted = true))
            }
        }
    }

    // ==========================================
    // PHASE 2 - FAMILY SPACE FEATURES & IOU LEDGER
    // ==========================================
    fun createFamilyMember(name: String, role: String) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "member_${UUID.randomUUID()}"
            val newMember = FamilyMember(id = id, spaceId = spaceId, name = name, role = role)
            repository.insertMember(newMember)
        }
    }

    fun logFamilySettlement(fromMemberId: String, toMemberId: String, amount: Double, note: String) {
        val spaceId = activeSpaceId.value ?: return
        val spaceBase = activeSpace.value?.currency ?: "USD"
        viewModelScope.launch {
            val settlementId = "settle_${UUID.randomUUID()}"
            val newSettlement = Settlement(
                id = settlementId,
                spaceId = spaceId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amount = amount,
                currency = spaceBase,
                note = note
            )
            repository.insertSettlement(newSettlement)

            // Also insert a normal transfer or transaction as cash balance record inside the shared account if logged
            val linkedTxnNote = "Settlement: Member bills ledger $note"
            val poolAcc = accounts.value.firstOrNull { it.id.startsWith("account_pool_") }
            if (poolAcc != null) {
                // Log balance flow reflecting pool cash
                insertTransaction(
                    accountId = poolAcc.id,
                    amount = amount,
                    type = TransactionType.INCOME,
                    categoryId = null,
                    note = linkedTxnNote,
                    date = System.currentTimeMillis()
                )
            }
        }
    }

    fun calculateDebtBalances(): List<DebtRecord> {
        val currentSpaceTxns = transactions.value.filter { it.splitType != null && it.splitDetailsJson != null }
        val members = familyMembers.value
        if (members.isEmpty()) return emptyList()

        val memberBalances = mutableMapOf<String, Double>() // Member ID -> Balance sheet (+ means owed, - means owes)
        members.forEach { memberBalances[it.id] = 0.0 }

        currentSpaceTxns.forEach { txn ->
            val paidById = txn.paidByMemberId ?: return@forEach
            val splitDetails = parseSplitDetails(txn.splitDetailsJson) ?: return@forEach
            val totalBill = txn.amount

            // Added to payer since they paid full bill
            memberBalances[paidById] = (memberBalances[paidById] ?: 0.0) + totalBill

            // Subtract members shares from total bill
            splitDetails.forEach { share ->
                val mId = share.memberId
                memberBalances[mId] = (memberBalances[mId] ?: 0.0) - share.shareAmount
            }
        }

        // Also incorporate settlement actions logged
        settlements.value.forEach { settlement ->
            val from = settlement.fromMemberId
            val to = settlement.toMemberId
            val amt = settlement.amount
            memberBalances[from] = (memberBalances[from] ?: 0.0) + amt
            memberBalances[to] = (memberBalances[to] ?: 0.0) - amt
        }

        // Generate optimal settlement suggestions (netting debt matrix)
        val creditors = mutableListOf<Pair<String, Double>>()
        val debtors = mutableListOf<Pair<String, Double>>()

        memberBalances.forEach { (mId, balance) ->
            if (balance > 0.01) creditors.add(Pair(mId, balance))
            else if (balance < -0.01) debtors.add(Pair(mId, -balance))
        }

        val results = mutableListOf<DebtRecord>()
        var cIdx = 0
        var dIdx = 0

        while (cIdx < creditors.size && dIdx < debtors.size) {
            val debtor = debtors[dIdx]
            val creditor = creditors[cIdx]

            val settleAmount = minOf(debtor.second, creditor.second)
            val debtorName = members.find { it.id == debtor.first }?.name ?: "Unknown"
            val creditorName = members.find { it.id == creditor.first }?.name ?: "Unknown"

            results.add(
                DebtRecord(
                    fromMemberId = debtor.first,
                    fromMemberName = debtorName,
                    toMemberId = creditor.first,
                    toMemberName = creditorName,
                    netOwed = settleAmount
                )
            )

            debtors[dIdx] = Pair(debtor.first, debtor.second - settleAmount)
            creditors[cIdx] = Pair(creditor.first, creditor.second - settleAmount)

            if (debtors[dIdx].second <= 0.01) dIdx++
            if (creditors[cIdx].second <= 0.01) cIdx++
        }

        return results
    }

    // ==========================================
    // PHASE 3 - BUSINESS CONTRACTING/SALARIES, PAYABLES, PETROL LOG, OUTGOING INVOICES
    // ==========================================
    // Salary Payroll
    fun createEmployee(name: String, role: String, baseSalary: Double, frequency: String) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "emp_${UUID.randomUUID()}"
            val newEmp = Employee(
                id = id,
                spaceId = spaceId,
                name = name,
                role = role,
                baseSalary = baseSalary,
                paymentFrequency = frequency,
                startDate = System.currentTimeMillis()
            )
            repository.insertEmployee(newEmp)
        }
    }

    fun makeSalaryPayment(employeeId: String, amount: Double, type: String, note: String) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val pId = "payment_${UUID.randomUUID()}"
            val payment = SalaryPayment(
                id = pId,
                spaceId = spaceId,
                employeeId = employeeId,
                amount = amount,
                payDate = System.currentTimeMillis(),
                type = type,
                note = note
            )
            repository.insertSalaryPayment(payment)

            // Link to a normal Expense transaction inside the Business space
            val payrollCat = categories.value.firstOrNull { it.name.contains("Payroll") || it.id.startsWith("biz_payroll_") }
            val firstAcc = accounts.value.firstOrNull() ?: return@launch

            insertTransaction(
                accountId = firstAcc.id,
                amount = amount,
                type = TransactionType.EXPENSE,
                categoryId = payrollCat?.id,
                note = "Salary Paid to employeeId: $note",
                date = System.currentTimeMillis()
            )
        }
    }

    // Vehicle Fuel logs
    fun createVehicle(name: String, fuelType: String, registration: String?) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "veh_${UUID.randomUUID()}"
            val vehicle = Vehicle(id = id, spaceId = spaceId, name = name, fuelType = fuelType, registration = registration)
            repository.insertVehicle(vehicle)
        }
    }

    fun logFuel(vehicleId: String, liters: Double, cost: Double, odometer: Double, fuelType: String, pricePerUnit: Double, mileage: Double) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val id = "fuel_${UUID.randomUUID()}"
            val log = FuelLog(
                id = id,
                spaceId = spaceId,
                vehicleId = vehicleId,
                date = System.currentTimeMillis(),
                liters = liters,
                cost = cost,
                odometer = odometer,
                fuelType = fuelType,
                pricePerUnit = pricePerUnit,
                mileage = mileage
            )
            repository.insertFuelLog(log)

            // Also map fuel log cost to expense transaction item
            val fuelCat = categories.value.firstOrNull { it.name.contains("Fuel") || it.id.startsWith("biz_fuel_") }
            val firstAcc = accounts.value.firstOrNull() ?: return@launch
            insertTransaction(
                accountId = firstAcc.id,
                amount = cost,
                type = TransactionType.EXPENSE,
                categoryId = fuelCat?.id,
                note = "Fuel refilled $liters L at ${baseCurrency.value} $pricePerUnit/unit (Mileage: ${mileage} km)",
                date = System.currentTimeMillis()
            )
        }
    }

    // Client Invoicing Outgoing
    fun createOutgoingInvoice(clientName: String, clientContact: String, items: List<InvoiceItemDraft>, taxRate: Double, dueDate: Long) {
        val spaceId = activeSpaceId.value ?: return
        viewModelScope.launch {
            val invoiceId = "inv_${UUID.randomUUID()}"
            val serial = (invoices.value.size + 1).toString().padStart(4, '0')
            val invNumber = "INV-$serial"
            
            val invoice = Invoice(
                id = invoiceId,
                spaceId = spaceId,
                invoiceNumber = invNumber,
                clientName = clientName,
                clientContact = clientContact,
                dueDate = dueDate,
                taxRate = taxRate,
                status = "SENT"
            )
            repository.insertInvoice(invoice)

            items.forEach { item ->
                val line = InvoiceItem(
                    id = "item_${UUID.randomUUID()}",
                    invoiceId = invoiceId,
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice
                )
                repository.insertInvoiceItem(line)
            }
        }
    }

    fun markInvoiceAsPaid(invoiceId: String) {
        viewModelScope.launch {
            val invoice = repository.getInvoiceById(invoiceId) ?: return@launch
            val updated = invoice.copy(status = "PAID")
            repository.insertInvoice(updated)

            // Calculate Subtotal & tax total
            val items = repository.getInvoiceItems(invoiceId)
            val subtotal = items.sumOf { it.quantity * it.unitPrice }
            val totalWithTax = subtotal * (1 + (invoice.taxRate / 100.0))

            // Auto log INCOME category
            val salesCat = categories.value.firstOrNull { it.name.contains("Sales") || it.id.startsWith("biz_sales_") }
            val firstAcc = accounts.value.firstOrNull() ?: return@launch

            insertTransaction(
                accountId = firstAcc.id,
                amount = totalWithTax,
                type = TransactionType.INCOME,
                categoryId = salesCat?.id,
                note = "Collection from Invoice ${invoice.invoiceNumber} for ${invoice.clientName}"
            )
        }
    }

    // Bank statement reconciliation
    fun toggleReconcileStatus(txnId: String) {
        viewModelScope.launch {
            val txn = repository.getTransactionById(txnId) ?: return@launch
            val updated = txn.copy(
                reconciled = !txn.reconciled,
                reconciledDate = if (!txn.reconciled) System.currentTimeMillis() else null
            )
            repository.insertTransaction(updated)
        }
    }

    // ==========================================
    // UTILS & PARSING
    // ==========================================
    private fun parseDoubleList(jsonStr: String): List<Double> {
        val list = mutableListOf<Double>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(array.getDouble(i))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseStringList(jsonStr: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseSplitDetails(jsonStr: String?): List<SplitShare>? {
        if (jsonStr == null) return null
        val shares = mutableListOf<SplitShare>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                shares.add(
                    SplitShare(
                        memberId = obj.getString("memberId"),
                        shareAmount = obj.getDouble("shareAmount")
                    )
                )
            }
        } catch (_: Exception) {
            return null
        }
        return shares
    }
}

data class DebtRecord(
    val fromMemberId: String,
    val fromMemberName: String,
    val toMemberId: String,
    val toMemberName: String,
    val netOwed: Double
)

data class SplitShare(
    val memberId: String,
    val shareAmount: Double
)

data class InvoiceItemDraft(
    val description: String,
    val quantity: Double,
    val unitPrice: Double
)
