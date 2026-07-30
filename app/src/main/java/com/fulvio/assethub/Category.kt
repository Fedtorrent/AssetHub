package com.fulvio.assethub

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val systemType: String, // CORRENTE, DEPOSITO, TITOLI, PENSIONE, IMMOBILI
    val color: Int
) {
    companion object {
        const val TYPE_CORRENTE = "CORRENTE"
        const val TYPE_DEPOSITO = "DEPOSITO"
        const val TYPE_DEPOSITO_LIBERO = "DEPOSITO_LIBERO"
        const val TYPE_TITOLI = "TITOLI"
        const val TYPE_PENSIONE = "PENSIONE"
        const val TYPE_IMMOBILI = "IMMOBILI"
        const val TYPE_CONTANTI = "CONTANTI"
        const val TYPE_VEICOLI = "VEICOLI"
        const val TYPE_GIOIELLI = "GIOIELLI"
        const val TYPE_OGGETTI = "OGGETTI"
    }
}
