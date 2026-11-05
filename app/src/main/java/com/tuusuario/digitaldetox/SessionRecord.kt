package com.tuusuario.digitaldetox

data class SessionRecord(
    val id: Long,
    val timestamp: Long,
    val durationMs: Long,
    val isDemo: Boolean,
    val formattedDate: String
)
