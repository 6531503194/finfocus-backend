package com.example.finfocus

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/expense")
@CrossOrigin(origins = ["*"]) 
class ExpenseController(
    private val expenseRepo: ExpenseRepository,
    private val userRepo: UserRepository,
    private val categoryRepo: CategoryRepository
) {

    @PostMapping("/add")
    fun addExpense(@RequestBody expenseRequest: ExpenseRequest): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val user = userRepo.findById(expenseRequest.userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "User not found"))

        val category = categoryRepo.findById(expenseRequest.categoryId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "Category not found"))

        if (expenseRequest.amount <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "Amount must be greater than 0"))
        }

        val date = try {
            expenseRequest.date?.let { LocalDate.parse(it) } ?: LocalDate.now()
        } catch (e: DateTimeParseException) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "Invalid date format. Use yyyy-MM-dd"))
        }

        val expense = Expense(
            userId = user.id!!,
            categoryId = category.id!!,
            amount = expenseRequest.amount,
            description = expenseRequest.description,
            date = date
        )
        expenseRepo.save(expense)

        user.balance -= expense.amount
        userRepo.save(user)

        val responseData = mapOf<String, Any>(
            "expenseId" to (expense.id ?: 0L),
            "newBalance" to user.balance
        )

        return ResponseEntity.ok(ApiResponse(true, "Expense added successfully", responseData))
    }

    @GetMapping("/{userId}/category-summary")
    fun getUserCategorySummary(@PathVariable userId: Long): ResponseEntity<ApiResponse<List<CategorySummary>>> {
        val user = userRepo.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "User not found"))

        val summaries = expenseRepo.findTotalAmountByCategoryForUser(userId)
            .map {
                CategorySummary(
                    categoryId = it[0] as Long,
                    categoryName = it[1] as String,
                    totalAmount = it[2] as Double
                )
            }

        return ResponseEntity.ok(ApiResponse(true, "Category summary retrieved", summaries))
    }


    @GetMapping("/history/{userId}")
    fun getHistory(@PathVariable userId: Long): ResponseEntity<ApiResponse<List<Expense>>> {
        val userExists = userRepo.existsById(userId)
        if (!userExists) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "User not found"))
        }

        val expenses = expenseRepo.findByUserId(userId)
        return ResponseEntity.ok(ApiResponse(true, "Expense history fetched", expenses))
    }
}


data class ExpenseRequest(
    val userId: Long,
    val categoryId: Long,
    val amount: Double,
    val description: String,
    val date: String? = null // accepts "yyyy-MM-dd" format
)


data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
