package com.harekrishna.data.remote

import com.harekrishna.data.remote.dto.ChangePasswordRequestDto
import com.harekrishna.data.remote.dto.KrishnaDasDto
import com.harekrishna.data.remote.dto.LoginRequestDto
import com.harekrishna.data.remote.dto.LoginResponseDto
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

    // Backend's upsertTodaySadhana does $set on naamJaapCount, so we POST
    // the absolute target count (baseline + delta), not the increment.
    @POST("scores")
    suspend fun updateScores(@Body updates: List<ScoreUpdateDto>): List<ScoreDto>

    @GET("stats")
    suspend fun getStats(): StatsResponseDto
}
