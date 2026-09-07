package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.QuickDrawApplication
import com.example.data.AppSettings
import com.example.data.GestureRepository
import com.example.model.ActionType
import com.example.model.GestureEntity
import com.example.service.QuickDrawOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuickDrawMainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GestureRepository = (application as QuickDrawApplication).repository

    val gestures: StateFlow<List<GestureEntity>> = repository.allGestures
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isServiceRunning = MutableStateFlow(QuickDrawOverlayService.isRunning)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(Settings.canDrawOverlays(application))
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Dialog state for creating a new gesture
    data class CreateGestureState(
        val isOpen: Boolean = false,
        val actionType: ActionType = ActionType.SYSTEM,
        val target: String = "FLASHLIGHT_TOGGLE",
        val label: String = "Latarka",
        val name: String = ""
    )

    private val _createGestureState = MutableStateFlow(CreateGestureState())
    val createGestureState: StateFlow<CreateGestureState> = _createGestureState.asStateFlow()

    private val _isTestPadOpen = MutableStateFlow(false)
    val isTestPadOpen: StateFlow<Boolean> = _isTestPadOpen.asStateFlow()

    private val _isTutorialOpen = MutableStateFlow(!AppSettings.hasSeenTutorial(application))
    val isTutorialOpen: StateFlow<Boolean> = _isTutorialOpen.asStateFlow()

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun checkPermission(context: Context) {
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
        _isServiceRunning.value = QuickDrawOverlayService.isRunning
    }

    fun toggleService(context: Context, enable: Boolean) {
        if (enable) {
            if (Settings.canDrawOverlays(context)) {
                QuickDrawOverlayService.start(context)
                _isServiceRunning.value = true
            } else {
                _hasOverlayPermission.value = false
            }
        } else {
            QuickDrawOverlayService.stop(context)
            _isServiceRunning.value = false
        }
    }

    fun openCreateGesture(
        actionType: ActionType = ActionType.SYSTEM,
        target: String = "FLASHLIGHT_TOGGLE",
        label: String = "Latarka",
        name: String = ""
    ) {
        _createGestureState.value = CreateGestureState(
            isOpen = true,
            actionType = actionType,
            target = target,
            label = label,
            name = name
        )
    }

    fun closeCreateGesture() {
        _createGestureState.value = CreateGestureState(isOpen = false)
    }

    fun saveNewGesture(gesture: GestureEntity) {
        viewModelScope.launch {
            repository.insertGesture(gesture)
            closeCreateGesture()
        }
    }

    fun toggleGestureEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(id, enabled)
        }
    }

    fun updateGestureSensitivity(id: Long, sensitivity: Float) {
        viewModelScope.launch {
            repository.updateSensitivity(id, sensitivity)
        }
    }

    fun deleteGesture(gesture: GestureEntity) {
        viewModelScope.launch {
            repository.deleteGesture(gesture)
        }
    }

    fun restoreDefaults(context: Context) {
        viewModelScope.launch {
            repository.prePopulateDefaultsIfEmpty(context)
        }
    }

    fun openTestPad() {
        _isTestPadOpen.value = true
    }

    fun closeTestPad() {
        _isTestPadOpen.value = false
    }

    fun openTutorial() {
        _isTutorialOpen.value = true
    }

    fun closeTutorial(context: Context) {
        AppSettings.setHasSeenTutorial(context, true)
        _isTutorialOpen.value = false
    }
}
