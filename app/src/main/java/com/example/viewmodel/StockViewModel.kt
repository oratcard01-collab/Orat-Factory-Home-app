package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.StockItem
import com.example.data.repository.StockRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(private val stockRepository: StockRepository) : ViewModel() {
    
    val allStockItems: StateFlow<List<StockItem>> = stockRepository.getAllStockItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val deductionLogs = stockRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addOrUpdateStockItem(item: StockItem) {
        viewModelScope.launch {
            stockRepository.addOrUpdateStockItem(item)
        }
    }

    fun deleteStockItem(id: Int) {
        viewModelScope.launch {
            stockRepository.deleteStockItem(id)
        }
    }

    fun deductStock(itemUid: String, userId: Int, quantityToDeduct: Float = 1f, reason: String? = null, invoiceNumber: String? = null, onResult: (Boolean, StockItem?) -> Unit) {
        viewModelScope.launch {
            val success = stockRepository.deductStock(itemUid, userId, quantityToDeduct, reason, invoiceNumber)
            if (success) {
                val updatedItem = stockRepository.getStockItemByUid(itemUid)
                onResult(true, updatedItem)
            } else {
                onResult(false, null)
            }
        }
    }
    
    suspend fun getStockItemByUid(uid: String): StockItem? {
        return stockRepository.getStockItemByUid(uid)
    }
}
