package com.example.finfocus

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = ["*"]) 
class UserController(private val userRepo: UserRepository) {

    @PostMapping("/register")
    fun register(@RequestBody user: User): ResponseEntity<String> {
        if (userRepo.findByEmail(user.email) != null) {
            return ResponseEntity.badRequest().body("Email already registered")
        }

        if (userRepo.findByUsername(user.username) != null) {
            return ResponseEntity.badRequest().body("Username already taken")
        }

        userRepo.save(user)
        return ResponseEntity.ok("User ${user.username} registered successfully with email ${user.email}!")
    }


    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val user = userRepo.findByEmail(request.email)
    
        return if (user != null && user.password == request.password) {
            val responseBody = mapOf(
                "message" to "Login successful",
                "userId" to user.id  
            )
            ResponseEntity.ok(responseBody)
        } else {
            val errorBody = mapOf(
                "message" to "Invalid email or password",
                "userId" to null
            )
            ResponseEntity.badRequest().body(errorBody)
        }
    }
    


    @PutMapping("/{id}/balance")
    fun updateBalance(@PathVariable id: Long, @RequestParam amount: Double): ResponseEntity<String> {
        val user = userRepo.findById(id).orElse(null) ?: return ResponseEntity.badRequest().body("User not found")
        user.balance += amount
        userRepo.save(user)
        return ResponseEntity.ok("Balance updated: ${user.balance}")
    }

    @PutMapping("/{id}/edit-balance")
    fun editBalance(@PathVariable id: Long, @RequestParam balance: Double): ResponseEntity<String> {
        val user = userRepo.findById(id).orElse(null)
            ?: return ResponseEntity.badRequest().body("User not found")

        user.balance = balance
        userRepo.save(user)

        return ResponseEntity.ok("Balance updated to: ${user.balance}")
    }



    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<User> =
        userRepo.findById(id).map { ResponseEntity.ok(it) }
            .orElseGet { ResponseEntity.notFound().build() }

}

data class LoginRequest(val email: String, val password: String)
