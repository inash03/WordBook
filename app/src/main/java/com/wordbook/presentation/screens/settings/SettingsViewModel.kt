package com.wordbook.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.data.preferences.AppPreferences
import com.wordbook.data.preferences.UserPreferencesRepository
import com.wordbook.domain.model.Label
import com.wordbook.domain.usecase.label.DeleteLabelUseCase
import com.wordbook.domain.usecase.label.GetLabelsUseCase
import com.wordbook.domain.usecase.label.SaveLabelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val getLabelsUseCase: GetLabelsUseCase,
    private val saveLabelUseCase: SaveLabelUseCase,
    private val deleteLabelUseCase: DeleteLabelUseCase
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val labels: StateFlow<List<Label>> = getLabelsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkMode(enabled) }
    }

    fun setFollowSystem(follow: Boolean) {
        viewModelScope.launch { preferencesRepository.setFollowSystemTheme(follow) }
    }

    fun setAccentColor(colorName: String) {
        viewModelScope.launch { preferencesRepository.setAccentColor(colorName) }
    }

    fun setDefaultTestMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setDefaultTestMode(mode) }
    }

    fun saveLabel(label: Label) {
        viewModelScope.launch { saveLabelUseCase(label) }
    }

    fun deleteLabel(id: Long) {
        viewModelScope.launch { deleteLabelUseCase(id) }
    }
}
