package com.harekrishna.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Contestant list ─────────────────────────────

@Serializable
data class KrishnaDasDto(
    @SerialName("_id") val id: String,
    val bhaktName: String,
    val includeInKeliKunj: Boolean = true,
    val auth: AuthSubDto? = null,
)

@Serializable
data class AuthSubDto(
    val role: String = "contestant",
)

// ── Auth ────────────────────────────────────────

@Serializable
data class LoginRequestDto(
    val bhaktName: String,
    val password:  String,
)

@Serializable
data class LoginResponseDto(
    val token:     String,
    val bhaktName: String,
    val role:      String,
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword:     String,
)

// ── Scores & stats ──────────────────────────────

@Serializable
data class ScoreDto(
    val bhaktName: String,
    val todayNaam: Int = 0,
    val score:     Int = 0,
)

@Serializable
data class ScoreUpdateDto(
    val bhaktName:     String,
    val naamJaapCount: Int,
)

// ── Incremental naam sync (POST /api/naam) ──────
// `total` is this device's absolute per-day high-water mark; the server adds
// only the new part (total − its stored snapshot), so retries can't double-count.

@Serializable
data class NaamSyncDto(
    val deviceId: String,
    val date:     String,   // "YYYY-MM-DD" UTC — the day these chants belong to
    val total:    Int,      // device's cumulative count for that day
)

@Serializable
data class NaamSyncResponseDto(
    val days:   List<NaamSyncDayDto> = emptyList(),
    val scores: List<ScoreDto>       = emptyList(),
)

@Serializable
data class NaamSyncDayDto(
    val date:          String? = null,
    val naamJaapCount: Int     = 0,
)

@Serializable
data class StatsResponseDto(
    val overall: List<OverallStatDto> = emptyList(),
)

@Serializable
data class OverallStatDto(
    val bhaktName:         String,
    val totalNaamCount:    Int = 0,   // Monday → today
    val lifetimeNaamCount: Int = 0,   // since the contestant joined
)
