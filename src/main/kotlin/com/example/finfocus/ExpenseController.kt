package com.example.finfocus


import com.example.dto.CategorySummary
import com.example.dto.ExpenseDetailDto
import com.example.dto.MonthlySummaryDto
import com.example.finfocus.service.ExpenseService
import com.example.finfocus.UserCategorySummary
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter
import java.time.YearMonth


@RestController
@RequestMapping("/expense")
@CrossOrigin(origins = ["*"]) 
class ExpenseController(
    private val expenseRepo: ExpenseRepository,
    private val userRepo: UserRepository,
    private val categoryRepo: CategoryRepository,
    private val expenseService: ExpenseService
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
    fun getUserCategorySummary(@PathVariable userId: Long): ResponseEntity<ApiResponse<List<UserCategorySummary>>> {
        val user = userRepo.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "User not found"))

            val summaries = expenseRepo.findTotalAmountByCategoryForUser(userId)
            .map {
                UserCategorySummary(
                    categoryId = it[0] as Long,
                    categoryName = it[1] as String,
                    totalAmount = it[2] as Double
                )
            }

        return ResponseEntity.ok(ApiResponse(true, "Category summary retrieved", summaries))
    }


    @GetMapping("/all")
    fun getAllExpensesForMonth(
        @RequestParam userId: Long,
        @RequestParam month: String // format: yyyy-MM
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val expenses = expenseService.getExpensesForUserByMonth(userId, month)
                .map { expense ->
                    val category = categoryRepo.findById(expense.categoryId).orElse(null)
                    listOf(
                        "id" to expense.id,
                        "userId" to expense.userId,
                        "categoryId" to expense.categoryId,
                        "categoryName" to (category?.name ?: "Uncategorized"),
                        "amount" to expense.amount,
                        "description" to expense.description,
                        "date" to expense.date.toString()
                    ).toMap()
                }

            ResponseEntity.ok(mapOf("data" to expenses))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to "Something went wrong"))
        }
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


    @GetMapping("/monthly-summary")
    fun getMonthlySummary(
        @RequestParam userId: Long,
        @RequestParam month: String
    ): ResponseEntity<ApiResponse<MonthlySummaryDto>> {
        val user = userRepo.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "User not found"))

        val (start, end) = getMonthRange(month)
        val rawSummary = expenseRepo.getMonthlyCategorySummary(userId, start, end)

        val categorySummaries = rawSummary.map {
            val name = it[0] as String
            val amount = it[1] as Double
            CategorySummary(name = name, amount = amount)
        }

        val spent = categorySummaries.sumOf { it.amount }
        val goal = user.saving
        val saving = user.balance - spent

        val summary = MonthlySummaryDto(
            goal = goal,
            spent = spent,
            saving = saving,
            categories = categorySummaries
        )

        return ResponseEntity.ok(ApiResponse(true, "Monthly summary fetched", summary))
    }


    @GetMapping("/details")
    fun getExpenseDetails(
        @RequestParam userId: Long,
        @RequestParam month: String,
        @RequestParam category: String
    ): ResponseEntity<ApiResponse<List<ExpenseDetailDto>>> {
        val user = userRepo.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "User not found"))

        val (start, end) = getMonthRange(month)
        val expenses = expenseRepo.findByUserIdAndCategoryNameAndDateBetween(userId, category, start, end)

        val details = expenses.map {
            ExpenseDetailDto(
                date = it.date,
                amount = it.amount,
                description = it.description
            )
        }

        return ResponseEntity.ok(ApiResponse(true, "Expense details fetched", details))
    }


    private fun getMonthRange(month: String): Pair<LocalDate, LocalDate> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val yearMonth = try {
            YearMonth.parse(month.trim(), formatter)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid month format. Expected 'yyyy-MM', got: '$month'")
        }
    
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return start to end
    }
    

    private val monthMap = mapOf(
        "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4,
        "May" to 5, "June" to 6, "July" to 7, "Aug" to 8,
        "Sep" to 9, "Oct" to 10, "Nov" to 11, "Dec" to 12
    )



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
