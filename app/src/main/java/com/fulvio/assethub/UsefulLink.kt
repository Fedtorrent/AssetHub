package com.fulvio.assethub

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "useful_links")
data class UsefulLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val url: String,
    val iconName: String // Ora usiamo il nome del file (es. "ic_globe")
)
