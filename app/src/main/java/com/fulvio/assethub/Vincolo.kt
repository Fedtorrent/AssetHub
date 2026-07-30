package com.fulvio.assethub

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vincoli")
data class Vincolo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val accountId: Long, // Chiave Esterna -> Account
    val nome: String,
    val dataDecorrenza: Long, // Millisecondi
    val durataMesi: Int,
    val svincolabile: Boolean,
    val importo: Double,
    val tassoVincolo: Double,
    val tassoSvincolo: Double,
    val periodoCedolaMesi: Int,
    val tassazione: Double, // 0.125 o 0.26
    val bolloCaricoBanca: Boolean,
    val tipo: String = "Conto Deposito",
    val note: String? = null,
    val codiceVincolo: Int = 0,
    val interessiMaturatiPrecedenti: Double = 0.0,
    val frequenzaRendicontazione: String = "Trimestrale",
    val bolliConsolidati: Double = 0.0,
    val strumentoDettaglio: String? = null, // Per Conto Titoli: Titolo di Stato, ETF, etc.
    val isDeleted: Boolean = false,
    val quotaVariazione: Double = 0.0,
    val numeroQuote: Double = 0.0,
    val prezzoAcquisto: Double = 0.0
)
