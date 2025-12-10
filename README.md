
### 1. Key issues identified

#### Architecture / Data layer
- **Blocking network call on main thread**
    - BrokenRepository.fetchArticlesBlocking()` used `Thread.sleep(2000)` and data were returned synchronously.
    - NewsScreen called this through GlobalScope.launch(Dispatchers.Main), which is not lifecycle aware and also risks leakage and crashes.

- **No proper data source separation or caching**
    - All data coming from BrokenRepository was hard-coded JSON.
    - No Room setup (empty AppDatabase, no Dao), so the app also had no local cache and it always fetch data from fake data.

- **Incorrect / incomplete Retrofit API usage**
    - ApiService.getArticles() returns List<Map<String, Any>>, which is not a typed response.
    - Used a dummy API key(demo) and didn’t match the actual NewsAPI response, making it hard to work with real data.

- **Room misconfiguration**
    - Article in the old version was simply a plain data class, and not annotated as @Entity.
    - AppDatabase was declared but it neither had Dao nor had singleton getInstance implementation.

#### UI / State management
- **State handled completely inside composables**
    - NewsScreen managed loading, error, and data state locally with mutable state.
    - This made it hard to share data between screens and to test.

- **Very basic UI and missing detail screen**
    - DetailScreen just render Details for: articleId with no article content.
    - List items were plain text with no spacing, shapes, or images; no loading skeleton; no proper error or empty states.

- **Navigation and lifecycle issues**
    - MainActivity used a static leakedActivity reference, which is a deliberate memory leak.
    - No navigation component, so no way to navigate from news list to detail page.

#### Project / configuration
- **No INTERNET permission**
    - Manifest did not declare android.permission.INTERNET, so real network calls wont happen.
- **Limited testing**
    - No tests around the new architecture or ViewModel behavior.


### 2. How I solved them

#### Clean data layer with Room + Retrofit + Repository

1. **Proper Room entity & database**
    - Replaced old Article with a Room entity:

      @Entity(tableName = "articles")
      data class Article(
      @PrimaryKey val id: Int,
      val title: String,
      val author: String?,
      val content: String?,
      val imageUrl: String?
      )


- Implemented ArticleDao with:
    - getArticles(): Flow<List<Article>>
    - getArticleById(id: Int)
    - insert, update, and clear().

- Implemented AppDatabase as a proper Room database with:
    - abstract fun articleDao(): ArticleDao
    - Thread-safe singleton getInstance(context).

2. **Typed Retrofit API + DTOs**
    - Defined DTOs to match NewsAPI structure:


     data class NewsResponse(
         val status: String?,
         val totalResults: Int?,
         val articles: List<NewsArticleDto>?
     )

     data class NewsArticleDto(
         val title: String?,
         val author: String?,
         val content: String?,
         @SerializedName("urlToImage") val imageUrl: String?
     )


- ApiService now returns NewsResponse instead of List<Map<String, Any>>.
- Created ApiClient with Retrofit + Gson converter.

3. **Repository pattern and offline fallback**
    - Introduced ArticleRepositoryContract and ArticleRepository:
        - val articles: Flow<List<Article>> streamed from Room.
        - refreshArticles(): Result<Unit> fetches from network on a background dispatcher and updates the DB.
        - If network returns an empty list, it falls back to parsing an intentionally broken JSON via a new FakeArticleDto.
        - If network throws, the exception is wrapped in a Result.failure.

    - Repository now map NewsArticleDto to Article and ensures every article has stable id.

#### State management with ViewModel + StateFlow

4. **NewsViewModel**
    - Created NewsViewModel which depends only on ArticleRepositoryContract.
    - Maintains a StateFlow with a sealed class:
        - Loading, Success(articles), Error, Empty.
    - Observes DB changes via repository.articles.collectLatest and updates UI state.
    - Exposes:
        - refresh() will trigger network refresh.
        - updateArticle(article) will update in Room via repository.
        - suspend fun getArticle(id: Int) is used by detail screen.
    - Added a simple Factory so MainActivity can create the ViewModel with the repository.

5. **Lifecycle-aware coroutines**
    - All work is now launched via viewModelScope and there is no GlobalScope.
    - All blocking operations moved off the main thread using Dispatchers.IO inside the repository.

#### UI & navigation improvements

6. **Navigation setup**
    - MainActivity now using NavHost and NavController:
        - news route for NewsScreen.
        - detail/{articleId} route for DetailScreen with NavType.
    - Click on an article navigates to the detail screen for that article.

7. **Fixed memory leak and cleaned activity**
    - Removed companion object { var leakedActivity: MainActivity? }.
    - Activity no longer stores static references to itself.

8. **News list UI**
    - Rewrote NewsScreen to use NewsViewModel.uiState and render:
        - A skeleton loading list with placeholder cards.
        - A centered error state with a retry button.
        - A centered empty state message.
        - A list of articles as elevated, rounded cards with:
            - Thumbnail image (via Coil AsyncImage) or a circular initial if no image.
            - Title with constraint only 2 lines max.
            - Author name in italic and lighter color.
            - Short content snippet upto max 2 line.
            - “Tap to read more” hint at the bottom of each card.

9. **Detail screen UI**
    - DetailScreen now:
        - Fetches the article by articleId from the ViewModel.
        - Shows a friendly “Article not found” state with an image illustration.
        - Displays the article inside a card with:
            - Title + author.
            - Large image (if available).
            - Divider and a Content label.
            - Editable OutlinedTextField for the content.
            - Full-width Save changes button which updates Room via viewModel.updateArticle.

10. **Top app bar behavior**
    - MainActivity top app bar title now changes based on route:
        - Broken News on the list screen.
        - Article details on the detail screen.
    - Shows a back arrow only when not on the news route.

#### Project configuration & tests

11. **Manifest & permissions**
    - Added android.permission.INTERNET to allow Retrofit to access the News API.

12. **Gradle configuration**
    - Enabled Compose + Kotlin kapt.
    - Added dependencies for:
        - Room (room-runtime, room-ktx, room-compiler with kapt).
        - Retrofit + Gson.
        - Coroutines (kotlinx-coroutines-android and testing artifact).
        - Lifecycle (lifecycle-runtime-compose, viewmodel-ktx, etc.).
        - Navigation-Compose.
        - Coil for image loading.
        - Material icons.

13. **Unit tests**
    - Created FakeArticleRepository implementing ArticleRepositoryContract:
        - Holds articles in a MutableStateFlow.
        - Can simulate success or failure via shouldFailRefresh.
    - Added NewsViewModelTest:
        - Verifies refresh() moves the state to Success when repository succeeds.
        - Verifies refresh() moves the state to Error when repository fails.
        - Uses StandardTestDispatcher and runTest to control coroutine timing.


### 3. Improvements beyond bug fixing

- **Architecture**
    - Introduced a proper Repository + Room + Retrofit stack.
    - Separated concerns between data layer, domain logic, and UI.
    - Made the app testable by depending on an ArticleRepositoryContract.

- **User experience**
    - Richer UI with cards, spacing, rounded corners, and thumbnails.
    - Clear visual states: loading skeleton, error with retry, empty list.
    - Detail screen now feels like a real article editor instead of a placeholder.

- **Performance & responsiveness**
    - All heavy IO now runs off the main thread.
    - Reactive updates using Flow + StateFlow from Room ensure UI stays in sync with DB changes.


### 4. Assumptions made

- **API contract**
    - Assumed NewsAPI’s `/v2/everything` schema with status, totalResults, and articles fields, and that the fields used (title, author, content, urlToImage) are available.

- **ID generation**
    - For network articles, IDs are generated from the list index (index + 1) because the API does not provide a stable primary key and this is acceptable for local caching and navigation within a single session.

- **Editing behavior**
    - Only the content field is editable in the detail screen; title and author are treated as read-only.
    - Updates are local (Room) only; there is no server-side update endpoint.

- **Error handling**
    - Simplified error handling: error messages are surfaced as simple strings in NewsUiState.Error, without error codes or retry strategies beyond Retry button.

- **Scope of UI changes**
    - Focused on improving readability and basic UX without introducing complex animations, theming systems, or multi-module architecture.



### Time Taken: Approx 5.5 hours in debugging and optimising the UI
