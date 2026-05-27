package com.harekrishna.data.mapper

import com.harekrishna.data.remote.dto.KrishnaDasDto
import com.harekrishna.data.remote.dto.LoginResponseDto
import com.harekrishna.domain.model.AuthSession
import com.harekrishna.domain.model.Contestant
import com.harekrishna.domain.model.Role

fun KrishnaDasDto.toDomain() = Contestant(
    id        = id,
    bhaktName = bhaktName,
    role      = Role.fromWire(auth?.role),
)

fun LoginResponseDto.toDomain() = AuthSession(
    bhaktName = bhaktName,
    role      = Role.fromWire(role),
    token     = token,
)
