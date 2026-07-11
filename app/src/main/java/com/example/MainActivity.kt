package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme

import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory
import com.example.ui.AppContent

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database, DAOs, and Central Repository
    val database = AppDatabase.getDatabase(this)
    val repository = Repository(
        userDao = database.userDao(),
        stockDao = database.stockDao(),
        transactionDao = database.transactionDao(),
        analysisDao = database.analysisDao()
    )

    // Instantiate MainViewModel with factory
    val factory = MainViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

    setContent {
      MyApplicationTheme {
        AppContent(viewModel = viewModel)
      }
    }
  }
}
