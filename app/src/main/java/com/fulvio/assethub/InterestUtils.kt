package com.fulvio.assethub

import java.util.*
import java.util.concurrent.TimeUnit

object InterestUtils {

    /**
     * Calcola gli interessi (Lordo e Netto) maturati in un periodo, considerando variazioni di saldo e tasso.
     * Basato su anno civile (365 giorni).
     * Ritorna un Pair(Lordo, Netto)
     */
    fun calculateCumulativeInterests(
        vincoli: List<Vincolo>,
        startDate: Long,
        endDate: Long,
        taxRate: Double = 0.26
    ): Pair<Double, Double> {
        if (vincoli.isEmpty()) return Pair(0.0, 0.0)

        // Filtriamo i movimenti utili (fino alla data di fine)
        val sortedVincoli = vincoli.filter { !it.isDeleted && it.dataDecorrenza <= endDate }
            .sortedBy { it.dataDecorrenza }

        if (sortedVincoli.isEmpty()) return Pair(0.0, 0.0)

        var totalLordo = 0.0
        
        // Normalizziamo le date per il calcolo dei giorni (mezzanotte)
        var currentPointer = normalizeDate(startDate)
        val finalEnd = normalizeDate(endDate)

        // Identifichiamo il saldo e il tasso iniziali alla data di inizio
        val initialMovement = sortedVincoli.findLast { normalizeDate(it.dataDecorrenza) <= currentPointer }
        
        var currentSaldo: Double
        var currentTasso: Double

        if (initialMovement != null) {
            currentSaldo = initialMovement.importo
            currentTasso = initialMovement.tassoVincolo
        } else {
            // Se non ci sono movimenti prima dell'inizio, iniziamo con 0
            currentSaldo = 0.0
            currentTasso = 0.0
        }

        // Movimenti che avvengono DOPO la data di inizio
        val futureMovements = sortedVincoli.filter { normalizeDate(it.dataDecorrenza) > currentPointer }

        for (move in futureMovements) {
            val moveDate = normalizeDate(move.dataDecorrenza)
            val days = (moveDate - currentPointer) / (24 * 60 * 60 * 1000)
            
            if (days > 0) {
                totalLordo += (currentSaldo * (currentTasso / 100.0) * days) / 365.0
            }
            
            currentPointer = moveDate
            currentSaldo = move.importo
            currentTasso = move.tassoVincolo
        }

        // Calcoliamo l'ultimo periodo: dall'ultimo movimento alla data di fine (oggi)
        val finalDays = (finalEnd - currentPointer) / (24 * 60 * 60 * 1000)
        if (finalDays > 0) {
            totalLordo += (currentSaldo * (currentTasso / 100.0) * finalDays) / 365.0
        }

        val totalNetto = totalLordo * (1.0 - taxRate)
        return Pair(totalLordo, totalNetto)
    }

    private fun normalizeDate(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Calcola la data di inizio del periodo di remunerazione basandosi sulla periodicità (mesi).
     * Esempio: periodicity = 3 (Trimestrale) -> Ritorna l'inizio del trimestre solare corrente.
     */
    fun getStartOfCalculationDate(periodicity: Int): Long {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        
        // Se la periodicità non è valida o è 12+ mesi, torniamo al 1° Gennaio
        if (periodicity <= 0 || periodicity >= 12) {
            return Calendar.getInstance().apply {
                set(currentYear, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        // Calcoliamo i blocchi partendo da Gennaio (es. ogni 3 mesi: 0, 3, 6, 9)
        val currentMonth = now.get(Calendar.MONTH) // 0-indexed
        val startMonth = (currentMonth / periodicity) * periodicity
        
        return Calendar.getInstance().apply {
            set(currentYear, startMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
