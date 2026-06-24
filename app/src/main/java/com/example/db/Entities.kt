package com.example.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // 'admin' | 'user' | 'consultateur'
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "articles",
    indices = [Index(value = ["reference"], unique = true)]
)
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reference: String,
    val name: String,
    val description: String,
    val quantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // 'client' | 'fournisseur' | 'both'
    val phone: String,
    val email: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_entries")
data class StockEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val articleId: Int,
    val supplierName: String,
    val destination: String, // location code
    val quantity: Int,
    val palletNumber: String, // auto P-001...
    val quality: String,
    val productionDate: String,
    val emballageDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_exits")
data class StockExit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val articleId: Int,
    val clientName: String,
    val matricule: String,
    val supplierName: String,
    val sourceLocation: String,
    val quantity: Int,
    val palletNumber: String,
    val quality: String,
    val productionDate: String,
    val emballageDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "slot_assignments",
    indices = [Index(value = ["locationCode"], unique = true)]
)
data class SlotAssignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationCode: String,
    val articleId: Int?, // null if vider/empty
    val supplierName: String = "", // to easily display supplier name with 🚛
    val palletNumber: String = "", // Pallet number of the occupying product
    val updatedAt: Long = System.currentTimeMillis()
)
