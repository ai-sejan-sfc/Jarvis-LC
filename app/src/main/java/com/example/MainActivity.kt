package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.example.ui.navigation.MainNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {

  private val jarvisViewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainNavigation(viewModel = jarvisViewModel)
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
  androidx.compose.material3.Text(
    text = "Hello $name!",
    modifier = modifier,
    color = com.example.ui.theme.JarvisCyan
  )
}


