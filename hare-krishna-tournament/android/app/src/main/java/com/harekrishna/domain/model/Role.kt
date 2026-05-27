package com.harekrishna.domain.model

// Server uses lowercase strings (`contestant` / `admin`); we keep that mapping
// explicit so wire deserialization is safe and centralized.
enum class Role(val wire: String) {
    CONTESTANT("contestant"),
    ADMIN     ("admin");

    companion object {
        // Unknown roles fall back to CONTESTANT — never escalate by accident.
        fun fromWire(value: String?): Role = entries.firstOrNull { it.wire == value } ?: CONTESTANT
    }
}
