package com.example.myzum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myzum.model.Product
import com.example.myzum.repository.CatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val repository: CatalogRepository,
    private val activeUserId: StateFlow<String?>
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = activeUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else repository.getProducts(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    fun removeProduct(id: String) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            repository.deleteProduct(id, userId)
        }
    }
}
