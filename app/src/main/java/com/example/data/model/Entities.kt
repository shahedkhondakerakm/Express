package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SpaceType {
    PERSONAL,
    FAMILY,
    BUSINESS
}

enum class AccountType {
    CASH,
    BANK,
    CREDIT_CARD,
    E_WALLET,
    OTHER
}

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Entity(
    tableName = "spaces",
    indices = [Index(value = ["type"])]
)
data class Space(
    @PrimaryKey val id: String,
    val name: String,
    val type: SpaceType,
    val currency: String, // Base currency like USD, EUR, BDT, JPY
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class Account(
    @PrimaryKey val id: String,
    val spaceId: String,
    val name: String,
    val type: AccountType,
    val currency: String, // Can differ from space's base currency
    val initialBalance: Double,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"]), Index(value = ["parentCategoryId"])]
)
data class Category(
    @PrimaryKey val id: String,
    val spaceId: String,
    val name: String,
    val icon: String, // Icon identifier from a curated set
    val color: Int, // Hex integer color code
    val isIncome: Boolean, // True for income, false for expense
    val parentCategoryId: String? = null, // Nested subcategory support
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["spaceId"]),
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["isDeleted"])
    ]
)
data class Transaction(
    @PrimaryKey val id: String,
    val spaceId: String,
    val accountId: String,
    val toAccountId: String? = null, // For TRANSFERS, this is the destination account
    val amount: Double,
    val type: TransactionType,
    val categoryId: String? = null, // Nullable for transfers or custom categorizations
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val refundOfTransactionId: String? = null, // Reference linked refund
    val paymentMethod: String? = null,
    val attachmentPath: String? = null, // Image photo file path stored locally
    val transferFee: Double? = null,

    // Phase 2 - Family space attributes (Forward-Compatible & Nullable)
    val paidByMemberId: String? = null,
    val splitType: String? = null, // EQUAL, PERCENTAGE, CUSTOM
    val splitDetailsJson: String? = null, // Serialized contributions

    // Phase 3 - Business space attributes (Forward-Compatible & Nullable)
    val vendorName: String? = null,
    val invoiceNumber: String? = null,
    val taxAmount: Double? = null,
    val taxRate: Double? = null,
    val isPaid: Boolean = true, // false for accounts payable
    val reconciled: Boolean = false, // Bank Reconciliation status
    val reconciledDate: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"]), Index(value = ["nextRunDate"]), Index(value = ["isActive"])]
)
data class RecurringRule(
    @PrimaryKey val id: String,
    val spaceId: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String? = null,
    val accountId: String,
    val toAccountId: String? = null,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val startDate: Long,
    val endDate: Long? = null,
    val autoLog: Boolean = true, // Whether to auto create or notify only
    val nextRunDate: Long,
    val isActive: Boolean = true,
    val note: String = "",
    
    // Family Split recurring parameters
    val splitType: String? = null,
    val splitDetailsJson: String? = null
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"]), Index(value = ["categoryId"])]
)
data class Budget(
    @PrimaryKey val id: String,
    val spaceId: String,
    val categoryId: String? = null, // Null for an overall space budget
    val limitAmount: Double,
    val period: String, // WEEKLY, MONTHLY, YEARLY
    val alertThresholdsJson: String = "[0.8, 1.0]", // Alert levels e.g. 80%, 100%
    val firedThresholdsJson: String = "[]", // Thresholds already triggered in current period to prevent repeat alerts
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class SavingsGoal(
    @PrimaryKey val id: String,
    val spaceId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long? = null,
    val icon: String,
    val color: Int,
    val isCompleted: Boolean = false,
    val isShared: Boolean = false, // Shared by family members in Family Space
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// Phase 2 Entities (Family Space add-ons)
// ==========================================

@Entity(
    tableName = "family_members",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class FamilyMember(
    @PrimaryKey val id: String,
    val spaceId: String,
    val name: String,
    val role: String // ADMIN, MEMBER, CONTRIBUTOR
)

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class Settlement(
    @PrimaryKey val id: String,
    val spaceId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Double,
    val currency: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

// ==========================================
// Phase 3 Entities (Business Space add-ons)
// ==========================================

@Entity(
    tableName = "employees",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class Employee(
    @PrimaryKey val id: String,
    val spaceId: String,
    val name: String,
    val role: String,
    val baseSalary: Double,
    val paymentFrequency: String, // WEEKLY, MONTHLY
    val startDate: Long,
    val isActive: Boolean = true
)

@Entity(
    tableName = "salary_payments",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"]), Index(value = ["employeeId"])]
)
data class SalaryPayment(
    @PrimaryKey val id: String,
    val spaceId: String,
    val employeeId: String,
    val amount: Double,
    val payDate: Long = System.currentTimeMillis(),
    val type: String, // REGULAR, ADVANCE, BONUS, DEDUCTION
    val note: String = ""
)

@Entity(
    tableName = "vehicles",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class Vehicle(
    @PrimaryKey val id: String,
    val spaceId: String,
    val name: String,
    val fuelType: String, // Petrol, Octane, Diesel, Hybrid, Electric
    val registration: String? = null
)

@Entity(
    tableName = "fuel_logs",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"]), Index(value = ["vehicleId"])]
)
data class FuelLog(
    @PrimaryKey val id: String,
    val spaceId: String,
    val vehicleId: String,
    val date: Long = System.currentTimeMillis(),
    val liters: Double,
    val cost: Double,
    val odometer: Double,
    val fuelType: String,
    val pricePerUnit: Double = 0.0,
    val mileage: Double = 0.0
)

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"]), Index(value = ["dueDate"]), Index(value = ["status"])]
)
data class Invoice(
    @PrimaryKey val id: String,
    val spaceId: String,
    val invoiceNumber: String, // Prefix-0001 format
    val clientName: String,
    val clientContact: String,
    val dueDate: Long,
    val date: Long = System.currentTimeMillis(),
    val taxRate: Double = 0.0, // Percentage
    val status: String = "DRAFT" // DRAFT, SENT, PAID, OVERDUE
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItem(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val description: String,
    val quantity: Double,
    val unitPrice: Double
)
