package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String,
    val password: String = "12345", // default for existing or fallback
    val role: Role
)

enum class Role {
    SUPER_ADMIN,
    ADMIN,
    DEDUCTION_OPERATOR,
    GENERAL_STAFF
}

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String = UUID.randomUUID().toString(), // Encoded in QR
    val name: String,
    val category: String,
    val isEssential: Boolean,
    val unitType: String,
    val quantity: Float,
    val reorderThreshold: Float,
    val fabricType: String? = null,
    val color: String? = null,
    val designNameOrNumber: String? = null,
    val dressPart: String? = null,
    val size: String? = null
)

@Entity(tableName = "deduction_logs")
data class DeductionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val userId: Int,
    val quantityDeducted: Float,
    val remainingQuantity: Float,
    val reason: String? = null,
    val invoiceNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
