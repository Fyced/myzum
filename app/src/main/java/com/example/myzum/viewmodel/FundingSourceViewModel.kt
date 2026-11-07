package com.example.myzum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myzum.model.FundingSource
import com.example.myzum.repository.FundingSourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FundingSourceViewModel(
    private val repository: FundingSourceRepository,
    private val activeUserId: StateFlow<String?>
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val fundingSources: StateFlow<List<FundingSource>> = activeUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else repository.getFundingSources(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPageIndex = MutableStateFlow(0)
    val selectedPageIndex: StateFlow<Int> = _selectedPageIndex.asStateFlow()

    fun addSource(source: FundingSource) {
        viewModelScope.launch {
            repository.addFundingSource(source)
        }
    }

    fun removeSource(id: String) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            repository.removeFundingSource(id, userId)
        }
    }

    fun setSelectedPageIndex(index: Int) {
        _selectedPageIndex.value = index
    }

    fun reset() {
        _selectedPageIndex.value = 0
    }
}
