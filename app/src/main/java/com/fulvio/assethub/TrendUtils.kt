package com.fulvio.assethub

import java.util.*

object TrendUtils {

    /**
     * Ritorna i punti temporali per il grafico.
     * Include: data primo movimento, fine dei mesi intermedi, e data odierna.
     */
    fun getTrendPoints(vincoli: List<Vincolo>, maxMonths: Int): List<Long> {
        val activeVincoli = vincoli.filter { !it.isDeleted }
        if (activeVincoli.isEmpty()) return emptyList()
        
        val firstDate = activeVincoli.minOf { it.dataDecorrenza }
        val now = System.currentTimeMillis()
        
        val points = mutableSetOf<Long>()
        
        // 1. Punto di inizio: data precisa del primo movimento
        points.add(normalizeDate(firstDate))
        
        // 2. Punti intermedi: fine dei mesi trascorsi
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = firstDate
        
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = now
        
        // Cicliamo sui mesi fino ad oggi
        while (calendar.before(currentCalendar)) {
            val endOfMonth = calendar.clone() as Calendar
            endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
            endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
            endOfMonth.set(Calendar.MINUTE, 59)
            
            if (endOfMonth.timeInMillis in (firstDate + 1)..<now) {
                points.add(endOfMonth.timeInMillis)
            }
            
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1) // Inizio mese successivo
        }
        
        // 3. Punto finale: oggi
        points.add(normalizeDate(now))
        
        // Limitiamo ai 10 punti più recenti e ordiniamo
        return points.toList().sorted().takeLast(maxMonths)
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

    private fun getEndOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    /**
     * Calcola il valore di un asset (composto da uno o più vincoli) a un determinato timestamp.
     */
    fun calculateValueAtTimestamp(vincoli: List<Vincolo>, targetTimestamp: Long): Double {
        val endOfTargetDay = getEndOfDay(targetTimestamp)
        
        // Filtriamo solo i movimenti avvenuti entro la FINE della data target
        val activeAtDate = vincoli.filter { it.dataDecorrenza <= endOfTargetDay && !it.isDeleted }
        if (activeAtDate.isEmpty()) return 0.0

        val first = activeAtDate.firstOrNull() ?: return 0.0
        val isHistory = InstrumentUtils.isHistoryBased(first)
        val isIncremental = InstrumentUtils.isIncremental(first.tipo, first.strumentoDettaglio)

        return if (isHistory) {
            if (isIncremental) {
                // PAC / ETF: Andamento del PREZZO UNITARIO (Valore Quota)
                // Prendiamo il prezzo dell'ultimo movimento alla data
                activeAtDate.maxByOrNull { it.dataDecorrenza }?.prezzoAcquisto ?: 0.0
            } else {
                // Conti Correnti / Fondi Pensione: Ultimo importo inserito entro la data
                activeAtDate.maxByOrNull { it.dataDecorrenza }?.importo ?: 0.0
            }
        } else {
            // Strumenti Singoli (BTP, Depositi): 
            // Valgono il capitale se il target è tra decorrenza e scadenza
            val v = first // Nelle single view abbiamo un solo vincolo
            val calScadenza = Calendar.getInstance().apply {
                timeInMillis = v.dataDecorrenza
                add(Calendar.MONTH, v.durataMesi)
            }
            if (targetTimestamp >= v.dataDecorrenza && targetTimestamp <= calScadenza.timeInMillis) v.importo else 0.0
        }
    }
}
