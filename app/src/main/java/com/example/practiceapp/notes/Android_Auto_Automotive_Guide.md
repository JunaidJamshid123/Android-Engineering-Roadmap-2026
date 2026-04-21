# 15.5 Android Auto and Automotive OS

## Overview

Android provides two distinct platforms for in-vehicle experiences:

1. **Android Auto**: Projects your phone app onto the car's display (phone-powered)
2. **Android Automotive OS**: A full Android OS built into the car's head unit (embedded)

Both use the **Car App Library** for building apps, but Automotive OS apps run natively on the vehicle's hardware.

```
┌─────────────────────────────────────────────────────────────────┐
│           Android Auto vs Android Automotive OS                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ANDROID AUTO                   ANDROID AUTOMOTIVE OS           │
│  ─────────────                  ─────────────────────           │
│                                                                  │
│  ┌─────────┐    ┌──────────┐   ┌──────────────────────┐       │
│  │  Phone  │───>│ Car      │   │  Car Head Unit       │       │
│  │  (App   │USB/│ Display  │   │  ┌──────────────┐    │       │
│  │  runs   │WiFi│ (Mirror) │   │  │  Android OS  │    │       │
│  │  here)  │    │          │   │  │  (Built-in)  │    │       │
│  └─────────┘    └──────────┘   │  │  App runs    │    │       │
│                                 │  │  natively    │    │       │
│  • Phone required              │  └──────────────┘    │       │
│  • App runs on phone           │                      │       │
│  • Screen projected to car     │  • No phone needed   │       │
│  • Limited app categories      │  • Full Android OS   │       │
│  • Available in most cars      │  • Play Store on car │       │
│                                 │  • Volvo, Polestar, │       │
│  Supported categories:         │    GM, Ford, etc.    │       │
│  • Media (audio)               │                      │       │
│  • Messaging                   │  All Auto categories │       │
│  • Navigation                  │  + Settings, Parking │       │
│  • POI (Point of Interest)     │  + EV Charging, etc. │       │
│                                 └──────────────────────┘       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Media Apps for Android Auto

### Theory

Media apps are the most common Android Auto apps. They let users play music, podcasts, audiobooks, and other audio content through the car's speakers. Media apps use **MediaBrowserService** and **MediaSession** to expose content and controls.

**Architecture:**
- **MediaBrowserServiceCompat**: Exposes your media library as a browsable tree
- **MediaSessionCompat**: Handles playback controls (play, pause, skip, seek)
- **MediaDescriptionCompat**: Describes individual media items (title, artist, art)
- **Car App Library**: Alternative for custom UI (non-standard layouts)

```
┌─────────────────────────────────────────────────────────────────┐
│             Media App Architecture for Auto                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────┐          │
│  │                 Your App (Phone)                   │          │
│  │                                                    │          │
│  │  ┌────────────────────┐   ┌────────────────────┐  │          │
│  │  │ MediaBrowserService│   │   MediaSession     │  │          │
│  │  │                    │   │                    │  │          │
│  │  │ • onGetRoot()      │   │ • Play/Pause       │  │          │
│  │  │ • onLoadChildren() │   │ • Skip Next/Prev  │  │          │
│  │  │ • Browse tree      │   │ • Seek             │  │          │
│  │  │                    │   │ • Custom actions   │  │          │
│  │  │  ┌──────────────┐  │   │                    │  │          │
│  │  │  │ Root         │  │   │ ┌──────────────┐   │  │          │
│  │  │  │ ├─ Playlists │  │   │ │ PlaybackState│   │  │          │
│  │  │  │ ├─ Albums    │  │   │ │ Metadata     │   │  │          │
│  │  │  │ ├─ Artists   │  │   │ │ Queue        │   │  │          │
│  │  │  │ └─ Favorites │  │   │ └──────────────┘   │  │          │
│  │  │  └──────────────┘  │   │                    │  │          │
│  │  └────────────────────┘   └────────────────────┘  │          │
│  │           │                        │               │          │
│  └───────────┼────────────────────────┼───────────────┘          │
│              │                        │                          │
│              ▼                        ▼                          │
│  ┌──────────────────────────────────────────────────┐          │
│  │            Android Auto Display                    │          │
│  │  ┌────────────────────────────────────────────┐   │          │
│  │  │  🎵 Song Title — Artist Name               │   │          │
│  │  │  Album Name                                │   │          │
│  │  │                                            │   │          │
│  │  │  [⏮] [⏯] [⏭]  ──●──────── 2:30/4:15     │   │          │
│  │  │                                            │   │          │
│  │  │  Browse:  Playlists | Albums | Artists     │   │          │
│  │  └────────────────────────────────────────────┘   │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: MediaBrowserService for Auto (Kotlin)

```kotlin
import android.media.browse.MediaBrowser.MediaItem
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

class AutoMediaService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playbackState: PlaybackStateCompat.Builder

    companion object {
        private const val ROOT_ID = "root"
        private const val PLAYLISTS_ID = "playlists"
        private const val ALBUMS_ID = "albums"
        private const val ARTISTS_ID = "artists"
        private const val EMPTY_ROOT = "empty_root"
    }

    override fun onCreate() {
        super.onCreate()

        // Create media session
        mediaSession = MediaSessionCompat(this, "AutoMediaService").apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }

        // Set the session token so Auto can connect
        sessionToken = mediaSession.sessionToken

        // Initialize playback state
        playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            )
    }

    // Called when a client (Android Auto) connects
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // Verify the client is Android Auto or your app
        return if (isValidClient(clientPackageName)) {
            BrowserRoot(ROOT_ID, null)
        } else {
            BrowserRoot(EMPTY_ROOT, null)
        }
    }

    // Called when Auto wants to browse the media tree
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // Allow async loading
        result.detach()

        val mediaItems = when (parentId) {
            ROOT_ID -> {
                // Top-level categories
                mutableListOf(
                    createBrowsableItem(PLAYLISTS_ID, "Playlists", "Your playlists"),
                    createBrowsableItem(ALBUMS_ID, "Albums", "Browse albums"),
                    createBrowsableItem(ARTISTS_ID, "Artists", "Browse artists")
                )
            }
            PLAYLISTS_ID -> {
                // Return playlists
                getPlaylists().map { playlist ->
                    createBrowsableItem(
                        "playlist_${playlist.id}",
                        playlist.name,
                        "${playlist.songCount} songs"
                    )
                }.toMutableList()
            }
            ALBUMS_ID -> {
                // Return albums
                getAlbums().map { album ->
                    createBrowsableItem(
                        "album_${album.id}",
                        album.title,
                        album.artist
                    )
                }.toMutableList()
            }
            else -> {
                // Return playable items (songs)
                if (parentId.startsWith("playlist_")) {
                    val playlistId = parentId.removePrefix("playlist_")
                    getSongsForPlaylist(playlistId).map { song ->
                        createPlayableItem(
                            song.id,
                            song.title,
                            song.artist,
                            song.albumArtUri
                        )
                    }.toMutableList()
                } else {
                    mutableListOf()
                }
            }
        }

        result.sendResult(mediaItems)
    }

    // Media session callback — handles playback controls
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            // Start/resume playback
            startPlayback()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }

        override fun onPause() {
            // Pause playback
            pausePlayback()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onSkipToNext() {
            // Skip to next track
            playNextTrack()
        }

        override fun onSkipToPrevious() {
            // Skip to previous track
            playPreviousTrack()
        }

        override fun onSeekTo(pos: Long) {
            // Seek to position
            seekTo(pos)
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
            // Play a specific item
            playMediaItem(mediaId)
        }

        override fun onPlayFromSearch(query: String, extras: Bundle?) {
            // Handle voice search: "Play jazz music"
            val results = searchMedia(query)
            if (results.isNotEmpty()) {
                playMediaItem(results.first().id)
            }
        }

        override fun onCustomAction(action: String, extras: Bundle?) {
            when (action) {
                "ACTION_LIKE" -> likeCurrentTrack()
                "ACTION_SHUFFLE" -> toggleShuffle()
            }
        }
    }

    private fun updatePlaybackState(state: Int) {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setState(state, getCurrentPosition(), 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO
            )
            // Custom actions shown on Auto UI
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "ACTION_LIKE", "Like", R.drawable.ic_like
                ).build()
            )

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun updateMetadata(song: Song) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.albumArtUri)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.durationMs)
            .build()

        mediaSession.setMetadata(metadata)
    }

    // Helper methods
    private fun createBrowsableItem(
        id: String, title: String, subtitle: String
    ): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()
        return MediaBrowserCompat.MediaItem(description, FLAG_BROWSABLE)
    }

    private fun createPlayableItem(
        id: String, title: String, artist: String, artUri: String
    ): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(artist)
            .setIconUri(Uri.parse(artUri))
            .build()
        return MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
    }

    private fun isValidClient(packageName: String): Boolean = true
    private fun startPlayback() { /* ... */ }
    private fun pausePlayback() { /* ... */ }
    private fun playNextTrack() { /* ... */ }
    private fun playPreviousTrack() { /* ... */ }
    private fun seekTo(pos: Long) { /* ... */ }
    private fun getCurrentPosition(): Long = 0L
    private fun playMediaItem(id: String) { /* ... */ }
    private fun searchMedia(query: String): List<Song> = emptyList()
    private fun likeCurrentTrack() { /* ... */ }
    private fun toggleShuffle() { /* ... */ }
    private fun getPlaylists(): List<Playlist> = emptyList()
    private fun getAlbums(): List<Album> = emptyList()
    private fun getSongsForPlaylist(id: String): List<Song> = emptyList()
}

data class Song(
    val id: String, val title: String, val artist: String,
    val album: String, val albumArtUri: String, val durationMs: Long
)
data class Playlist(val id: String, val name: String, val songCount: Int)
data class Album(val id: String, val title: String, val artist: String)
```

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".AutoMediaService"
    android:exported="true">
    <intent-filter>
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>

<!-- Declare Android Auto support -->
<meta-data
    android:name="com.google.android.gms.car.application"
    android:resource="@xml/automotive_app_desc" />
```

```xml
<!-- res/xml/automotive_app_desc.xml -->
<automotiveApp>
    <uses name="media" />
</automotiveApp>
```

---

## 2. Messaging Apps Integration

### Theory

Messaging apps on Android Auto allow users to **receive and reply to messages** using voice — keeping hands on the wheel and eyes on the road. The system reads messages aloud and uses voice-to-text for replies.

```
┌─────────────────────────────────────────────────────────────────┐
│            Messaging on Android Auto Flow                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. New message arrives on phone                                │
│     │                                                            │
│     ▼                                                            │
│  2. App posts notification with MessagingStyle                  │
│     │                                                            │
│     ▼                                                            │
│  3. Android Auto displays notification                          │
│     ┌─────────────────────────────────────────┐                 │
│     │  💬 John: "Are you on your way?"        │                 │
│     │        [Play]  [Reply]  [Mark Read]     │                 │
│     └─────────────────────────────────────────┘                 │
│     │                                                            │
│     ├── User taps "Play" → System reads message aloud           │
│     │                                                            │
│     └── User taps "Reply" → Voice input activated               │
│         │                                                        │
│         ▼                                                        │
│     4. User speaks reply: "Yes, 10 minutes away"                │
│         │                                                        │
│         ▼                                                        │
│     5. System sends RemoteInput to your app                     │
│         │                                                        │
│         ▼                                                        │
│     6. App's BroadcastReceiver handles the reply                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Messaging Notification for Auto (Kotlin)

```kotlin
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput

class MessagingNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "messaging_channel"
        const val NOTIFICATION_ID = 1001
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val ACTION_REPLY = "com.example.ACTION_REPLY"
        const val ACTION_MARK_READ = "com.example.ACTION_MARK_READ"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
    }

    init {
        createNotificationChannel()
    }

    fun showMessageNotification(
        conversationId: String,
        senderName: String,
        messageText: String,
        timestamp: Long
    ) {
        // Create the sender
        val sender = Person.Builder()
            .setName(senderName)
            .setKey(conversationId)
            .build()

        // Create the "me" person
        val me = Person.Builder()
            .setName("Me")
            .build()

        // Build MessagingStyle — REQUIRED for Android Auto
        val messagingStyle = NotificationCompat.MessagingStyle(me)
            .setConversationTitle(senderName)
            .addMessage(
                NotificationCompat.MessagingStyle.Message(
                    messageText,
                    timestamp,
                    sender
                )
            )

        // Create Reply action with RemoteInput (voice reply on Auto)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_CONVERSATION_ID, conversationId)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context, conversationId.hashCode(), replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_reply, "Reply", replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .build()

        // Create Mark as Read action
        val markReadIntent = Intent(context, ReplyReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_CONVERSATION_ID, conversationId)
        }

        val markReadPendingIntent = PendingIntent.getBroadcast(
            context, conversationId.hashCode() + 1, markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadAction = NotificationCompat.Action.Builder(
            R.drawable.ic_check, "Mark Read", markReadPendingIntent
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setStyle(messagingStyle)  // Must use MessagingStyle for Auto
            .addAction(replyAction)
            .addAction(markReadAction)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming messages"
            }
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}

// BroadcastReceiver to handle replies
class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val conversationId = intent.getStringExtra(
            MessagingNotificationHelper.EXTRA_CONVERSATION_ID
        ) ?: return

        when (intent.action) {
            MessagingNotificationHelper.ACTION_REPLY -> {
                // Get the reply text from RemoteInput
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val replyText = remoteInput?.getCharSequence(
                    MessagingNotificationHelper.KEY_TEXT_REPLY
                )?.toString()

                if (replyText != null) {
                    // Send the reply through your messaging service
                    sendReply(context, conversationId, replyText)

                    // Update the notification to show the reply was sent
                    updateNotificationWithReply(context, conversationId, replyText)
                }
            }

            MessagingNotificationHelper.ACTION_MARK_READ -> {
                // Mark conversation as read
                markAsRead(context, conversationId)

                // Cancel the notification
                NotificationManagerCompat.from(context)
                    .cancel(MessagingNotificationHelper.NOTIFICATION_ID)
            }
        }
    }

    private fun sendReply(context: Context, conversationId: String, text: String) {
        // Send reply via your messaging backend
    }

    private fun markAsRead(context: Context, conversationId: String) {
        // Mark conversation as read
    }

    private fun updateNotificationWithReply(
        context: Context, conversationId: String, replyText: String
    ) {
        // Update notification to show reply was sent
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<receiver
    android:name=".ReplyReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="com.example.ACTION_REPLY" />
        <action android:name="com.example.ACTION_MARK_READ" />
    </intent-filter>
</receiver>
```

---

## 3. Android Automotive OS Development

### Theory

Android Automotive OS (AAOS) is a **full Android distribution embedded in the car**. Unlike Android Auto (which mirrors your phone), AAOS apps run natively on the vehicle's hardware. This opens up more app categories and deeper vehicle integration.

**Key Differences from Android Auto:**
- Apps are installed from the Play Store on the car
- No phone required
- Access to vehicle-specific APIs (speed, fuel, HVAC)
- More app categories: video, parking, charging, settings
- Car App Library used for building most app types

```
┌─────────────────────────────────────────────────────────────────┐
│           Android Automotive OS Architecture                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   Car Head Unit                           │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │              Android Automotive OS                   │  │  │
│  │  │                                                      │  │  │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐            │  │  │
│  │  │  │ Media    │ │ Nav      │ │ Parking  │   Apps     │  │  │
│  │  │  │ Apps     │ │ Apps     │ │ Apps     │   from     │  │  │
│  │  │  └──────────┘ └──────────┘ └──────────┘   Play     │  │  │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐  Store    │  │  │
│  │  │  │ Video    │ │ EV       │ │ Settings │            │  │  │
│  │  │  │ (parked) │ │ Charging │ │          │            │  │  │
│  │  │  └──────────┘ └──────────┘ └──────────┘            │  │  │
│  │  │                                                      │  │  │
│  │  │  ┌──────────────────────────────────────────────┐    │  │  │
│  │  │  │         Car Service / Vehicle HAL            │    │  │  │
│  │  │  │  • Speed    • Fuel level   • HVAC            │    │  │  │
│  │  │  │  • Gear     • Door status  • Battery (EV)    │    │  │  │
│  │  │  └──────────────────────────────────────────────┘    │  │  │
│  │  │                                                      │  │  │
│  │  └──────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Car App Library — Screen-based App (Kotlin)

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.car.app:app:1.4.0")
    // For Automotive OS specific features
    implementation("androidx.car.app:app-automotive:1.4.0")
}
```

```kotlin
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator

class MyCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return MyCarSession()
    }
}

class MyCarSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen {
        return MainCarScreen(carContext)
    }
}
```

```kotlin
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class MainCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        // Create a list of items
        val itemList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Now Playing")
                    .addText("Song Title — Artist Name")
                    .setImage(
                        CarIcon.Builder(
                            IconCompat.createWithResource(
                                carContext, R.drawable.ic_music
                            )
                        ).build()
                    )
                    .setOnClickListener {
                        screenManager.push(NowPlayingScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Browse Library")
                    .addText("Playlists, Albums, Artists")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(BrowseScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Settings")
                    .addText("Audio quality, Downloads")
                    .setOnClickListener {
                        screenManager.push(SettingsCarScreen(carContext))
                    }
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setTitle("My Music App")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(itemList)
            .build()
    }
}

// Grid template for visual browsing
class BrowseScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val gridItems = ItemList.Builder()
            .addItem(
                GridItem.Builder()
                    .setTitle("Rock")
                    .setImage(
                        CarIcon.Builder(
                            IconCompat.createWithResource(carContext, R.drawable.ic_rock)
                        ).build()
                    )
                    .setOnClickListener { /* Open rock category */ }
                    .build()
            )
            .addItem(
                GridItem.Builder()
                    .setTitle("Pop")
                    .setImage(
                        CarIcon.Builder(
                            IconCompat.createWithResource(carContext, R.drawable.ic_pop)
                        ).build()
                    )
                    .setOnClickListener { /* Open pop category */ }
                    .build()
            )
            .addItem(
                GridItem.Builder()
                    .setTitle("Jazz")
                    .setImage(
                        CarIcon.Builder(
                            IconCompat.createWithResource(carContext, R.drawable.ic_jazz)
                        ).build()
                    )
                    .setOnClickListener { /* Open jazz category */ }
                    .build()
            )
            .build()

        return GridTemplate.Builder()
            .setTitle("Browse")
            .setHeaderAction(Action.BACK)
            .setSingleList(gridItems)
            .build()
    }
}

// Sign-in screen (Automotive OS)
class SignInCarScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val signInMethod = SignInTemplate.SignInMethod.InputSignInMethod.Builder(
            object : SignInTemplate.SignInMethod.InputSignInMethod.OnInputCompletedListener {
                override fun onInputCompleted(text: String) {
                    // Handle sign-in with entered text (e.g., PIN)
                    authenticate(text)
                }
            }
        )
            .setKeyboardType(
                SignInTemplate.SignInMethod.InputSignInMethod.KEYBOARD_DEFAULT
            )
            .setHint("Enter your PIN")
            .build()

        return SignInTemplate.Builder(signInMethod)
            .setTitle("Sign In")
            .setHeaderAction(Action.BACK)
            .addAction(
                Action.Builder()
                    .setTitle("QR Code Sign In")
                    .setOnClickListener {
                        // Show QR code for phone-based sign in
                    }
                    .build()
            )
            .build()
    }

    private fun authenticate(pin: String) { /* ... */ }
}
```

```xml
<!-- AndroidManifest.xml for Automotive OS -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Declare Automotive OS support -->
    <uses-feature
        android:name="android.hardware.type.automotive"
        android:required="true" />

    <!-- Required for media apps -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <application>
        <!-- Car App Service -->
        <service
            android:name=".MyCarAppService"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.car.app.CarAppService" />
                <category android:name="androidx.car.app.category.MEDIA" />
            </intent-filter>
        </service>

        <!-- Automotive OS Activity (embedded in car UI) -->
        <activity
            android:name="androidx.car.app.activity.CarAppActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:theme="@android:style/Theme.DeviceDefault.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <meta-data
                android:name="distractionOptimized"
                android:value="true" />
        </activity>
    </application>
</manifest>
```

### Code: Accessing Vehicle Properties (Automotive OS) (Kotlin)

```kotlin
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class VehicleInfoActivity : AppCompatActivity() {

    private lateinit var car: Car
    private lateinit var propertyManager: CarPropertyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connect to the Car service
        car = Car.createCar(this)
        propertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

        // Read vehicle properties
        readVehicleData()

        // Listen for property changes
        registerPropertyListeners()
    }

    private fun readVehicleData() {
        // Get current speed (m/s)
        val speed = propertyManager.getFloatProperty(
            VehiclePropertyIds.PERF_VEHICLE_SPEED, 0
        )

        // Get fuel level (percentage)
        val fuelLevel = propertyManager.getFloatProperty(
            VehiclePropertyIds.FUEL_LEVEL, 0
        )

        // Get gear selection
        val gear = propertyManager.getIntProperty(
            VehiclePropertyIds.GEAR_SELECTION, 0
        )

        // Get EV battery level
        val evBattery = propertyManager.getFloatProperty(
            VehiclePropertyIds.EV_BATTERY_LEVEL, 0
        )
    }

    private fun registerPropertyListeners() {
        // Listen for speed changes
        propertyManager.registerCallback(
            object : CarPropertyManager.CarPropertyEventCallback {
                override fun onChangeEvent(value: CarPropertyValue<*>) {
                    val speed = value.value as Float
                    updateSpeedDisplay(speed)
                }

                override fun onErrorEvent(propertyId: Int, zone: Int) {
                    // Handle error
                }
            },
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_NORMAL
        )
    }

    private fun updateSpeedDisplay(speedMs: Float) {
        val speedKmh = speedMs * 3.6f
        // Update UI
    }

    override fun onDestroy() {
        super.onDestroy()
        car.disconnect()
    }
}
```

---

## Testing

### Android Auto Testing

```
# Desktop Head Unit (DHU) — Test Android Auto on your computer

# 1. Install DHU from SDK Manager:
# SDK Manager → SDK Tools → Android Auto Desktop Head Unit Emulator

# 2. Enable developer mode in Android Auto app on phone:
# Android Auto → Settings → Tap version 10 times → Developer settings

# 3. Run DHU:
cd $ANDROID_SDK/extras/google/auto/
./desktop-head-unit

# 4. Connect phone via USB and test your app
```

### Android Automotive OS Testing

```
# Use the Automotive emulator in Android Studio:
# 1. AVD Manager → Create Virtual Device
# 2. Select "Automotive" category
# 3. Choose system image (e.g., Automotive with Play Store)
# 4. Run your app on the emulator
```

---

## Summary Table

| Feature | Key API | Platform |
|---------|---------|----------|
| Media Browsing | `MediaBrowserServiceCompat` | Auto + Automotive |
| Playback Control | `MediaSessionCompat` | Auto + Automotive |
| Voice Search | `onPlayFromSearch()` | Auto + Automotive |
| Messaging | `MessagingStyle` + `RemoteInput` | Auto + Automotive |
| Car App UI | `CarAppService` + Templates | Auto + Automotive |
| Vehicle Data | `CarPropertyManager` | Automotive only |
| Sign In | `SignInTemplate` | Automotive only |
| Video Playback | Video templates (parked only) | Automotive only |

---

## Best Practices

1. **Minimize driver distraction** — follow Auto design guidelines strictly
2. **Support voice interaction** — implement `onPlayFromSearch()` for hands-free
3. **Use MessagingStyle** — it's required for notifications on Auto
4. **Test with DHU** — Desktop Head Unit emulator for Android Auto testing
5. **Handle parking vs driving** — some features only available when parked
6. **Keep template depth shallow** — max 5 screens deep in screen stack
7. **Use the Car App Library** — it handles safety restrictions automatically
8. **Dual target**: Build one app that works on both Auto and Automotive when possible
