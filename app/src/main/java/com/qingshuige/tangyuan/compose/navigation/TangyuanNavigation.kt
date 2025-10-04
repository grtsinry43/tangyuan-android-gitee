//package com.qingshuige.tangyuan.compose.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import com.qingshuige.tangyuan.compose.screens.HomeScreen
//import com.qingshuige.tangyuan.compose.screens.PostDetailScreen
//import com.qingshuige.tangyuan.compose.screens.CategoryScreen
//
///**
// * 应用导航图
// */
//@Composable
//fun TangyuanNavigation(
//    navController: NavHostController,
//    modifier: Modifier = Modifier
//) {
//    NavHost(
//        navController = navController,
//        startDestination = "home",
//        modifier = modifier
//    ) {
//        composable("home") {
//            HomeScreen(
//                onPostClick = { postId ->
//                    navController.navigate("post_detail/$postId")
//                },
//                onCategoryClick = { categoryId ->
//                    navController.navigate("category/$categoryId")
//                }
//            )
//        }
//
//        composable("post_detail/{postId}") { backStackEntry ->
//            val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: 0
//            PostDetailScreen(
//                postId = postId,
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable("category/{categoryId}") { backStackEntry ->
//            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
//            CategoryScreen(
//                categoryId = categoryId,
//                onBackClick = { navController.popBackStack() },
//                onPostClick = { postId ->
//                    navController.navigate("post_detail/$postId")
//                }
//            )
//        }
//    }
//}