package com.fulvio.assethub

object InstrumentUtils {
    val historyBasedDettagli = setOf("Azioni", "BTC", "ETC", "ETF", "ETN", "Fondo")

    fun isIncremental(type: String, detail: String?): Boolean {
        return type == "Conto Titoli" && detail in historyBasedDettagli
    }

    fun isHistoryBased(vincolo: Vincolo): Boolean {
        if (vincolo.tipo == "Conto Corrente" || 
            vincolo.tipo == "Conto Deposito Libero" ||
            vincolo.tipo == "Fondo Pensione" || 
            vincolo.tipo == "Immobili" ||
            vincolo.tipo == "Contanti" ||
            vincolo.tipo == "Veicoli" ||
            vincolo.tipo == "Gioielli" ||
            vincolo.tipo == "Oggetti di valore") {
            return true
        }
        
        if (vincolo.tipo == "Conto Titoli" && vincolo.strumentoDettaglio in historyBasedDettagli) {
            return true
        }
        
        return false
    }

    fun calculatePMC(vincoli: List<Vincolo>): Double {
        val buyVincoli = vincoli.filter { it.numeroQuote > 0 && !it.isDeleted }
        if (buyVincoli.isEmpty()) return 0.0
        val totalInvested = buyVincoli.sumOf { it.numeroQuote * it.prezzoAcquisto }
        val totalQuotes = buyVincoli.sumOf { it.numeroQuote }
        return if (totalQuotes > 0) totalInvested / totalQuotes else 0.0
    }

    fun calculateTotalQuotes(vincoli: List<Vincolo>): Double {
        return vincoli.filter { !it.isDeleted }.sumOf { it.numeroQuote }
    }

    /**
     * Calcola il saldo totale e il conteggio effettivo degli strumenti per un account.
     * Gestisce il raggruppamento per gli strumenti a storico (Azioni, ETF, etc.)
     */
    fun calculateAccountStats(
        systemType: String?,
        activeVincoli: List<Vincolo>
    ): Pair<Double, Int> {
        val now = System.currentTimeMillis()
        val type = systemType ?: Category.TYPE_DEPOSITO
        
        // Filtriamo solo gli strumenti effettivamente attivi ad oggi (nè futuri nè scaduti)
        val currentlyActive = activeVincoli.filter { vincolo ->
            val calScadenza = java.util.Calendar.getInstance().apply {
                timeInMillis = vincolo.dataDecorrenza
                add(java.util.Calendar.MONTH, vincolo.durataMesi)
            }
            val isExpired = vincolo.durataMesi > 0 && calScadenza.timeInMillis < now
            val isFuture = vincolo.dataDecorrenza > now
            
            !isExpired && !isFuture
        }

        return if (type == Category.TYPE_CORRENTE || type == Category.TYPE_DEPOSITO_LIBERO || type == Category.TYPE_PENSIONE || 
            type == Category.TYPE_IMMOBILI || type == Category.TYPE_CONTANTI || type == Category.TYPE_VEICOLI || 
            type == Category.TYPE_GIOIELLI || type == Category.TYPE_OGGETTI) {
            // Per questi tipi, il saldo è l'ultimo valore inserito nello storico
            val lastSaldo = currentlyActive.maxByOrNull { it.dataDecorrenza }?.importo ?: 0.0
            Pair(lastSaldo, currentlyActive.size) 
        } else {
            // Per Deposito e Titoli
            val historyGroups = currentlyActive.filter { isHistoryBased(it) }
                .groupBy { it.nome }
                .mapValues { entry -> 
                    val items = entry.value
                    if (isIncremental("Conto Titoli", items.first().strumentoDettaglio)) {
                        // Per PAC/ETF: Valorizzazione = Totale Quote * Ultimo Prezzo
                        val totalQuotes = items.sumOf { it.numeroQuote }
                        val lastPrice = items.maxBy { it.dataDecorrenza }.prezzoAcquisto
                        totalQuotes * lastPrice
                    } else {
                        // Per altri: Ultimo importo
                        items.maxBy { it.dataDecorrenza }.importo
                    }
                }
            
            val singleTotal = currentlyActive.filter { !isHistoryBased(it) }
                .sumOf { it.importo }
            
            val totalBalance = historyGroups.values.sum() + singleTotal
            
            val uniqueHistoryCount = historyGroups.size
            val singlesCount = currentlyActive.count { !isHistoryBased(it) }
            
            Pair(totalBalance, uniqueHistoryCount + singlesCount)
        }
    }
}
