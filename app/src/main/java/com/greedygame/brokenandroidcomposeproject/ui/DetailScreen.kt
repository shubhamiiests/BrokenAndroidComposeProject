package com.greedygame.brokenandroidcomposeproject.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.greedygame.brokenandroidcomposeproject.R
import com.greedygame.brokenandroidcomposeproject.data.Article
import com.greedygame.brokenandroidcomposeproject.ui.viewModel.NewsViewModel

@Composable
fun DetailScreen(
    articleId: Int,
    viewModel: NewsViewModel
) {
    var article by remember { mutableStateOf<Article?>(null) }

    LaunchedEffect(articleId) {
        article = viewModel.getArticle(articleId)
    }

    val current = article

    // ------- Not found -------
    if (current == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.notfound),
                contentDescription = "Not found",
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Article not found",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    var content by remember(current) { mutableStateOf(current.content.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // Title label
        Text(
            text = "Title",
            style = MaterialTheme.typography.labelLarge
        )

        // Title value in bold
        Text(
            text = current.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Image
        if (!current.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = current.imageUrl,
                contentDescription = current.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Content",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            maxLines = Int.MAX_VALUE,
            label = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val updated = current.copy(content = content)
                viewModel.updateArticle(updated)
            }
        ) {
            Text("Save")
        }
    }
}
