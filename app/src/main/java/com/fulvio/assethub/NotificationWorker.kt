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
        val notifyScadenze = prefs.getBoolean("notify_scadenze", false) || isTest
        
        if (!notifyScadenze) return Result.success()

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

            // Controllo Scadenze Vincoli (escludendo i Conti Liberi)
            if (v.tipo != "Conto Corrente") {
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
        }

        return Result.success()
    }
}
