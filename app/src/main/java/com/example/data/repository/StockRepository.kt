package com.example.data.repository

import com.example.data.dao.DeductionLogDao
import com.example.data.dao.StockItemDao
import com.example.data.dao.UserDao
import com.example.data.model.DeductionLog
import com.example.data.model.StockItem
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class StockRepository(
    private val stockItemDao: StockItemDao,
    private val deductionLogDao: DeductionLogDao
) {
    fun getAllStockItems(): Flow<List<StockItem>> = stockItemDao.getAllStockItems()

    suspend fun getStockItemByUid(uid: String): StockItem? = stockItemDao.getStockItemByUid(uid)

    suspend fun getStockItemById(id: Int): StockItem? = stockItemDao.getStockItemById(id)

    suspend fun addOrUpdateStockItem(item: StockItem) {
        if (item.id == 0) {
            stockItemDao.insertStockItem(item)
        } else {
            stockItemDao.updateStockItem(item)
        }
    }

    suspend fun deleteStockItem(id: Int) {
        stockItemDao.deleteStockItemById(id)
    }

    fun getAllLogs(): Flow<List<DeductionLog>> = deductionLogDao.getAllLogs()

    suspend fun deductStock(itemUid: String, userId: Int, quantityToDeduct: Float = 1f, reason: String? = null, invoiceNumber: String? = null): Boolean {
        val item = stockItemDao.getStockItemByUid(itemUid) ?: return false
        val newQuantity = item.quantity - quantityToDeduct
        
        stockItemDao.updateStockItem(item.copy(quantity = newQuantity))
        deductionLogDao.insertLog(
            DeductionLog(
                itemId = item.id,
                userId = userId,
                quantityDeducted = quantityToDeduct,
                remainingQuantity = newQuantity,
                reason = reason,
                invoiceNumber = invoiceNumber
            )
        )
        return true
    }
}
