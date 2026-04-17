# Android Networking and Data Guide

## Overview

Modern Android apps are data-driven applications that communicate with remote servers, databases, and services. This guide covers the essential networking and data handling libraries that form the backbone of professional Android development.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ANDROID NETWORKING ECOSYSTEM                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        APPLICATION LAYER                            │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐  │   │
│  │  │  Retrofit   │  │   Apollo    │  │  WebSocket  │  │   Image    │  │   │
│  │  │  (REST)     │  │  (GraphQL)  │  │  (Real-time)│  │   Loader   │  │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────┬──────┘  │   │
│  └─────────┼────────────────┼────────────────┼───────────────┼─────────┘   │
│            │                │                │               │              │
│            └────────────────┴────────────────┴───────────────┘              │
│                                    │                                        │
│  ┌─────────────────────────────────▼───────────────────────────────────┐   │
│  │                         HTTP CLIENT LAYER                            │   │
│  │                    ┌───────────────────────┐                        │   │
│  │                    │        OkHttp         │                        │   │
│  │                    │  • Connection Pooling │                        │   │
│  │                    │  • Caching            │                        │   │
│  │                    │  • Interceptors       │                        │   │
│  │                    └───────────┬───────────┘                        │   │
│  └────────────────────────────────┼────────────────────────────────────┘   │
│                                   │                                        │
│  ┌────────────────────────────────▼────────────────────────────────────┐   │
│  │                      SERIALIZATION LAYER                             │   │
│  │    ┌────────────┐    ┌────────────┐    ┌────────────────────────┐   │   │
│  │    │    Gson    │    │   Moshi    │    │  Kotlinx Serialization │   │   │
│  │    └────────────┘    └────────────┘    └────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                        │
│                                   ▼                                        │
│                          ┌───────────────┐                                 │
│                          │    Server     │                                 │
│                          │  (REST/GQL)   │                                 │
│                          └───────────────┘                                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Why These Libraries Matter

| Library | Purpose | When to Use |
|---------|---------|-------------|
| **Retrofit** | Type-safe REST client | Any REST API integration |
| **OkHttp** | HTTP client foundation | Underlying HTTP operations |
| **Apollo** | GraphQL client | GraphQL API integration |
| **WebSocket** | Real-time communication | Chat, live updates, gaming |
| **Coil/Glide** | Image loading | Any image-heavy app |
| **Kotlinx Serialization** | JSON parsing | Modern Kotlin projects |

---

## 6.1 Retrofit

### What is Retrofit?

Retrofit is a **type-safe HTTP client** developed by Square that transforms REST API endpoints into Kotlin/Java interfaces using annotations. It abstracts away the complexity of making HTTP requests and parsing responses.

### Why Use Retrofit?

1. **Type Safety**: Compile-time verification of API calls
2. **Declarative**: Define APIs as interfaces with annotations
3. **Extensible**: Pluggable converters and call adapters
4. **Kotlin Integration**: First-class suspend function support
5. **Industry Standard**: Used by most professional Android apps

### How Retrofit Works

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          RETROFIT REQUEST LIFECYCLE                           │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. INTERFACE DEFINITION                                                     │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  interface ApiService {                                                 │  │
│  │      @GET("users/{id}")                                                │  │
│  │      suspend fun getUser(@Path("id") id: Int): User                    │  │
│  │  }                                                                     │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│                                    ▼                                         │
│  2. DYNAMIC PROXY GENERATION                                                 │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  Retrofit uses Java's Dynamic Proxy to create implementation           │  │
│  │  at runtime. When you call a method, it intercepts and builds          │  │
│  │  the HTTP request based on annotations.                                │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│                                    ▼                                         │
│  3. REQUEST BUILDING                                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  @GET("users/{id}") + id=42  →  GET /users/42                          │  │
│  │  @Query("sort")="name"       →  ?sort=name                             │  │
│  │  @Body user                  →  Request body (JSON)                    │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│                                    ▼                                         │
│  4. OKHTTP EXECUTION                                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  OkHttp handles actual HTTP:                                           │  │
│  │  • Connection management    • TLS/SSL                                  │  │
│  │  • Request/Response I/O     • Interceptors                             │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│                                    ▼                                         │
│  5. RESPONSE CONVERSION                                                      │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  Converter Factory (Gson/Moshi/Kotlinx) parses JSON:                   │  │
│  │  {"id": 42, "name": "John"}  →  User(id=42, name="John")               │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Dependencies Setup

```kotlin
// build.gradle.kts
dependencies {
    // Retrofit core
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    
    // Converter factories (choose one or more)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    
    // For Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // OkHttp with logging
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Call adapters
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0") // For RxJava
}
```

### HTTP Annotation Reference

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          RETROFIT ANNOTATIONS                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  HTTP METHODS:                                                               │
│  ┌────────────────┬──────────────────────────────────────────────────────┐  │
│  │ @GET           │ Read resource(s) - idempotent, safe                  │  │
│  │ @POST          │ Create resource - not idempotent                     │  │
│  │ @PUT           │ Update/replace resource - idempotent                 │  │
│  │ @PATCH         │ Partial update - not idempotent                      │  │
│  │ @DELETE        │ Remove resource - idempotent                         │  │
│  │ @HEAD          │ Headers only, no body                                │  │
│  │ @OPTIONS       │ Supported methods                                    │  │
│  │ @HTTP          │ Custom method                                        │  │
│  └────────────────┴──────────────────────────────────────────────────────┘  │
│                                                                              │
│  PARAMETER ANNOTATIONS:                                                      │
│  ┌────────────────┬──────────────────────────────────────────────────────┐  │
│  │ @Path          │ URL path replacement: /users/{id}                    │  │
│  │ @Query         │ Query parameter: ?name=value                         │  │
│  │ @QueryMap      │ Multiple query params as Map                         │  │
│  │ @Body          │ Request body (serialized to JSON)                    │  │
│  │ @Field         │ Form-encoded field                                   │  │
│  │ @FieldMap      │ Multiple form fields as Map                          │  │
│  │ @Header        │ Single header value                                  │  │
│  │ @HeaderMap     │ Multiple headers as Map                              │  │
│  │ @Part          │ Multipart body part                                  │  │
│  │ @PartMap       │ Multiple parts as Map                                │  │
│  │ @Url           │ Dynamic URL (overrides base URL)                     │  │
│  └────────────────┴──────────────────────────────────────────────────────┘  │
│                                                                              │
│  REQUEST MODIFIERS:                                                          │
│  ┌────────────────┬──────────────────────────────────────────────────────┐  │
│  │ @Headers       │ Static headers on method                             │  │
│  │ @FormUrlEncoded│ application/x-www-form-urlencoded                    │  │
│  │ @Multipart     │ multipart/form-data                                  │  │
│  │ @Streaming     │ Don't buffer response in memory                      │  │
│  └────────────────┴──────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Complete API Interface Example

```kotlin
interface ApiService {
    
    // ═══════════════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * GET all users with pagination
     * URL: GET /users?page=1&limit=20&sort=name
     */
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sortBy: String? = null
    ): Response<List<User>>
    
    /**
     * GET single user by ID
     * URL: GET /users/42
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): User
    
    /**
     * Create new user
     * URL: POST /users
     * Body: {"name": "John", "email": "john@example.com"}
     */
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): User
    
    /**
     * Update entire user (replace)
     * URL: PUT /users/42
     */
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body user: UpdateUserRequest
    ): User
    
    /**
     * Partial user update
     * URL: PATCH /users/42
     */
    @PATCH("users/{id}")
    suspend fun patchUser(
        @Path("id") userId: Int,
        @Body updates: Map<String, @JvmSuppressWildcards Any>
    ): User
    
    /**
     * Delete user
     * URL: DELETE /users/42
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>
    
    // ═══════════════════════════════════════════════════════════════════════
    // QUERY PARAMETERS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Search with multiple optional filters
     * URL: GET /users/search?name=john&status=active&role=admin
     */
    @GET("users/search")
    suspend fun searchUsers(
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("role") role: String? = null,
        @Query("minAge") minAge: Int? = null,
        @Query("maxAge") maxAge: Int? = null
    ): List<User>
    
    /**
     * Dynamic query parameters using Map
     * URL: GET /users/filter?key1=value1&key2=value2...
     */
    @GET("users/filter")
    suspend fun filterUsers(@QueryMap filters: Map<String, String>): List<User>
    
    // ═══════════════════════════════════════════════════════════════════════
    // HEADERS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Static headers defined on method
     */
    @Headers(
        "Accept: application/json",
        "X-Api-Version: 2",
        "Cache-Control: no-cache"
    )
    @GET("protected/resource")
    suspend fun getProtectedResource(): Resource
    
    /**
     * Dynamic header passed as parameter
     */
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Header("Accept-Language") language: String = "en"
    ): UserProfile
    
    /**
     * Multiple dynamic headers
     */
    @GET("analytics/data")
    suspend fun getAnalytics(@HeaderMap headers: Map<String, String>): Analytics
    
    // ═══════════════════════════════════════════════════════════════════════
    // FORM DATA
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Form URL encoded login (OAuth style)
     * Content-Type: application/x-www-form-urlencoded
     */
    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String = "read write"
    ): TokenResponse
    
    /**
     * Registration with multiple fields
     */
    @FormUrlEncoded
    @POST("register")
    suspend fun register(@FieldMap fields: Map<String, String>): User
    
    // ═══════════════════════════════════════════════════════════════════════
    // FILE UPLOADS (Multipart)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Single file upload with metadata
     * Content-Type: multipart/form-data
     */
    @Multipart
    @POST("upload/avatar")
    suspend fun uploadAvatar(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): UploadResponse
    
    /**
     * Multiple files upload
     */
    @Multipart
    @POST("upload/gallery")
    suspend fun uploadGallery(
        @Part images: List<MultipartBody.Part>,
        @Part("album_id") albumId: RequestBody
    ): UploadResponse
    
    /**
     * Complex upload with multiple parts
     */
    @Multipart
    @POST("posts/create")
    suspend fun createPostWithMedia(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part media: List<MultipartBody.Part>
    ): Post
    
    // ═══════════════════════════════════════════════════════════════════════
    // STREAMING & DOWNLOAD
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Download file without buffering in memory
     * Use for large files
     */
    @Streaming
    @GET("files/{id}/download")
    suspend fun downloadFile(@Path("id") fileId: String): ResponseBody
    
    /**
     * Dynamic URL (ignores base URL)
     */
    @GET
    suspend fun downloadFromUrl(@Url url: String): ResponseBody
}
```

### Data Models

```kotlin
// Using Kotlinx Serialization
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val roles: List<String> = emptyList()
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class UpdateUserRequest(
    val name: String,
    val email: String
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String
)

@Serializable
data class UploadResponse(
    val success: Boolean,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_id") val fileId: String
)
```

### Converter Factories Comparison

Converter factories transform HTTP response bodies into Kotlin/Java objects and vice versa.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                      CONVERTER FACTORIES DEEP DIVE                            │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                           GSON                                       │   │
│  ├──────────────────────────────────────────────────────────────────────┤   │
│  │  ✓ Mature and stable (Google)       ✗ Reflection-based (slower)     │   │
│  │  ✓ Wide community support           ✗ Larger APK size               │   │
│  │  ✓ Extensive documentation          ✗ No Kotlin null safety         │   │
│  │  ✓ Custom TypeAdapters              ✗ Runtime errors for bad JSON   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                           MOSHI                                      │   │
│  ├──────────────────────────────────────────────────────────────────────┤   │
│  │  ✓ Modern (Square)                  ✗ Smaller community             │   │
│  │  ✓ Kotlin support with codegen      ✗ Still needs adapters          │   │
│  │  ✓ Better null safety               ✗ Codegen adds build time       │   │
│  │  ✓ Smaller and faster than Gson                                     │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                    KOTLINX SERIALIZATION                             │   │
│  ├──────────────────────────────────────────────────────────────────────┤   │
│  │  ✓ Official JetBrains library       ✗ Kotlin-only                   │   │
│  │  ✓ Compile-time code generation     ✗ Requires compiler plugin      │   │
│  │  ✓ Full null safety                 ✗ Newer, less documentation     │   │
│  │  ✓ Multiplatform support            ✓ Fastest performance           │   │
│  │  ✓ Sealed class support             ✓ Smallest size                 │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  PERFORMANCE BENCHMARK (approximate):                                        │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │  Parse 1000 JSON objects:                                          │     │
│  │  Kotlinx Serialization: ████████░░░░░░░░░░░░  ~50ms                │     │
│  │  Moshi (codegen):       ██████████░░░░░░░░░░  ~70ms                │     │
│  │  Moshi (reflection):    ████████████░░░░░░░░  ~90ms                │     │
│  │  Gson:                  ██████████████░░░░░░  ~120ms               │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

#### Gson Setup (Detailed)

```kotlin
// build.gradle.kts
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Data class with Gson annotations
data class User(
    @SerializedName("user_id") 
    val id: Int,
    
    @SerializedName("user_name") 
    val name: String,
    
    val email: String,  // No annotation = uses field name
    
    @SerializedName("created_at")
    @Expose  // Only serialize if Expose is enabled
    val createdAt: String? = null,
    
    @Transient  // Never serialize
    val localOnlyField: String? = null
)

// Custom Gson configuration
val gson = GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    .serializeNulls()  // Include null fields in JSON
    .excludeFieldsWithoutExposeAnnotation()  // Require @Expose
    .setPrettyPrinting()
    .registerTypeAdapter(Date::class.java, DateTypeAdapter())
    .create()

// Custom TypeAdapter for complex types
class DateTypeAdapter : TypeAdapter<Date>() {
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(format.format(value))
        }
    }
    
    override fun read(input: JsonReader): Date? {
        return if (input.peek() == JsonToken.NULL) {
            input.nextNull()
            null
        } else {
            format.parse(input.nextString())
        }
    }
}

// Retrofit with custom Gson
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create(gson))
    .build()
```

#### Moshi Setup (Detailed)

```kotlin
// build.gradle.kts
implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
implementation("com.squareup.moshi:moshi:1.15.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

// Data class with Moshi annotations
@JsonClass(generateAdapter = true)  // Generate adapter at compile time
data class User(
    @Json(name = "user_id") 
    val id: Int,
    
    @Json(name = "user_name") 
    val name: String,
    
    val email: String,
    
    @Json(name = "created_at")
    val createdAt: String? = null,
    
    @Transient  // Ignored in serialization
    val localField: String = ""
)

// Custom Moshi adapter
class DateAdapter {
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    @ToJson
    fun toJson(date: Date): String = format.format(date)
    
    @FromJson
    fun fromJson(json: String): Date = format.parse(json)!!
}

// Enum adapter
@JsonClass(generateAdapter = false)
enum class UserStatus {
    @Json(name = "active") ACTIVE,
    @Json(name = "inactive") INACTIVE,
    @Json(name = "pending") PENDING
}

// Moshi builder
val moshi = Moshi.Builder()
    .add(DateAdapter())
    .addLast(KotlinJsonAdapterFactory())  // Fallback for non-codegen classes
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
```

#### Kotlinx Serialization Setup (Detailed)

```kotlin
// build.gradle.kts
plugins {
    kotlin("plugin.serialization") version "1.9.0"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
}

// Data classes with Kotlinx annotations
@Serializable
data class User(
    val id: Int,
    
    @SerialName("user_name")
    val name: String,
    
    val email: String,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    // Default values work seamlessly
    val roles: List<String> = emptyList(),
    
    // Nested objects
    val profile: UserProfile? = null,
    
    // Transient = ignored
    @Transient
    val localOnly: String = ""
)

@Serializable
data class UserProfile(
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

// Sealed classes for polymorphism
@Serializable
sealed class ApiResponse<out T> {
    @Serializable
    @SerialName("success")
    data class Success<T>(val data: T) : ApiResponse<T>()
    
    @Serializable
    @SerialName("error")
    data class Error(
        val code: Int,
        val message: String,
        val details: Map<String, String>? = null
    ) : ApiResponse<Nothing>()
}

// Custom serializer
object DateAsLongSerializer : KSerializer<Date> {
    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeLong(value.time)
    }
    
    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeLong())
    }
}

// JSON configuration
val json = Json {
    ignoreUnknownKeys = true        // Don't fail on unknown fields
    isLenient = true                 // Accept malformed JSON
    encodeDefaults = false           // Don't include default values
    explicitNulls = false            // Don't include null values
    coerceInputValues = true         // Coerce invalid values to defaults
    prettyPrint = false              // Compact output
    classDiscriminator = "type"      // For polymorphic types
}

// Retrofit setup
val contentType = "application/json".toMediaType()
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(json.asConverterFactory(contentType))
    .build()
```

### Call Adapters

Call adapters transform the way Retrofit handles async operations.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            CALL ADAPTERS                                      │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                        CALL ADAPTER FLOW                            │    │
│  │                                                                     │    │
│  │   Interface Method  ──▶  CallAdapter  ──▶  Return Type              │    │
│  │                                                                     │    │
│  │   suspend fun getUser(): User          ──▶  Direct result           │    │
│  │   fun getUser(): Call<User>            ──▶  Callback-based          │    │
│  │   fun getUser(): Single<User>          ──▶  RxJava Single           │    │
│  │   fun getUser(): Observable<User>      ──▶  RxJava Observable       │    │
│  │   fun getUser(): Deferred<User>        ──▶  Kotlin Deferred         │    │
│  │   fun getUser(): Flow<User>            ──▶  Kotlin Flow             │    │
│  │                                                                     │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

#### Coroutines (Built-in Suspend Support)

```kotlin
interface ApiService {
    // Directly returns data (throws exception on error)
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
    
    // Returns Response wrapper (access to headers, code, etc.)
    @GET("users")
    suspend fun getUsers(): Response<List<User>>
    
    // Returns ResponseBody for raw access
    @GET("files/{id}")
    suspend fun downloadFile(@Path("id") id: String): ResponseBody
}

// Usage in ViewModel
class UserViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                } else {
                    _error.emit("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                _error.emit("Network error: ${e.message}")
            } catch (e: HttpException) {
                _error.emit("HTTP error: ${e.code()}")
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun loadUser(id: Int) {
        viewModelScope.launch {
            try {
                val user = apiService.getUser(id)  // Direct return
                // Use user
            } catch (e: Exception) {
                _error.emit(e.message ?: "Unknown error")
            }
        }
    }
}
```

#### RxJava Adapter

```kotlin
// build.gradle.kts
implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
implementation("io.reactivex.rxjava3:rxjava:3.1.6")
implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

interface ApiService {
    @GET("users")
    fun getUsers(): Single<List<User>>
    
    @GET("users/{id}")
    fun getUser(@Path("id") id: Int): Single<User>
    
    @GET("users")
    fun getUsersObservable(): Observable<List<User>>
    
    @GET("stream/updates")
    fun getUpdates(): Flowable<Update>  // Backpressure support
    
    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: Int): Completable
    
    @POST("users")
    fun createUser(@Body user: User): Maybe<User>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Usage
apiService.getUsers()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { users -> handleUsers(users) },
        { error -> handleError(error) }
    )
```

### Interceptors (Detailed)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         INTERCEPTOR CHAIN                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Request ──▶ App Interceptor 1 ──▶ App Interceptor 2 ──▶ ...               │
│                                                              │               │
│                                                              ▼               │
│                                                      ┌──────────────┐        │
│                                                      │    Cache     │        │
│                                                      │  (if exists) │        │
│                                                      └──────┬───────┘        │
│                                                             │               │
│   ... ◀── Network Interceptor 2 ◀── Network Interceptor 1 ◀─┘               │
│    │                                                                        │
│    ▼                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐        │
│   │                         NETWORK                                 │        │
│   │  Connection Pool ──▶ Socket ──▶ TLS ──▶ Server                 │        │
│   └────────────────────────────────────────────────────────────────┘        │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// LOGGING INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

val loggingInterceptor = HttpLoggingInterceptor { message ->
    Log.d("OkHttp", message)
}.apply {
    level = when {
        BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
        else -> HttpLoggingInterceptor.Level.NONE
    }
    // Redact sensitive headers
    redactHeader("Authorization")
    redactHeader("Cookie")
}

// ═══════════════════════════════════════════════════════════════════════════
// AUTHENTICATION INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for certain endpoints
        if (originalRequest.url.encodedPath.contains("public")) {
            return chain.proceed(originalRequest)
        }
        
        val token = tokenManager.getAccessToken()
        
        val authenticatedRequest = originalRequest.newBuilder()
            .apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TOKEN REFRESH AUTHENTICATOR (handles 401)
// ═══════════════════════════════════════════════════════════════════════════

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi  // Separate API for auth calls
) : Authenticator {
    
    private val lock = Object()
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't retry if we already tried
        if (response.request.header("X-Retry-Count")?.toInt() ?: 0 >= 2) {
            return null
        }
        
        synchronized(lock) {
            val currentToken = tokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
            
            // Another thread already refreshed the token
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }
            
            // Refresh token synchronously
            val newToken = runBlocking {
                try {
                    val refreshToken = tokenManager.getRefreshToken() ?: return@runBlocking null
                    val tokens = authApi.refreshToken(RefreshRequest(refreshToken))
                    tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                    tokens.accessToken
                } catch (e: Exception) {
                    tokenManager.clearTokens()
                    null
                }
            }
            
            return newToken?.let { token ->
                response.request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("X-Retry-Count", 
                        ((response.request.header("X-Retry-Count")?.toInt() ?: 0) + 1).toString())
                    .build()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM HEADERS INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

class HeadersInterceptor(
    private val context: Context
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .addHeader("X-App-Build", BuildConfig.VERSION_CODE.toString())
            .addHeader("X-Platform", "Android")
            .addHeader("X-OS-Version", Build.VERSION.SDK_INT.toString())
            .addHeader("X-Device", "${Build.MANUFACTURER} ${Build.MODEL}")
            .addHeader("X-Request-Id", UUID.randomUUID().toString())
            .addHeader("Accept-Language", Locale.getDefault().language)
            .build()
        
        return chain.proceed(request)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// NETWORK CONNECTIVITY INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

class ConnectivityInterceptor(
    private val context: Context
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class NoConnectivityException : IOException("No internet connection")

// ═══════════════════════════════════════════════════════════════════════════
// RETRY INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                response?.close()
                response = chain.proceed(request)
                
                if (response.isSuccessful || !shouldRetry(response.code)) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
                if (attempt < maxRetries - 1) {
                    Thread.sleep(retryDelayMs * (attempt + 1))  // Exponential backoff
                }
            }
        }
        
        return response ?: throw exception ?: IOException("Unknown error")
    }
    
    private fun shouldRetry(code: Int): Boolean {
        return code in listOf(408, 429, 500, 502, 503, 504)
    }
}
```

### Error Handling

Robust error handling is critical for production apps. Here's a comprehensive approach:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          ERROR HANDLING STRATEGY                              │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                        ERROR TYPES                                  │    │
│  ├─────────────────────────────────────────────────────────────────────┤    │
│  │                                                                     │    │
│  │  NETWORK ERRORS (IOException)                                       │    │
│  │  ├── NoConnectivityException   → No internet connection            │    │
│  │  ├── SocketTimeoutException    → Request timed out                 │    │
│  │  ├── UnknownHostException      → DNS resolution failed             │    │
│  │  └── SSLException              → TLS/SSL handshake failed          │    │
│  │                                                                     │    │
│  │  HTTP ERRORS (Response.code != 2xx)                                │    │
│  │  ├── 400 Bad Request           → Invalid request format            │    │
│  │  ├── 401 Unauthorized          → Token expired/invalid             │    │
│  │  ├── 403 Forbidden             → No permission                     │    │
│  │  ├── 404 Not Found             → Resource doesn't exist            │    │
│  │  ├── 422 Unprocessable         → Validation errors                 │    │
│  │  ├── 429 Too Many Requests     → Rate limited                      │    │
│  │  └── 500+ Server Error         → Backend issue                     │    │
│  │                                                                     │    │
│  │  PARSING ERRORS                                                     │    │
│  │  ├── JsonParseException        → Invalid JSON                      │    │
│  │  ├── SerializationException    → Mismatch between JSON and model   │    │
│  │  └── NullPointerException      → Required field is null            │    │
│  │                                                                     │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// NETWORK RESULT WRAPPER
// ═══════════════════════════════════════════════════════════════════════════

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    data class Error(
        val code: Int,
        val message: String,
        val errorBody: ErrorResponse? = null
    ) : NetworkResult<Nothing>()
    
    data class Exception(
        val throwable: Throwable,
        val errorType: ErrorType
    ) : NetworkResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isException: Boolean get() = this is Exception
    
    fun getOrNull(): T? = (this as? Success)?.data
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw ApiException(code, message)
        is Exception -> throw throwable
    }
    
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Exception -> this
    }
    
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Int, String) -> Unit): NetworkResult<T> {
        if (this is Error) action(code, message)
        return this
    }
    
    inline fun onException(action: (Throwable) -> Unit): NetworkResult<T> {
        if (this is Exception) action(throwable)
        return this
    }
}

enum class ErrorType {
    NETWORK,           // No internet, timeout
    SERVER,            // 5xx errors
    CLIENT,            // 4xx errors
    AUTHENTICATION,    // 401, 403
    VALIDATION,        // 422
    RATE_LIMIT,        // 429
    PARSING,           // JSON parse error
    UNKNOWN
}

@Serializable
data class ErrorResponse(
    val code: String? = null,
    val message: String,
    val errors: Map<String, List<String>>? = null,
    val timestamp: String? = null
)

class ApiException(val code: Int, message: String) : Exception(message)

// ═══════════════════════════════════════════════════════════════════════════
// SAFE API CALL WRAPPER
// ═══════════════════════════════════════════════════════════════════════════

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = apiCall()
        
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = "Empty response body"
                )
            }
        } else {
            val errorBody = parseErrorBody(response.errorBody())
            NetworkResult.Error(
                code = response.code(),
                message = errorBody?.message ?: response.message(),
                errorBody = errorBody
            )
        }
    } catch (e: IOException) {
        val errorType = when (e) {
            is SocketTimeoutException -> ErrorType.NETWORK
            is UnknownHostException -> ErrorType.NETWORK
            is SSLException -> ErrorType.NETWORK
            is NoConnectivityException -> ErrorType.NETWORK
            else -> ErrorType.NETWORK
        }
        NetworkResult.Exception(e, errorType)
    } catch (e: HttpException) {
        NetworkResult.Error(e.code(), e.message())
    } catch (e: SerializationException) {
        NetworkResult.Exception(e, ErrorType.PARSING)
    } catch (e: Exception) {
        NetworkResult.Exception(e, ErrorType.UNKNOWN)
    }
}

private fun parseErrorBody(errorBody: ResponseBody?): ErrorResponse? {
    return try {
        errorBody?.string()?.let { json ->
            Json.decodeFromString<ErrorResponse>(json)
        }
    } catch (e: Exception) {
        null
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// REPOSITORY USAGE
// ═══════════════════════════════════════════════════════════════════════════

class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUsers(): NetworkResult<List<User>> {
        return safeApiCall { apiService.getUsers() }
    }
    
    suspend fun getUser(id: Int): NetworkResult<User> {
        return safeApiCall { apiService.getUserById(id) }
    }
    
    suspend fun createUser(request: CreateUserRequest): NetworkResult<User> {
        return safeApiCall { apiService.createUser(request) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// VIEWMODEL USAGE
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            when (val result = repository.getUsers()) {
                is NetworkResult.Success -> {
                    _uiState.value = UserUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = UserUiState.Error(
                        message = getErrorMessage(result.code),
                        code = result.code,
                        isRetryable = isRetryableError(result.code)
                    )
                }
                is NetworkResult.Exception -> {
                    _uiState.value = UserUiState.Error(
                        message = getExceptionMessage(result.throwable),
                        isRetryable = result.errorType == ErrorType.NETWORK
                    )
                }
            }
        }
    }
    
    private fun getErrorMessage(code: Int): String = when (code) {
        401 -> "Session expired. Please login again."
        403 -> "You don't have permission to access this resource."
        404 -> "The requested resource was not found."
        422 -> "Invalid data provided."
        429 -> "Too many requests. Please try again later."
        in 500..599 -> "Server error. Please try again later."
        else -> "An error occurred. Please try again."
    }
    
    private fun getExceptionMessage(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> "Request timed out. Check your connection."
        is UnknownHostException -> "No internet connection."
        is NoConnectivityException -> "No internet connection."
        is SSLException -> "Security error. Please update the app."
        else -> "An unexpected error occurred."
    }
    
    private fun isRetryableError(code: Int): Boolean = code in listOf(408, 429, 500, 502, 503, 504)
}

sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    data class Error(
        val message: String,
        val code: Int? = null,
        val isRetryable: Boolean = false
    ) : UserUiState()
}

// ═══════════════════════════════════════════════════════════════════════════
// FLOW-BASED API CALL (WITH LOADING STATE)
// ═══════════════════════════════════════════════════════════════════════════

fun <T> networkFlow(
    apiCall: suspend () -> Response<T>
): Flow<NetworkResult<T>> = flow {
    emit(safeApiCall(apiCall))
}.onStart {
    // Could emit loading state here if using a wrapper
}.catch { e ->
    emit(NetworkResult.Exception(e, ErrorType.UNKNOWN))
}.flowOn(Dispatchers.IO)

// Usage
viewModelScope.launch {
    networkFlow { apiService.getUsers() }
        .collect { result ->
            handleResult(result)
        }
}

### Multipart File Uploads (Detailed)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         MULTIPART REQUEST STRUCTURE                           │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  POST /upload HTTP/1.1                                                       │
│  Content-Type: multipart/form-data; boundary=----WebKitFormBoundary         │
│                                                                              │
│  ------WebKitFormBoundary                                                    │
│  Content-Disposition: form-data; name="file"; filename="photo.jpg"           │
│  Content-Type: image/jpeg                                                    │
│                                                                              │
│  [Binary file data]                                                          │
│                                                                              │
│  ------WebKitFormBoundary                                                    │
│  Content-Disposition: form-data; name="description"                          │
│  Content-Type: text/plain                                                    │
│                                                                              │
│  Profile picture upload                                                      │
│                                                                              │
│  ------WebKitFormBoundary--                                                  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
interface ApiService {
    
    // Single file upload
    @Multipart
    @POST("upload/avatar")
    suspend fun uploadAvatar(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): UploadResponse
    
    // Multiple files upload
    @Multipart
    @POST("upload/gallery")
    suspend fun uploadGallery(
        @Part images: List<MultipartBody.Part>,
        @Part("album_name") albumName: RequestBody
    ): UploadResponse
    
    // Mixed: files + JSON body
    @Multipart
    @POST("posts/create")
    suspend fun createPostWithMedia(
        @Part("post") post: RequestBody,  // JSON serialized
        @Part media: List<MultipartBody.Part>
    ): Post
    
    // Upload with progress tracking
    @Multipart
    @POST("upload/large")
    suspend fun uploadLargeFile(
        @Part file: MultipartBody.Part
    ): UploadResponse
}

// ═══════════════════════════════════════════════════════════════════════════
// FILE UPLOAD UTILITY CLASS
// ═══════════════════════════════════════════════════════════════════════════

object FileUploadHelper {
    
    /**
     * Create MultipartBody.Part from File
     */
    fun createFilePart(
        file: File,
        partName: String = "file",
        mimeType: String? = null
    ): MultipartBody.Part {
        val mediaType = (mimeType ?: getMimeType(file)).toMediaTypeOrNull()
        val requestBody = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }
    
    /**
     * Create MultipartBody.Part from Uri (Android content:// uri)
     */
    fun createFilePart(
        context: Context,
        uri: Uri,
        partName: String = "file"
    ): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val fileName = getFileName(context, uri) ?: "file"
        
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")
        
        val requestBody = object : RequestBody() {
            override fun contentType() = mimeType.toMediaTypeOrNull()
            
            override fun contentLength(): Long {
                return try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use {
                        it.statSize
                    } ?: -1
                } catch (e: Exception) {
                    -1
                }
            }
            
            override fun writeTo(sink: BufferedSink) {
                inputStream.use { input ->
                    sink.writeAll(input.source().buffer())
                }
            }
        }
        
        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }
    
    /**
     * Create request body for text fields
     */
    fun createTextPart(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }
    
    /**
     * Create request body for JSON data
     */
    inline fun <reified T> createJsonPart(data: T): RequestBody {
        val json = Json.encodeToString(data)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }
    
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "application/octet-stream"
        }
    }
    
    private fun getFileName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// UPLOAD WITH PROGRESS TRACKING
// ═══════════════════════════════════════════════════════════════════════════

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val onProgress: (Float) -> Unit
) : RequestBody() {
    
    override fun contentType() = contentType
    
    override fun contentLength() = file.length()
    
    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0
        
        file.inputStream().use { inputStream ->
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                onProgress(uploaded.toFloat() / fileLength)
            }
        }
    }
    
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 4096
    }
}

// Usage in Repository
class FileRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun uploadWithProgress(
        file: File,
        onProgress: (Float) -> Unit
    ): NetworkResult<UploadResponse> {
        val progressBody = ProgressRequestBody(
            file = file,
            contentType = "image/*".toMediaTypeOrNull(),
            onProgress = onProgress
        )
        
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            progressBody
        )
        
        return safeApiCall { apiService.uploadLargeFile(part) }
    }
}

// Usage in ViewModel
fun uploadFile(file: File) {
    viewModelScope.launch {
        _uploadProgress.value = 0f
        
        val result = repository.uploadWithProgress(file) { progress ->
            _uploadProgress.value = progress
        }
        
        when (result) {
            is NetworkResult.Success -> {
                _uploadState.value = UploadState.Success(result.data.fileUrl)
            }
            is NetworkResult.Error -> {
                _uploadState.value = UploadState.Error(result.message)
            }
            is NetworkResult.Exception -> {
                _uploadState.value = UploadState.Error("Upload failed")
            }
        }
    }
}
```

### Complete Retrofit Setup with Dependency Injection

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        headersInterceptor: HeadersInterceptor,
        connectivityInterceptor: ConnectivityInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(connectivityInterceptor)
            .addInterceptor(headersInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

---

## 6.2 OkHttp

### What is OkHttp?

OkHttp is a **high-performance HTTP client** developed by Square that serves as the foundation for Retrofit. It handles the low-level details of HTTP communication including connection management, caching, compression, and security.

### Why Use OkHttp?

1. **Connection Pooling**: Reuses connections to reduce latency
2. **Transparent GZIP**: Automatically compresses requests
3. **Response Caching**: Avoids redundant network requests
4. **HTTP/2 Support**: Multiplexing for better performance
5. **Silent Recovery**: Handles common connection problems automatically
6. **Modern TLS**: Certificate pinning and modern cipher suites

### OkHttp Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            OKHTTP ARCHITECTURE                                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                              APPLICATION                               │  │
│  │                                  │                                     │  │
│  │                         OkHttpClient.newCall(request)                  │  │
│  └──────────────────────────────────┼─────────────────────────────────────┘  │
│                                     ▼                                        │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                         INTERCEPTOR CHAIN                              │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  APPLICATION INTERCEPTORS (addInterceptor)                       │  │  │
│  │  │  • Auth headers        • Logging        • Custom headers         │  │  │
│  │  └──────────────────────────────────┬───────────────────────────────┘  │  │
│  │                                     ▼                                  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  RETRY AND FOLLOW UP INTERCEPTOR                                 │  │  │
│  │  │  • Retry failed requests    • Follow redirects                   │  │  │
│  │  └──────────────────────────────────┬───────────────────────────────┘  │  │
│  │                                     ▼                                  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  BRIDGE INTERCEPTOR                                              │  │  │
│  │  │  • Add headers (User-Agent, Host, Accept-Encoding)               │  │  │
│  │  │  • Handle cookies        • Decompress gzip                       │  │  │
│  │  └──────────────────────────────────┬───────────────────────────────┘  │  │
│  │                                     ▼                                  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  CACHE INTERCEPTOR                                               │  │  │
│  │  │  • Check cache         • Store responses    • Validate freshness │  │  │
│  │  └──────────────────────────────────┬───────────────────────────────┘  │  │
│  │                                     ▼                                  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  CONNECT INTERCEPTOR                                             │  │  │
│  │  │  • Connection pool     • TLS handshake      • Proxy handling     │  │  │
│  │  └──────────────────────────────────┬───────────────────────────────┘  │  │
│  │                                     ▼                                  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  NETWORK INTERCEPTORS (addNetworkInterceptor)                    │  │  │
│  │  │  • Cache control       • Compression        • Metrics            │  │  │
│  │  └──────────────────────────────────┬───────────────────────────────┘  │  │
│  │                                     ▼                                  │  │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │  │
│  │  │  CALL SERVER INTERCEPTOR                                         │  │  │
│  │  │  • Write request       • Read response      • Actual I/O         │  │  │
│  │  └──────────────────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                     │                                        │
│                                     ▼                                        │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                              NETWORK                                   │  │
│  │  ┌────────────────┐  ┌────────────────┐  ┌────────────────────────┐   │  │
│  │  │ Connection Pool│  │     DNS        │  │    TLS/SSL             │   │  │
│  │  │ Keep-alive     │  │   Resolution   │  │    Handshake           │   │  │
│  │  └────────────────┘  └────────────────┘  └────────────────────────┘   │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Client Configuration (Comprehensive)

```kotlin
val okHttpClient = OkHttpClient.Builder()
    // ═══════════════════════════════════════════════════════════════════
    // TIMEOUTS
    // ═══════════════════════════════════════════════════════════════════
    .connectTimeout(30, TimeUnit.SECONDS)    // Time to establish connection
    .readTimeout(30, TimeUnit.SECONDS)       // Time between bytes read
    .writeTimeout(30, TimeUnit.SECONDS)      // Time between bytes written
    .callTimeout(2, TimeUnit.MINUTES)        // Entire call timeout
    
    // ═══════════════════════════════════════════════════════════════════
    // CONNECTION SETTINGS
    // ═══════════════════════════════════════════════════════════════════
    .retryOnConnectionFailure(true)          // Retry failed connections
    .followRedirects(true)                   // Follow HTTP redirects
    .followSslRedirects(true)                // Follow HTTPS → HTTP redirects
    
    // ═══════════════════════════════════════════════════════════════════
    // INTERCEPTORS
    // ═══════════════════════════════════════════════════════════════════
    .addInterceptor(loggingInterceptor)      // Application interceptor
    .addInterceptor(authInterceptor)
    .addNetworkInterceptor(cacheInterceptor) // Network interceptor
    
    // ═══════════════════════════════════════════════════════════════════
    // SECURITY
    // ═══════════════════════════════════════════════════════════════════
    .certificatePinner(certificatePinner)
    .authenticator(tokenAuthenticator)
    
    // ═══════════════════════════════════════════════════════════════════
    // CACHING
    // ═══════════════════════════════════════════════════════════════════
    .cache(Cache(cacheDirectory, cacheSize))
    
    // ═══════════════════════════════════════════════════════════════════
    // CONNECTION POOL
    // ═══════════════════════════════════════════════════════════════════
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    
    // ═══════════════════════════════════════════════════════════════════
    // DISPATCHER (for async calls)
    // ═══════════════════════════════════════════════════════════════════
    .dispatcher(Dispatcher().apply {
        maxRequests = 64                     // Max concurrent requests
        maxRequestsPerHost = 5               // Max requests to same host
    })
    
    .build()
```

### Interceptor Types Deep Dive

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                 APPLICATION vs NETWORK INTERCEPTORS                           │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────┬───────────────────────────────────┐  │
│  │      APPLICATION INTERCEPTORS      │       NETWORK INTERCEPTORS        │  │
│  │       (.addInterceptor())          │    (.addNetworkInterceptor())     │  │
│  ├────────────────────────────────────┼───────────────────────────────────┤  │
│  │                                    │                                   │  │
│  │  • Called ONCE per request         │  • Called for EACH network req   │  │
│  │  • Even if served from cache       │  • Not called for cached resp    │  │
│  │                                    │                                   │  │
│  │  • See original request            │  • See final/modified request   │  │
│  │  • Before retry/redirect           │  • After redirects resolved      │  │
│  │                                    │                                   │  │
│  │  • Cannot access Connection        │  • Can access connection info    │  │
│  │  • Cannot modify cache headers     │  • CAN modify cache headers      │  │
│  │                                    │                                   │  │
│  │  USE FOR:                          │  USE FOR:                        │  │
│  │  ├── Authentication headers        │  ├── Cache control               │  │
│  │  ├── Logging (app level)           │  ├── Compression                 │  │
│  │  ├── Retry logic                   │  ├── Network logging             │  │
│  │  └── Request modification          │  └── Response modification       │  │
│  │                                    │                                   │  │
│  └────────────────────────────────────┴───────────────────────────────────┘  │
│                                                                              │
│  EXECUTION ORDER:                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                                                                      │   │
│  │   Request ──▶ App Int 1 ──▶ App Int 2 ──▶ [Cache] ──▶ Net Int 1     │   │
│  │                                                              │       │   │
│  │                                                              ▼       │   │
│  │   Response ◀── App Int 1 ◀── App Int 2 ◀── [Cache] ◀── Net Int 1    │   │
│  │                                                              │       │   │
│  │                                                          Network     │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Detailed Interceptor Examples

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// COMPREHENSIVE LOGGING INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

class DetailedLoggingInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()
        
        // Log request
        Log.d("HTTP", "──────────────── REQUEST ────────────────")
        Log.d("HTTP", "${request.method} ${request.url}")
        request.headers.forEach { (name, value) ->
            val displayValue = if (name.equals("Authorization", true)) "***" else value
            Log.d("HTTP", "$name: $displayValue")
        }
        request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            Log.d("HTTP", "Body: ${buffer.readUtf8()}")
        }
        
        // Execute
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.e("HTTP", "Request failed: ${e.message}")
            throw e
        }
        
        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        
        // Log response
        Log.d("HTTP", "──────────────── RESPONSE ────────────────")
        Log.d("HTTP", "${response.code} ${response.message} (${duration}ms)")
        response.headers.forEach { (name, value) ->
            Log.d("HTTP", "$name: $value")
        }
        
        // Clone body for logging (body can only be read once)
        val responseBody = response.body
        val bodyString = responseBody?.source()?.let { source ->
            source.request(Long.MAX_VALUE)
            source.buffer.clone().readUtf8()
        }
        
        bodyString?.let {
            if (it.length < 10000) {
                Log.d("HTTP", "Body: $it")
            } else {
                Log.d("HTTP", "Body: [${it.length} chars - truncated]")
            }
        }
        Log.d("HTTP", "────────────────────────────────────────────")
        
        return response
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// METRICS INTERCEPTOR
// ═══════════════════════════════════════════════════════════════════════════

class MetricsInterceptor(
    private val analytics: Analytics
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            analytics.trackNetworkError(
                url = request.url.toString(),
                method = request.method,
                error = e.javaClass.simpleName,
                message = e.message
            )
            throw e
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        analytics.trackApiCall(
            url = request.url.encodedPath,
            method = request.method,
            statusCode = response.code,
            durationMs = duration,
            requestSize = request.body?.contentLength() ?: 0,
            responseSize = response.body?.contentLength() ?: 0
        )
        
        return response
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CHUCKED RESPONSE INTERCEPTOR (for debugging)
// ═══════════════════════════════════════════════════════════════════════════

class MockResponseInterceptor(
    private val mockResponses: Map<String, String>
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        
        mockResponses[path]?.let { mockJson ->
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(mockJson.toResponseBody("application/json".toMediaType()))
                .build()
        }
        
        return chain.proceed(request)
    }
}
```

### Caching (Comprehensive)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              HTTP CACHING                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                         CACHE-CONTROL DIRECTIVES                       │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │                                                                        │  │
│  │  REQUEST DIRECTIVES:                                                   │  │
│  │  ├── no-cache        → Validate with server before using cached       │  │
│  │  ├── no-store        → Don't cache at all                             │  │
│  │  ├── max-age=N       → Accept cached if not older than N seconds      │  │
│  │  ├── max-stale=N     → Accept stale cache up to N seconds             │  │
│  │  ├── min-fresh=N     → Cache must be fresh for at least N seconds     │  │
│  │  └── only-if-cached  → Only return cached response, fail otherwise    │  │
│  │                                                                        │  │
│  │  RESPONSE DIRECTIVES:                                                  │  │
│  │  ├── public          → Response can be cached by any cache            │  │
│  │  ├── private         → Response only for single user                  │  │
│  │  ├── no-cache        → Must revalidate before reuse                   │  │
│  │  ├── no-store        → Don't store response                           │  │
│  │  ├── max-age=N       → Response is fresh for N seconds                │  │
│  │  └── must-revalidate → Must check freshness after expiry              │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  CACHE DECISION FLOW:                                                        │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │   Request ──▶ Cache exists? ──No──▶ Network ──▶ Store in cache        │  │
│  │                    │                                                   │  │
│  │                   Yes                                                  │  │
│  │                    │                                                   │  │
│  │                    ▼                                                   │  │
│  │             Is it fresh? ──Yes──▶ Return cached response              │  │
│  │                    │                                                   │  │
│  │                   No                                                   │  │
│  │                    │                                                   │  │
│  │                    ▼                                                   │  │
│  │        Validate with server (If-None-Match / If-Modified-Since)       │  │
│  │                    │                                                   │  │
│  │              ┌─────┴─────┐                                            │  │
│  │              ▼           ▼                                            │  │
│  │         304 Not     200 OK                                            │  │
│  │         Modified    (new data)                                         │  │
│  │              │           │                                            │  │
│  │              ▼           ▼                                            │  │
│  │      Return cached   Store & return                                   │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// CACHE SETUP
// ═══════════════════════════════════════════════════════════════════════════

val cacheSize = 50L * 1024 * 1024  // 50 MB
val cacheDirectory = File(context.cacheDir, "http_cache")
val cache = Cache(cacheDirectory, cacheSize)

// ═══════════════════════════════════════════════════════════════════════════
// ONLINE CACHE INTERCEPTOR (adds cache headers to responses)
// ═══════════════════════════════════════════════════════════════════════════

class OnlineCacheInterceptor(
    private val maxAgeSeconds: Int = 60
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        val cacheControl = CacheControl.Builder()
            .maxAge(maxAgeSeconds, TimeUnit.SECONDS)
            .build()
        
        return response.newBuilder()
            .removeHeader("Pragma")  // Remove no-cache pragma
            .removeHeader("Cache-Control")
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// OFFLINE CACHE INTERCEPTOR (use cache when offline)
// ═══════════════════════════════════════════════════════════════════════════

class OfflineCacheInterceptor(
    private val context: Context,
    private val maxStaleSeconds: Int = 7 * 24 * 60 * 60  // 1 week
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        if (!isNetworkAvailable()) {
            val cacheControl = CacheControl.Builder()
                .maxStale(maxStaleSeconds, TimeUnit.SECONDS)
                .onlyIfCached()
                .build()
            
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }
        
        return chain.proceed(request)
    }
    
    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService<ConnectivityManager>()
        return cm?.activeNetwork != null
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// FORCE CACHE OR NETWORK
// ═══════════════════════════════════════════════════════════════════════════

// Force network (skip cache)
val forceNetworkRequest = originalRequest.newBuilder()
    .cacheControl(CacheControl.FORCE_NETWORK)
    .build()

// Force cache only
val forceCacheRequest = originalRequest.newBuilder()
    .cacheControl(CacheControl.FORCE_CACHE)
    .build()

// Custom cache control
val customCacheControl = CacheControl.Builder()
    .maxAge(5, TimeUnit.MINUTES)
    .maxStale(1, TimeUnit.HOURS)
    .build()

val customRequest = originalRequest.newBuilder()
    .cacheControl(customCacheControl)
    .build()

// ═══════════════════════════════════════════════════════════════════════════
// COMPLETE CLIENT WITH CACHING
// ═══════════════════════════════════════════════════════════════════════════

val client = OkHttpClient.Builder()
    .cache(cache)
    .addInterceptor(OfflineCacheInterceptor(context))
    .addNetworkInterceptor(OnlineCacheInterceptor(maxAgeSeconds = 300))
    .build()
```

### Connection Pooling

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           CONNECTION POOLING                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  WITHOUT CONNECTION POOL:                                                    │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  Request 1: DNS → TCP → TLS → HTTP ────────────────────── Close       │  │
│  │  Request 2: DNS → TCP → TLS → HTTP ────────────────────── Close       │  │
│  │  Request 3: DNS → TCP → TLS → HTTP ────────────────────── Close       │  │
│  │                                                                        │  │
│  │  Each request: ~300-500ms overhead                                     │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  WITH CONNECTION POOL:                                                       │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  Request 1: DNS → TCP → TLS → HTTP ─────────┐                          │  │
│  │                                              │  Keep-alive             │  │
│  │  Request 2: ──────────────────── HTTP ──────┤                          │  │
│  │                                              │                          │  │
│  │  Request 3: ──────────────────── HTTP ──────┘                          │  │
│  │                                                                        │  │
│  │  Subsequent requests: ~10-50ms                                         │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
val connectionPool = ConnectionPool(
    maxIdleConnections = 5,      // Keep up to 5 idle connections
    keepAliveDuration = 5,       // For 5 minutes
    timeUnit = TimeUnit.MINUTES
)

val client = OkHttpClient.Builder()
    .connectionPool(connectionPool)
    .build()

// Access pool stats
val idleConnections = connectionPool.idleConnectionCount()
val totalConnections = connectionPool.connectionCount()

// Evict all connections (useful on logout)
connectionPool.evictAll()
```

### Certificate Pinning (Security)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          CERTIFICATE PINNING                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  WHAT IS IT?                                                                 │
│  Certificate pinning ensures your app only trusts specific certificates,     │
│  preventing man-in-the-middle attacks even if a CA is compromised.          │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  WITHOUT PINNING:                                                      │  │
│  │  App ──▶ Any valid certificate signed by trusted CA ✓                 │  │
│  │  App ──▶ Attacker's certificate (signed by compromised CA) ✓ DANGER!  │  │
│  │                                                                        │  │
│  │  WITH PINNING:                                                         │  │
│  │  App ──▶ Your server's certificate ✓                                  │  │
│  │  App ──▶ Attacker's certificate ✗ Rejected!                           │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  PIN TYPES:                                                                  │
│  1. Certificate Pin: Pin entire certificate (must update when cert renews) │
│  2. Public Key Pin: Pin public key (survives cert renewal)                  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// BASIC CERTIFICATE PINNING
// ═══════════════════════════════════════════════════════════════════════════

val certificatePinner = CertificatePinner.Builder()
    // Pin for specific domain
    .add(
        "api.example.com",
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="  // Leaf cert
    )
    // Pin for wildcard domain with backup pin
    .add(
        "*.example.com",
        "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=", // Primary
        "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="  // Backup
    )
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()

// ═══════════════════════════════════════════════════════════════════════════
// GET CERTIFICATE PINS (for debugging)
// ═══════════════════════════════════════════════════════════════════════════

// Run this to get the SHA256 pins for your server
fun getCertificatePins(hostname: String) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://$hostname")
        .build()
    
    try {
        client.newCall(request).execute().use { response ->
            val handshake = response.handshake
            handshake?.peerCertificates?.forEach { cert ->
                val pin = CertificatePinner.pin(cert)
                Log.d("CertPin", "$hostname: $pin")
            }
        }
    } catch (e: Exception) {
        Log.e("CertPin", "Error: ${e.message}")
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// RECOMMENDED: PIN INTERMEDIATE CERTIFICATE
// ═══════════════════════════════════════════════════════════════════════════

// Pinning the intermediate CA certificate is more flexible
// It survives leaf certificate renewals while still providing security
val certificatePinner = CertificatePinner.Builder()
    .add("api.example.com", 
         "sha256/IntermediateCAPublicKeyHash=")
    .build()
```

### Custom DNS

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// DNS OVER HTTPS (DoH)
// ═══════════════════════════════════════════════════════════════════════════

class DnsOverHttps(
    private val bootstrapClient: OkHttpClient,
    private val dohUrl: HttpUrl
) : Dns {
    
    override fun lookup(hostname: String): List<InetAddress> {
        val request = Request.Builder()
            .url(dohUrl.newBuilder()
                .addQueryParameter("name", hostname)
                .addQueryParameter("type", "A")
                .build())
            .header("Accept", "application/dns-json")
            .build()
        
        val response = bootstrapClient.newCall(request).execute()
        val json = response.body?.string() ?: throw UnknownHostException(hostname)
        
        // Parse DNS response and return addresses
        return parseDnsResponse(json, hostname)
    }
    
    private fun parseDnsResponse(json: String, hostname: String): List<InetAddress> {
        // Parse JSON response from DoH provider
        // Return list of InetAddress
        // Implementation depends on DoH provider format
        return emptyList()
    }
}

// Cloudflare DoH
val dohUrl = "https://cloudflare-dns.com/dns-query".toHttpUrl()

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM DNS RESOLVER
// ═══════════════════════════════════════════════════════════════════════════

class CustomDns : Dns {
    
    private val customMappings = mapOf(
        "api.example.com" to listOf("192.168.1.100", "192.168.1.101"),
        "cdn.example.com" to listOf("192.168.2.100")
    )
    
    override fun lookup(hostname: String): List<InetAddress> {
        // Check custom mappings first
        customMappings[hostname]?.let { addresses ->
            return addresses.map { InetAddress.getByName(it) }
        }
        
        // Fall back to system DNS
        return Dns.SYSTEM.lookup(hostname)
    }
}

val client = OkHttpClient.Builder()
    .dns(CustomDns())
    .build()
```

### Custom Socket Factory (Proxy/VPN)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// SOCKS PROXY
// ═══════════════════════════════════════════════════════════════════════════

val proxy = Proxy(
    Proxy.Type.SOCKS,
    InetSocketAddress("proxy.example.com", 1080)
)

val client = OkHttpClient.Builder()
    .proxy(proxy)
    .proxyAuthenticator { _, response ->
        val credential = Credentials.basic("username", "password")
        response.request.newBuilder()
            .header("Proxy-Authorization", credential)
            .build()
    }
    .build()

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM SOCKET FACTORY (for VPN apps)
// ═══════════════════════════════════════════════════════════════════════════

class ProtectedSocketFactory(
    private val vpnService: VpnService
) : SocketFactory() {
    
    override fun createSocket(): Socket {
        val socket = Socket()
        vpnService.protect(socket)  // Bypass VPN for this socket
        return socket
    }
    
    override fun createSocket(host: String, port: Int): Socket {
        val socket = Socket(host, port)
        vpnService.protect(socket)
        return socket
    }
    
    // ... other createSocket overloads
}

val client = OkHttpClient.Builder()
    .socketFactory(ProtectedSocketFactory(vpnService))
    .build()
```

---

## 6.3 GraphQL with Apollo

### What is GraphQL?

GraphQL is a **query language for APIs** and a runtime for executing those queries. Unlike REST, GraphQL allows clients to request exactly the data they need, nothing more, nothing less.

### Why GraphQL over REST?

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         REST vs GRAPHQL                                       │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  PROBLEM 1: OVER-FETCHING (REST returns too much data)                       │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  REST: GET /users/1                                                    │  │
│  │  Response: {                                                           │  │
│  │    "id": 1,                                                            │  │
│  │    "name": "John",        ← You only need this                        │  │
│  │    "email": "...",        ← Unnecessary                               │  │
│  │    "address": {...},      ← Unnecessary (wastes bandwidth)            │  │
│  │    "preferences": {...},  ← Unnecessary                               │  │
│  │    "history": [...]       ← Unnecessary                               │  │
│  │  }                                                                     │  │
│  │                                                                        │  │
│  │  GRAPHQL:                                                              │  │
│  │  query { user(id: 1) { name } }                                        │  │
│  │  Response: { "user": { "name": "John" } }  ← Exactly what you need    │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  PROBLEM 2: UNDER-FETCHING (REST requires multiple requests)                 │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  REST:                                                                 │  │
│  │  1. GET /users/1           → Get user                                 │  │
│  │  2. GET /users/1/posts     → Get user's posts                         │  │
│  │  3. GET /posts/5/comments  → Get comments for each post               │  │
│  │  (3+ round trips)                                                      │  │
│  │                                                                        │  │
│  │  GRAPHQL (1 request):                                                  │  │
│  │  query {                                                               │  │
│  │    user(id: 1) {                                                       │  │
│  │      name                                                              │  │
│  │      posts { title, comments { text } }                                │  │
│  │    }                                                                   │  │
│  │  }                                                                     │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  COMPARISON TABLE:                                                           │
│  ┌───────────────────────┬──────────────────┬───────────────────────────┐   │
│  │       FEATURE         │       REST       │        GRAPHQL            │   │
│  ├───────────────────────┼──────────────────┼───────────────────────────┤   │
│  │ Endpoints             │ Multiple         │ Single (/graphql)         │   │
│  │ Data fetching         │ Server decides   │ Client decides            │   │
│  │ Versioning            │ /v1, /v2         │ Schema evolution          │   │
│  │ Documentation         │ OpenAPI/Swagger  │ Introspection (built-in)  │   │
│  │ Caching               │ HTTP caching     │ Normalized cache          │   │
│  │ File uploads          │ Native           │ Needs multipart spec      │   │
│  │ Learning curve        │ Lower            │ Higher                    │   │
│  │ Tooling               │ Mature           │ Growing rapidly           │   │
│  └───────────────────────┴──────────────────┴───────────────────────────┘   │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Apollo Android Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        APOLLO ANDROID ARCHITECTURE                            │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  BUILD TIME:                                                                 │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │   schema.graphqls  +  *.graphql files  ──▶  Apollo Gradle Plugin      │  │
│  │        │                    │                       │                  │  │
│  │        │                    │                       ▼                  │  │
│  │        │                    │              Generated Kotlin Models     │  │
│  │        │                    │              ├── GetUsersQuery           │  │
│  │        │                    │              ├── CreateUserMutation      │  │
│  │        │                    │              ├── User (data class)       │  │
│  │        │                    │              └── Input types             │  │
│  │        │                    │                                          │  │
│  └────────┼────────────────────┼──────────────────────────────────────────┘  │
│           │                    │                                             │
│  RUNTIME: │                    │                                             │
│  ┌────────▼────────────────────▼──────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │   ┌─────────────────┐     ┌─────────────────┐     ┌────────────────┐  │  │
│  │   │  ApolloClient   │────▶│  Normalized     │────▶│    OkHttp      │  │  │
│  │   │                 │     │  Cache          │     │    Client      │  │  │
│  │   └─────────────────┘     └─────────────────┘     └────────────────┘  │  │
│  │           │                       │                       │            │  │
│  │           ▼                       ▼                       ▼            │  │
│  │   ┌─────────────────┐     ┌─────────────────┐     ┌────────────────┐  │  │
│  │   │   Interceptors  │     │  Memory Cache   │     │    GraphQL     │  │  │
│  │   │   (Auth, etc)   │     │  + SQL Cache    │     │    Server      │  │  │
│  │   └─────────────────┘     └─────────────────┘     └────────────────┘  │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Setup

```kotlin
// build.gradle.kts (Project level)
plugins {
    id("com.apollographql.apollo3") version "3.8.2" apply false
}

// build.gradle.kts (App level)
plugins {
    id("com.apollographql.apollo3")
}

dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
    implementation("com.apollographql.apollo3:apollo-normalized-cache:3.8.2")
    implementation("com.apollographql.apollo3:apollo-normalized-cache-sqlite:3.8.2")
}

apollo {
    service("service") {
        packageName.set("com.example.graphql")
        
        // Generate Kotlin models
        generateKotlinModels.set(true)
        
        // Schema location
        schemaFile.set(file("src/main/graphql/schema.graphqls"))
        
        // Custom scalar mappings
        mapScalar("DateTime", "java.time.Instant", "com.example.DateTimeAdapter")
        mapScalar("JSON", "kotlinx.serialization.json.JsonObject")
        
        // Generate operation document hashes (for APQ)
        generateOperationOutput.set(true)
    }
}
```

### Schema Definition

```graphql
# schema.graphqls

# Scalars
scalar DateTime
scalar JSON

# Enums
enum UserRole {
    ADMIN
    USER
    MODERATOR
}

enum PostStatus {
    DRAFT
    PUBLISHED
    ARCHIVED
}

# Types
type Query {
    """Get all users with optional filtering"""
    users(
        first: Int = 10
        after: String
        filter: UserFilter
    ): UserConnection!
    
    """Get a single user by ID"""
    user(id: ID!): User
    
    """Search users by name or email"""
    searchUsers(query: String!, limit: Int = 10): [User!]!
    
    """Get current authenticated user"""
    me: User
    
    """Get posts with pagination"""
    posts(
        first: Int
        after: String
        status: PostStatus
    ): PostConnection!
}

type Mutation {
    """Create a new user"""
    createUser(input: CreateUserInput!): CreateUserPayload!
    
    """Update an existing user"""
    updateUser(id: ID!, input: UpdateUserInput!): UpdateUserPayload!
    
    """Delete a user"""
    deleteUser(id: ID!): DeleteUserPayload!
    
    """Create a post"""
    createPost(input: CreatePostInput!): CreatePostPayload!
    
    """Like a post"""
    likePost(id: ID!): LikePostPayload!
}

type Subscription {
    """Subscribe to new posts"""
    postCreated: Post!
    
    """Subscribe to messages in a channel"""
    messageReceived(channelId: ID!): Message!
    
    """Subscribe to user online status"""
    userStatusChanged: UserStatus!
}

# User types
type User {
    id: ID!
    name: String!
    email: String!
    avatar: String
    role: UserRole!
    posts(first: Int, after: String): PostConnection!
    followers: [User!]!
    following: [User!]!
    createdAt: DateTime!
    updatedAt: DateTime
}

type UserConnection {
    edges: [UserEdge!]!
    pageInfo: PageInfo!
    totalCount: Int!
}

type UserEdge {
    node: User!
    cursor: String!
}

# Post types
type Post {
    id: ID!
    title: String!
    content: String!
    status: PostStatus!
    author: User!
    comments(first: Int, after: String): CommentConnection!
    likes: Int!
    likedByMe: Boolean!
    tags: [String!]!
    createdAt: DateTime!
}

type PostConnection {
    edges: [PostEdge!]!
    pageInfo: PageInfo!
}

type PostEdge {
    node: Post!
    cursor: String!
}

# Comment type
type Comment {
    id: ID!
    text: String!
    author: User!
    post: Post!
    createdAt: DateTime!
}

type CommentConnection {
    edges: [CommentEdge!]!
    pageInfo: PageInfo!
}

type CommentEdge {
    node: Comment!
    cursor: String!
}

# Pagination
type PageInfo {
    hasNextPage: Boolean!
    hasPreviousPage: Boolean!
    startCursor: String
    endCursor: String
}

# Inputs
input UserFilter {
    role: UserRole
    searchQuery: String
}

input CreateUserInput {
    name: String!
    email: String!
    password: String!
    role: UserRole = USER
}

input UpdateUserInput {
    name: String
    email: String
    avatar: String
}

input CreatePostInput {
    title: String!
    content: String!
    tags: [String!]
    status: PostStatus = DRAFT
}

# Payloads (for mutations)
type CreateUserPayload {
    user: User
    errors: [Error!]
}

type UpdateUserPayload {
    user: User
    errors: [Error!]
}

type DeleteUserPayload {
    success: Boolean!
    errors: [Error!]
}

type CreatePostPayload {
    post: Post
    errors: [Error!]
}

type LikePostPayload {
    post: Post
    errors: [Error!]
}

type Error {
    field: String
    message: String!
    code: String!
}

# Other types
type Message {
    id: ID!
    text: String!
    sender: User!
    channelId: ID!
    createdAt: DateTime!
}

type UserStatus {
    user: User!
    isOnline: Boolean!
    lastSeen: DateTime
}
```

### GraphQL Operations

```graphql
# src/main/graphql/queries/GetUsers.graphql
query GetUsers($first: Int!, $after: String, $filter: UserFilter) {
    users(first: $first, after: $after, filter: $filter) {
        edges {
            node {
                ...UserBasicInfo
            }
            cursor
        }
        pageInfo {
            hasNextPage
            endCursor
        }
        totalCount
    }
}

# src/main/graphql/queries/GetUser.graphql
query GetUser($id: ID!) {
    user(id: $id) {
        ...UserDetails
        posts(first: 10) {
            edges {
                node {
                    ...PostBasicInfo
                }
            }
        }
    }
}

# src/main/graphql/queries/GetMe.graphql
query GetMe {
    me {
        ...UserDetails
    }
}

# src/main/graphql/fragments/UserFragments.graphql
fragment UserBasicInfo on User {
    id
    name
    email
    avatar
    role
}

fragment UserDetails on User {
    ...UserBasicInfo
    followers {
        id
        name
    }
    following {
        id
        name
    }
    createdAt
    updatedAt
}

fragment PostBasicInfo on Post {
    id
    title
    content
    status
    likes
    likedByMe
    createdAt
    author {
        id
        name
        avatar
    }
}

# src/main/graphql/mutations/CreateUser.graphql
mutation CreateUser($input: CreateUserInput!) {
    createUser(input: $input) {
        user {
            ...UserDetails
        }
        errors {
            field
            message
            code
        }
    }
}

# src/main/graphql/mutations/UpdateUser.graphql
mutation UpdateUser($id: ID!, $input: UpdateUserInput!) {
    updateUser(id: $id, input: $input) {
        user {
            ...UserDetails
        }
        errors {
            field
            message
            code
        }
    }
}

# src/main/graphql/mutations/CreatePost.graphql
mutation CreatePost($input: CreatePostInput!) {
    createPost(input: $input) {
        post {
            ...PostBasicInfo
        }
        errors {
            field
            message
            code
        }
    }
}

# src/main/graphql/subscriptions/PostCreated.graphql
subscription OnPostCreated {
    postCreated {
        ...PostBasicInfo
    }
}

# src/main/graphql/subscriptions/MessageReceived.graphql
subscription OnMessageReceived($channelId: ID!) {
    messageReceived(channelId: $channelId) {
        id
        text
        sender {
            id
            name
            avatar
        }
        createdAt
    }
}
```

### Apollo Client Setup

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {
    
    @Provides
    @Singleton
    fun provideApolloClient(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        tokenManager: TokenManager
    ): ApolloClient {
        // Memory cache
        val memoryCache = MemoryCacheFactory(
            maxSizeBytes = 10 * 1024 * 1024  // 10 MB
        )
        
        // SQL cache (persistent)
        val sqlCache = SqlNormalizedCacheFactory(
            context = context,
            name = "apollo_cache.db"
        )
        
        // Chained cache: memory first, then SQL
        val cacheFactory = memoryCache.chain(sqlCache)
        
        return ApolloClient.Builder()
            .serverUrl(BuildConfig.GRAPHQL_ENDPOINT)
            .okHttpClient(okHttpClient)
            .normalizedCache(cacheFactory)
            
            // Add auth header
            .addHttpHeader("Authorization", "Bearer ${tokenManager.getAccessToken()}")
            
            // Or use interceptor for dynamic token
            .addHttpInterceptor(AuthorizationInterceptor(tokenManager))
            
            // WebSocket for subscriptions
            .webSocketServerUrl(BuildConfig.GRAPHQL_WS_ENDPOINT)
            .webSocketReopenWhen { throwable, attempt ->
                delay(2.0.pow(attempt.toDouble()).toLong() * 1000)
                true  // Reconnect
            }
            
            // Auto-persist queries (APQ)
            .autoPersistedQueries()
            
            // Custom scalar adapters
            .addCustomScalarAdapter(
                DateTime.type,
                object : Adapter<Instant> {
                    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Instant {
                        return Instant.parse(reader.nextString())
                    }
                    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Instant) {
                        writer.value(value.toString())
                    }
                }
            )
            .build()
    }
}

class AuthorizationInterceptor(
    private val tokenManager: TokenManager
) : HttpInterceptor {
    
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        val token = tokenManager.getAccessToken()
        val newRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}
```

### Repository Pattern with Apollo

```kotlin
class UserRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    // ═══════════════════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun getUsers(
        first: Int = 20,
        after: String? = null,
        filter: UserFilter? = null
    ): ApolloResult<List<User>> {
        return try {
            val response = apolloClient
                .query(GetUsersQuery(first, Optional.presentIfNotNull(after), Optional.presentIfNotNull(filter)))
                .fetchPolicy(FetchPolicy.CacheFirst)
                .execute()
            
            if (response.hasErrors()) {
                ApolloResult.Error(response.errors?.firstOrNull()?.message ?: "Unknown error")
            } else {
                val users = response.data?.users?.edges?.map { edge ->
                    edge.node.toUser()
                } ?: emptyList()
                ApolloResult.Success(users)
            }
        } catch (e: ApolloException) {
            ApolloResult.Exception(e)
        }
    }
    
    fun getUsersFlow(first: Int = 20): Flow<ApolloResponse<GetUsersQuery.Data>> {
        return apolloClient
            .query(GetUsersQuery(first, Optional.absent(), Optional.absent()))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
    }
    
    suspend fun getUser(id: String): ApolloResult<UserDetails> {
        return try {
            val response = apolloClient
                .query(GetUserQuery(id))
                .fetchPolicy(FetchPolicy.CacheFirst)
                .execute()
            
            response.data?.user?.let { user ->
                ApolloResult.Success(user.toUserDetails())
            } ?: ApolloResult.Error("User not found")
        } catch (e: ApolloException) {
            ApolloResult.Exception(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // MUTATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun createUser(input: CreateUserInput): ApolloResult<User> {
        return try {
            val response = apolloClient
                .mutation(CreateUserMutation(input))
                .execute()
            
            val payload = response.data?.createUser
            
            when {
                response.hasErrors() -> {
                    ApolloResult.Error(response.errors?.firstOrNull()?.message ?: "Unknown error")
                }
                payload?.errors?.isNotEmpty() == true -> {
                    ApolloResult.ValidationError(
                        payload.errors.map { it.field to it.message }
                    )
                }
                payload?.user != null -> {
                    ApolloResult.Success(payload.user.toUser())
                }
                else -> {
                    ApolloResult.Error("Unknown error")
                }
            }
        } catch (e: ApolloException) {
            ApolloResult.Exception(e)
        }
    }
    
    suspend fun updateUser(id: String, input: UpdateUserInput): ApolloResult<User> {
        return try {
            val response = apolloClient
                .mutation(UpdateUserMutation(id, input))
                .execute()
            
            response.data?.updateUser?.user?.let { user ->
                ApolloResult.Success(user.toUser())
            } ?: ApolloResult.Error("Update failed")
        } catch (e: ApolloException) {
            ApolloResult.Exception(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // SUBSCRIPTIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    fun subscribeToNewPosts(): Flow<Post> {
        return apolloClient
            .subscription(OnPostCreatedSubscription())
            .toFlow()
            .mapNotNull { response ->
                response.data?.postCreated?.toPost()
            }
            .catch { e ->
                Log.e("UserRepository", "Subscription error: ${e.message}")
            }
    }
    
    fun subscribeToMessages(channelId: String): Flow<Message> {
        return apolloClient
            .subscription(OnMessageReceivedSubscription(channelId))
            .toFlow()
            .mapNotNull { it.data?.messageReceived?.toMessage() }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // CACHE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun clearCache() {
        apolloClient.apolloStore.clearAll()
    }
    
    suspend fun refreshUser(id: String): ApolloResult<UserDetails> {
        return try {
            val response = apolloClient
                .query(GetUserQuery(id))
                .fetchPolicy(FetchPolicy.NetworkOnly)  // Skip cache
                .execute()
            
            response.data?.user?.let { user ->
                ApolloResult.Success(user.toUserDetails())
            } ?: ApolloResult.Error("User not found")
        } catch (e: ApolloException) {
            ApolloResult.Exception(e)
        }
    }
}

// Result wrapper
sealed class ApolloResult<out T> {
    data class Success<T>(val data: T) : ApolloResult<T>()
    data class Error(val message: String) : ApolloResult<Nothing>()
    data class ValidationError(val errors: List<Pair<String?, String>>) : ApolloResult<Nothing>()
    data class Exception(val exception: ApolloException) : ApolloResult<Nothing>()
}

// Extension functions for mapping
private fun GetUsersQuery.Node.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    avatar = avatar,
    role = role.toUserRole()
)

private fun GetUserQuery.User.toUserDetails(): UserDetails = UserDetails(
    id = id,
    name = name,
    email = email,
    avatar = avatar,
    role = role.toUserRole(),
    followers = followers.map { it.toFollower() },
    following = following.map { it.toFollower() },
    posts = posts.edges.map { it.node.toPost() }
)
```

### Caching Strategies

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         APOLLO FETCH POLICIES                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  CacheFirst (default):                                                 │  │
│  │  ┌─────────┐    ┌─────────┐                                           │  │
│  │  │  Cache  │───▶│ Network │  (if cache miss)                          │  │
│  │  └─────────┘    └─────────┘                                           │  │
│  │  Best for: Mostly static data, offline-first                          │  │
│  │                                                                        │  │
│  │  NetworkFirst:                                                         │  │
│  │  ┌─────────┐    ┌─────────┐                                           │  │
│  │  │ Network │───▶│  Cache  │  (if network error)                       │  │
│  │  └─────────┘    └─────────┘                                           │  │
│  │  Best for: Frequently updated data                                    │  │
│  │                                                                        │  │
│  │  CacheOnly:                                                            │  │
│  │  ┌─────────┐                                                          │  │
│  │  │  Cache  │  (no network)                                            │  │
│  │  └─────────┘                                                          │  │
│  │  Best for: Offline mode, previously fetched data                      │  │
│  │                                                                        │  │
│  │  NetworkOnly:                                                          │  │
│  │  ┌─────────┐                                                          │  │
│  │  │ Network │  (no cache read, still writes to cache)                  │  │
│  │  └─────────┘                                                          │  │
│  │  Best for: Sensitive data, force refresh                              │  │
│  │                                                                        │  │
│  │  CacheAndNetwork:                                                      │  │
│  │  ┌─────────┐    ┌─────────┐                                           │  │
│  │  │  Cache  │    │ Network │  (emits both, cache first if available)   │  │
│  │  └────┬────┘    └────┬────┘                                           │  │
│  │       └──────┬───────┘                                                │  │
│  │              ▼                                                         │  │
│  │       Two emissions                                                    │  │
│  │  Best for: Show stale data immediately, then update                   │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// Different fetch policies usage
suspend fun getUserWithPolicy(id: String, policy: FetchPolicy): User? {
    val response = apolloClient
        .query(GetUserQuery(id))
        .fetchPolicy(policy)
        .execute()
    
    return response.data?.user?.toUser()
}

// CacheAndNetwork in Flow
fun watchUser(id: String): Flow<User?> {
    return apolloClient
        .query(GetUserQuery(id))
        .fetchPolicy(FetchPolicy.CacheAndNetwork)
        .toFlow()
        .map { it.data?.user?.toUser() }
}

// Cache watching (reactive updates)
fun watchUsers(): Flow<List<User>> {
    return apolloClient
        .query(GetUsersQuery(20, Optional.absent(), Optional.absent()))
        .watch()  // Emits when cache changes
        .map { response ->
            response.data?.users?.edges?.map { it.node.toUser() } ?: emptyList()
        }
}

// Manual cache updates after mutation
suspend fun createUserAndUpdateCache(input: CreateUserInput): User? {
    val response = apolloClient
        .mutation(CreateUserMutation(input))
        .execute()
    
    val newUser = response.data?.createUser?.user ?: return null
    
    // Update the users list cache manually
    apolloClient.apolloStore.writeOperation(
        operation = GetUsersQuery(20, Optional.absent(), Optional.absent()),
        operationData = GetUsersQuery.Data(
            users = GetUsersQuery.Users(
                edges = listOf(
                    GetUsersQuery.Edge(
                        node = GetUsersQuery.Node(
                            __typename = "User",
                            id = newUser.id,
                            name = newUser.name,
                            email = newUser.email,
                            avatar = newUser.avatar,
                            role = newUser.role
                        ),
                        cursor = newUser.id
                    )
                ) + /* existing edges */,
                pageInfo = /* existing pageInfo */,
                totalCount = /* updated count */
            )
        )
    )
    
    return newUser.toUser()
}
```

---

## 6.4 WebSocket

### What is WebSocket?

WebSocket is a communication protocol that provides **full-duplex (bidirectional) communication** channels over a single TCP connection. Unlike HTTP's request-response model, WebSocket allows the server to push data to the client at any time.

### When to Use WebSocket?

- **Real-time Chat**: Messages need instant delivery
- **Live Notifications**: Push updates without polling
- **Live Sports/Stocks**: Continuous data streams
- **Multiplayer Games**: Low-latency bidirectional communication
- **Collaborative Editing**: Real-time document sync
- **IoT Dashboards**: Live sensor data

### WebSocket vs Alternatives

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    REAL-TIME COMMUNICATION OPTIONS                            │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                          HTTP POLLING                                  │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │                                                                        │  │
│  │   Client ──Request──▶ Server                                          │  │
│  │   Client ◀──Response── Server                                         │  │
│  │   (wait 5 seconds)                                                     │  │
│  │   Client ──Request──▶ Server                                          │  │
│  │   Client ◀──Response── Server                                         │  │
│  │   ...repeat forever                                                    │  │
│  │                                                                        │  │
│  │   ✗ High latency (poll interval)                                      │  │
│  │   ✗ Wastes bandwidth (most polls return "no new data")                │  │
│  │   ✗ Server overhead (handling many connections)                       │  │
│  │   ✓ Simple to implement                                               │  │
│  │   ✓ Works through all proxies/firewalls                               │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                       LONG POLLING (Comet)                             │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │                                                                        │  │
│  │   Client ──Request──▶ Server                                          │  │
│  │   (server holds connection until data available)                       │  │
│  │   Client ◀──Response── Server (when data ready)                       │  │
│  │   Client ──Request──▶ Server (immediately reconnect)                  │  │
│  │   ...                                                                  │  │
│  │                                                                        │  │
│  │   ✓ Lower latency than polling                                        │  │
│  │   ✗ Still one-way (server → client)                                   │  │
│  │   ✗ Connection overhead for each message                              │  │
│  │   ✓ Works through proxies/firewalls                                   │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                     SERVER-SENT EVENTS (SSE)                           │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │                                                                        │  │
│  │   Client ──Request──▶ Server                                          │  │
│  │   Client ◀══Stream══▶ Server (one-way stream)                         │  │
│  │                                                                        │  │
│  │   ✓ Native browser support                                            │  │
│  │   ✓ Auto-reconnection                                                 │  │
│  │   ✗ One-way only (server → client)                                    │  │
│  │   ✗ Limited binary support                                            │  │
│  │   ✓ Works over HTTP/2                                                 │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                          WEBSOCKET                                     │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │                                                                        │  │
│  │   Client ──HTTP Upgrade──▶ Server                                     │  │
│  │   Client ◀═══════════════▶ Server (full-duplex)                       │  │
│  │                                                                        │  │
│  │   ✓ True bidirectional communication                                  │  │
│  │   ✓ Low latency                                                       │  │
│  │   ✓ Low overhead (no HTTP headers per message)                        │  │
│  │   ✓ Binary and text support                                           │  │
│  │   ✗ May be blocked by some proxies/firewalls                          │  │
│  │   ✗ No auto-reconnection (must implement)                             │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### WebSocket Lifecycle

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         WEBSOCKET LIFECYCLE                                   │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. HANDSHAKE (HTTP Upgrade)                                                 │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  Client Request:                                                       │  │
│  │  GET /chat HTTP/1.1                                                    │  │
│  │  Host: server.example.com                                              │  │
│  │  Upgrade: websocket                                                    │  │
│  │  Connection: Upgrade                                                   │  │
│  │  Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==                          │  │
│  │  Sec-WebSocket-Version: 13                                             │  │
│  │                                                                        │  │
│  │  Server Response:                                                      │  │
│  │  HTTP/1.1 101 Switching Protocols                                      │  │
│  │  Upgrade: websocket                                                    │  │
│  │  Connection: Upgrade                                                   │  │
│  │  Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=                    │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                          │                                   │
│                                          ▼                                   │
│  2. OPEN STATE                                                               │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  onOpen() callback fired                                               │  │
│  │  Connection established, ready for messaging                           │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                          │                                   │
│                                          ▼                                   │
│  3. MESSAGING                                                                │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  ┌────────┐                              ┌────────┐                   │  │
│  │  │ Client │ ◀═══ Text/Binary frames ═══▶ │ Server │                   │  │
│  │  └────────┘                              └────────┘                   │  │
│  │                                                                        │  │
│  │  onMessage(text: String)   - Text frame received                       │  │
│  │  onMessage(bytes: ByteString) - Binary frame received                  │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                          │                                   │
│                                          ▼                                   │
│  4. CLOSING                                                                  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  onClosing(code, reason) - Close frame received (graceful)             │  │
│  │  onClosed(code, reason)  - Connection fully closed                     │  │
│  │  onFailure(throwable)    - Connection error (not graceful)             │  │
│  │                                                                        │  │
│  │  Close Codes:                                                          │  │
│  │  1000 = Normal closure                                                 │  │
│  │  1001 = Going away (server shutdown, browser navigating away)         │  │
│  │  1002 = Protocol error                                                 │  │
│  │  1003 = Unsupported data                                               │  │
│  │  1006 = Abnormal closure (no close frame)                              │  │
│  │  1011 = Server error                                                   │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### OkHttp WebSocket (Comprehensive)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// WEBSOCKET MANAGER WITH RECONNECTION
// ═══════════════════════════════════════════════════════════════════════════

class WebSocketManager(
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var reconnectJob: Job? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // State flows
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _messages = MutableSharedFlow<WebSocketMessage>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<WebSocketMessage> = _messages.asSharedFlow()
    
    private val _errors = MutableSharedFlow<WebSocketError>()
    val errors: SharedFlow<WebSocketError> = _errors.asSharedFlow()
    
    fun connect(url: String, headers: Map<String, String> = emptyMap()) {
        if (isConnected) {
            Log.w("WebSocket", "Already connected")
            return
        }
        
        _connectionState.value = ConnectionState.Connecting
        
        val request = Request.Builder()
            .url(url)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, createWebSocketListener())
    }
    
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected: ${response.code}")
                isConnected = true
                reconnectAttempts = 0
                _connectionState.value = ConnectionState.Connected
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received text: $text")
                scope.launch {
                    try {
                        val message = parseMessage(text)
                        _messages.emit(message)
                    } catch (e: Exception) {
                        _errors.emit(WebSocketError.ParseError(e.message ?: "Parse error"))
                    }
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Received binary: ${bytes.size} bytes")
                scope.launch {
                    _messages.emit(WebSocketMessage.Binary(bytes.toByteArray()))
                }
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $code - $reason")
                webSocket.close(1000, null)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closed: $code - $reason")
                isConnected = false
                _connectionState.value = ConnectionState.Disconnected
                
                if (code != 1000) {  // Not a normal closure
                    scheduleReconnect()
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
                isConnected = false
                _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
                
                scope.launch {
                    _errors.emit(WebSocketError.ConnectionError(t.message ?: "Connection failed"))
                }
                
                scheduleReconnect()
            }
        }
    }
    
    private fun parseMessage(text: String): WebSocketMessage {
        val json = Json.parseToJsonElement(text).jsonObject
        val type = json["type"]?.jsonPrimitive?.content
        
        return when (type) {
            "chat" -> WebSocketMessage.Chat(
                id = json["id"]?.jsonPrimitive?.content ?: "",
                sender = json["sender"]?.jsonPrimitive?.content ?: "",
                message = json["message"]?.jsonPrimitive?.content ?: "",
                timestamp = json["timestamp"]?.jsonPrimitive?.long ?: 0
            )
            "typing" -> WebSocketMessage.Typing(
                userId = json["userId"]?.jsonPrimitive?.content ?: "",
                isTyping = json["isTyping"]?.jsonPrimitive?.boolean ?: false
            )
            "presence" -> WebSocketMessage.Presence(
                userId = json["userId"]?.jsonPrimitive?.content ?: "",
                status = json["status"]?.jsonPrimitive?.content ?: ""
            )
            else -> WebSocketMessage.Raw(text)
        }
    }
    
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.e("WebSocket", "Max reconnect attempts reached")
            _connectionState.value = ConnectionState.Failed
            return
        }
        
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delay = (2.0.pow(reconnectAttempts) * 1000).toLong()
                .coerceAtMost(30000)  // Max 30 seconds
            
            Log.d("WebSocket", "Reconnecting in ${delay}ms (attempt ${reconnectAttempts + 1})")
            _connectionState.value = ConnectionState.Reconnecting(reconnectAttempts + 1)
            
            delay(delay)
            reconnectAttempts++
            
            // Get the URL from somewhere (should be stored)
            // connect(storedUrl, storedHeaders)
        }
    }
    
    fun send(message: String): Boolean {
        if (!isConnected) {
            Log.w("WebSocket", "Not connected, cannot send")
            return false
        }
        return webSocket?.send(message) ?: false
    }
    
    fun send(bytes: ByteArray): Boolean {
        if (!isConnected) {
            Log.w("WebSocket", "Not connected, cannot send")
            return false
        }
        return webSocket?.send(bytes.toByteString()) ?: false
    }
    
    fun sendJson(data: Any): Boolean {
        val json = Json.encodeToString(data)
        return send(json)
    }
    
    fun disconnect(code: Int = 1000, reason: String? = null) {
        reconnectJob?.cancel()
        webSocket?.close(code, reason)
        webSocket = null
        isConnected = false
        _connectionState.value = ConnectionState.Disconnected
    }
    
    fun destroy() {
        disconnect()
        scope.cancel()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STATE AND MESSAGE TYPES
// ═══════════════════════════════════════════════════════════════════════════

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
    object Failed : ConnectionState()
}

sealed class WebSocketMessage {
    data class Chat(
        val id: String,
        val sender: String,
        val message: String,
        val timestamp: Long
    ) : WebSocketMessage()
    
    data class Typing(
        val userId: String,
        val isTyping: Boolean
    ) : WebSocketMessage()
    
    data class Presence(
        val userId: String,
        val status: String
    ) : WebSocketMessage()
    
    data class Binary(val data: ByteArray) : WebSocketMessage()
    data class Raw(val text: String) : WebSocketMessage()
}

sealed class WebSocketError {
    data class ConnectionError(val message: String) : WebSocketError()
    data class ParseError(val message: String) : WebSocketError()
    data class SendError(val message: String) : WebSocketError()
}

// ═══════════════════════════════════════════════════════════════════════════
// USAGE IN VIEWMODEL
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    val connectionState: StateFlow<ConnectionState> = webSocketManager.connectionState
    
    init {
        observeMessages()
        observeErrors()
    }
    
    private fun observeMessages() {
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                when (message) {
                    is WebSocketMessage.Chat -> {
                        val chatMessage = ChatMessage(
                            id = message.id,
                            sender = message.sender,
                            text = message.message,
                            timestamp = message.timestamp
                        )
                        _chatMessages.update { it + chatMessage }
                    }
                    is WebSocketMessage.Typing -> {
                        // Handle typing indicator
                    }
                    is WebSocketMessage.Presence -> {
                        // Handle user presence
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun observeErrors() {
        viewModelScope.launch {
            webSocketManager.errors.collect { error ->
                // Handle error (show toast, etc.)
            }
        }
    }
    
    fun connect(roomId: String) {
        val url = "wss://chat.example.com/ws?room=$roomId"
        val headers = mapOf(
            "Authorization" to "Bearer ${getToken()}"
        )
        webSocketManager.connect(url, headers)
    }
    
    fun sendMessage(text: String) {
        val message = mapOf(
            "type" to "chat",
            "message" to text,
            "timestamp" to System.currentTimeMillis()
        )
        webSocketManager.sendJson(message)
    }
    
    fun sendTyping(isTyping: Boolean) {
        val message = mapOf(
            "type" to "typing",
            "isTyping" to isTyping
        )
        webSocketManager.sendJson(message)
    }
    
    fun disconnect() {
        webSocketManager.disconnect()
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketManager.destroy()
    }
}
```

### Scarlet Library (Declarative WebSocket)

Scarlet provides a Retrofit-like experience for WebSocket APIs.

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// DEPENDENCIES
// ═══════════════════════════════════════════════════════════════════════════

dependencies {
    implementation("com.tinder.scarlet:scarlet:0.1.12")
    implementation("com.tinder.scarlet:websocket-okhttp:0.1.12")
    implementation("com.tinder.scarlet:stream-adapter-coroutines:0.1.12")
    implementation("com.tinder.scarlet:message-adapter-moshi:0.1.12")
    implementation("com.tinder.scarlet:lifecycle-android:0.1.12")
}

// ═══════════════════════════════════════════════════════════════════════════
// SERVICE INTERFACE (Like Retrofit)
// ═══════════════════════════════════════════════════════════════════════════

interface ChatService {
    
    // Receive WebSocket events
    @Receive
    fun observeWebSocketEvent(): Flow<WebSocket.Event>
    
    // Receive specific message types
    @Receive
    fun observeChatMessages(): Flow<ChatMessage>
    
    @Receive
    fun observeTypingEvents(): Flow<TypingEvent>
    
    @Receive
    fun observePresenceEvents(): Flow<PresenceEvent>
    
    // Send messages
    @Send
    fun sendMessage(message: SendMessageRequest): Boolean
    
    @Send
    fun sendTyping(event: TypingEvent): Boolean
    
    @Send
    fun sendPresence(event: PresenceEvent): Boolean
}

// ═══════════════════════════════════════════════════════════════════════════
// MESSAGE TYPES
// ═══════════════════════════════════════════════════════════════════════════

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String,
    val type: String = "chat",
    val sender: String,
    val message: String,
    val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val type: String = "chat",
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class TypingEvent(
    val type: String = "typing",
    val userId: String,
    val isTyping: Boolean
)

@JsonClass(generateAdapter = true)
data class PresenceEvent(
    val type: String = "presence",
    val userId: String,
    val status: String  // "online", "offline", "away"
)

// ═══════════════════════════════════════════════════════════════════════════
// SCARLET SETUP
// ═══════════════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    @Provides
    @Singleton
    fun provideScarlet(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        application: Application
    ): ChatService {
        val scarlet = Scarlet.Builder()
            .webSocketFactory(
                okHttpClient.newWebSocketFactory("wss://chat.example.com/ws")
            )
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .lifecycle(AndroidLifecycle.ofApplicationForeground(application))
            .backoffStrategy(
                ExponentialBackoffStrategy(
                    initialDurationMillis = 1000,
                    maxDurationMillis = 30000
                )
            )
            .build()
        
        return scarlet.create()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// USAGE
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class ScarletChatViewModel @Inject constructor(
    private val chatService: ChatService
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _connectionState = MutableStateFlow<WebSocket.Event?>(null)
    val connectionState: StateFlow<WebSocket.Event?> = _connectionState.asStateFlow()
    
    init {
        observeConnection()
        observeMessages()
    }
    
    private fun observeConnection() {
        viewModelScope.launch {
            chatService.observeWebSocketEvent().collect { event ->
                _connectionState.value = event
                when (event) {
                    is WebSocket.Event.OnConnectionOpened<*> -> {
                        Log.d("Scarlet", "Connected")
                    }
                    is WebSocket.Event.OnConnectionClosed -> {
                        Log.d("Scarlet", "Closed")
                    }
                    is WebSocket.Event.OnConnectionFailed -> {
                        Log.e("Scarlet", "Failed: ${event.throwable.message}")
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun observeMessages() {
        viewModelScope.launch {
            chatService.observeChatMessages().collect { message ->
                _messages.update { it + message }
            }
        }
    }
    
    fun sendMessage(text: String) {
        val request = SendMessageRequest(message = text)
        chatService.sendMessage(request)
    }
    
    fun setTyping(isTyping: Boolean) {
        val event = TypingEvent(
            userId = getCurrentUserId(),
            isTyping = isTyping
        )
        chatService.sendTyping(event)
    }
}
```

---

## 6.5 Serialization

### What is Serialization?

Serialization is the process of converting data structures or objects into a format that can be stored or transmitted. In Android networking, we primarily deal with **JSON serialization** - converting between Kotlin/Java objects and JSON strings.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        SERIALIZATION PROCESS                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  SERIALIZATION (Object → JSON):                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  Kotlin Object:                    JSON String:                        │  │
│  │  ┌────────────────────┐           ┌────────────────────────────────┐  │  │
│  │  │ User(              │           │ {                              │  │  │
│  │  │   id = 1,          │  ──────▶  │   "id": 1,                     │  │  │
│  │  │   name = "John",   │  Encode   │   "name": "John",              │  │  │
│  │  │   email = "j@..."  │           │   "email": "j@..."             │  │  │
│  │  │ )                  │           │ }                              │  │  │
│  │  └────────────────────┘           └────────────────────────────────┘  │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  DESERIALIZATION (JSON → Object):                                            │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  JSON String:                      Kotlin Object:                      │  │
│  │  ┌────────────────────────────┐   ┌────────────────────┐              │  │
│  │  │ {                          │   │ User(              │              │  │
│  │  │   "id": 1,                 │   │   id = 1,          │              │  │
│  │  │   "name": "John",          │  ──────▶  name = "John",   │          │  │
│  │  │   "email": "j@..."         │  Decode   email = "j@..."  │          │  │
│  │  │ }                          │   │ )                  │              │  │
│  │  └────────────────────────────┘   └────────────────────┘              │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Library Comparison (Detailed)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                   SERIALIZATION LIBRARIES COMPARISON                          │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                              GSON                                      │  │
│  │                         (Google, 2008)                                 │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  How it works: Runtime reflection to analyze object structure          │  │
│  │                                                                        │  │
│  │  ✓ Pros:                           ✗ Cons:                            │  │
│  │  • Most mature, widely used        • Slowest performance               │  │
│  │  • Extensive documentation         • Largest library size (~300KB)     │  │
│  │  • Works with any class            • No Kotlin null safety             │  │
│  │  • Flexible configuration          • Runtime errors for malformed JSON │  │
│  │  • No annotation required          • Reflection impacts R8/ProGuard    │  │
│  │                                                                        │  │
│  │  Best for: Legacy projects, quick prototypes, Java codebases          │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                             MOSHI                                      │  │
│  │                         (Square, 2015)                                 │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  How it works: Codegen (compile-time) OR reflection (runtime)          │  │
│  │                                                                        │  │
│  │  ✓ Pros:                           ✗ Cons:                            │  │
│  │  • Faster than Gson                • Smaller community than Gson       │  │
│  │  • Kotlin-friendly                 • Codegen adds build time           │  │
│  │  • Better null handling            • Requires @JsonClass annotation    │  │
│  │  • Modern API design               • Learning curve from Gson          │  │
│  │  • Smaller library (~150KB)                                            │  │
│  │                                                                        │  │
│  │  Best for: New Kotlin projects, Square ecosystem (Retrofit/OkHttp)     │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                     KOTLINX SERIALIZATION                              │  │
│  │                       (JetBrains, 2017)                                │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  How it works: Compiler plugin generates serializers at compile-time   │  │
│  │                                                                        │  │
│  │  ✓ Pros:                           ✗ Cons:                            │  │
│  │  • Fastest performance             • Kotlin-only                       │  │
│  │  • Smallest size (~70KB)           • Requires compiler plugin          │  │
│  │  • Full Kotlin type safety         • Newer, less Stack Overflow help   │  │
│  │  • Multiplatform support           • Different API from Gson/Moshi     │  │
│  │  • Sealed class support            • Requires @Serializable annotation │  │
│  │  • Compile-time error checking                                         │  │
│  │                                                                        │  │
│  │  Best for: New Kotlin projects, KMP, performance-critical apps         │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  PERFORMANCE BENCHMARK (parsing 10,000 JSON objects):                        │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  Kotlinx Serialization: ████████████░░░░░░░░░░░░░░░░░░  ~120ms        │  │
│  │  Moshi (codegen):       ████████████████░░░░░░░░░░░░░░  ~180ms        │  │
│  │  Moshi (reflection):    ████████████████████░░░░░░░░░░  ~240ms        │  │
│  │  Gson:                  ████████████████████████████░░  ~350ms        │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  APK SIZE IMPACT (library + generated code):                                 │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                                                                        │  │
│  │  Kotlinx Serialization: ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░  ~70KB         │  │
│  │  Moshi (codegen):       ████░░░░░░░░░░░░░░░░░░░░░░░░░░  ~150KB        │  │
│  │  Moshi (reflection):    ██████░░░░░░░░░░░░░░░░░░░░░░░░  ~200KB        │  │
│  │  Gson:                  ██████████░░░░░░░░░░░░░░░░░░░░  ~300KB        │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Kotlinx Serialization (Comprehensive)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// SETUP
// ═══════════════════════════════════════════════════════════════════════════

// build.gradle.kts (Project)
plugins {
    kotlin("plugin.serialization") version "1.9.0" apply false
}

// build.gradle.kts (App)
plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

// ═══════════════════════════════════════════════════════════════════════════
// BASIC USAGE
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val isActive: Boolean = true  // Default values work
)

// Serialize (Object → JSON)
val user = User(1, "John", "john@example.com")
val json = Json.encodeToString(user)
// {"id":1,"name":"John","email":"john@example.com"}
// Note: isActive not included because encodeDefaults = false by default

// Deserialize (JSON → Object)
val jsonString = """{"id":1,"name":"John","email":"john@example.com"}"""
val user = Json.decodeFromString<User>(jsonString)

// ═══════════════════════════════════════════════════════════════════════════
// JSON CONFIGURATION
// ═══════════════════════════════════════════════════════════════════════════

val json = Json {
    // Parsing options
    ignoreUnknownKeys = true      // Don't fail on unknown JSON keys
    isLenient = true              // Allow quotes around non-string values
    coerceInputValues = true      // Replace null with default for non-null fields
    
    // Encoding options
    prettyPrint = true            // Format with indentation
    encodeDefaults = false        // Don't include properties with default values
    explicitNulls = false         // Don't include null values
    
    // Class discrimination (for polymorphism)
    classDiscriminator = "type"   // Field name for type info
    
    // Naming
    namingStrategy = JsonNamingStrategy.SnakeCase  // Convert to snake_case
}

// ═══════════════════════════════════════════════════════════════════════════
// PROPERTY CUSTOMIZATION
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
data class UserProfile(
    val id: Int,
    
    @SerialName("user_name")           // JSON key name
    val name: String,
    
    @SerialName("email_address")
    val email: String,
    
    @Transient                          // Excluded from serialization
    val localCache: String = "",
    
    @EncodeDefault                      // Always include even if default
    val role: String = "user",
    
    @Required                           // Must be present in JSON
    val createdAt: String,
    
    val metadata: JsonObject? = null    // Raw JSON object
)

// ═══════════════════════════════════════════════════════════════════════════
// NESTED OBJECTS AND COLLECTIONS
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val author: Author,                  // Nested object
    val tags: List<String>,              // List
    val comments: List<Comment>,         // List of objects
    val metadata: Map<String, String>    // Map
)

@Serializable
data class Author(
    val id: Int,
    val name: String
)

@Serializable
data class Comment(
    val id: Int,
    val text: String,
    val author: Author
)

val post = Post(
    id = 1,
    title = "Hello World",
    content = "Content here",
    author = Author(1, "John"),
    tags = listOf("kotlin", "android"),
    comments = listOf(
        Comment(1, "Great post!", Author(2, "Jane"))
    ),
    metadata = mapOf("views" to "100", "likes" to "10")
)

val json = Json.encodeToString(post)

// ═══════════════════════════════════════════════════════════════════════════
// POLYMORPHIC SERIALIZATION (Sealed Classes)
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
sealed class NetworkResponse<out T> {
    
    @Serializable
    @SerialName("success")
    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : NetworkResponse<T>()
    
    @Serializable
    @SerialName("error")
    data class Error(
        val code: Int,
        val message: String,
        val details: List<String>? = null
    ) : NetworkResponse<Nothing>()
    
    @Serializable
    @SerialName("loading")
    object Loading : NetworkResponse<Nothing>()
}

// For generic types, you need to specify the serializer
val json = Json { classDiscriminator = "status" }

// Serialize
val success: NetworkResponse<User> = NetworkResponse.Success(
    data = User(1, "John", "john@example.com"),
    message = "User fetched successfully"
)
val jsonString = json.encodeToString(
    NetworkResponse.serializer(User.serializer()),
    success
)
// {"status":"success","data":{"id":1,"name":"John","email":"john@example.com"},"message":"User fetched successfully"}

// Deserialize
val response: NetworkResponse<User> = json.decodeFromString(
    NetworkResponse.serializer(User.serializer()),
    jsonString
)

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM SERIALIZERS
// ═══════════════════════════════════════════════════════════════════════════

// Date as ISO string
object DateAsStringSerializer : KSerializer<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    
    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(formatter.format(value))
    }
    
    override fun deserialize(decoder: Decoder): Date {
        return formatter.parse(decoder.decodeString())!!
    }
}

// Date as timestamp (Long)
object DateAsLongSerializer : KSerializer<Date> {
    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeLong(value.time)
    }
    
    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeLong())
    }
}

// UUID serializer
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
    
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

// Using custom serializers
@Serializable
data class Event(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    
    val name: String,
    
    @Serializable(with = DateAsStringSerializer::class)
    val startDate: Date,
    
    @Serializable(with = DateAsLongSerializer::class)
    val endDate: Date
)

// Or register globally
val json = Json {
    serializersModule = SerializersModule {
        contextual(Date::class, DateAsStringSerializer)
        contextual(UUID::class, UUIDSerializer)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// WORKING WITH RAW JSON
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
data class ApiResponse(
    val success: Boolean,
    val data: JsonElement  // Raw JSON - parse later
)

val response = json.decodeFromString<ApiResponse>(jsonString)

// Check JSON type
when (response.data) {
    is JsonObject -> {
        val obj = response.data.jsonObject
        val name = obj["name"]?.jsonPrimitive?.content
    }
    is JsonArray -> {
        val arr = response.data.jsonArray
        arr.forEach { element ->
            // Process each element
        }
    }
    is JsonPrimitive -> {
        val value = response.data.jsonPrimitive.content
    }
    is JsonNull -> {
        // Handle null
    }
}

// Build JSON dynamically
val dynamicJson = buildJsonObject {
    put("name", "John")
    put("age", 30)
    put("active", true)
    putJsonArray("tags") {
        add("kotlin")
        add("android")
    }
    putJsonObject("address") {
        put("city", "NYC")
        put("country", "USA")
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ENUM SERIALIZATION
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
enum class UserRole {
    @SerialName("admin") ADMIN,
    @SerialName("user") USER,
    @SerialName("moderator") MODERATOR
}

// With custom serializer for unknown values
@Serializable(with = UserStatusSerializer::class)
enum class UserStatus {
    ACTIVE, INACTIVE, PENDING, UNKNOWN
}

object UserStatusSerializer : KSerializer<UserStatus> {
    override val descriptor = PrimitiveSerialDescriptor("UserStatus", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: UserStatus) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): UserStatus {
        val value = decoder.decodeString()
        return try {
            UserStatus.valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            UserStatus.UNKNOWN  // Fallback for unknown values
        }
    }
}
```

### Moshi (Comprehensive)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// SETUP
// ═══════════════════════════════════════════════════════════════════════════

// build.gradle.kts
dependencies {
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    
    // Choose one: codegen (recommended) OR reflection
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    // OR
    // implementation("com.squareup.moshi:moshi-kotlin-reflect:1.15.0")
}

// ═══════════════════════════════════════════════════════════════════════════
// BASIC USAGE
// ═══════════════════════════════════════════════════════════════════════════

@JsonClass(generateAdapter = true)  // Required for codegen
data class User(
    @Json(name = "user_id")
    val id: Int,
    
    @Json(name = "user_name")
    val name: String,
    
    val email: String,
    
    @Json(name = "is_active")
    val isActive: Boolean = true
)

// Create Moshi instance
val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())  // For non-codegen classes
    .build()

// Get adapter for User
val adapter = moshi.adapter<User>()

// Serialize
val json = adapter.toJson(user)

// Deserialize  
val user = adapter.fromJson(jsonString)

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM ADAPTERS
// ═══════════════════════════════════════════════════════════════════════════

// Date adapter
class DateAdapter {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    @ToJson
    fun toJson(date: Date): String = formatter.format(date)
    
    @FromJson
    fun fromJson(json: String): Date = formatter.parse(json)!!
}

// Null-safe date adapter
class NullableDateAdapter {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    @ToJson
    fun toJson(date: Date?): String? = date?.let { formatter.format(it) }
    
    @FromJson
    fun fromJson(json: String?): Date? = json?.let { formatter.parse(it) }
}

// Enum adapter with fallback
class EnumFallbackAdapter<T : Enum<T>>(
    private val enumClass: Class<T>,
    private val fallback: T
) {
    @ToJson
    fun toJson(value: T): String = value.name.lowercase()
    
    @FromJson
    fun fromJson(json: String): T {
        return try {
            java.lang.Enum.valueOf(enumClass, json.uppercase())
        } catch (e: IllegalArgumentException) {
            fallback
        }
    }
}

// Register adapters
val moshi = Moshi.Builder()
    .add(DateAdapter())
    .add(EnumFallbackAdapter(UserStatus::class.java, UserStatus.UNKNOWN))
    .addLast(KotlinJsonAdapterFactory())
    .build()

// ═══════════════════════════════════════════════════════════════════════════
// POLYMORPHIC ADAPTERS
// ═══════════════════════════════════════════════════════════════════════════

sealed class Message {
    abstract val id: String
    abstract val timestamp: Long
}

@JsonClass(generateAdapter = true)
data class TextMessage(
    override val id: String,
    override val timestamp: Long,
    val text: String
) : Message()

@JsonClass(generateAdapter = true)
data class ImageMessage(
    override val id: String,
    override val timestamp: Long,
    val imageUrl: String,
    val caption: String?
) : Message()

// Polymorphic adapter factory
object MessageAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        if (type != Message::class.java) return null
        
        val textAdapter = moshi.adapter<TextMessage>()
        val imageAdapter = moshi.adapter<ImageMessage>()
        
        return object : JsonAdapter<Message>() {
            override fun fromJson(reader: JsonReader): Message? {
                val jsonObject = reader.readJsonValue() as? Map<*, *>
                    ?: return null
                
                return when (jsonObject["type"]) {
                    "text" -> textAdapter.fromJsonValue(jsonObject)
                    "image" -> imageAdapter.fromJsonValue(jsonObject)
                    else -> throw JsonDataException("Unknown message type")
                }
            }
            
            override fun toJson(writer: JsonWriter, value: Message?) {
                when (value) {
                    is TextMessage -> textAdapter.toJson(writer, value)
                    is ImageMessage -> imageAdapter.toJson(writer, value)
                    null -> writer.nullValue()
                }
            }
        }
    }
}

val moshi = Moshi.Builder()
    .add(MessageAdapterFactory)
    .addLast(KotlinJsonAdapterFactory())
    .build()
```

### Gson (For Legacy Reference)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// SETUP
// ═══════════════════════════════════════════════════════════════════════════

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

// ═══════════════════════════════════════════════════════════════════════════
// BASIC USAGE
// ═══════════════════════════════════════════════════════════════════════════

data class User(
    @SerializedName("user_id")
    val id: Int,
    
    @SerializedName("user_name")
    val name: String,
    
    val email: String,
    
    @Expose  // Only if excludeFieldsWithoutExposeAnnotation() is set
    val isActive: Boolean = true,
    
    @Transient  // Always excluded
    val localCache: String = ""
)

// Create Gson instance
val gson = GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    .serializeNulls()
    .setPrettyPrinting()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .registerTypeAdapter(Date::class.java, DateTypeAdapter())
    .create()

// Serialize
val json = gson.toJson(user)

// Deserialize
val user = gson.fromJson(jsonString, User::class.java)

// For generic types
val type = object : TypeToken<List<User>>() {}.type
val users: List<User> = gson.fromJson(jsonString, type)

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM TYPE ADAPTER
// ═══════════════════════════════════════════════════════════════════════════

class DateTypeAdapter : TypeAdapter<Date>() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }
    
    override fun read(reader: JsonReader): Date? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            else -> formatter.parse(reader.nextString())
        }
    }
}
```

---

## 6.6 Image Loading

### Why Use Image Loading Libraries?

Loading images in Android is complex due to:
- **Memory management**: Images can be large and cause OOM errors
- **Threading**: Network/disk I/O must happen off the main thread
- **Caching**: Multiple cache layers (memory, disk) needed for performance
- **Lifecycle**: Images must be cancelled when views are destroyed
- **Transformations**: Resizing, cropping, effects
- **Formats**: JPG, PNG, GIF, WebP, SVG, video thumbnails

### Image Loading Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                       IMAGE LOADING PIPELINE                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. REQUEST                                                                  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  ImageView.load(url) {                                                 │  │
│  │      placeholder(R.drawable.loading)                                   │  │
│  │      error(R.drawable.error)                                           │  │
│  │      transformations(CircleCrop())                                     │  │
│  │  }                                                                     │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                      │                                       │
│                                      ▼                                       │
│  2. MEMORY CACHE CHECK                                                       │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────────────┐                                                   │  │
│  │  │  Memory Cache   │ ── Hit ──▶ Return bitmap immediately (~1ms)      │  │
│  │  │  (LRU ~25% RAM) │                                                   │  │
│  │  └────────┬────────┘                                                   │  │
│  │           │ Miss                                                       │  │
│  │           ▼                                                            │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                      │                                       │
│                                      ▼                                       │
│  3. DISK CACHE CHECK                                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────────────┐                                                   │  │
│  │  │   Disk Cache    │ ── Hit ──▶ Decode & return (~10-50ms)            │  │
│  │  │  (~100-250MB)   │                                                   │  │
│  │  └────────┬────────┘                                                   │  │
│  │           │ Miss                                                       │  │
│  │           ▼                                                            │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                      │                                       │
│                                      ▼                                       │
│  4. NETWORK FETCH                                                            │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    │  │
│  │  │  Fetch from     │───▶│  Decode image   │───▶│ Apply           │    │  │
│  │  │  network/file   │    │  (downsampling) │    │ transformations │    │  │
│  │  └─────────────────┘    └─────────────────┘    └─────────────────┘    │  │
│  │         │                                              │               │  │
│  │         ▼                                              ▼               │  │
│  │  [Save to disk cache]                        [Save to memory cache]   │  │
│  │                                                                        │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                      │                                       │
│                                      ▼                                       │
│  5. DISPLAY                                                                  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────────────┐    ┌─────────────────┐                           │  │
│  │  │  Transition     │───▶│  Set bitmap to  │                           │  │
│  │  │  (crossfade)    │    │  ImageView      │                           │  │
│  │  └─────────────────┘    └─────────────────┘                           │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Library Comparison

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                      IMAGE LOADING LIBRARIES                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                              COIL                                      │  │
│  │                    (Coroutine Image Loader)                            │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  • Kotlin-first, uses coroutines                                       │  │
│  │  • Native Jetpack Compose support                                      │  │
│  │  • Modern API, extension functions                                     │  │
│  │  • Backed by Instacart                                                 │  │
│  │  • Smallest library size (~1.5MB with Compose)                         │  │
│  │  • Extensions: GIF, SVG, Video frames                                  │  │
│  │                                                                        │  │
│  │  Best for: New Kotlin/Compose projects                                 │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                              GLIDE                                     │  │
│  │                       (Google recommended)                             │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  • Most mature, widely tested                                          │  │
│  │  • Excellent GIF support built-in                                      │  │
│  │  • Video thumbnail support                                             │  │
│  │  • Generated API for type safety                                       │  │
│  │  • Larger library size (~2MB)                                          │  │
│  │  • Java-based, works with Kotlin                                       │  │
│  │                                                                        │  │
│  │  Best for: Existing Java projects, GIF-heavy apps                      │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                             PICASSO                                    │  │
│  │                          (by Square)                                   │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  • Simplest API                                                        │  │
│  │  • Smallest size (~120KB)                                              │  │
│  │  • Good for simple use cases                                           │  │
│  │  • Less actively maintained                                            │  │
│  │  • No GIF support                                                      │  │
│  │  • No Compose support                                                  │  │
│  │                                                                        │  │
│  │  Best for: Simple apps with basic image loading needs                  │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  FEATURE MATRIX:                                                             │
│  ┌─────────────────────┬────────────┬────────────┬────────────────────────┐  │
│  │ Feature             │   COIL     │   GLIDE    │       PICASSO          │  │
│  ├─────────────────────┼────────────┼────────────┼────────────────────────┤  │
│  │ Language            │ Kotlin     │ Java       │ Java                   │  │
│  │ Coroutines          │ ✅ Native  │ ❌         │ ❌                     │  │
│  │ Compose             │ ✅ Native  │ ⚠️ Library │ ❌                     │  │
│  │ GIF                 │ ✅ Ext     │ ✅ Built-in│ ❌                     │  │
│  │ SVG                 │ ✅ Ext     │ ❌         │ ❌                     │  │
│  │ Video thumbnails    │ ✅ Ext     │ ✅ Built-in│ ❌                     │  │
│  │ WebP                │ ✅         │ ✅         │ ✅                     │  │
│  │ Transformations     │ ✅         │ ✅         │ ✅ (limited)           │  │
│  │ Preloading          │ ✅         │ ✅         │ ✅                     │  │
│  │ Priority loading    │ ✅         │ ✅         │ ❌                     │  │
│  │ Library size        │ ~1.5MB     │ ~2MB       │ ~120KB                 │  │
│  │ Lifecycle aware     │ ✅         │ ✅         │ ❌                     │  │
│  │ Memory efficiency   │ ⭐⭐⭐⭐⭐ │ ⭐⭐⭐⭐⭐│ ⭐⭐⭐                 │  │
│  └─────────────────────┴────────────┴────────────┴────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Coil (Comprehensive)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// DEPENDENCIES
// ═══════════════════════════════════════════════════════════════════════════

dependencies {
    // Core
    implementation("io.coil-kt:coil:2.5.0")
    
    // Jetpack Compose support
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Extensions
    implementation("io.coil-kt:coil-gif:2.5.0")     // GIF support
    implementation("io.coil-kt:coil-svg:2.5.0")     // SVG support
    implementation("io.coil-kt:coil-video:2.5.0")   // Video frame extraction
}

// ═══════════════════════════════════════════════════════════════════════════
// TRADITIONAL VIEWS (XML)
// ═══════════════════════════════════════════════════════════════════════════

// Basic usage
imageView.load("https://example.com/image.jpg")

// With options
imageView.load("https://example.com/image.jpg") {
    crossfade(true)
    crossfade(300)  // Custom duration
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
    fallback(R.drawable.fallback)  // When data is null
    
    // Sizing
    size(ViewSizeResolver(imageView))
    size(100, 100)  // Specific size
    scale(Scale.FILL)  // or Scale.FIT
    
    // Caching
    memoryCacheKey("custom_key")
    diskCacheKey("custom_disk_key")
    memoryCachePolicy(CachePolicy.ENABLED)
    diskCachePolicy(CachePolicy.READ_ONLY)
    networkCachePolicy(CachePolicy.ENABLED)
    
    // Priority
    priority(Priority.HIGH)  // or NORMAL, LOW
    
    // Headers
    headers {
        set("Authorization", "Bearer $token")
    }
    
    // Transformations
    transformations(
        CircleCropTransformation(),
        RoundedCornersTransformation(16f)
    )
    
    // Lifecycle
    lifecycle(viewLifecycleOwner)
    
    // Listeners
    listener(
        onStart = { request -> /* loading started */ },
        onSuccess = { request, result -> /* success */ },
        onError = { request, throwable -> /* error */ },
        onCancel = { request -> /* cancelled */ }
    )
    
    // Target
    target(imageView)
}

// Load into custom target
val request = ImageRequest.Builder(context)
    .data("https://example.com/image.jpg")
    .target { drawable ->
        // Use the drawable
    }
    .build()

context.imageLoader.enqueue(request)

// Synchronous loading (for backgrounds, etc.)
val request = ImageRequest.Builder(context)
    .data("https://example.com/image.jpg")
    .allowHardware(false)  // Required for getting Bitmap
    .build()

val result = context.imageLoader.execute(request)
if (result is SuccessResult) {
    val bitmap = result.drawable.toBitmap()
}

// ═══════════════════════════════════════════════════════════════════════════
// JETPACK COMPOSE
// ═══════════════════════════════════════════════════════════════════════════

// AsyncImage - Simple usage
@Composable
fun UserAvatar(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "User avatar",
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Gray, CircleShape),
        contentScale = ContentScale.Crop
    )
}

// AsyncImage with ImageRequest
@Composable
fun ProductImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            .placeholderMemoryCacheKey("placeholder")
            .build(),
        contentDescription = "Product image",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp)),
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.error),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        colorFilter = null,  // Optional color filter
        filterQuality = FilterQuality.High
    )
}

// SubcomposeAsyncImage - Custom loading/error states
@Composable
fun ImageWithStates(imageUrl: String) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AsyncImagePainter.State.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red
                    )
                    Text("Failed to load image")
                    Button(onClick = { /* retry */ }) {
                        Text("Retry")
                    }
                }
            }
            is AsyncImagePainter.State.Success -> {
                SubcomposeAsyncImageContent()
            }
            is AsyncImagePainter.State.Empty -> {
                // No image data
            }
        }
    }
}

// rememberAsyncImagePainter - For more control
@Composable
fun CustomImage(imageUrl: String) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build()
    )
    
    // Access painter state
    val state = painter.state
    
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    
    // Show loading indicator overlay
    if (state is AsyncImagePainter.State.Loading) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TRANSFORMATIONS
// ═══════════════════════════════════════════════════════════════════════════

// Built-in transformations
imageView.load(url) {
    transformations(
        CircleCropTransformation(),
        RoundedCornersTransformation(
            topLeft = 8f,
            topRight = 8f,
            bottomLeft = 0f,
            bottomRight = 0f
        )
    )
}

// Multiple transformations (applied in order)
imageView.load(url) {
    transformations(
        BlurTransformation(context, radius = 10f, sampling = 2f),
        GrayscaleTransformation(),
        CircleCropTransformation()
    )
}

// Custom transformation
class WatermarkTransformation(
    private val watermarkText: String,
    private val textSize: Float = 48f
) : Transformation {
    
    override val cacheKey: String = "watermark_${watermarkText}_$textSize"
    
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        
        val paint = Paint().apply {
            color = Color.WHITE
            this.textSize = textSize
            isAntiAlias = true
            alpha = 128
        }
        
        canvas.drawText(
            watermarkText,
            output.width - paint.measureText(watermarkText) - 20f,
            output.height - 20f,
            paint
        )
        
        return output
    }
}

// Blur transformation
class BlurTransformation(
    private val context: Context,
    private val radius: Float = 10f,
    private val sampling: Float = 1f
) : Transformation {
    
    override val cacheKey = "blur_${radius}_$sampling"
    
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val scaledWidth = (input.width / sampling).toInt()
        val scaledHeight = (input.height / sampling).toInt()
        
        val scaled = Bitmap.createScaledBitmap(input, scaledWidth, scaledHeight, true)
        
        val rs = RenderScript.create(context)
        val rsInput = Allocation.createFromBitmap(rs, scaled)
        val rsOutput = Allocation.createTyped(rs, rsInput.type)
        
        ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
            setRadius(radius.coerceIn(0f, 25f))
            setInput(rsInput)
            forEach(rsOutput)
        }
        
        rsOutput.copyTo(scaled)
        
        rs.destroy()
        
        return Bitmap.createScaledBitmap(scaled, input.width, input.height, true)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM IMAGELOADER WITH ALL OPTIONS
// ═══════════════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {
    
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // Crossfade
            .crossfade(true)
            .crossfade(300)
            
            // Memory cache
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)  // 25% of available memory
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            }
            
            // Disk cache
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)  // 250 MB
                    .build()
            }
            
            // Network
            .okHttpClient { okHttpClient }
            
            // Components
            .components {
                // SVG decoder
                add(SvgDecoder.Factory())
                
                // GIF decoder (use with coil-gif)
                add(ImageDecoderDecoder.Factory())
                // OR for older API: add(GifDecoder.Factory())
                
                // Video frame decoder
                add(VideoFrameDecoder.Factory())
                
                // Custom fetcher
                add(FirebaseStorageFetcher.Factory())
            }
            
            // Options
            .allowHardware(true)
            .allowRgb565(false)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .addLastModifiedToFileCacheKey(true)
            .respectCacheHeaders(true)
            
            // Logger
            .logger(DebugLogger(Log.VERBOSE))
            
            // Event listener
            .eventListener(object : EventListener {
                override fun onStart(request: ImageRequest) {
                    Log.d("Coil", "Loading: ${request.data}")
                }
                
                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    Log.d("Coil", "Success: ${request.data}")
                }
                
                override fun onError(request: ImageRequest, result: ErrorResult) {
                    Log.e("Coil", "Error: ${result.throwable.message}")
                }
                
                override fun onCancel(request: ImageRequest) {
                    Log.d("Coil", "Cancelled: ${request.data}")
                }
            })
            
            .build()
    }
}

// Initialize in Application class
@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override fun newImageLoader(): ImageLoader = imageLoader
}

// ═══════════════════════════════════════════════════════════════════════════
// PRELOADING AND PREFETCHING
// ═══════════════════════════════════════════════════════════════════════════

// Preload into cache
suspend fun preloadImages(urls: List<String>, context: Context) {
    val imageLoader = context.imageLoader
    
    urls.forEach { url ->
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        
        imageLoader.enqueue(request)
    }
}

// Preload with coroutines
suspend fun preloadImagesParallel(urls: List<String>, context: Context) {
    coroutineScope {
        urls.map { url ->
            async {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(Size.ORIGINAL)
                    .build()
                context.imageLoader.execute(request)
            }
        }.awaitAll()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM FETCHER (Firebase Storage example)
// ═══════════════════════════════════════════════════════════════════════════

data class FirebaseStorageUri(val uri: String)

class FirebaseStorageFetcher(
    private val data: FirebaseStorageUri,
    private val options: Options
) : Fetcher {
    
    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        val storage = Firebase.storage
        val ref = storage.getReferenceFromUrl(data.uri)
        
        val metadata = ref.metadata.await()
        val bytes = ref.getBytes(Long.MAX_VALUE).await()
        
        SourceResult(
            source = ImageSource(
                source = Buffer().apply { write(bytes) },
                context = options.context
            ),
            mimeType = metadata.contentType,
            dataSource = DataSource.NETWORK
        )
    }
    
    class Factory : Fetcher.Factory<FirebaseStorageUri> {
        override fun create(
            data: FirebaseStorageUri,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher = FirebaseStorageFetcher(data, options)
    }
}

// Usage
imageView.load(FirebaseStorageUri("gs://bucket/path/image.jpg"))
```

### Glide (Detailed)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// DEPENDENCIES
// ═══════════════════════════════════════════════════════════════════════════

dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    
    // OkHttp integration
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
}

// ═══════════════════════════════════════════════════════════════════════════
// BASIC USAGE
// ═══════════════════════════════════════════════════════════════════════════

// Simple load
Glide.with(context)
    .load("https://example.com/image.jpg")
    .into(imageView)

// With options
Glide.with(context)
    .load("https://example.com/image.jpg")
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .fallback(R.drawable.fallback)
    .centerCrop()
    .override(300, 200)  // Resize
    .timeout(30_000)     // Connection timeout
    .into(imageView)

// RequestOptions for reuse
val requestOptions = RequestOptions()
    .centerCrop()
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .priority(Priority.HIGH)
    .skipMemoryCache(false)
    .signature(ObjectKey(System.currentTimeMillis()))  // Cache invalidation

Glide.with(context)
    .load(url)
    .apply(requestOptions)
    .into(imageView)

// ═══════════════════════════════════════════════════════════════════════════
// TRANSFORMATIONS
// ═══════════════════════════════════════════════════════════════════════════

Glide.with(context)
    .load(url)
    .circleCrop()        // Circle crop
    .centerCrop()        // Center crop
    .centerInside()      // Scale to fit
    .fitCenter()         // Fit center
    .transform(
        MultiTransformation(
            CenterCrop(),
            RoundedCorners(16),
            BlurTransformation(25)  // From glide-transformations library
        )
    )
    .into(imageView)

// ═══════════════════════════════════════════════════════════════════════════
// LISTENERS
// ═══════════════════════════════════════════════════════════════════════════

Glide.with(context)
    .load(url)
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>,
            isFirstResource: Boolean
        ): Boolean {
            Log.e("Glide", "Load failed: ${e?.message}")
            return false  // Return true to prevent error placeholder
        }
        
        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            Log.d("Glide", "Load success from: $dataSource")
            return false  // Return true to prevent Glide from setting
        }
    })
    .into(imageView)

// ═══════════════════════════════════════════════════════════════════════════
// CUSTOM GLIDE MODULE
// ═══════════════════════════════════════════════════════════════════════════

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Memory cache
        builder.setMemoryCache(LruResourceCache(50 * 1024 * 1024))
        
        // Disk cache
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(context, "glide_cache", 250 * 1024 * 1024)
        )
        
        // Default options
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .disallowHardwareConfig()
        )
        
        // Log level
        builder.setLogLevel(Log.VERBOSE)
    }
    
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Use OkHttp instead of HttpUrlConnection
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okHttpClient)
        )
    }
}
```

### Picasso (For Reference)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// DEPENDENCIES
// ═══════════════════════════════════════════════════════════════════════════

dependencies {
    implementation("com.squareup.picasso:picasso:2.8")
}

// ═══════════════════════════════════════════════════════════════════════════
// USAGE
// ═══════════════════════════════════════════════════════════════════════════

// Basic
Picasso.get()
    .load("https://example.com/image.jpg")
    .into(imageView)

// With options
Picasso.get()
    .load("https://example.com/image.jpg")
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .fit()
    .centerCrop()
    .rotate(90f)
    .priority(Picasso.Priority.HIGH)
    .tag("gallery")  // For cancellation
    .noFade()
    .into(imageView)

// With custom Picasso instance
val picasso = Picasso.Builder(context)
    .downloader(OkHttp3Downloader(okHttpClient))
    .indicatorsEnabled(BuildConfig.DEBUG)
    .loggingEnabled(BuildConfig.DEBUG)
    .listener { _, uri, exception ->
        Log.e("Picasso", "Failed to load: $uri", exception)
    }
    .build()

Picasso.setSingletonInstance(picasso)

// Cancel requests
Picasso.get().cancelTag("gallery")
```

---

## Complete Networking Layer Example

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// COMPLETE DI MODULE
// ═══════════════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
            redactHeader("Cookie")
        }
    }
    
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 50L * 1024 * 1024  // 50 MB
        )
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient { okHttpClient }
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
```

---

## Best Practices

### 1. Network Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                      RECOMMENDED ARCHITECTURE                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐               │
│   │   ViewModel  │────▶│  Repository  │────▶│  Data Source │               │
│   └──────────────┘     └──────────────┘     └──────────────┘               │
│         │                    │                    │                         │
│         │              NetworkResult         ApiService                     │
│         │                    │                    │                         │
│         ▼                    ▼                    ▼                         │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐               │
│   │   UI State   │     │ Error Handle │     │   Retrofit   │               │
│   └──────────────┘     └──────────────┘     └──────────────┘               │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### 2. Summary of Best Practices

| Area | Recommendation |
|------|----------------|
| **HTTP Client** | Use OkHttp with proper timeouts and interceptors |
| **REST Client** | Use Retrofit with suspend functions |
| **Serialization** | Use Kotlinx Serialization for new projects |
| **Error Handling** | Use sealed classes (NetworkResult) |
| **Caching** | Implement both memory and disk caching |
| **Security** | Use certificate pinning for sensitive APIs |
| **Image Loading** | Use Coil for Compose, Glide for Views |
| **Testing** | Use MockWebServer for unit tests |
| **Connectivity** | Check network state before requests |
| **Offline Mode** | Cache data and provide offline experience |

### 3. Code Quality Checklist

- [ ] Use dependency injection (Hilt) for network components
- [ ] Implement proper error handling with meaningful messages
- [ ] Add logging in debug builds, remove in release
- [ ] Configure appropriate timeouts
- [ ] Use connection pooling
- [ ] Implement retry logic for transient failures
- [ ] Cache responses where appropriate
- [ ] Handle configuration changes (rotation)
- [ ] Cancel requests when view is destroyed
- [ ] Use proper threading (IO dispatcher for network calls)
- [ ] Implement certificate pinning for security-critical apps
- [ ] Test with MockWebServer
