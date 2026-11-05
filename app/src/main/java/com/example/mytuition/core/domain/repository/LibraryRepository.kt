package com.example.mytuition.core.domain.repository

import com.example.mytuition.core.domain.model.Resource

interface LibraryRepository {
    suspend fun getResources(): Result<List<Resource>>
}
