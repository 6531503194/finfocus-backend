package com.example.finfocus

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = ["*"]) 
class CategoryController(
    private val categoryRepo: CategoryRepository
) {

    @PostMapping("/add")
    fun addCategory(@RequestBody category: Category): ResponseEntity<ApiResponse<Category>> {
        if (category.userId == null || category.name.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "Invalid category data"))
        }
    
        // Optional: Check for duplicate by name + userId
        val existing = categoryRepo.findByUserId(category.userId!!)
            .firstOrNull { it.name.equals(category.name, ignoreCase = true) }
    
        if (existing != null) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "Category already exists"))
        }
    
        val saved = categoryRepo.save(category)
        return ResponseEntity.ok(ApiResponse(true, "Category created", saved))
    }
    

    @DeleteMapping("/user/{userId}/category/{categoryId}")
    fun deleteUserCategory(
        @PathVariable userId: Long,
        @PathVariable categoryId: Long
    ): ResponseEntity<ApiResponse<Void>> {
        val category = categoryRepo.findById(categoryId).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "Category not found"))

        // Prevent deleting global default categories
        if (category.isDefault || category.userId != userId) {
            return ResponseEntity.badRequest().body(ApiResponse(false, "Not allowed to delete this category"))
        }

        categoryRepo.deleteById(categoryId)
        return ResponseEntity.ok(ApiResponse(true, "Category deleted"))
    }


    @GetMapping("/user/{userId}")
    fun getUserCategories(@PathVariable userId: Long): ResponseEntity<ApiResponse<List<Category>>> {
        
        val categories = categoryRepo.findByUserId(userId)
        return ResponseEntity.ok(ApiResponse(true, "User categories", categories))
    }


    @GetMapping
    fun getAllCategories(): ResponseEntity<ApiResponse<List<Category>>> {
        val categories = categoryRepo.findAll()
        return ResponseEntity.ok(ApiResponse(true, "All categories", categories))
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<ApiResponse<Category>> {
        val category = categoryRepo.findById(id)
        return if (category.isPresent) {
            ResponseEntity.ok(ApiResponse(true, "Category found", category.get()))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "Category not found"))
        }
    }

    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Long, @RequestBody updatedCategory: Category): ResponseEntity<ApiResponse<Category>> {
        val category = categoryRepo.findById(id).orElse(null)
            ?: return ResponseEntity.badRequest().body(ApiResponse(false, "Category not found"))

        val newCategory = category.copy(name = updatedCategory.name)
        val saved = categoryRepo.save(newCategory)

        return ResponseEntity.ok(ApiResponse(true, "Category updated", saved))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<ApiResponse<Void>> {
        return if (categoryRepo.existsById(id)) {
            categoryRepo.deleteById(id)
            ResponseEntity.ok(ApiResponse(true, "Category deleted"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "Category not found"))
        }
    }

    
}
