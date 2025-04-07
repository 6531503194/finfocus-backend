package com.example.finfocus

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryRepo: CategoryRepository
) {

    @PostMapping("/add")
    fun addCategory(@RequestBody category: Category): ResponseEntity<ApiResponse<Category>> {
        val saved = categoryRepo.save(category)
        return ResponseEntity.ok(ApiResponse(true, "Category created", saved))
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
