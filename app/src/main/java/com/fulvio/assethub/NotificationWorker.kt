package com.fulvio.assethub

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.*

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        val isTest = inputData.getBoolean("is_test", false)
        val notifyCedole = prefs.getBoolean("notify_cedole", false) || isTest
        val notifyScadenze = prefs.getBoolean("notify_scadenze", false) || isTest
        
        if (!notifyCedole && !notifyScadenze) return Result.success()

        val modeCedole = prefs.getInt("notify_cedole_mode", 1) // 1: Oggi, 2: Domani, 3: Entrambi
        val modeScadenze = prefs.getInt("notify_scadenze_mode", 1)

        val database = AppDatabase.getDatabase(context)
        val allVincoliWithAccount = database.vincoloDao().getAllVincoliWithFullInfo().first()
        
        val helper = NotificationHelper(context)
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val tomorrow = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }

        var notificationId = 100

        for (item in allVincoliWithAccount) {
            val v = item.vincolo
            val banca = item.accountWithBank?.bank?.name ?: "Banca Sconosciuta"
            val calScadenza = Calendar.getInstance().apply {
                timeInMillis = v.dataDecorrenza
                add(Calendar.MONTH, v.durataMesi)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 1. Controllo Scadenze Vincoli (escludendo i Conti Liberi)
            if (notifyScadenze && v.tipo != "Conto Corrente") {
                if ((modeScadenze == 1 || modeScadenze == 3) && calScadenza.timeInMillis == today.timeInMillis) {
                    helper.sendNotification(
                        notificationId++,
                        "Scadenza Vincolo",
                        "Ciao, oggi è in scadenza il vincolo ${v.nome} ($banca) di ${currencyFormatter.format(v.importo)}"
                    )
                }
                if ((modeScadenze == 2 || modeScadenze == 3) && calScadenza.timeInMillis == tomorrow.timeInMillis) {
                    helper.sendNotification(
                        notificationId++,
                        "Scadenza Vincolo",
                        "Ciao, domani è in scadenza il vincolo ${v.nome} ($banca) di ${currencyFormatter.format(v.importo)}"
                    )
                }
            }

            // 2. Controllo Cedole
            if (notifyCedole) {
                val calCedola = Calendar.getInstance().apply {
                    timeInMillis = v.dataDecorrenza
                }
                
                while (calCedola.before(calScadenza)) {
                    val calInizio = calCedola.clone() as Calendar
                    if (v.periodoCedolaMesi > 0) {
                        calCedola.add(Calendar.MONTH, v.periodoCedolaMesi)
                    } else {
                        calCedola.timeInMillis = calScadenza.timeInMillis
                    }
                    
                    if (calCedola.after(calScadenza)) {
                        calCedola.timeInMillis = calScadenza.timeInMillis
                    }
                    
                    val calCheck = (calCedola.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if ((modeCedole == 1 || modeCedole == 3) && calCheck.timeInMillis == today.timeInMillis) {
                        val netto = calcolaCedolaNetta(v, calInizio, calCedola)
                        helper.sendNotification(
                            notificationId++,
                            "Scadenza Cedola",
                            "Ciao, oggi è in scadenza la cedola del vincolo ${v.nome} ($banca) di ${currencyFormatter.format(netto)}"
                        )
                    }
                    if ((modeCedole == 2 || modeCedole == 3) && calCheck.timeInMillis == tomorrow.timeInMillis) {
                        val netto = calcolaCedolaNetta(v, calInizio, calCedola)
                        helper.sendNotification(
                            notificationId++,
                            "Scadenza Cedola",
                            "Ciao, domani è in scadenza la cedola del vincolo ${v.nome} ($banca) di ${currencyFormatter.format(netto)}"
                        )
                    }
                }
            }
        }

        return Result.success()
    }

    private fun calcolaCedolaNetta(vincolo: Vincolo, calInizio: Calendar, calFine: Calendar): Double {
        val lordo: Double
        if (vincolo.tipo == "Conto Deposito") {
            val diffMillis = calFine.timeInMillis - calInizio.timeInMillis
            val giorni = (diffMillis / (24 * 60 * 60 * 1000)).toDouble()
            lordo = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * giorni) / 365.0
        } else {
            val mesi = if (vincolo.periodoCedolaMesi > 0) vincolo.periodoCedolaMesi.toDouble() else vincolo.durataMesi.toDouble()
            lordo = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * mesi) / 12.0
        }
        return lordo * (1.0 - vincolo.tassazione)
    }
}
