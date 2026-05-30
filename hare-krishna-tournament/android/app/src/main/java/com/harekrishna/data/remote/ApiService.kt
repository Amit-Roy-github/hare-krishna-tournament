package com.harekrishna.data.remote

import com.harekrishna.data.remote.dto.ChangePasswordRequestDto
import com.harekrishna.data.remote.dto.KrishnaDasDto
import com.harekrishna.data.remote.dto.KrishnaDasStatsDto
import com.harekrishna.data.remote.dto.LoginRequestDto
import com.harekrishna.data.remote.dto.LoginResponseDto
import com.harekrishna.data.remote.dto.NaamSyncDto
import com.harekrishna.data.remote.dto.NaamSyncResponseDto
import com.harekrishna.data.remote.dto.ScoreDto
import com.harekrishna.data.remote.dto.ScoreUpdateDto
import com.harekrishna.data.remote.dto.StatsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("krishnaDas")
    suspend fun listContestants(): List<KrishnaDasDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequestDto)

    @GET("scores")
    suspend fun getScores(): List<ScoreDto>

    // Legacy absolute write (admin / web). Kept for compatibility.
    @POST("scores")
    suspend fun updateScores(@Body updates: List<ScoreUpdateDto>): List<ScoreDto>

    // Idempotent incremental sync: send per-day device high-water marks; the
    // server adds only the new part. See BE/api/naam.js.
    @POST("naam")
    suspend fun syncNaam(@Body body: List<NaamSyncDto>): NaamSyncResponseDto

    // Tournament-wide stats (leaderboard breakdown). Filtered by active
    // contestants. Don't use this for a single user's display.
    @GET("stats")
    suspend fun getStats(): StatsResponseDto

    // Signed-in user's own today / week / lifetime — NOT the leaderboard.
    // Use this anywhere the UI shows the current user's numbers.
    @GET("krishnaDasStats")
    suspend fun getMyStats(): KrishnaDasStatsDto
}
