package com.example.memoir.data

enum class FontSizeOption(val label: String, val scale: Float) {
    SMALL("Small", 0.9f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.12f),
    HUGE("Huge", 1.28f);

    companion object {
        val default = MEDIUM

        fun fromIndex(index: Int): FontSizeOption {
            val safeIndex = index.coerceIn(0, entries.size - 1)
            return entries[safeIndex]
        }
    }
}
