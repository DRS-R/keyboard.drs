package com.example.engine

import kotlin.math.min

object LevenshteinEngine {

    /**
     * Damerau-Levenshtein distance handles:
     * - Insertion
     * - Deletion
     * - Substitution
     * - Transposition of adjacent characters (e.g. "teh" -> "the", "fucntion" -> "function")
     */
    fun damerauLevenshteinDistance(source: String, target: String): Int {
        val s = source.lowercase()
        val t = target.lowercase()
        val sLen = s.length
        val tLen = t.length

        if (sLen == 0) return tLen
        if (tLen == 0) return sLen

        val d = Array(sLen + 1) { IntArray(tLen + 1) }

        for (i in 0..sLen) d[i][0] = i
        for (j in 0..tLen) d[0][j] = j

        for (i in 1..sLen) {
            for (j in 1..tLen) {
                val cost = if (s[i - 1] == t[j - 1]) 0 else 1

                d[i][j] = min(
                    min(
                        d[i - 1][j] + 1,      // deletion
                        d[i][j - 1] + 1       // insertion
                    ),
                    d[i - 1][j - 1] + cost     // substitution
                )

                // Transposition
                if (i > 1 && j > 1 && s[i - 1] == t[j - 2] && s[i - 2] == t[j - 1]) {
                    d[i][j] = min(d[i][j], d[i - 2][j - 2] + cost)
                }
            }
        }

        return d[sLen][tLen]
    }

    /**
     * Suggest corrections for a potentially misspelled word from a list of known candidates.
     */
    fun findBestCorrections(
        query: String,
        candidates: List<String>,
        maxDistance: Int = 2,
        limit: Int = 3
    ): List<String> {
        if (query.isBlank() || candidates.isEmpty()) return emptyList()

        return candidates
            .map { candidate -> candidate to damerauLevenshteinDistance(query, candidate) }
            .filter { (_, dist) -> dist <= maxDistance }
            .sortedBy { (_, dist) -> dist }
            .map { (candidate, _) -> candidate }
            .distinct()
            .take(limit)
    }
}
