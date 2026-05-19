package com.example.memoir.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Memoirs : Route
    
    @Serializable
    data object Milestones : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Archive : Route

    @Serializable
    data object RecentlyDeleted : Route

    @Serializable
    data object Folders : Route
    
    @Serializable
    data class Desk(val id: String? = null, val isMilestone: Boolean = false) : Route
}
