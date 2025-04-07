package com.example.finfocus.service

import com.example.dto.*
import com.example.finfocus.Expense
import com.example.finfocus.ExpenseRepository
import com.example.finfocus.User
import com.example.finfocus.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ExpenseService(
    private val expenseRepository: ExpenseRepository
) {
    fun getSummaryForMonth(userId: Long, month: String): MonthlySummaryDto {
        val (start, end) = getMonthRange(month)
    
        val rawSummary = expenseRepository.getMonthlyCategorySummary(userId, start, end)
        val categorySummaries = rawSummary.map {
            val name = it[0] as String
            val amount = it[1] as Double
            CategorySummary(name, amount)
        }
    
        val spent = categorySummaries.sumOf { it.amount }
        val goal = 10000.0
        val saving = goal - spent
    
        return MonthlySummaryDto(
            goal = goal,
            spent = spent,
            saving = saving,
            categories = categorySummaries
        )
    }
    

    fun getExpenseDetails(userId: Long, month: String, category: String): List<ExpenseDetailDto> {
        val (start, end) = getMonthRange(month)
    
        val expenses = expenseRepository.findByUserIdAndCategoryNameAndDateBetween(userId, category, start, end)
    
        return expenses.map {
            ExpenseDetailDto(
                date = it.date,
                amount = it.amount,
                description = it.description
            )
        }
    }
    

    private fun getMonthRange(month: String): Pair<LocalDate, LocalDate> {
        val formatter = DateTimeFormatter.ofPattern("MM-yyyy")
        val monthNumber = monthMap[month] ?: throw IllegalArgumentException("Invalid month")
        val year = 2024 // Or dynamic based on real data
        val start = LocalDate.of(year, monthNumber, 1)
        val end = start.plusMonths(1).minusDays(1)
        return start to end
    }

    companion object {
        private val monthMap = mapOf(
            "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4,
            "May" to 5, "June" to 6, "July" to 7, "Aug" to 8,
            "Sep" to 9, "Oct" to 10, "Nov" to 11, "Dec" to 12
        )
    }
}
