package com.greedygame.brokenandroidcomposeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greedygame.brokenandroidcomposeproject.data.Article
import com.greedygame.brokenandroidcomposeproject.ui.viewModel.NewsUiState
import com.greedygame.brokenandroidcomposeproject.ui.viewModel.NewsViewModel

@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onArticleClick: (Article) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val uiState = state) {
        is NewsUiState.Loading -> {
            LoadingView()
            /*Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }*/
        }

        is NewsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.message)
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
        }

        is NewsUiState.Empty -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No articles found")
            }
        }

        is NewsUiState.Success -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.articles, key = { it.id }) { article ->
                    ArticleRow(article = article, onClick = { onArticleClick(article) })
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium
            )
            if (!article.author.isNullOrBlank()) {
                Text(
                    text = "- ${article.author}" ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                    //fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
@Composable
fun LoadingView() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}