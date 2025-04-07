package com.example.dto

import java.time.LocalDate

data class MonthlySummaryDto(
    val goal: Double,
    val spent: Double,
    val saving: Double,
    val categories: List<CategorySummary>
)

data class CategorySummary(
    val name: String,
    val amount: Double
)


data class ExpenseDetailDto(
    val date: LocalDate,
    val amount: Double,
    val description: String
)