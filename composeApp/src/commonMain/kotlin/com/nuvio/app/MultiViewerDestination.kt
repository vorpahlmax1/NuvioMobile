package com.nuvio.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nuvio.app.features.multiview.MultiViewerScreen
import com.nuvio.app.navigation.MultiViewerRoute
import com.nuvio.app.navigation.NuvioNavigator

@Composable
internal fun MultiViewerDestination(
    route: MultiViewerRoute,
    navController: NuvioNavigator,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    MultiViewerScreen(
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
    )
}
