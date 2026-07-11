package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phone: String,
    val username: String,
    val passwordHash: String,
    val pin: String,
    val eyeLockConfigured: Boolean,
    val biometricEnabled: Boolean,
    val activeLockType: String, // "PASSWORD", "PIN", "EYELOCK", "BIOMETRIC"
    val isAuthorizedByAdmin: Boolean,
    val otpVerified: Boolean
)

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val rsi: Double,
    val macd: String,
    val peRatio: Double,
    val divYield: Double,
    val updatedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val type: String, // "BUY", "SELL"
    val quantity: Int,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyName: String,
    val patternName: String,
    val buySellDecision: String, // "BUY", "SELL", "HOLD"
    val earnProbability: Double, // percentage e.g. 78.5
    val bestBuyDate: String,
    val bestSellDate: String,
    val analysisText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val base64Image: String? = null // Store base64 thumbnail of chart screenshot
)
