package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone")
    fun getUser(phone: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phone = :phone")
    suspend fun getUserSync(phone: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE phone = :phone")
    suspend fun deleteUser(phone: String)
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Update
    suspend fun updateStock(stock: StockEntity)

    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getStock(symbol: String): StockEntity?
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<AnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity)

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteAnalysis(id: Int)
}
