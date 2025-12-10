package com.greedygame.brokenandroidcomposeproject.ui.viewModel

import com.greedygame.brokenandroidcomposeproject.data.ArticleRepositoryContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.greedygame.brokenandroidcomposeproject.data.Article
import com.greedygame.brokenandroidcomposeproject.data.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<Article>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
    object Empty : NewsUiState()
}

class NewsViewModel(
    private val repository: ArticleRepositoryContract
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState

    init {
        observeDatabase()
        refresh()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.articles.collectLatest { items ->
                if (items.isEmpty()) {
                    // Either loading or genuinely empty; keep current error/loading if any
                    if (_uiState.value is NewsUiState.Success) {
                        _uiState.value = NewsUiState.Empty
                    }
                } else {
                    _uiState.value = NewsUiState.Success(items)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            val result = repository.refreshArticles()
            result.exceptionOrNull()?.let { throwable ->
                if (_uiState.value !is NewsUiState.Success) {
                    _uiState.value =
                        NewsUiState.Error(throwable.message ?: "Something went wrong")
                }
            }
        }
    }

    fun updateArticle(article: Article) {
        viewModelScope.launch {
            repository.updateArticle(article)
        }
    }

    suspend fun getArticle(id: Int): Article? = repository.getArticle(id)

    // Simple factory so MainActivity can provide the repository
    class Factory(private val repository: ArticleRepositoryContract) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                return NewsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
