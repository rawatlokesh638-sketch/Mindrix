package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.room.Room
import com.example.data.MindrixDatabase
import com.example.data.MindrixRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MINDRIXTheme

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            MindrixDatabase::class.java,
            "mindrix_db"
        ).fallbackToDestructiveMigration().build()
    }
    
    private val repository by lazy { MindrixRepository(database.userStatsDao()) }
    
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MINDRIXTheme {
                AppNavigation(viewModel)
            }
        }
    }
}
