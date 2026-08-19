package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DeductionLog
import com.example.data.model.StockItem
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface StockItemDao {
    @Query("SELECT * FROM stock_items")
    fun getAllStockItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE uid = :uid LIMIT 1")
    suspend fun getStockItemByUid(uid: String): StockItem?

    @Query("SELECT * FROM stock_items WHERE id = :id LIMIT 1")
    suspend fun getStockItemById(id: Int): StockItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItem)

    @Update
    suspend fun updateStockItem(item: StockItem)

    @Query("DELETE FROM stock_items WHERE id = :id")
    suspend fun deleteStockItemById(id: Int)
}

@Dao
interface DeductionLogDao {
    @Query("SELECT * FROM deduction_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DeductionLog>>
    
    @Query("SELECT * FROM deduction_logs WHERE itemId = :itemId ORDER BY timestamp DESC")
    fun getLogsForItem(itemId: Int): Flow<List<DeductionLog>>

    @Insert
    suspend fun insertLog(log: DeductionLog)
}
