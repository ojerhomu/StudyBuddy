package com.example.studybuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimerViewModel : ViewModel() {
    private val _time = MutableStateFlow(Duration.ZERO)
    val time = _time.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private var job: Job? = null

    fun setTime(duration: Duration) {
        _time.value = duration
    }

    fun start() {
        if (_isRunning.value || _time.value <= Duration.ZERO) return
        _isRunning.value = true
        job = viewModelScope.launch {
            while (_time.value > Duration.ZERO) {
                delay(1000)
                _time.value -= 1.seconds
            }
            _isRunning.value = false
        }
    }

    fun pause() {
        _isRunning.value = false
        job?.cancel()
    }

    fun reset() {
        job?.cancel()
        _isRunning.value = false
    }
}