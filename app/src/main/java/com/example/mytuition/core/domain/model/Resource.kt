package com.example.mytuition.core.domain.model

data class Resource(
    val id: String,
    val title: String,
    val type: ResourceType,
    val url: String?,
    val sizeBytes: Long?,
    val isFromCache: Boolean = false,
    val subject: String? = null,
    val batch: String? = null
)
