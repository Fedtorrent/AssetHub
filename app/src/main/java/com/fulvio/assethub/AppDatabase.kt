package com.fulvio.assethub

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(entities = [Vincolo::class, Account::class, Category::class, Bank::class, UsefulLink::class], version = 16, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vincoloDao(): VincoloDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `useful_links` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `url` TEXT NOT NULL, `iconResId` INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vincoli ADD COLUMN numeroQuote REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE vincoli ADD COLUMN prezzoAcquisto REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE accounts ADD COLUMN bolloCaricoBanca INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Crea la nuova tabella senza saldoLibero
                database.execSQL("CREATE TABLE IF NOT EXISTS `accounts_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bankId` INTEGER NOT NULL, `categoryId` INTEGER NOT NULL, `name` TEXT NOT NULL, `lastUpdate` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `frequenzaRendicontazione` TEXT NOT NULL, `bolloCaricoBanca` INTEGER NOT NULL)")
                
                // 2. Copia i dati dalla vecchia alla nuova
                database.execSQL("INSERT INTO `accounts_new` (`id`, `bankId`, `categoryId`, `name`, `lastUpdate`, `isDeleted`, `frequenzaRendicontazione`, `bolloCaricoBanca`) " +
                        "SELECT `id`, `bankId`, `categoryId`, `name`, `lastUpdate`, `isDeleted`, `frequenzaRendicontazione`, `bolloCaricoBanca` FROM `accounts` ")
                
                // 3. Rimuovi la vecchia tabella
                database.execSQL("DROP TABLE `accounts` ")
                
                // 4. Rinomina quella nuova
                database.execSQL("ALTER TABLE `accounts_new` RENAME TO `accounts` ")
            }
        }

        suspend fun seedCategories(context: Context, dao: VincoloDao) {
             // Rendiamo disponibile un metodo statico per il seeding
             if (dao.getCategoriesCount() == 0) {
                dao.insertCategory(Category(name = "Conto Corrente", systemType = Category.TYPE_CORRENTE, color = Color.parseColor("#4CAF50")))
                dao.insertCategory(Category(name = "Conto Deposito Libero", systemType = Category.TYPE_DEPOSITO_LIBERO, color = Color.parseColor("#00BCD4")))
                dao.insertCategory(Category(name = "Conto Deposito", systemType = Category.TYPE_DEPOSITO, color = Color.parseColor("#0F3ADA")))
                dao.insertCategory(Category(name = "Conto Titoli", systemType = Category.TYPE_TITOLI, color = Color.parseColor("#9C27B0")))
                dao.insertCategory(Category(name = "Fondo Pensione", systemType = Category.TYPE_PENSIONE, color = Color.parseColor("#FF9800")))
                dao.insertCategory(Category(name = "Immobili", systemType = Category.TYPE_IMMOBILI, color = Color.parseColor("#FF8141")))
                dao.insertCategory(Category(name = "Contanti", systemType = Category.TYPE_CONTANTI, color = Color.parseColor("#9E9E9E")))
                dao.insertCategory(Category(name = "Veicoli", systemType = Category.TYPE_VEICOLI, color = Color.parseColor("#212121")))
                dao.insertCategory(Category(name = "Gioielli", systemType = Category.TYPE_GIOIELLI, color = Color.parseColor("#FFD700")))
                dao.insertCategory(Category(name = "Oggetti di valore", systemType = Category.TYPE_OGGETTI, color = Color.parseColor("#795548")))
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "imieivincoli_database"
                )
                .addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                
                // Seeding immediato dopo la creazione
                scope.launch(Dispatchers.IO) {
                    val dao = instance.vincoloDao()
                    if (dao.getCategoriesCount() == 0) {
                        seedCategories(context, dao)
                    }
                    seedSystemBanks(dao)
                }
                
                instance
            }
        }

        private suspend fun seedSystemBanks(dao: VincoloDao) {
            val banks = dao.getAllBanks().first()
            if (banks.none { it.name == "Asset Personali" }) {
                dao.insertBank(Bank(
                    name = "Asset Personali",
                    color = Color.parseColor("#607D8B") // Grigio scuro/neutro
                ))
            }
        }

        suspend fun seedUsefulLinks(dao: VincoloDao) {
            if (dao.getUsefulLinksCount() == 0) {
                val initialLinks = listOf(
                    UsefulLink(title = "FinanzaOnline", description = "News, forum e monitor per il confronto tra prodotti bancari e investimenti.", url = "https://www.finanzaonline.com/", iconResId = R.drawable.ic_finanzaonline),
                    UsefulLink(title = "justETF", description = "Il portale leader in Europa per la ricerca, il confronto e l'analisi dei fondi ETF.", url = "https://www.justetf.com/it/", iconResId = R.drawable.ic_justetf),
                    UsefulLink(title = "Simple Tools for Investors", description = "Suite completa per l'analisi e il calcolo dei rendimenti di obbligazioni e titoli di stato quotati.", url = "https://www.simpletoolsforinvestors.eu/index.shtml#", iconResId = R.drawable.ic_sti),
                    UsefulLink(title = "Calcolatore Rendimento", description = "Strumento di calcolo per determinare il rendimento netto reale di obbligazioni e titoli di stato.", url = "https://davideberti.it/calcolatore-rendimento-obbligazioni-e-titoli-di-stato", iconResId = R.drawable.ic_dberti),
                    UsefulLink(title = "Calcolo Rendimenti BFP", description = "Calcolatore ufficiale CDP per conoscere interessi e valore di rimborso dei Buoni Fruttiferi Postali.", url = "https://www.cdp.it/sitointernet/it/calcolo_dei_rendimenti.page", iconResId = R.drawable.ic_cdp),
                    UsefulLink(title = "Deposifire", description = "Aggregatore indipendente per confrontare i tassi dei migliori conti deposito e conti correnti remunerati.", url = "https://www.deposifire.com/?p=conti", iconResId = R.drawable.ic_df),
                    UsefulLink(title = "Curvo Backtest", description = "Simula l'andamento storico di un PAC o di un portafoglio di ETF.", url = "https://curvo.eu/backtest/it", iconResId = R.drawable.ic_curvobacktest),
                    UsefulLink(title = "Morningstar Italia", description = "Dati, rating e analisi approfondite su Fondi Comuni ed ETF.", url = "https://www.morningstar.it/", iconResId = R.drawable.ic_morningstar)
                )
                for (link in initialLinks) {
                    dao.insertUsefulLink(link)
                }
            }
        }

        // Metodo legacy per compatibilità temporanea se necessario
        fun getDatabase(context: Context): AppDatabase {
            return getDatabase(context, CoroutineScope(Dispatchers.IO))
        }
    }
}
