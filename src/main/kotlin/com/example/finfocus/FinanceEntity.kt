package com.example.finfocus

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val username: String,
    val password: String,
    var balance: Double = 0.0,
    val email: String
)

@Entity
@Table(name = "categories")
data class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "is_default")
    val isDefault: Boolean = false, 

    @Column(name = "user_id")
    val userId: Long? = null,

    val name: String
)

@Entity
@Table(name = "expenses")
data class Expense(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: Long,

    val categoryId: Long,

    val amount: Double,
    val description: String,

    val date: LocalDate = LocalDate.now()
)
