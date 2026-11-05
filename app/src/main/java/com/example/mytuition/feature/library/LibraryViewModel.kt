package com.example.mytuition.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytuition.core.di.AppContainer
import com.example.mytuition.core.domain.model.Resource
import com.example.mytuition.core.domain.model.ResourceType
import com.example.mytuition.core.domain.usecase.GetLibraryResourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState

    data class Success(
        val resources: List<Resource>,
        val isFromCache: Boolean = false
    ) : LibraryUiState

    data class Empty(
        val message: String,
        val isFromCache: Boolean = false
    ) : LibraryUiState

    data class Error(
        val message: String
    ) : LibraryUiState
}

class LibraryViewModel(
    private val getLibraryResourcesUseCase: GetLibraryResourcesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _allResources = MutableStateFlow<List<Resource>>(emptyList())
    val allResources: StateFlow<List<Resource>> = _allResources.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<ResourceType?>(null)
    val selectedType: StateFlow<ResourceType?> = _selectedType.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject.asStateFlow()

    private val _selectedBatch = MutableStateFlow<String?>(null)
    val selectedBatch: StateFlow<String?> = _selectedBatch.asStateFlow()

    private var hasLoadedResources = false
    private var isRefreshing = false

    init {
        loadResources()
    }

    fun loadResources() {
        if (isRefreshing) return

        viewModelScope.launch {
            isRefreshing = true

            if (!hasLoadedResources) {
                _uiState.value = LibraryUiState.Loading
            }

            try {
                val result = getLibraryResourcesUseCase()

                result.fold(
                    onSuccess = { resources ->
                        _allResources.value = resources
                        hasLoadedResources = true
                        applyFilters()
                    },
                    onFailure = { exception ->
                        if (_allResources.value.isEmpty()) {
                            _uiState.value = LibraryUiState.Error(
                                exception.message ?: "Unable to load resources."
                            )
                        } else {
                            applyFilters()
                        }
                    }
                )
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (_allResources.value.isEmpty()) {
                    _uiState.value = LibraryUiState.Error(
                        exception.message ?: "Unable to load resources."
                    )
                } else {
                    applyFilters()
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    fun refresh() {
        loadResources()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun selectType(type: ResourceType?) {
        _selectedType.value = type
        applyFilters()
    }

    fun selectSubject(subject: String?) {
        _selectedSubject.value = subject
        applyFilters()
    }

    fun selectBatch(batch: String?) {
        _selectedBatch.value = batch
        applyFilters()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _selectedSubject.value = null
        _selectedBatch.value = null
        applyFilters()
    }

    private fun applyFilters() {
        val all = _allResources.value
        val query = _searchQuery.value.trim()
        val selected = _selectedType.value
        val subject = _selectedSubject.value
        val batch = _selectedBatch.value

        val filtered = all.filter { resource ->
            val matchesSearch = query.isBlank() ||
                resource.title.contains(query, ignoreCase = true)

            val matchesType = selected == null || resource.type == selected
            val matchesSubject = subject == null || resource.subject == subject
            val matchesBatch = batch == null || resource.batch == batch

            matchesSearch && matchesType && matchesSubject && matchesBatch
        }

        val fromCache = all.any { it.isFromCache }

        _uiState.value = if (filtered.isEmpty()) {
            val message = if (all.isEmpty()) {
                "No resources are available yet."
            } else {
                "No resources match your search or filter."
            }

            LibraryUiState.Empty(message, isFromCache = fromCache)
        } else {
            LibraryUiState.Success(filtered, isFromCache = fromCache)
        }
    }

    companion object {
        fun provideFactory(
            useCase: GetLibraryResourcesUseCase =
                AppContainer.getLibraryResourcesUseCase
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                        return LibraryViewModel(useCase) as T
                    }
                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}"
                    )
                }
            }
    }
}
