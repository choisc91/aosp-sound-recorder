package com.android.soundrecorder.presentation.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

class CommonUi {
    companion object {
        fun NavGraphBuilder.defaultComposable(
            route: String,
            content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
        ) {
            composable(
                route = route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() },
                content = content,
            )
        }
    }
}