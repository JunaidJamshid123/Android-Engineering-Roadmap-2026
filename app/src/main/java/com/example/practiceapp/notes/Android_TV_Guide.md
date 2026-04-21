# 15.6 Android TV

## Overview

Android TV is Google's platform for **television and set-top box** experiences. It provides a lean-back, 10-foot UI optimized for remote control navigation. Modern Android TV development uses the **Leanback library** for traditional views or **Compose for TV** for declarative UI.

```
┌─────────────────────────────────────────────────────────────────┐
│                Android TV Architecture                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Android TV Device                      │  │
│  │  ┌────────────────────────────────────────────────────┐   │  │
│  │  │                 Home Screen                         │   │  │
│  │  │  ┌─────────────────────────────────────────────┐   │   │  │
│  │  │  │  Recommendations Row                        │   │   │  │
│  │  │  │  [🎬 Movie1] [📺 Show2] [🎮 Game3] [▶ Vid] │   │   │  │
│  │  │  └─────────────────────────────────────────────┘   │   │  │
│  │  │  ┌─────────────────────────────────────────────┐   │   │  │
│  │  │  │  Apps Row                                   │   │   │  │
│  │  │  │  [Netflix] [YouTube] [YourApp] [Disney+]    │   │   │  │
│  │  │  └─────────────────────────────────────────────┘   │   │  │
│  │  └────────────────────────────────────────────────────┘   │  │
│  │                                                            │  │
│  │  ┌────────────────────────┐  ┌─────────────────────────┐  │  │
│  │  │  Your TV App           │  │  Input Sources          │  │  │
│  │  │  ┌──────────────────┐  │  │  • HDMI 1              │  │  │
│  │  │  │ Browse Fragment  │  │  │  • HDMI 2              │  │  │
│  │  │  │ (Leanback)       │  │  │  • Antenna (TV Input)  │  │  │
│  │  │  │ Search           │  │  │  • App Channels        │  │  │
│  │  │  │ Playback         │  │  │                        │  │  │
│  │  │  └──────────────────┘  │  └─────────────────────────┘  │  │
│  │  └────────────────────────┘                                │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Input: D-pad (remote), Voice (Google Assistant)                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Leanback Library

### Theory

The **Leanback library** (`androidx.leanback`) provides TV-optimized UI components designed for the 10-foot (lean-back) experience. It includes pre-built fragments for browsing, searching, playback, and details.

**Key Components:**
- **BrowseSupportFragment**: The main browsing screen with rows of content
- **DetailsSupportFragment**: Shows detailed info about a selected item
- **SearchSupportFragment**: Voice and text search
- **PlaybackSupportFragment**: Video playback with transport controls
- **GuidedStepSupportFragment**: Multi-step wizards (settings, sign-in)

```
┌─────────────────────────────────────────────────────────────────┐
│              Leanback Library Components                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  BrowseSupportFragment:                                         │
│  ┌──────────────────────────────────────────────────────┐      │
│  │  App Title                              🔍 Search    │      │
│  │  ─────────────────────────────────────────────────── │      │
│  │  Category 1                                          │      │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │      │
│  │  │ Card │ │ Card │ │ Card │ │ Card │ │ Card │ ──►   │      │
│  │  │  1   │ │  2   │ │  3   │ │  4   │ │  5   │      │      │
│  │  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘      │      │
│  │  Category 2                                          │      │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐               │      │
│  │  │ Card │ │ Card │ │ Card │ │ Card │ ──►            │      │
│  │  │  6   │ │  7   │ │  8   │ │  9   │               │      │
│  │  └──────┘ └──────┘ └──────┘ └──────┘               │      │
│  │  Category 3                                          │      │
│  │  ┌──────┐ ┌──────┐ ┌──────┐                        │      │
│  │  │      │ │      │ │      │ ──►                     │      │
│  │  └──────┘ └──────┘ └──────┘                        │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                  │
│  Navigation: D-pad (Up/Down/Left/Right/Select/Back)            │
│  Focus: Items receive focus → zoom/highlight animation          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Project Setup for TV

```kotlin
// build.gradle.kts (app)
dependencies {
    // Leanback library
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.leanback:leanback-preference:1.0.0")

    // For image loading on cards
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Compose for TV (alternative to Leanback)
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")
}
```

```xml
<!-- AndroidManifest.xml for TV -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Declare it's a TV app -->
    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />

    <!-- TV apps don't require touchscreen -->
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:banner="@drawable/app_banner"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.Leanback">

        <activity
            android:name=".TvMainActivity"
            android:exported="true"
            android:screenOrientation="landscape">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Also declare LAUNCHER for phones if it's a dual-screen app -->
        <!-- <category android:name="android.intent.category.LAUNCHER" /> -->

    </application>
</manifest>
```

### Code: Browse Fragment (Kotlin)

```kotlin
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*

class TvBrowseFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadData()
        setupEventListeners()
    }

    private fun setupUI() {
        title = "My TV App"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Set colors
        brandColor = ContextCompat.getColor(requireContext(), R.color.tv_brand_color)
        searchAffordanceColor = ContextCompat.getColor(
            requireContext(), R.color.tv_search_color
        )
    }

    private fun loadData() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // Category 1: Trending
        val trendingAdapter = ArrayObjectAdapter(CardPresenter())
        trendingAdapter.add(Movie("Movie 1", "Action", R.drawable.movie1))
        trendingAdapter.add(Movie("Movie 2", "Comedy", R.drawable.movie2))
        trendingAdapter.add(Movie("Movie 3", "Drama", R.drawable.movie3))
        trendingAdapter.add(Movie("Movie 4", "Sci-Fi", R.drawable.movie4))
        trendingAdapter.add(Movie("Movie 5", "Horror", R.drawable.movie5))

        val trendingHeader = HeaderItem(0, "Trending Now")
        rowsAdapter.add(ListRow(trendingHeader, trendingAdapter))

        // Category 2: Continue Watching
        val continueAdapter = ArrayObjectAdapter(CardPresenter())
        continueAdapter.add(Movie("Show 1", "Episode 5", R.drawable.show1))
        continueAdapter.add(Movie("Show 2", "Episode 3", R.drawable.show2))
        continueAdapter.add(Movie("Show 3", "Episode 12", R.drawable.show3))

        val continueHeader = HeaderItem(1, "Continue Watching")
        rowsAdapter.add(ListRow(continueHeader, continueAdapter))

        // Category 3: Recommended
        val recommendedAdapter = ArrayObjectAdapter(CardPresenter())
        recommendedAdapter.add(Movie("Doc 1", "Nature", R.drawable.doc1))
        recommendedAdapter.add(Movie("Doc 2", "Science", R.drawable.doc2))

        val recommendedHeader = HeaderItem(2, "Recommended for You")
        rowsAdapter.add(ListRow(recommendedHeader, recommendedAdapter))

        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        // Item selected (focus changed)
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
            if (item is Movie) {
                // Update background image or show preview
                updateBackground(item)
            }
        }

        // Item clicked
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Movie) {
                // Navigate to details screen
                val intent = Intent(requireContext(), TvDetailsActivity::class.java)
                intent.putExtra("movie", item)
                startActivity(intent)
            }
        }

        // Search button clicked
        setOnSearchClickedListener {
            val intent = Intent(requireContext(), TvSearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateBackground(movie: Movie) {
        // Update the blurred background image
    }
}

// Data model
data class Movie(
    val title: String,
    val description: String,
    val imageResId: Int
) : java.io.Serializable
```

### Code: Card Presenter (Kotlin)

```kotlin
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

class CardPresenter : Presenter() {

    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            // Set card dimensions
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val movie = item as Movie
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = movie.title
        cardView.contentText = movie.description

        // Load image with Glide
        Glide.with(cardView.context)
            .load(movie.imageResId)
            .centerCrop()
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}
```

### Code: Details Fragment (Kotlin)

```kotlin
import android.os.Bundle
import android.view.View
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*

class TvDetailsFragment : DetailsSupportFragment() {

    private lateinit var movie: Movie

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movie = requireActivity().intent.getSerializableExtra("movie") as Movie
        buildDetails()
    }

    private fun buildDetails() {
        val selector = ClassPresenterSelector()
        val rowsAdapter = ArrayObjectAdapter(selector)

        // Details overview row
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(
            DetailsDescriptionPresenter()
        )

        // Action buttons
        detailsPresenter.setOnActionClickedListener { action ->
            when (action.id) {
                1L -> playMovie()
                2L -> addToWatchlist()
                3L -> showTrailer()
            }
        }

        selector.addClassPresenter(
            DetailsOverviewRow::class.java, detailsPresenter
        )
        selector.addClassPresenter(
            ListRow::class.java, ListRowPresenter()
        )

        // Build details row
        val detailsRow = DetailsOverviewRow(movie).apply {
            // Set poster image
            imageDrawable = ContextCompat.getDrawable(
                requireContext(), movie.imageResId
            )

            // Add actions
            val actionAdapter = ArrayObjectAdapter()
            actionAdapter.add(Action(1, "Play", "Watch now"))
            actionAdapter.add(Action(2, "Watchlist", "Add to list"))
            actionAdapter.add(Action(3, "Trailer", "Watch trailer"))
            actionsAdapter = actionAdapter
        }

        rowsAdapter.add(detailsRow)

        // Related content row
        val relatedAdapter = ArrayObjectAdapter(CardPresenter())
        relatedAdapter.add(Movie("Related 1", "Similar", R.drawable.related1))
        relatedAdapter.add(Movie("Related 2", "Similar", R.drawable.related2))
        relatedAdapter.add(Movie("Related 3", "Similar", R.drawable.related3))

        val relatedHeader = HeaderItem(0, "Related Content")
        rowsAdapter.add(ListRow(relatedHeader, relatedAdapter))

        adapter = rowsAdapter
    }

    private fun playMovie() {
        val intent = Intent(requireContext(), TvPlaybackActivity::class.java)
        intent.putExtra("movie", movie)
        startActivity(intent)
    }

    private fun addToWatchlist() { /* ... */ }
    private fun showTrailer() { /* ... */ }
}

// Description presenter for the details row
class DetailsDescriptionPresenter :
    AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any
    ) {
        val movie = item as Movie
        viewHolder.title.text = movie.title
        viewHolder.subtitle.text = movie.description
        viewHolder.body.text = "Full description of the movie goes here. " +
            "This can be a longer text with plot summary, cast, " +
            "ratings, and other details."
    }
}
```

### Code: Search Fragment (Kotlin)

```kotlin
import android.os.Bundle
import android.view.View
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*

class TvSearchFragment : SearchSupportFragment(),
    SearchSupportFragment.SearchResultProvider {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private val allMovies = mutableListOf<Movie>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchResultProvider(this)

        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Movie) {
                // Navigate to details
            }
        }

        // Load all searchable data
        loadAllMovies()
    }

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onQueryTextChange(newQuery: String): Boolean {
        search(newQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        search(query)
        return true
    }

    private fun search(query: String) {
        rowsAdapter.clear()

        if (query.isBlank()) return

        val results = allMovies.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }

        if (results.isNotEmpty()) {
            val listRowAdapter = ArrayObjectAdapter(CardPresenter())
            results.forEach { listRowAdapter.add(it) }

            val header = HeaderItem(0, "Search Results")
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
    }

    private fun loadAllMovies() {
        // Load your movie catalog
        allMovies.addAll(
            listOf(
                Movie("Movie 1", "Action thriller", R.drawable.movie1),
                Movie("Movie 2", "Romantic comedy", R.drawable.movie2),
                // ... more movies
            )
        )
    }
}
```

---

## 2. TV Input Framework (TIF)

### Theory

The **TV Input Framework** allows apps to act as **live TV sources** on Android TV. A TV Input provides channel data and live video streams that appear in the system's Live Channels app alongside traditional antenna/cable channels.

```
┌─────────────────────────────────────────────────────────────────┐
│              TV Input Framework Architecture                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                 Live Channels App                         │  │
│  │  ┌──────────────────────────────────────────────────┐    │  │
│  │  │  Channel  1 ─── Antenna (Built-in tuner)        │    │  │
│  │  │  Channel  2 ─── Antenna                         │    │  │
│  │  │  Channel  3 ─── HDMI Input                      │    │  │
│  │  │  Channel 10 ─── Your App (TV Input)  ◄──────┐   │    │  │
│  │  │  Channel 11 ─── Your App (TV Input)  ◄──────┤   │    │  │
│  │  │  Channel 12 ─── Another App (TV Input)      │   │    │  │
│  │  └──────────────────────────────────────────────┘   │    │  │
│  └──────────────────────────────────────────────────────┘   │  │
│                                                         │      │
│  ┌──────────────────────────────────────────────────┐  │      │
│  │           Your TV Input Service                   │  │      │
│  │  ┌──────────────────────────────────────────┐    │  │      │
│  │  │  TvInputService                          │    │──┘      │
│  │  │  • onCreateSession() → Session           │    │         │
│  │  │                                          │    │         │
│  │  │  Session:                                │    │         │
│  │  │  • onTune(channelUri) → Surface render   │    │         │
│  │  │  • onSetSurface(surface) → Draw video    │    │         │
│  │  │  • EPG data → ContentProvider            │    │         │
│  │  └──────────────────────────────────────────┘    │         │
│  │                                                   │         │
│  │  ┌──────────────────────────────────────────┐    │         │
│  │  │  Setup Activity                          │    │         │
│  │  │  • Channel scan / configuration          │    │         │
│  │  │  • Populate channels in system DB        │    │         │
│  │  └──────────────────────────────────────────┘    │         │
│  └──────────────────────────────────────────────────┘         │
│                                                                  │
│  Channel/Program Data:                                          │
│  • Channels → TvContract.Channels (ContentProvider)             │
│  • Programs → TvContract.Programs (EPG data)                    │
│  • Preview Programs → TvContract.PreviewPrograms                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: TV Input Service (Kotlin)

```kotlin
import android.content.Context
import android.media.tv.TvInputService
import android.net.Uri
import android.view.Surface

class MyTvInputService : TvInputService() {

    override fun onCreateSession(inputId: String): Session {
        return MyTvInputSession(this, inputId)
    }

    inner class MyTvInputSession(
        private val context: Context,
        private val inputId: String
    ) : TvInputService.Session(context) {

        private var surface: Surface? = null

        override fun onSetSurface(surface: Surface?): Boolean {
            this.surface = surface
            return true
        }

        override fun onRelease() {
            // Clean up resources
            surface = null
        }

        override fun onTune(channelUri: Uri): Boolean {
            // Tune to the channel and start rendering video
            notifyVideoUnavailable(
                TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING
            )

            // Start your video playback on the surface
            startPlayback(channelUri)

            return true
        }

        override fun onSetStreamVolume(volume: Float) {
            // Adjust stream volume
        }

        override fun onSetCaptionEnabled(enabled: Boolean) {
            // Enable/disable closed captions
        }

        private fun startPlayback(channelUri: Uri) {
            // Initialize media player with the channel's stream URL
            // Render to the surface
            // When ready:
            notifyVideoAvailable()
        }
    }
}
```

### Code: Setting Up Channels (Kotlin)

```kotlin
import android.content.ContentValues
import android.content.Context
import android.media.tv.TvContract
import android.net.Uri

class ChannelManager(private val context: Context) {

    fun setupChannels(inputId: String) {
        // Define channels your input provides
        val channels = listOf(
            ChannelInfo("News 24/7", "1", "https://stream.example.com/news"),
            ChannelInfo("Sports Live", "2", "https://stream.example.com/sports"),
            ChannelInfo("Music TV", "3", "https://stream.example.com/music")
        )

        channels.forEach { channel ->
            insertChannel(inputId, channel)
        }
    }

    private fun insertChannel(inputId: String, channelInfo: ChannelInfo) {
        val values = ContentValues().apply {
            put(TvContract.Channels.COLUMN_INPUT_ID, inputId)
            put(TvContract.Channels.COLUMN_DISPLAY_NAME, channelInfo.name)
            put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, channelInfo.number)
            put(TvContract.Channels.COLUMN_TYPE, TvContract.Channels.TYPE_OTHER)
            put(
                TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA,
                channelInfo.streamUrl
            )
        }

        val channelUri = context.contentResolver.insert(
            TvContract.Channels.CONTENT_URI, values
        )

        // Add programs (EPG data) for this channel
        channelUri?.let { uri ->
            val channelId = uri.lastPathSegment?.toLong() ?: return
            insertPrograms(channelId)
        }
    }

    private fun insertPrograms(channelId: Long) {
        // Insert EPG (Electronic Program Guide) data
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L

        val programs = listOf(
            ProgramInfo("Morning Show", now, now + hourMs * 2),
            ProgramInfo("Midday Report", now + hourMs * 2, now + hourMs * 3),
            ProgramInfo("Afternoon Special", now + hourMs * 3, now + hourMs * 5)
        )

        programs.forEach { program ->
            val values = ContentValues().apply {
                put(TvContract.Programs.COLUMN_CHANNEL_ID, channelId)
                put(TvContract.Programs.COLUMN_TITLE, program.title)
                put(
                    TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS,
                    program.startTime
                )
                put(
                    TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS,
                    program.endTime
                )
                put(TvContract.Programs.COLUMN_TYPE, TvContract.Programs.TYPE_MOVIE)
            }
            context.contentResolver.insert(
                TvContract.Programs.CONTENT_URI, values
            )
        }
    }

    data class ChannelInfo(
        val name: String, val number: String, val streamUrl: String
    )
    data class ProgramInfo(
        val title: String, val startTime: Long, val endTime: Long
    )
}
```

```xml
<!-- AndroidManifest.xml — TV Input Service -->
<service
    android:name=".MyTvInputService"
    android:exported="true"
    android:permission="android.permission.BIND_TV_INPUT">
    <intent-filter>
        <action android:name="android.media.tv.TvInputService" />
    </intent-filter>
    <meta-data
        android:name="android.media.tv.input"
        android:resource="@xml/tv_input" />
</service>

<!-- Setup activity for channel scanning -->
<activity
    android:name=".TvInputSetupActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.media.tv.action.SETUP" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

```xml
<!-- res/xml/tv_input.xml -->
<tv-input xmlns:android="http://schemas.android.com/apk/res/android"
    android:settingsActivity="com.example.app.TvInputSettingsActivity"
    android:setupActivity="com.example.app.TvInputSetupActivity" />
```

---

## 3. Content Recommendations

### Theory

Content recommendations allow your app to surface content on the **Android TV home screen**. Users see recommended content from your app without opening it. There are two approaches:

1. **Home Screen Channels** (API 26+): Rows of preview programs on the home screen
2. **Watch Next Row**: System-managed row for content the user started watching

```
┌─────────────────────────────────────────────────────────────────┐
│           Content Recommendations Architecture                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Android TV Home Screen:                                        │
│  ┌──────────────────────────────────────────────────────┐      │
│  │                                                       │      │
│  │  Watch Next (System row):                            │      │
│  │  ┌──────┐ ┌──────┐ ┌──────┐                         │      │
│  │  │ Ep.5 │ │ Mov. │ │ Show │                         │      │
│  │  │ cont.│ │ cont.│ │ new  │                         │      │
│  │  └──────┘ └──────┘ └──────┘                         │      │
│  │                                                       │      │
│  │  Your App Channel (Subscribed by user):              │      │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │      │
│  │  │ Rec  │ │ Rec  │ │ Rec  │ │ Rec  │ │ Rec  │      │      │
│  │  │  1   │ │  2   │ │  3   │ │  4   │ │  5   │      │      │
│  │  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘      │      │
│  │                                                       │      │
│  │  Other App Channel:                                  │      │
│  │  ┌──────┐ ┌──────┐ ┌──────┐                         │      │
│  │  │      │ │      │ │      │                         │      │
│  │  └──────┘ └──────┘ └──────┘                         │      │
│  │                                                       │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                  │
│  API:                                                           │
│  • PreviewChannel → Your branded row on home screen            │
│  • PreviewProgram → Individual items in the channel            │
│  • WatchNextProgram → Items in "Watch Next" row               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Home Screen Channels (Kotlin)

```kotlin
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.tvprovider.media.tv.PreviewChannel
import androidx.tvprovider.media.tv.PreviewChannelHelper
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram

class RecommendationManager(private val context: Context) {

    private val channelHelper = PreviewChannelHelper(context)

    // Create a channel on the home screen
    fun createRecommendationChannel(): Long {
        val channel = PreviewChannel.Builder()
            .setDisplayName("Recommended for You")
            .setDescription("Personalized picks from My TV App")
            .setAppLinkIntentUri(
                Uri.parse("myapp://browse/recommended")
            )
            .setInternalProviderId("recommended_channel")
            .build()

        val channelId = channelHelper.publishChannel(channel)

        // Request that the channel be shown on the home screen
        TvContractCompat.requestChannelBrowsable(context, channelId)

        return channelId
    }

    // Add programs to the channel
    fun addProgramsToChannel(channelId: Long) {
        val movies = getRecommendedMovies()

        movies.forEach { movie ->
            val program = PreviewProgram.Builder()
                .setChannelId(channelId)
                .setTitle(movie.title)
                .setDescription(movie.description)
                .setPosterArtUri(Uri.parse(movie.posterUrl))
                .setIntentUri(
                    Uri.parse("myapp://play/${movie.id}")
                )
                .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                .setDurationMillis(movie.durationMs.toInt())
                .setGenre(movie.genre)
                .setPosterArtAspectRatio(
                    TvContractCompat.PreviewPrograms.ASPECT_RATIO_16_9
                )
                .setInternalProviderId(movie.id)
                .build()

            channelHelper.publishPreviewProgram(program)
        }
    }

    // Add to "Watch Next" row (for partially watched content)
    fun addToWatchNext(movie: Movie, watchedPosition: Long) {
        val watchNextProgram = WatchNextProgram.Builder()
            .setTitle(movie.title)
            .setDescription("Continue watching")
            .setPosterArtUri(Uri.parse(movie.posterUrl))
            .setIntentUri(
                Uri.parse("myapp://play/${movie.id}?position=$watchedPosition")
            )
            .setType(TvContractCompat.WatchNextPrograms.TYPE_MOVIE)
            .setWatchNextType(
                TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE
            )
            .setLastPlaybackPositionMillis(watchedPosition.toInt())
            .setDurationMillis(movie.durationMs.toInt())
            .setInternalProviderId(movie.id)
            .build()

        channelHelper.publishWatchNextProgram(watchNextProgram)
    }

    // Update existing program in Watch Next
    fun updateWatchNextProgress(programId: Long, newPosition: Long) {
        val values = ContentValues().apply {
            put(
                TvContractCompat.WatchNextPrograms
                    .COLUMN_LAST_PLAYBACK_POSITION_MILLIS,
                newPosition
            )
        }
        context.contentResolver.update(
            TvContractCompat.buildWatchNextProgramUri(programId),
            values, null, null
        )
    }

    // Remove from Watch Next when user finishes watching
    fun removeFromWatchNext(programId: Long) {
        context.contentResolver.delete(
            TvContractCompat.buildWatchNextProgramUri(programId),
            null, null
        )
    }

    private fun getRecommendedMovies(): List<RecommendedMovie> {
        return listOf(
            RecommendedMovie(
                "1", "Action Movie", "Thrilling action film",
                "https://example.com/poster1.jpg", "Action", 7200000L
            ),
            RecommendedMovie(
                "2", "Comedy Show", "Hilarious comedy",
                "https://example.com/poster2.jpg", "Comedy", 5400000L
            )
        )
    }

    data class RecommendedMovie(
        val id: String,
        val title: String,
        val description: String,
        val posterUrl: String,
        val genre: String,
        val durationMs: Long
    )
}
```

### Code: Playback Fragment (Kotlin)

```kotlin
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow

class TvPlaybackFragment : VideoSupportFragment() {

    private lateinit var transportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movie = requireActivity().intent
            .getSerializableExtra("movie") as Movie

        setupPlayer(movie)
    }

    private fun setupPlayer(movie: Movie) {
        val glueHost = VideoSupportFragmentGlueHost(this)

        val playerAdapter = MediaPlayerAdapter(requireContext()).apply {
            setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)
        }

        transportControlGlue = PlaybackTransportControlGlue(
            requireActivity(), playerAdapter
        ).apply {
            host = glueHost
            title = movie.title
            subtitle = movie.description

            // Add action buttons
            playWhenPrepared()
        }

        // Set the media source
        playerAdapter.setDataSource(
            Uri.parse("https://example.com/stream/${movie.title}")
        )
    }

    override fun onPause() {
        super.onPause()
        transportControlGlue.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        transportControlGlue.release()
    }
}
```

### Code: D-pad Navigation Handling (Kotlin)

```kotlin
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView

// Handle D-pad navigation in custom views
class TvFocusHelper {

    fun setupDpadNavigation(view: View) {
        // Make view focusable
        view.isFocusable = true
        view.isFocusableInTouchMode = true

        // Handle focus changes (zoom/highlight on focus)
        view.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // Scale up on focus
                v.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .start()
                // Add highlight
                v.elevation = 8f
            } else {
                // Scale back to normal
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
                v.elevation = 0f
            }
        }
    }

    // Handle key events for custom behavior
    fun handleKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                // Select/confirm action
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                // Navigate left
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                // Navigate right
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                // Navigate up
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                // Navigate down
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // Toggle playback
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                // Go back
                false  // Let system handle
            }
            else -> false
        }
    }
}
```

### Code: Compose for TV (Alternative to Leanback) (Kotlin)

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvBrowseScreen(
    categories: List<Category>,
    onMovieSelected: (Movie) -> Unit
) {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        items(categories) { category ->
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 48.dp, bottom = 8.dp, top = 24.dp)
            )

            TvLazyRow(
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(category.movies) { movie ->
                    TvMovieCard(
                        movie = movie,
                        onClick = { onMovieSelected(movie) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvMovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(196.dp)
            .height(110.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Movie poster image
            AsyncImage(
                model = movie.imageResId,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Title overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}

data class Category(val name: String, val movies: List<Movie>)
```

---

## Summary Table

| Feature | Key API / Library | Description |
|---------|-------------------|-------------|
| Leanback UI | `BrowseSupportFragment` | TV-optimized browsing layout |
| Card Views | `ImageCardView` + `Presenter` | Content cards with focus animation |
| Details | `DetailsSupportFragment` | Full item detail screen |
| Search | `SearchSupportFragment` | Voice + text search |
| Playback | `VideoSupportFragment` | Video player with transport controls |
| TV Input | `TvInputService` | Live TV channel provider |
| Channels | `TvContract.Channels` | System channel database |
| EPG | `TvContract.Programs` | Electronic Program Guide data |
| Home Row | `PreviewChannel` | Branded row on home screen |
| Watch Next | `WatchNextProgram` | Continue watching system row |
| Compose TV | `tv-material` | Declarative TV UI |
| D-pad Nav | `KeyEvent.KEYCODE_DPAD_*` | Remote control navigation |

---

## Best Practices

1. **Design for the 10-foot experience** — large text, high contrast, simple layouts
2. **Support D-pad navigation** — every interactive element must be focusable
3. **Provide visual focus feedback** — scale, highlight, or elevate focused items
4. **Don't require touch input** — TV remotes don't have touch
5. **Use landscape orientation** — TVs are always landscape
6. **Provide a banner image** — 320×180 for the home screen app row
7. **Keep loading times short** — show placeholder/shimmer while loading
8. **Support voice search** — integrate with Google Assistant
9. **Update Watch Next** — keep the "Continue Watching" row current
10. **Test with the TV emulator** — use Android TV emulator in Android Studio
