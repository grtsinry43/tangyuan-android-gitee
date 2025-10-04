//package com.qingshuige.tangyuan.compose
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.navigation.compose.rememberNavController
//import com.qingshuige.tangyuan.compose.navigation.TangyuanNavigation
//import com.qingshuige.tangyuan.compose.theme.TangyuanTheme
//
///**
// * 主要的Compose Activity - 逐步替代传统Activity
// */
//class ComposeMainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        setContent {
//            TangyuanTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    TangyuanApp()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TangyuanApp() {
//    val navController = rememberNavController()
//
//    Scaffold(
//        modifier = Modifier.fillMaxSize()
//    ) { innerPadding ->
//        TangyuanNavigation(
//            navController = navController,
//            modifier = Modifier.padding(innerPadding)
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun TangyuanAppPreview() {
//    TangyuanTheme {
//        TangyuanApp()
//    }
//}