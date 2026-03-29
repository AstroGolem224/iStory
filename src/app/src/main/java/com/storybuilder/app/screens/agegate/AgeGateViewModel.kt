package com.storybuilder.app.screens.agegate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.data.local.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgeGateViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    fun confirmAge() {
        viewModelScope.launch {
            settingsDataStore.setAgeGateConfirmed(true)
        }
    }

    fun hasConfirmedAge(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            settingsDataStore.getAgeGateConfirmed().collect { confirmed ->
                onResult(confirmed)
            }
        }
    }
}
