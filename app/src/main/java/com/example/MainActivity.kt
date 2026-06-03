package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.StudyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Simple manual injection pattern for the ViewModel
        val viewModel = ViewModelProvider(
            this,
            StudyViewModelFactory(application)
        )[StudyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
