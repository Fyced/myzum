package com.example.myzum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myzum.model.statistics.BusinessInsight
import com.example.myzum.model.statistics.BusinessStatistics
import com.example.myzum.model.statistics.StatisticsPeriod
import com.example.myzum.repository.StatisticsRepository
import com.example.myzum.statistics.BusinessInsightEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val stats: BusinessStatistics, val insights: List<BusinessInsight>) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val repository: StatisticsRepository,
    private val activeUserId: StateFlow<String?>
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatisticsPeriod.TODAY)
    val selectedPeriod: StateFlow<StatisticsPeriod> = _selectedPeriod.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        _refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = combine(
        activeUserId,
        _selectedPeriod,
        _refreshTrigger
    ) { userId, period, _ ->
        userId to period
    }.flatMapLatest { (userId, period) ->
        if (userId == null) flowOf(StatisticsUiState.Error("No user logged in"))
        else {
            val (start, end) = getRangeForPeriod(period)
            val (pStart, pEnd) = getPreviousRangeForPeriod(period)
            repository.getStatistics(userId, start, end, pStart, pEnd)
                .map<BusinessStatistics, StatisticsUiState> { stats ->
                    StatisticsUiState.Success(
                        stats = stats,
                        insights = BusinessInsightEngine.generateInsights(stats)
                    )
                }
                .onStart { emit(StatisticsUiState.Loading) }
                .catch { emit(StatisticsUiState.Error(it.localizedMessage ?: "Unknown error")) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState.Loading)

    fun setPeriod(period: StatisticsPeriod) {
        _selectedPeriod.value = period
    }

    fun refresh() {
        _refreshTrigger.tryEmit(Unit)
    }

    private fun getRangeForPeriod(period: StatisticsPeriod): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val end = calendar.time
        when (period) {
            StatisticsPeriod.TODAY -> calendar.set(Calendar.HOUR_OF_DAY, 0)
            StatisticsPeriod.LAST_7_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -6)
            StatisticsPeriod.LAST_30_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -29)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time to end
    }

    private fun getPreviousRangeForPeriod(period: StatisticsPeriod): Pair<Date, Date> {
        val (currentStart, _) = getRangeForPeriod(period)
        val calendar = Calendar.getInstance()
        calendar.time = currentStart
        val end = calendar.time
        when (period) {
            StatisticsPeriod.TODAY -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            StatisticsPeriod.LAST_7_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            StatisticsPeriod.LAST_30_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -30)
        }
        return calendar.time to end
    }
}
