package com.fulvio.assethub

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val bankId: Long, // FK -> Bank
    val categoryId: Long, // FK -> Category
    val name: String, // Nome del prodotto (es. "Conto Online", "Portafoglio Titoli")
    val lastUpdate: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val frequenzaRendicontazione: String = "Trimestrale",
    val bolloCaricoBanca: Boolean = false
)
