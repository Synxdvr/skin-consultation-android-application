package com.example.skinconsultform.domain.validation

object FormValidators {

    // ── Phone ─────────────────────────────────────────────────────────
    // Fixed +63 prefix — user enters remaining 10 digits only
    fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length == 10
    }

    fun phoneErrorMessage(phone: String): String? {
        if (phone.isBlank()) return "Phone number is required"
        val digits = phone.filter { it.isDigit() }
        return if (digits.length != 10) "Invalid phone number" else null
    }

    // ── Email ─────────────────────────────────────────────────────────
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true // optional field
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun emailErrorMessage(email: String): String? {
        if (email.isBlank()) return null
        return if (!isValidEmail(email))
            "Please enter a valid email address"
        else null
    }

    // ── Name ──────────────────────────────────────────────────────────
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun nameErrorMessage(name: String): String? {
        return when {
            name.isBlank()        -> "Full name is required"
            name.trim().length < 2 -> "Name must be at least 2 characters"
            name.trim().length > 100 -> "Name is too long"
            else                  -> null
        }
    }

    // ── Step 1 overall ────────────────────────────────────────────────
    fun isStep1Valid(
        name: String,
        dateOfBirth: String,
        phone: String,
        email: String
    ): Boolean {
        return isValidName(name) &&
                dateOfBirth.isNotBlank() &&
                isValidPhone(phone) &&
                isValidEmail(email)
    }
}