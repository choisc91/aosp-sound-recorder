package com.android.soundrecorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.android.soundrecorder.entities.Route
import com.android.soundrecorder.presentation.list.ListViewModel
import com.android.soundrecorder.presentation.list.routeList
import com.android.soundrecorder.presentation.main.MainViewModel
import com.android.soundrecorder.presentation.main.routeMain
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var controller: NavHostController

    private val viewModel: MainViewModel by viewModels()
    private val listViewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            controller = rememberNavController().also {
                NavHost(
                    navController = it,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    startDestination = Route.MAIN,
                ) {
                    routeMain(it, viewModel)
                    routeList(listViewModel)
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        viewModel.pauseRecord()
        listViewModel.resetAllState()
    }
}