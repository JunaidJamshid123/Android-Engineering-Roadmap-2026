# NDK (Native Development Kit)

## Overview

The Android NDK lets you write **performance-critical** portions of your app in **C/C++**. These native functions are called from Kotlin/Java via **JNI (Java Native Interface)**. The NDK is NOT for entire apps — it's for specific bottlenecks where native code provides a clear advantage.

```
┌──────────────────────────────────────────────────────────────┐
│                   NDK Architecture                            │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────┐                            │
│  │      Kotlin / Java Layer     │                            │
│  │  ┌────────────────────────┐  │                            │
│  │  │  ViewModel / Activity  │  │                            │
│  │  │  Repository            │  │                            │
│  │  │  Business Logic        │  │                            │
│  │  └───────────┬────────────┘  │                            │
│  │              │               │                            │
│  │  ┌───────────▼────────────┐  │                            │
│  │  │  external fun nativeX()│  │  ← JNI declarations       │
│  │  │  System.loadLibrary()  │  │                            │
│  │  └───────────┬────────────┘  │                            │
│  └──────────────┼───────────────┘                            │
│                 │  JNI Boundary                               │
│  ┌──────────────▼───────────────┐                            │
│  │      Native C/C++ Layer      │                            │
│  │  ┌────────────────────────┐  │                            │
│  │  │  JNIEXPORT functions   │  │  ← JNI implementations    │
│  │  │  Image processing      │  │                            │
│  │  │  Signal processing     │  │                            │
│  │  │  Physics engines       │  │                            │
│  │  │  Crypto operations     │  │                            │
│  │  └────────────────────────┘  │                            │
│  │                               │                            │
│  │  ┌────────────────────────┐  │                            │
│  │  │  libnative-lib.so      │  │  ← Compiled .so library   │
│  │  └────────────────────────┘  │                            │
│  └───────────────────────────────┘                            │
│                                                               │
│  Supported ABIs:                                              │
│  ┌────────────┬────────────┬──────────────┬────────────┐     │
│  │ armeabi-v7a│  arm64-v8a │   x86        │  x86_64    │     │
│  │ (32-bit    │ (64-bit    │ (Emulator    │ (Emulator  │     │
│  │  ARM)      │  ARM)      │  x86)        │  x86_64)   │     │
│  └────────────┴────────────┴──────────────┴────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. JNI (Java Native Interface)

### Project Structure

```
app/
├── src/main/
│   ├── java/com/example/app/
│   │   └── NativeLib.kt           ← JNI declarations
│   ├── cpp/
│   │   ├── CMakeLists.txt          ← Build config
│   │   ├── native-lib.cpp          ← C++ implementation
│   │   ├── image_processor.cpp
│   │   ├── image_processor.h
│   │   └── utils/
│   │       ├── math_utils.cpp
│   │       └── math_utils.h
│   └── jniLibs/                    ← Pre-built .so files (optional)
│       ├── arm64-v8a/
│       ├── armeabi-v7a/
│       ├── x86/
│       └── x86_64/
```

### Kotlin Side: Declaring Native Functions

```kotlin
// app/src/main/java/com/example/app/NativeLib.kt

class NativeLib {

    companion object {
        init {
            // Load the native library (libnative-lib.so)
            System.loadLibrary("native-lib")
        }
    }

    // ---- Basic types ----
    external fun stringFromJNI(): String
    external fun addNumbers(a: Int, b: Int): Int
    external fun calculatePi(iterations: Long): Double

    // ---- Arrays ----
    external fun processArray(input: IntArray): IntArray
    external fun applyFilter(pixels: IntArray, width: Int, height: Int): IntArray

    // ---- Strings ----
    external fun encryptString(input: String, key: String): String
    external fun decryptString(input: String, key: String): String

    // ---- Byte arrays (common for image/audio data) ----
    external fun compressData(data: ByteArray): ByteArray
    external fun processImageBuffer(
        buffer: ByteArray,
        width: Int,
        height: Int,
        brightness: Float
    ): ByteArray

    // ---- Objects ----
    external fun createNativeObject(): Long   // Returns a native pointer
    external fun destroyNativeObject(ptr: Long)
    external fun processWithObject(ptr: Long, data: FloatArray): FloatArray

    // ---- Callbacks (calling Kotlin from C++) ----
    external fun startProcessing(callback: ProcessingCallback)

    interface ProcessingCallback {
        fun onProgress(percent: Int)
        fun onComplete(result: String)
        fun onError(errorCode: Int, message: String)
    }
}
```

### Usage from Kotlin

```kotlin
class ImageActivity : AppCompatActivity() {

    private val nativeLib = NativeLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple call
        val greeting = nativeLib.stringFromJNI()
        val sum = nativeLib.addNumbers(42, 58) // 100

        // Process image natively (much faster than Kotlin for pixel manipulation)
        lifecycleScope.launch(Dispatchers.Default) {
            val bitmap = loadBitmap()
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val filtered = nativeLib.applyFilter(pixels, bitmap.width, bitmap.height)

            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            result.setPixels(filtered, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(result)
            }
        }

        // With callbacks
        nativeLib.startProcessing(object : NativeLib.ProcessingCallback {
            override fun onProgress(percent: Int) {
                progressBar.progress = percent
            }
            override fun onComplete(result: String) {
                textView.text = result
            }
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@ImageActivity, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
```

### C++ Side: JNI Implementation

```cpp
// app/src/main/cpp/native-lib.cpp

#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ============================================================
// JNI Naming Convention:
//   Java_<package>_<class>_<method>
//   Dots in package replaced with underscores
// ============================================================

extern "C" {

// ---- Simple String Return ----
JNIEXPORT jstring JNICALL
Java_com_example_app_NativeLib_stringFromJNI(
    JNIEnv *env,
    jobject /* this */
) {
    std::string hello = "Hello from C++!";
    return env->NewStringUTF(hello.c_str());
}

// ---- Arithmetic ----
JNIEXPORT jint JNICALL
Java_com_example_app_NativeLib_addNumbers(
    JNIEnv *env,
    jobject /* this */,
    jint a,
    jint b
) {
    return a + b;
}

// ---- CPU-Intensive Calculation ----
JNIEXPORT jdouble JNICALL
Java_com_example_app_NativeLib_calculatePi(
    JNIEnv *env,
    jobject /* this */,
    jlong iterations
) {
    double pi = 0.0;
    for (long i = 0; i < iterations; i++) {
        double term = (i % 2 == 0 ? 1.0 : -1.0) / (2.0 * i + 1.0);
        pi += term;
    }
    return pi * 4.0;
}

// ---- Array Processing ----
JNIEXPORT jintArray JNICALL
Java_com_example_app_NativeLib_processArray(
    JNIEnv *env,
    jobject /* this */,
    jintArray input
) {
    // Get array length
    jsize length = env->GetArrayLength(input);

    // Get a pointer to the array elements
    jint *inputElements = env->GetIntArrayElements(input, nullptr);

    // Create result array
    jintArray result = env->NewIntArray(length);
    jint *resultElements = env->GetIntArrayElements(result, nullptr);

    // Process: square each element
    for (int i = 0; i < length; i++) {
        resultElements[i] = inputElements[i] * inputElements[i];
    }

    // Release arrays
    env->ReleaseIntArrayElements(input, inputElements, JNI_ABORT); // JNI_ABORT = don't copy back
    env->ReleaseIntArrayElements(result, resultElements, 0);        // 0 = copy back changes

    return result;
}

// ---- Image Filter (Grayscale) ----
JNIEXPORT jintArray JNICALL
Java_com_example_app_NativeLib_applyFilter(
    JNIEnv *env,
    jobject /* this */,
    jintArray pixels,
    jint width,
    jint height
) {
    jsize length = env->GetArrayLength(pixels);
    jint *pixelData = env->GetIntArrayElements(pixels, nullptr);

    jintArray result = env->NewIntArray(length);
    jint *resultData = env->GetIntArrayElements(result, nullptr);

    for (int i = 0; i < length; i++) {
        int pixel = pixelData[i];
        int a = (pixel >> 24) & 0xFF;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;

        // Grayscale conversion (luminance weights)
        int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

        resultData[i] = (a << 24) | (gray << 16) | (gray << 8) | gray;
    }

    env->ReleaseIntArrayElements(pixels, pixelData, JNI_ABORT);
    env->ReleaseIntArrayElements(result, resultData, 0);

    return result;
}

// ---- String Processing ----
JNIEXPORT jstring JNICALL
Java_com_example_app_NativeLib_encryptString(
    JNIEnv *env,
    jobject /* this */,
    jstring input,
    jstring key
) {
    const char *inputStr = env->GetStringUTFChars(input, nullptr);
    const char *keyStr = env->GetStringUTFChars(key, nullptr);

    std::string result(inputStr);
    size_t keyLen = strlen(keyStr);

    // Simple XOR cipher (demo only — not secure!)
    for (size_t i = 0; i < result.length(); i++) {
        result[i] = result[i] ^ keyStr[i % keyLen];
    }

    // Release strings
    env->ReleaseStringUTFChars(input, inputStr);
    env->ReleaseStringUTFChars(key, keyStr);

    return env->NewStringUTF(result.c_str());
}

// ---- Calling Kotlin From C++ (Callbacks) ----
JNIEXPORT void JNICALL
Java_com_example_app_NativeLib_startProcessing(
    JNIEnv *env,
    jobject /* this */,
    jobject callback
) {
    // Get callback class and methods
    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onProgress = env->GetMethodID(callbackClass, "onProgress", "(I)V");
    jmethodID onComplete = env->GetMethodID(callbackClass, "onComplete", "(Ljava/lang/String;)V");
    jmethodID onError = env->GetMethodID(callbackClass, "onError", "(ILjava/lang/String;)V");

    // Simulate processing
    for (int i = 0; i <= 100; i += 10) {
        // Call onProgress(i)
        env->CallVoidMethod(callback, onProgress, (jint)i);
    }

    // Call onComplete("Processing done!")
    jstring result = env->NewStringUTF("Processing done!");
    env->CallVoidMethod(callback, onComplete, result);
}

} // extern "C"
```

---

## 2. JNI Type Mapping

```
┌──────────────────────────────────────────────────────────┐
│              JNI Type Mapping Reference                    │
├──────────────┬────────────────┬───────────────────────────┤
│  Kotlin/Java │   JNI Type     │   C/C++ Type              │
├──────────────┼────────────────┼───────────────────────────┤
│  Boolean     │  jboolean      │  unsigned char             │
│  Byte        │  jbyte         │  signed char               │
│  Char        │  jchar         │  unsigned short            │
│  Short       │  jshort        │  short                     │
│  Int         │  jint          │  int                       │
│  Long        │  jlong         │  long long                 │
│  Float       │  jfloat        │  float                     │
│  Double      │  jdouble       │  double                    │
│  String      │  jstring       │  (JNI function to convert) │
│  Object      │  jobject       │  (JNI function to access)  │
│  IntArray    │  jintArray     │  (JNI function to access)  │
│  ByteArray   │  jbyteArray    │  (JNI function to access)  │
│  FloatArray  │  jfloatArray   │  (JNI function to access)  │
│  Class       │  jclass        │  —                         │
│  void        │  void          │  void                      │
├──────────────┴────────────────┴───────────────────────────┤
│                                                            │
│  JNI Method Signature Format:                              │
│  "(paramTypes)returnType"                                  │
│                                                            │
│  V = void    I = int       J = long       F = float        │
│  D = double  Z = boolean   B = byte       S = short        │
│  C = char                                                  │
│  L<class>;  = object  (e.g., Ljava/lang/String;)           │
│  [I = int[]   [B = byte[]   [Ljava/lang/String; = String[] │
│                                                            │
│  Examples:                                                  │
│   (II)I           → int add(int a, int b)                  │
│   (Ljava/lang/String;)V → void setName(String name)       │
│   ()[B            → byte[] getData()                       │
└────────────────────────────────────────────────────────────┘
```

---

## 3. CMake Build Configuration

### `CMakeLists.txt`

```cmake
# app/src/main/cpp/CMakeLists.txt

cmake_minimum_required(VERSION 3.22.1)

project("myapp")

# ---- Set C++ standard ----
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ---- Add main native library ----
add_library(
    native-lib           # Library name → libnative-lib.so
    SHARED               # Shared library (.so)
    native-lib.cpp       # Source files
    image_processor.cpp
    utils/math_utils.cpp
)

# ---- Include directories ----
target_include_directories(native-lib PRIVATE
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/utils
)

# ---- Link system libraries ----
find_library(log-lib log)        # Android logging
find_library(jnigraphics-lib jnigraphics)  # Bitmap processing

target_link_libraries(
    native-lib
    ${log-lib}
    ${jnigraphics-lib}
    android            # For AAssetManager etc.
    z                  # zlib compression
)

# ---- Pre-built third-party library (optional) ----
# add_library(opencv SHARED IMPORTED)
# set_target_properties(opencv PROPERTIES
#     IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so
# )
# target_link_libraries(native-lib opencv)

# ---- Compiler flags ----
target_compile_options(native-lib PRIVATE
    -Wall               # Enable warnings
    -O2                 # Optimization level
    -fexceptions        # Enable C++ exceptions
)
```

### `app/build.gradle.kts` — NDK Configuration

```kotlin
android {
    namespace = "com.example.app"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        // Specify which ABIs to build for
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        // CMake arguments
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",  // STL variant
                    "-DANDROID_TOOLCHAIN=clang"
                )
            }
        }
    }

    // Point to CMakeLists.txt
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            // Strip debug symbols for smaller APK
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }

    // Build variants per ABI (optional, for smaller APKs)
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }
}
```

---

## 4. When to Use Native Code

```
┌──────────────────────────────────────────────────────────────┐
│                When to Use NDK (Decision Tree)                │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Is performance CRITICAL and measurably slow in Kotlin/Java?  │
│        │                                                      │
│    ┌───▼───┐                                                  │
│    │  YES  │──────────────────────────────────┐               │
│    └───────┘                                  │               │
│        │                                      │               │
│        ▼                                      ▼               │
│  Is it CPU-bound?                    Is it an existing        │
│  (not I/O-bound)                     C/C++ library?           │
│        │                                      │               │
│    ┌───▼───┐                             ┌────▼───┐          │
│    │  YES  │──► USE NDK ✅               │  YES   │──► USE   │
│    └───────┘                             └────────┘   NDK ✅  │
│        │                                                      │
│    ┌───▼───┐                                                  │
│    │  NO   │──► Stay with Kotlin ❌                           │
│    └───────┘                                                  │
│                                                               │
│    ┌───▼───┐                                                  │
│    │  NO   │──► Stay with Kotlin ❌                           │
│    └───────┘    (Most tasks don't need NDK)                   │
│                                                               │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ✅ GOOD USE CASES:              ❌ BAD USE CASES:            │
│  ┌─────────────────────┐        ┌────────────────────────┐   │
│  │ Real-time image/     │        │ UI logic               │   │
│  │   video processing   │        │ Networking             │   │
│  │ Audio DSP (effects,  │        │ Database operations    │   │
│  │   synthesis)         │        │ Simple CRUD apps       │   │
│  │ Game engines /       │        │ Business logic         │   │
│  │   physics simulation │        │ File I/O               │   │
│  │ Cryptography         │        │ String manipulation    │   │
│  │ Machine learning     │        │ JSON parsing           │   │
│  │   inference          │        │ "It might be faster"   │   │
│  │ Codecs (encode/      │        │   without benchmarks   │   │
│  │   decode)            │        │                        │   │
│  │ Porting existing     │        │                        │   │
│  │   C/C++ libraries    │        │                        │   │
│  │ Signal processing    │        │                        │   │
│  └─────────────────────┘        └────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## 5. Performance Critical Operations — Real Examples

### Example 1: Fast Image Blur (Native)

```cpp
// app/src/main/cpp/image_processor.cpp

#include <jni.h>
#include <android/bitmap.h>
#include <cstring>
#include <algorithm>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_app_NativeLib_blurBitmap(
    JNIEnv *env,
    jobject /* this */,
    jobject bitmap,
    jint radius
) {
    AndroidBitmapInfo info;
    void *pixels;

    // Lock the bitmap pixels
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    int width = info.width;
    int height = info.height;

    auto *src = static_cast<uint32_t *>(pixels);
    auto *tmp = new uint32_t[width * height];

    // Horizontal pass (box blur)
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int r = 0, g = 0, b = 0, count = 0;

            for (int dx = -radius; dx <= radius; dx++) {
                int nx = std::clamp(x + dx, 0, width - 1);
                uint32_t pixel = src[y * width + nx];
                r += (pixel >> 16) & 0xFF;
                g += (pixel >> 8) & 0xFF;
                b += pixel & 0xFF;
                count++;
            }

            tmp[y * width + x] = (0xFF << 24) |
                                  ((r / count) << 16) |
                                  ((g / count) << 8) |
                                  (b / count);
        }
    }

    // Vertical pass
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            int r = 0, g = 0, b = 0, count = 0;

            for (int dy = -radius; dy <= radius; dy++) {
                int ny = std::clamp(y + dy, 0, height - 1);
                uint32_t pixel = tmp[ny * width + x];
                r += (pixel >> 16) & 0xFF;
                g += (pixel >> 8) & 0xFF;
                b += pixel & 0xFF;
                count++;
            }

            src[y * width + x] = (0xFF << 24) |
                                  ((r / count) << 16) |
                                  ((g / count) << 8) |
                                  (b / count);
        }
    }

    delete[] tmp;

    // Unlock bitmap
    AndroidBitmap_unlockPixels(env, bitmap);
}
```

### Example 2: Audio Processing

```cpp
// app/src/main/cpp/audio_processor.cpp

#include <jni.h>
#include <cmath>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_app_NativeLib_applyGain(
    JNIEnv *env,
    jobject /* this */,
    jfloatArray audioBuffer,
    jfloat gainDb
) {
    jsize length = env->GetArrayLength(audioBuffer);
    jfloat *samples = env->GetFloatArrayElements(audioBuffer, nullptr);

    float gainLinear = powf(10.0f, gainDb / 20.0f);

    for (int i = 0; i < length; i++) {
        samples[i] *= gainLinear;
        // Clamp to prevent clipping
        samples[i] = fmaxf(-1.0f, fminf(1.0f, samples[i]));
    }

    env->ReleaseFloatArrayElements(audioBuffer, samples, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_app_NativeLib_applyLowPassFilter(
    JNIEnv *env,
    jobject /* this */,
    jfloatArray audioBuffer,
    jfloat cutoffFreq,
    jint sampleRate
) {
    jsize length = env->GetArrayLength(audioBuffer);
    jfloat *samples = env->GetFloatArrayElements(audioBuffer, nullptr);

    float rc = 1.0f / (2.0f * M_PI * cutoffFreq);
    float dt = 1.0f / (float)sampleRate;
    float alpha = dt / (rc + dt);

    for (int i = 1; i < length; i++) {
        samples[i] = samples[i - 1] + alpha * (samples[i] - samples[i - 1]);
    }

    env->ReleaseFloatArrayElements(audioBuffer, samples, 0);
}
```

---

## 6. Memory Management Best Practices

```cpp
// CRITICAL: JNI memory management rules

extern "C"
JNIEXPORT void JNICALL
Java_com_example_app_NativeLib_memoryDemo(JNIEnv *env, jobject thiz) {

    // ---- 1. ALWAYS release local references in loops ----
    for (int i = 0; i < 1000; i++) {
        jstring str = env->NewStringUTF("temp");
        // Use str...
        env->DeleteLocalRef(str);  // ✅ Release to avoid table overflow
    }

    // ---- 2. ALWAYS release array elements ----
    jintArray arr = env->NewIntArray(100);
    jint *elements = env->GetIntArrayElements(arr, nullptr);
    // ... use elements ...
    env->ReleaseIntArrayElements(arr, elements, 0);  // ✅

    // ---- 3. ALWAYS release string chars ----
    // const char *str = env->GetStringUTFChars(jstr, nullptr);
    // ... use str ...
    // env->ReleaseStringUTFChars(jstr, str);  // ✅

    // ---- 4. Use global refs for persistent objects ----
    // jobject globalRef = env->NewGlobalRef(localRef);
    // ... store globalRef for later use ...
    // env->DeleteGlobalRef(globalRef);  // When done

    // ---- 5. Check for exceptions after JNI calls ----
    // env->CallVoidMethod(obj, method);
    // if (env->ExceptionCheck()) {
    //     env->ExceptionDescribe();
    //     env->ExceptionClear();
    //     return;
    // }
}
```

---

## 7. NDK Cost-Benefit Summary

```
┌────────────────────────────────────────────────────────────┐
│                   NDK Trade-offs                            │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  BENEFITS:                          COSTS:                  │
│  ┌───────────────────────┐         ┌──────────────────────┐│
│  │ ✅ Better CPU perf for │         │ ❌ Harder to debug    ││
│  │    compute-heavy tasks │         │ ❌ More complex build ││
│  │ ✅ Reuse existing C/C++│         │ ❌ JNI boilerplate    ││
│  │    libraries           │         │ ❌ Manual memory mgmt ││
│  │ ✅ SIMD/NEON support   │         │ ❌ No garbage collect ││
│  │ ✅ Low-level HW access │         │ ❌ Larger APK size    ││
│  │ ✅ Predictable perf    │         │ ❌ Security risks     ││
│  │    (no GC pauses)      │         │    (buffer overflows) ││
│  └───────────────────────┘         └──────────────────────┘│
│                                                             │
│  Rule of thumb: Profile first in Kotlin. Only use NDK       │
│  when you have MEASURED evidence of a bottleneck.           │
└────────────────────────────────────────────────────────────┘
```
