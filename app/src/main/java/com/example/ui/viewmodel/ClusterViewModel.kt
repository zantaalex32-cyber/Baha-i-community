package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClusterInfo
import com.example.data.model.CoreActivity
import com.example.data.model.EventSchedule
import com.example.data.model.Neighborhood
import com.example.data.model.UserProfile
import com.example.data.repository.ClusterRepository
import com.example.data.security.CryptoManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class DashboardTab(val title: String) {
    OVERVIEW("Overview"),
    SCHEDULE("Events & Feasts"),
    ACTIVITIES("Core Activities"),
    NEIGHBORHOODS("Neighborhoods"),
    CLUSTER_INFO("Cluster & Goals"),
    ENCRYPTION("Security & Crypto")
}

class ClusterViewModel(
    private val repository: ClusterRepository
) : ViewModel() {

    private val currentUserId: String
        get() = Firebase.auth.currentUser?.uid ?: ""

    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load profile")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val clustersState: StateFlow<UiState<List<ClusterInfo>>> = repository.observeClusters()
        .map<List<ClusterInfo>, UiState<List<ClusterInfo>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe clusters")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val neighborhoodsState: StateFlow<UiState<List<Neighborhood>>> = repository.observeNeighborhoods()
        .map<List<Neighborhood>, UiState<List<Neighborhood>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe neighborhoods")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val activitiesState: StateFlow<UiState<List<CoreActivity>>> = repository.observeActivities()
        .map<List<CoreActivity>, UiState<List<CoreActivity>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe activities")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val eventsState: StateFlow<UiState<List<EventSchedule>>> = repository.observeEvents()
        .map<List<EventSchedule>, UiState<List<EventSchedule>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe events")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Navigation and filtering state
    private val _selectedTab = MutableStateFlow(DashboardTab.OVERVIEW)
    val selectedTab: StateFlow<DashboardTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedNeighborhoodFilter = MutableStateFlow("All")
    val selectedNeighborhoodFilter: StateFlow<String> = _selectedNeighborhoodFilter.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Automatically check if sample data is needed so the user lands on a vibrant cluster
        viewModelScope.launch {
            try {
                repository.seedSampleDataIfEmpty()
            } catch (e: Exception) {
                // If permission issue or already populated, gracefully continue
            }
        }
    }

    fun selectTab(tab: DashboardTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setNeighborhoodFilter(neighborhood: String) {
        _selectedNeighborhoodFilter.value = neighborhood
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun saveEvent(event: EventSchedule, encryptNotes: Boolean) {
        viewModelScope.launch {
            try {
                val preparedNotes = if (encryptNotes && event.encryptedCoordinationNotes.isNotBlank()) {
                    CryptoManager.encrypt(event.encryptedCoordinationNotes)
                } else {
                    event.encryptedCoordinationNotes
                }
                repository.saveEvent(event.copy(encryptedCoordinationNotes = preparedNotes))
                _userMessage.value = "Event schedule saved successfully"
            } catch (e: Exception) {
                _userMessage.value = "Error saving event: ${e.localizedMessage}"
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteEvent(id)
                _userMessage.value = "Event deleted"
            } catch (e: Exception) {
                _userMessage.value = "Error deleting event: ${e.localizedMessage}"
            }
        }
    }

    fun saveCoreActivity(activity: CoreActivity) {
        viewModelScope.launch {
            try {
                repository.saveCoreActivity(activity)
                _userMessage.value = "Activity updated"
            } catch (e: Exception) {
                _userMessage.value = "Error saving activity: ${e.localizedMessage}"
            }
        }
    }

    fun deleteCoreActivity(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteCoreActivity(id)
                _userMessage.value = "Activity removed"
            } catch (e: Exception) {
                _userMessage.value = "Error deleting activity: ${e.localizedMessage}"
            }
        }
    }

    fun saveNeighborhood(neighborhood: Neighborhood) {
        viewModelScope.launch {
            try {
                repository.saveNeighborhood(neighborhood)
                _userMessage.value = "Neighborhood saved"
            } catch (e: Exception) {
                _userMessage.value = "Error saving neighborhood: ${e.localizedMessage}"
            }
        }
    }

    fun deleteNeighborhood(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteNeighborhood(id)
                _userMessage.value = "Neighborhood removed"
            } catch (e: Exception) {
                _userMessage.value = "Error deleting neighborhood: ${e.localizedMessage}"
            }
        }
    }

    fun saveCluster(cluster: ClusterInfo) {
        viewModelScope.launch {
            try {
                repository.saveCluster(cluster)
                _userMessage.value = "Cluster goals updated"
            } catch (e: Exception) {
                _userMessage.value = "Error updating cluster: ${e.localizedMessage}"
            }
        }
    }

    fun saveUserProfile(profile: UserProfile, encryptNotes: Boolean) {
        viewModelScope.launch {
            try {
                val preparedNotes = if (encryptNotes && profile.encryptedNotes.isNotBlank()) {
                    CryptoManager.encrypt(profile.encryptedNotes)
                } else {
                    profile.encryptedNotes
                }
                repository.saveUserProfile(profile.copy(encryptedNotes = preparedNotes))
                _userMessage.value = "Profile updated"
            } catch (e: Exception) {
                _userMessage.value = "Error updating profile: ${e.localizedMessage}"
            }
        }
    }

    fun reseedSampleData() {
        viewModelScope.launch {
            try {
                repository.seedSampleDataIfEmpty()
                _userMessage.value = "Sample cluster data refreshed"
            } catch (e: Exception) {
                _userMessage.value = "Failed to load sample data: ${e.localizedMessage}"
            }
        }
    }

    companion object {
        fun provideFactory(repository: ClusterRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ClusterViewModel(repository) as T
                }
            }
    }
}
