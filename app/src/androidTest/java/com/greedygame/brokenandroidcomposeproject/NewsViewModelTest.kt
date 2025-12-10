package com.greedygame.brokenandroidcomposeproject

import com.greedygame.brokenandroidcomposeproject.ui.viewModel.NewsUiState
import com.greedygame.brokenandroidcomposeproject.ui.viewModel.NewsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun refresh_success_updates_state_to_Success() = runTest(dispatcher) {
        val fakeRepo = FakeArticleRepository()
        val vm = NewsViewModel(fakeRepo)

        vm.refresh()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is NewsUiState.Success)
    }

    @Test
    fun refresh_failure_updates_state_to_Error() = runTest(dispatcher) {
        val fakeRepo = FakeArticleRepository().apply { shouldFailRefresh = true }
        val vm = NewsViewModel(fakeRepo)

        vm.refresh()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is NewsUiState.Error)
    }
}