package com.example.finfocus

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?
}

interface ExpenseRepository : JpaRepository<Expense, Long> {
    fun findByUserId(userId: Long): List<Expense>

    fun findByUserIdAndDateBetween(userId: Long, startDate: LocalDate, endDate: LocalDate): List<Expense>


    @Query("""
    SELECT c.id, c.name, SUM(e.amount)
    FROM Expense e
    JOIN Category c ON e.categoryId = c.id
    WHERE e.userId = :userId
    GROUP BY c.id, c.name
    """)    
    fun findTotalAmountByCategoryForUser(@Param("userId") userId: Long): List<Array<Any>>

    @Query("""
    SELECT c.name, SUM(e.amount)
    FROM Expense e
    JOIN Category c ON e.categoryId = c.id
    WHERE e.userId = :userId AND e.date BETWEEN :start AND :end
    GROUP BY c.name
    """)
    fun getMonthlyCategorySummary(
        @Param("userId") userId: Long,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<Array<Any>>

    @Query("""
    SELECT e FROM Expense e
    JOIN Category c ON e.categoryId = c.id
    WHERE e.userId = :userId AND c.name = :category AND e.date BETWEEN :start AND :end
    """)
    fun findByUserIdAndCategoryNameAndDateBetween(
        @Param("userId") userId: Long,
        @Param("category") category: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<Expense>
}

interface CategoryRepository : JpaRepository<Category, Long>{
    
    fun findByName(name: String): Category?
    fun findByUserIdOrIsDefaultTrue(userId: Long): List<Category>

    fun findByIsDefaultTrue(): List<Category>
    fun findByUserId(userId: Long): List<Category>
    fun findByUserIdAndName(userId: Long, name: String): Category?
}

