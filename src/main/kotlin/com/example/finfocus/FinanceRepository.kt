package com.example.finfocus

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?
}

interface ExpenseRepository : JpaRepository<Expense, Long> {
    fun findByUserId(userId: Long): List<Expense>

    @Query("""
    SELECT c.id, c.name, SUM(e.amount)
    FROM Expense e
    JOIN Category c ON e.categoryId = c.id
    WHERE e.userId = :userId
    GROUP BY c.id, c.name
    """)    
    fun findTotalAmountByCategoryForUser(@Param("userId") userId: Long): List<Array<Any>>


}

interface CategoryRepository : JpaRepository<Category, Long>{
    fun findByName(name: String): Category?
}

