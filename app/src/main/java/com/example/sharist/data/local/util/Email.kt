package com.example.sharist.data.local.util

/**
 * Email validation, kept as a pure-regex util (no Android framework dependency) so it
 * stays unit-testable, mirroring [isValidPortugueseLicencePlate].
 *
 * Accepts the common `local-part@domain.tld` shape:
 *  - local part: letters, digits and `. _ % + -`
 *  - domain: dot-separated labels of letters/digits/hyphens
 *  - a top-level domain of at least two letters
 */
private val EMAIL_REGEX = Regex(
    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
)

/**
 * Normalises a raw email input: trims surrounding whitespace and lower-cases it.
 * Kotlin's no-arg [String.lowercase] uses the locale-independent invariant locale
 * (unlike Java's `toLowerCase()`), so this is safe across locales (e.g. Turkish).
 */
fun normalizeEmail(input: String): String = input.trim().lowercase()

/**
 * Returns true if [email] matches a valid email format.
 * Tolerates surrounding whitespace and case (e.g. " MyEmail@Gmail.com ").
 */
fun isValidEmail(email: String): Boolean {
    return EMAIL_REGEX.matches(normalizeEmail(email))
}
