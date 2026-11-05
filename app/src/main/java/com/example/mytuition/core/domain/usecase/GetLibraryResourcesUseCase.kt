package com.example.mytuition.core.domain.usecase

import com.example.mytuition.core.domain.model.Resource
import com.example.mytuition.core.domain.repository.LibraryRepository

class GetLibraryResourcesUseCase(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(): Result<List<Resource>> {
        return repository.getResources()
    }
}
