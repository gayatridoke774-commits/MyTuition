package com.example.mytuition.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytuition.core.data.repository.HomeRepository
import com.example.mytuition.core.domain.model.HomeData
import com.example.mytuition.core.domain.model.TimelineSessionItem
import com.example.mytuition.core.domain.model.WeekDayItem
import com.example.mytuition.core.designsystem.components.NextClassInfo
import com.example.mytuition.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val data: HomeData,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null
    ) : HomeUiState {
        val studentName: String get() = data.studentName
        val className: String get() = data.className
        val tuitionName: String get() = data.tuitionName
        val nextClass: NextClassInfo? get() = data.nextClass
        val weekDates: List<WeekDayItem> get() = data.weekDates
        val timeline: List<TimelineSessionItem> get() = data.timeline
        val isFromCache: Boolean get() = data.isFromCache
    }
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    init {
        loadHomeData(currentDate)
    }

    fun loadHomeData(date: String = currentDate) {
        currentDate = date
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = homeRepository.getHomeData(date)
            result.onSuccess { data ->
                _uiState.value = HomeUiState.Success(data)
            }.onFailure { error ->
                _uiState.value = HomeUiState.Error(
                    error.localizedMessage ?: "Failed to load tuition schedule. Please retry."
                )
            }
        }
    }

    fun refreshHomeData() {
        val existingData = (_uiState.value as? HomeUiState.Success)?.data ?: return

        viewModelScope.launch {
            _uiState.value = HomeUiState.Success(
                data = existingData,
                isRefreshing = true
            )

            val result = homeRepository.getHomeData(currentDate)

            result.onSuccess { data ->
                _uiState.value = HomeUiState.Success(data)
            }.onFailure { error ->
                _uiState.value = HomeUiState.Success(
                    data = existingData,
                    refreshError = error.localizedMessage
                        ?: "Unable to refresh. Showing previously loaded data."
                )
            }
        }
    }
    fun selectDate(date: String) {
        if (date == currentDate) return
        loadHomeData(date)
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutSuccess()
        }
    }

    companion object {
        fun provideFactory(
            homeRepository: HomeRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(homeRepository, authRepository) as T
                }
            }
    }
}
