package com.example.myzum.repository

import com.example.myzum.model.Product
import com.example.myzum.repository.local.ProductDao
import com.example.myzum.repository.local.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CatalogRepository(private val productDao: ProductDao) {
    
    fun getProducts(userId: String): Flow<List<Product>> = productDao.getAllProducts(userId).map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    suspend fun deleteProduct(id: String, userId: String) {
        productDao.deleteProduct(id, userId)
    }
}

private fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    price = price,
    ownerId = ownerId
)

private fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    price = price,
    ownerId = ownerId
)
