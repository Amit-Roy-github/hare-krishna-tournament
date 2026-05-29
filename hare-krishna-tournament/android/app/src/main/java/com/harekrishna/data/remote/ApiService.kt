package com.harekrishna.data.remote

import com.harekrishna.data.remote.dto.ChangePasswordRequestDto
import com.harekrishna.data.remote.dto.KrishnaDasDto
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

    @GET("stats")
    suspend fun getStats(): StatsResponseDto
}
