package com.san.heartratemonitorwearos.view.viewmodelimpl

import androidx.lifecycle.ViewModel
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.view.viewmodel.HomeViewModel

class HomeViewModelImpl(
    private val repository: HeartRateRepository
) : HomeViewModel, ViewModel() {
}