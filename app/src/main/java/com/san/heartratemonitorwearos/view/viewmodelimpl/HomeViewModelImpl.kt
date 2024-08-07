package com.san.heartratemonitorwearos.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.heartratemonitorwearos.data.Success
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.domain.state.UiState
import com.san.heartratemonitorwearos.view.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModelImpl(
    private val repository: HeartRateRepository
) : HomeViewModel, ViewModel() {
    override val state: LiveData<UiState>
        get() = viewModelState
    private val viewModelState = MutableLiveData<UiState>(UiState.Loading)

    override fun updateWorkStatus() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateWorkNow()
            }
        }
    }

    private suspend fun updateWorkNow() {
        val result = repository.updateWorkNow()

        if (result is Success) viewModelState.postValue(UiState.Success)
        else viewModelState.postValue(UiState.ServiceError)
    }
}