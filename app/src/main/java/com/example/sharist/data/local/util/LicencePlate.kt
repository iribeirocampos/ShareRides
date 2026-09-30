package com.example.sharist.data.local.util

import java.util.Locale

/**
 * Portuguese licence plates are made of three pairs separated by hyphens (XX-XX-XX).
 * Only formats from 2005 onwards are accepted (no vehicle older than 2005 is allowed):
 *  - LL-NN-NN  (2005 - 2020)
 *  - LL-NN-LL  (March 2020 - present)
 */
private val PORTUGUESE_PLATE_REGEX = Regex(
    "^(?:" +
        "[A-Z]{2}-[0-9]{2}-[0-9]{2}|" +
        "[A-Z]{2}-[0-9]{2}-[A-Z]{2}" +
    ")$"
)

/**
 * Normalises a raw licence plate input into the canonical XX-XX-XX form:
 * trims whitespace, upper-cases letters and inserts hyphens between each pair.
 * When the input does not contain exactly six alphanumeric characters the
 * filtered, upper-cased characters are returned without hyphens (never punctuation).
 */
fun normalizeLicencePlate(input: String): String {
    val raw = input.trim().uppercase(Locale.ROOT).filter { it.isLetterOrDigit() }
    if (raw.length != 6) return raw
    return "${raw.substring(0, 2)}-${raw.substring(2, 4)}-${raw.substring(4, 6)}"
}

/**
 * Returns true if [plate] matches a valid Portuguese licence plate format.
 * Accepts input with or without hyphens (e.g. "AA-00-AA" or "aa00aa").
 */
fun isValidPortugueseLicencePlate(plate: String): Boolean {
    return PORTUGUESE_PLATE_REGEX.matches(normalizeLicencePlate(plate))
}
