package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant

object PhotoRefreshPolicy {
    val retryDelaysMillis: List<Long> = listOf(900L, 1_800L)

    fun mergePreservingOrder(
        current: List<Restaurant>,
        refreshed: List<Restaurant>
    ): List<Restaurant> {
        val refreshedById = refreshed.associateBy { it.id }
        return current.map { old ->
            refreshedById[old.id]?.takeIf { it.photoUrl != null } ?: old
        }
    }
}
