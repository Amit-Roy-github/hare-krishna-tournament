package com.harekrishna.data.repository

import com.harekrishna.data.mapper.toDomain
import com.harekrishna.data.remote.ApiService
import com.harekrishna.domain.model.Contestant

class ContestantRepository(private val api: ApiService) {
    suspend fun list(): List<Contestant> = api.listContestants().map { it.toDomain() }
}
