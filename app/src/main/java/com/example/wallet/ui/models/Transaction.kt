package com.example.wallet.ui.models

import com.example.wallet.data.models.TransactionCategory
import com.example.wallet.data.models.TransactionType

data class Transaction(
    val id: Int,
    val description: String,
    val price: Int,
    val date: String,
    val category: TransactionCategory,
    val type: TransactionType,
)