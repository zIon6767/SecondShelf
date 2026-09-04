package com.example.secondshelf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondshelf.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the different states the scanner can be in
sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    object Success : ScanState()
    data class Error(val message: String) : ScanState()
}

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // This state flow is what your ScannerScreen will watch
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    /**
     * Called when the camera detects a QR code.
     */
    fun verifyQrCode(scannedToken: String, currentUserId: String) {
        // Prevent multiple rapid-fire scans while we are already checking one
        if (_scanState.value == ScanState.Loading) return

        _scanState.value = ScanState.Loading

        viewModelScope.launch {
            // Ask the database if this token is legit
            val isSuccess = repository.verifyScannedToken(scannedToken, currentUserId)

            if (isSuccess) {
                _scanState.value = ScanState.Success
            } else {
                _scanState.value = ScanState.Error("Invalid or expired QR code.")
            }
        }
    }

    /**
     * Reset the state so the scanner is ready to go again if needed
     */
    fun resetScanState() {
        _scanState.value = ScanState.Idle
    }
}