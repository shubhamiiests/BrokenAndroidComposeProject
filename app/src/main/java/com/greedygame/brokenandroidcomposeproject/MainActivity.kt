package com.greedygame.brokenandroidcomposeproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.greedygame.brokenandroidcomposeproject.data.ArticleRepository
import com.greedygame.brokenandroidcomposeproject.db.AppDatabase
import com.greedygame.brokenandroidcomposeproject.network.ApiClient
import com.greedygame.brokenandroidcomposeproject.ui.DetailScreen
import com.greedygame.brokenandroidcomposeproject.ui.NewsScreen
import com.greedygame.brokenandroidcomposeproject.ui.viewModel.NewsViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getInstance(this) }
    private val repository by lazy { ArticleRepository(ApiClient.api, database) }

    private val newsViewModel: NewsViewModel by viewModels {
        NewsViewModel.Factory(repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val canNavigateBack = currentRoute != "news"

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (currentRoute?.startsWith("detail") == true) {
                                    "Article details"
                                } else {
                                    "Broken News"
                                }
                            )
                        },
                        navigationIcon = {
                            if (canNavigateBack) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "news",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("news") {
                            NewsScreen(
                                viewModel = newsViewModel,
                                onArticleClick = { article ->
                                    navController.navigate("detail/${article.id}")
                                }
                            )
                        }

                        composable(
                            "detail/{articleId}",
                            arguments = listOf(navArgument("articleId") { type = NavType.IntType })
                        ) { entry ->
                            val id = entry.arguments?.getInt("articleId") ?: return@composable
                            DetailScreen(
                                articleId = id,
                                viewModel = newsViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
