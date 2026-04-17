# MediaPipe (Google) — Real-Time Perception Pipelines

## Overview

MediaPipe is Google's open-source framework for building **real-time, cross-platform ML perception pipelines**. It provides pre-built solutions for common perception tasks and a graph-based framework for creating custom pipelines.

```
MediaPipe Architecture:
┌──────────────────────────────────────────────────────────────┐
│                     MediaPipe Framework                       │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                  Calculator Graph                       │  │
│  │                                                        │  │
│  │  ┌──────┐    ┌──────────┐    ┌──────────┐    ┌─────┐ │  │
│  │  │Input │───▶│Calculator│───▶│Calculator│───▶│Out  │ │  │
│  │  │Node  │    │  (Resize)│    │  (Model) │    │put  │ │  │
│  │  └──────┘    └──────────┘    └──────────┘    └─────┘ │  │
│  │                    │              │                    │  │
│  │              ┌─────▼──────┐ ┌────▼──────┐            │  │
│  │              │Side Packet │ │Calculator │            │  │
│  │              │(Config)    │ │(Render)   │            │  │
│  │              └────────────┘ └───────────┘            │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Pre-built Solutions:                                        │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌────────┐  │
│  │Hands │ │Face  │ │Pose  │ │Holis-│ │Object│ │Selfie  │  │
│  │Track │ │Mesh  │ │Estim │ │tic   │ │Detec │ │Segment │  │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ └────────┘  │
└──────────────────────────────────────────────────────────────┘

MediaPipe vs ML Kit:
┌──────────────────┬──────────────────┬──────────────────┐
│ Feature          │ MediaPipe        │ ML Kit           │
├──────────────────┼──────────────────┼──────────────────┤
│ Customization    │ High (graphs)    │ Low (pre-built)  │
│ Real-time perf   │ Excellent        │ Good             │
│ Platform         │ Cross-platform   │ Mobile only      │
│ Learning curve   │ Steeper          │ Easy             │
│ Pipeline control │ Full             │ Limited          │
│ Custom models    │ Easy to swap     │ Limited support  │
└──────────────────┴──────────────────┴──────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // MediaPipe Tasks — Vision
    implementation("com.google.mediapipe:tasks-vision:0.10.14")
    
    // MediaPipe Tasks — Text
    implementation("com.google.mediapipe:tasks-text:0.10.14")
    
    // MediaPipe Tasks — Audio
    implementation("com.google.mediapipe:tasks-audio:0.10.14")
    
    // CameraX (for real-time camera input)
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
}
```

---

## 1. Hand Tracking and Gesture Recognition

### Theory

MediaPipe Hands detects **21 hand landmarks** per hand in real-time. The gesture recognizer builds on this to classify hand gestures.

```
21 Hand Landmarks:
                    ┌─ 8 (INDEX_FINGER_TIP)
              7 ───┤
         6 ───┘    │    ┌─ 12 (MIDDLE_FINGER_TIP)
    5 ───┘         │ 11─┤
                   │    │    ┌─ 16 (RING_FINGER_TIP)
                   │ 10─┘ 15─┤
                   │         │    ┌─ 20 (PINKY_TIP)
                   │      14─┘ 19─┤
                   │              │
    4 (THUMB_TIP)  │  9───13──17──18
    │              │  │
    3              │  │
    │              │  │
    2           ───┤  │
    │         /    │  │
    1        0 ────┘──┘
    │     (WRIST)
    │

Hand Detection Pipeline:
┌────────┐   ┌────────────┐   ┌────────────┐   ┌───────────┐
│ Frame  │──▶│ Palm       │──▶│ Hand       │──▶│ Gesture   │
│        │   │ Detection  │   │ Landmark   │   │ Recogn.   │
│        │   │ (BlazePalm)│   │ (21 pts)   │   │ (optional)│
└────────┘   └────────────┘   └────────────┘   └───────────┘

Built-in Gestures:
  👍 Thumbs Up    ✌️ Victory     ☝️ Pointing Up
  👎 Thumbs Down  ✊ Closed Fist  🖐️ Open Palm
  🤟 ILoveYou
```

### Code

```kotlin
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage

class HandTrackingHelper(private val context: Context) {

    private var handLandmarker: HandLandmarker? = null
    private var gestureRecognizer: GestureRecognizer? = null

    // --- Hand Landmark Detection ---
    fun initializeHandLandmarker(
        runningMode: RunningMode = RunningMode.IMAGE,
        onResult: ((HandLandmarkerResult, MPImage) -> Unit)? = null
    ) {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(runningMode)
            .setNumHands(2)  // Detect up to 2 hands
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)

        // For LIVE_STREAM mode, set result listener
        if (runningMode == RunningMode.LIVE_STREAM && onResult != null) {
            optionsBuilder.setResultListener { result, inputImage ->
                onResult(result, inputImage)
            }
            optionsBuilder.setErrorListener { error ->
                Log.e("Hand", "Error: ${error.message}")
            }
        }

        handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
    }

    // Detect in a single image
    fun detectHands(bitmap: Bitmap): HandLandmarkerResult? {
        val mpImage = BitmapImageBuilder(bitmap).build()
        return handLandmarker?.detect(mpImage)
    }

    // Process results
    fun processHandResult(result: HandLandmarkerResult) {
        if (result.landmarks().isEmpty()) {
            Log.d("Hand", "No hands detected")
            return
        }

        for (handIndex in result.landmarks().indices) {
            val landmarks = result.landmarks()[handIndex]
            val handedness = result.handednesses()[handIndex]

            val handLabel = handedness[0].categoryName()  // "Left" or "Right"
            val confidence = handedness[0].score()

            Log.d("Hand", "Hand $handIndex: $handLabel ($confidence)")

            // Access specific landmarks
            val wrist = landmarks[0]                  // Wrist
            val thumbTip = landmarks[4]               // Thumb tip
            val indexTip = landmarks[8]               // Index finger tip
            val middleTip = landmarks[12]             // Middle finger tip

            Log.d("Hand", """
                Wrist: (${wrist.x()}, ${wrist.y()}, ${wrist.z()})
                Index tip: (${indexTip.x()}, ${indexTip.y()})
                Thumb tip: (${thumbTip.x()}, ${thumbTip.y()})
            """.trimIndent())

            // Calculate finger distances (e.g., pinch detection)
            val pinchDistance = calculateDistance(thumbTip, indexTip)
            if (pinchDistance < 0.05f) {
                Log.d("Hand", "PINCH detected!")
            }
        }
    }

    private fun calculateDistance(
        p1: com.google.mediapipe.tasks.components.containers.NormalizedLandmark,
        p2: com.google.mediapipe.tasks.components.containers.NormalizedLandmark
    ): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    // --- Gesture Recognition ---
    fun initializeGestureRecognizer(
        onResult: ((GestureRecognizerResult, MPImage) -> Unit)? = null
    ) {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("gesture_recognizer.task")
            .build()

        val options = GestureRecognizer.GestureRecognizerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumHands(2)
            .build()

        gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
    }

    fun recognizeGesture(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = gestureRecognizer?.recognize(mpImage) ?: return

        for (i in result.gestures().indices) {
            val gestures = result.gestures()[i]
            val topGesture = gestures[0]

            val gestureName = topGesture.categoryName()  // e.g., "Thumb_Up"
            val gestureScore = topGesture.score()

            Log.d("Gesture", "Gesture: $gestureName, Score: $gestureScore")
        }
    }

    fun close() {
        handLandmarker?.close()
        gestureRecognizer?.close()
    }
}
```

---

## 2. Face Mesh

### Theory

MediaPipe Face Mesh detects **478 3D face landmarks** in real-time — far more detailed than ML Kit's 133 contour points.

```
Face Mesh: 478 Landmarks
┌─────────────────────────────────────────┐
│                                         │
│    Face Mesh Regions:                   │
│    ┌─────────────────────────┐          │
│    │       Forehead          │          │
│    │  ┌─────┐     ┌─────┐   │           │
│    │  │Left │     │Right│   │           │
│    │  │ Eye │     │ Eye │   │           │
│    │  │ 16  │     │ 16  │   │  468 face │
│    │  │pts  │     │pts  │   │  landmarks│
│    │  └─────┘     └─────┘   │           │
│    │       ┌─────┐          │  + 10 iris│
│    │       │Nose │          │  landmarks│
│    │       │     │          │           │
│    │    ┌──┴─────┴──┐       │  = 478    │
│    │    │   Lips    │       │  total    │
│    │    │  40 pts   │       │           │
│    │    └───────────┘       │           │
│    │      Jawline           │           │
│    └─────────────────────────┘          │
│                                         │
│  Blendshapes (52 facial expressions):   │
│  - eyeBlinkLeft, eyeBlinkRight          │
│  - jawOpen, mouthSmile                  │
│  - browInnerUp, cheekPuff               │
│  - tongueOut, etc.                      │
│                                         │
│  Face Geometry:                         │
│  - 3D transformation matrix             │
│  - Canonical face mesh                  │
└─────────────────────────────────────────┘
```

### Code

```kotlin
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceMeshHelper(private val context: Context) {

    private var faceLandmarker: FaceLandmarker? = null

    fun initialize(
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(runningMode)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinFacePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setOutputFaceBlendshapes(true)      // Enable blendshapes
            .setOutputFacialTransformationMatrixes(true) // Enable 3D transform
            .build()

        faceLandmarker = FaceLandmarker.createFromOptions(context, options)
    }

    fun detectFaceMesh(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = faceLandmarker?.detect(mpImage) ?: return

        processFaceMeshResult(result)
    }

    private fun processFaceMeshResult(result: FaceLandmarkerResult) {
        if (result.faceLandmarks().isEmpty()) return

        for (faceIndex in result.faceLandmarks().indices) {
            val landmarks = result.faceLandmarks()[faceIndex]

            // 478 landmarks
            Log.d("FaceMesh", "Face $faceIndex: ${landmarks.size} landmarks")

            // Key landmarks
            val noseTip = landmarks[1]       // Nose tip
            val leftEye = landmarks[33]      // Left eye inner corner
            val rightEye = landmarks[263]    // Right eye inner corner
            val upperLip = landmarks[13]     // Upper lip center
            val lowerLip = landmarks[14]     // Lower lip center
            val leftIris = landmarks[468]    // Left iris center
            val rightIris = landmarks[473]   // Right iris center

            Log.d("FaceMesh", """
                Nose tip: (${noseTip.x()}, ${noseTip.y()}, ${noseTip.z()})
                Left eye: (${leftEye.x()}, ${leftEye.y()})
                Left iris: (${leftIris.x()}, ${leftIris.y()})
            """.trimIndent())

            // Blendshapes (facial expressions)
            val blendshapes = result.faceBlendshapes()
            if (blendshapes.isPresent && blendshapes.get().isNotEmpty()) {
                val faceBlendshapes = blendshapes.get()[faceIndex]
                for (blendshape in faceBlendshapes) {
                    if (blendshape.score() > 0.5f) {
                        Log.d("FaceMesh", "Expression: ${blendshape.categoryName()} = ${blendshape.score()}")
                    }
                }

                // Check specific expressions
                val eyeBlinkLeft = faceBlendshapes.find { 
                    it.categoryName() == "eyeBlinkLeft" 
                }?.score() ?: 0f
                
                val mouthSmile = faceBlendshapes.find { 
                    it.categoryName() == "mouthSmileLeft" 
                }?.score() ?: 0f

                if (eyeBlinkLeft > 0.7f) Log.d("FaceMesh", "Left eye blinked!")
                if (mouthSmile > 0.5f) Log.d("FaceMesh", "Smiling!")
            }

            // Face transformation matrix (3D head pose)
            val transforms = result.facialTransformationMatrixes()
            if (transforms.isPresent && transforms.get().isNotEmpty()) {
                val matrix = transforms.get()[faceIndex]
                Log.d("FaceMesh", "Transformation matrix: $matrix")
            }
        }
    }

    fun close() = faceLandmarker?.close()
}
```

---

## 3. Pose Estimation

### Theory

MediaPipe Pose detects **33 body landmarks** with 3D coordinates and segmentation mask.

```
33 Pose Landmarks (same as ML Kit but with world coordinates):

                    0 (nose)
                   / \
  11 (L.shoulder) /   \ 12 (R.shoulder)
                 / │   \
  13 (L.elbow)  /  │    \ 14 (R.elbow)
               /   │     \
  15 (L.wrist)/    │      \ 16 (R.wrist)
             /     │       \
  23 (L.hip)───────┼────────24 (R.hip)
             \     │       /
  25 (L.knee) \    │      / 26 (R.knee)
               \   │     /
  27 (L.ankle)  \  │    / 28 (R.ankle)
                 \ │   /
  29 (L.heel)    \│  / 30 (R.heel)
                  │ /
  31 (L.toe)     │/ 32 (R.toe)

Two coordinate systems:
  1. Normalized (0-1): Relative to image dimensions
  2. World (meters): Real-world 3D coordinates relative to hip
```

### Code

```kotlin
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseEstimationHelper(private val context: Context) {

    private var poseLandmarker: PoseLandmarker? = null

    fun initialize(runningMode: RunningMode = RunningMode.IMAGE) {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_full.task")
            // Options: pose_landmarker_lite.task, pose_landmarker_full.task, pose_landmarker_heavy.task
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(runningMode)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setOutputSegmentationMasks(true)  // Enable body segmentation
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detectPose(bitmap: Bitmap): PoseLandmarkerResult? {
        val mpImage = BitmapImageBuilder(bitmap).build()
        return poseLandmarker?.detect(mpImage)
    }

    fun processPoseResult(result: PoseLandmarkerResult) {
        if (result.landmarks().isEmpty()) return

        val landmarks = result.landmarks()[0]   // First person
        val worldLandmarks = result.worldLandmarks()[0]  // 3D world coords

        // Normalized landmarks (0-1, relative to image)
        val leftShoulder = landmarks[11]
        val rightShoulder = landmarks[12]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]

        // World landmarks (meters, relative to hip center)
        val leftShoulderWorld = worldLandmarks[11]
        val rightShoulderWorld = worldLandmarks[12]

        Log.d("Pose", """
            Left shoulder (normalized): (${leftShoulder.x()}, ${leftShoulder.y()})
            Left shoulder (world, m): (${leftShoulderWorld.x()}, ${leftShoulderWorld.y()}, ${leftShoulderWorld.z()})
        """.trimIndent())

        // Calculate body measurements
        val shoulderWidth = calculateDistance3D(leftShoulderWorld, rightShoulderWorld)
        Log.d("Pose", "Shoulder width: ${shoulderWidth * 100} cm")

        // Exercise rep counting example
        val leftElbow = landmarks[13]
        val leftWrist = landmarks[15]
        val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
        Log.d("Pose", "Left elbow angle: $elbowAngle°")

        // Segmentation mask
        val masks = result.segmentationMasks()
        if (masks.isPresent && masks.get().isNotEmpty()) {
            val mask = masks.get()[0]
            Log.d("Pose", "Mask size: ${mask.width}x${mask.height}")
        }
    }

    private fun calculateDistance3D(
        p1: com.google.mediapipe.tasks.components.containers.Landmark,
        p2: com.google.mediapipe.tasks.components.containers.Landmark
    ): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        val dz = p1.z() - p2.z()
        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    private fun calculateAngle(
        p1: com.google.mediapipe.tasks.components.containers.NormalizedLandmark,
        p2: com.google.mediapipe.tasks.components.containers.NormalizedLandmark,
        p3: com.google.mediapipe.tasks.components.containers.NormalizedLandmark
    ): Double {
        val angle = Math.toDegrees(
            Math.atan2(
                (p3.y() - p2.y()).toDouble(),
                (p3.x() - p2.x()).toDouble()
            ) - Math.atan2(
                (p1.y() - p2.y()).toDouble(),
                (p1.x() - p2.x()).toDouble()
            )
        )
        var result = Math.abs(angle)
        if (result > 180) result = 360.0 - result
        return result
    }

    fun close() = poseLandmarker?.close()
}
```

---

## 4. Holistic Tracking

### Theory

Holistic tracking combines face mesh, pose estimation, and hand tracking into a single unified pipeline.

```
Holistic Tracking Pipeline:
┌──────────┐
│  Video   │
│  Frame   │
└────┬─────┘
     │
     ▼
┌────────────────────────────────────────────────────┐
│              Holistic Pipeline                      │
│                                                    │
│  ┌─────────────┐   ┌──────────────┐               │
│  │ Person       │   │ ROI Tracking │               │
│  │ Detection    │──▶│              │               │
│  └─────────────┘   └──────┬───────┘               │
│                           │                        │
│           ┌───────────────┼───────────────┐        │
│           ▼               ▼               ▼        │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │ Face Mesh  │  │   Pose     │  │   Hand     │   │
│  │ 478 pts    │  │  33 pts    │  │  21 pts x2 │   │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘   │
│        │               │               │          │
│        ▼               ▼               ▼          │
│  ┌──────────────────────────────────────────┐      │
│  │         Combined Result                  │      │
│  │  Face: 478 landmarks                     │      │
│  │  Pose: 33 landmarks + world coords       │      │
│  │  Left Hand: 21 landmarks                 │      │
│  │  Right Hand: 21 landmarks                │      │
│  │  Total: 553+ landmarks per frame         │      │
│  └──────────────────────────────────────────┘      │
└────────────────────────────────────────────────────┘
```

### Code

```kotlin
// Holistic tracking is achieved by combining Face + Pose + Hands
// In MediaPipe Tasks API, you run them together

class HolisticTrackingHelper(private val context: Context) {

    private var faceLandmarker: FaceLandmarker? = null
    private var poseLandmarker: PoseLandmarker? = null
    private var handLandmarker: HandLandmarker? = null

    fun initialize() {
        // Initialize all three
        val faceOptions = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build())
            .setRunningMode(RunningMode.IMAGE)
            .setNumFaces(1)
            .setOutputFaceBlendshapes(true)
            .build()
        faceLandmarker = FaceLandmarker.createFromOptions(context, faceOptions)

        val poseOptions = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("pose_landmarker_full.task").build())
            .setRunningMode(RunningMode.IMAGE)
            .setNumPoses(1)
            .build()
        poseLandmarker = PoseLandmarker.createFromOptions(context, poseOptions)

        val handOptions = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
            .setRunningMode(RunningMode.IMAGE)
            .setNumHands(2)
            .build()
        handLandmarker = HandLandmarker.createFromOptions(context, handOptions)
    }

    data class HolisticResult(
        val faceResult: FaceLandmarkerResult?,
        val poseResult: PoseLandmarkerResult?,
        val handResult: HandLandmarkerResult?
    )

    fun detectHolistic(bitmap: Bitmap): HolisticResult {
        val mpImage = BitmapImageBuilder(bitmap).build()

        return HolisticResult(
            faceResult = faceLandmarker?.detect(mpImage),
            poseResult = poseLandmarker?.detect(mpImage),
            handResult = handLandmarker?.detect(mpImage)
        )
    }

    fun close() {
        faceLandmarker?.close()
        poseLandmarker?.close()
        handLandmarker?.close()
    }
}
```

---

## 5. Object Detection

### Theory

MediaPipe Object Detection identifies and localizes multiple objects with bounding boxes.

```
Object Detection Output:
┌──────────────────────────────────────┐
│  ┌──────────────┐                    │
│  │  Person  0.95│                    │
│  │              │    ┌────────┐      │
│  │              │    │Car 0.88│      │
│  │              │    │        │      │
│  └──────────────┘    └────────┘      │
│           ┌─────────┐                │
│           │Dog  0.91│                │
│           └─────────┘                │
│                                      │
│  Each detection:                     │
│  - BoundingBox (x, y, width, height) │
│  - Category (label + score)          │
│  - Multiple categories possible      │
└──────────────────────────────────────┘
```

### Code

```kotlin
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

class MPObjectDetector(private val context: Context) {

    private var objectDetector: ObjectDetector? = null

    fun initialize() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("efficientdet_lite0.tflite")
            .build()

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(10)
            .setScoreThreshold(0.5f)
            .setCategoryAllowlist(listOf("person", "car", "dog"))  // Optional filter
            .build()

        objectDetector = ObjectDetector.createFromOptions(context, options)
    }

    fun detect(bitmap: Bitmap): ObjectDetectorResult? {
        val mpImage = BitmapImageBuilder(bitmap).build()
        return objectDetector?.detect(mpImage)
    }

    fun processResults(result: ObjectDetectorResult) {
        for (detection in result.detections()) {
            val bbox = detection.boundingBox()

            Log.d("ObjectDet", "BBox: (${bbox.left}, ${bbox.top}, ${bbox.right}, ${bbox.bottom})")

            for (category in detection.categories()) {
                Log.d("ObjectDet", "  ${category.categoryName()}: ${category.score()}")
            }
        }
    }

    // Real-time with CameraX
    fun initializeLiveStream(
        onResult: (ObjectDetectorResult, MPImage) -> Unit
    ) {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("efficientdet_lite0.tflite")
            .build()

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMaxResults(5)
            .setScoreThreshold(0.5f)
            .setResultListener { result, image -> onResult(result, image) }
            .setErrorListener { error -> Log.e("ObjectDet", error.message ?: "Error") }
            .build()

        objectDetector = ObjectDetector.createFromOptions(context, options)
    }

    fun detectAsync(mpImage: MPImage, timestampMs: Long) {
        objectDetector?.detectAsync(mpImage, timestampMs)
    }

    fun close() = objectDetector?.close()
}
```

---

## 6. Custom Graph Creation

### Theory

MediaPipe's graph-based framework lets you build custom perception pipelines by connecting processing nodes (calculators).

```
Custom Graph Concept:
┌──────────────────────────────────────────────────┐
│                Custom Graph (.pbtxt)              │
│                                                  │
│  input_stream: "input_video"                     │
│                                                  │
│  ┌─────────────┐    ┌─────────────┐              │
│  │ ImageTransf  │───▶│ TfLiteInfer │              │
│  │ ormation     │    │ ence        │              │
│  │ Calculator   │    │ Calculator  │              │
│  └──────┬──────┘    └──────┬──────┘              │
│         │                   │                    │
│         │           ┌──────▼──────┐              │
│         │           │ Detection    │              │
│         │           │ PostProcess  │              │
│         │           │ Calculator   │              │
│         │           └──────┬──────┘              │
│         │                  │                     │
│         │   ┌──────────────▼──────────┐          │
│         └──▶│ Annotation Overlay      │          │
│             │ Calculator              │          │
│             └──────────────┬──────────┘          │
│                            │                     │
│  output_stream: "output_video"                   │
└──────────────────────────────────────────────────┘

Calculator Types:
┌──────────────────────────────────────────────────┐
│ Input Calculators:                               │
│   - ImageTransformationCalculator                │
│   - ImageToTensorCalculator                      │
│                                                  │
│ Inference Calculators:                           │
│   - TfLiteInferenceCalculator                    │
│   - InferenceCalculator                          │
│                                                  │
│ Processing Calculators:                          │
│   - DetectionPostProcessCalculator               │
│   - NonMaxSuppressionCalculator                  │
│   - LandmarkProjectionCalculator                 │
│                                                  │
│ Output Calculators:                              │
│   - AnnotationOverlayCalculator                  │
│   - DetectionsToRenderDataCalculator             │
└──────────────────────────────────────────────────┘
```

### Graph Definition (ProtoBuf Text)

```protobuf
# custom_graph.pbtxt
# Custom object detection pipeline

input_stream: "input_video"
output_stream: "output_video"
output_stream: "detections"

# Step 1: Resize and preprocess image
node {
  calculator: "ImageTransformationCalculator"
  input_stream: "IMAGE:input_video"
  output_stream: "IMAGE:transformed_input"
  node_options: {
    [type.googleapis.com/mediapipe.ImageTransformationCalculatorOptions] {
      output_width: 320
      output_height: 320
    }
  }
}

# Step 2: Convert to tensor
node {
  calculator: "ImageToTensorCalculator" 
  input_stream: "IMAGE:transformed_input"
  output_stream: "TENSORS:input_tensor"
  node_options: {
    [type.googleapis.com/mediapipe.ImageToTensorCalculatorOptions] {
      output_tensor_float_range { min: 0.0 max: 1.0 }
    }
  }
}

# Step 3: Run TFLite model
node {
  calculator: "InferenceCalculator"
  input_stream: "TENSORS:input_tensor"
  output_stream: "TENSORS:output_tensors"
  node_options: {
    [type.googleapis.com/mediapipe.InferenceCalculatorOptions] {
      model_path: "custom_model.tflite"
      delegate { gpu {} }
    }
  }
}

# Step 4: Post-process detections
node {
  calculator: "DetectionPostProcessCalculator"
  input_stream: "TENSORS:output_tensors"
  output_stream: "DETECTIONS:detections"
}

# Step 5: Render on original image
node {
  calculator: "DetectionsToRenderDataCalculator"
  input_stream: "DETECTIONS:detections"
  output_stream: "RENDER_DATA:render_data"
}

node {
  calculator: "AnnotationOverlayCalculator"
  input_stream: "IMAGE:input_video"
  input_stream: "render_data"
  output_stream: "IMAGE:output_video"
}
```

### Using Custom Models with MediaPipe Tasks

```kotlin
// You can swap custom models into any MediaPipe Task
class CustomModelPipeline(private val context: Context) {

    // Custom image classifier
    fun createCustomClassifier(): com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("my_custom_classifier.tflite")
            .setDelegate(com.google.mediapipe.tasks.core.Delegate.GPU)
            .build()

        val options = com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
            .ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(5)
            .setScoreThreshold(0.3f)
            .build()

        return com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
            .createFromOptions(context, options)
    }

    // Custom text classifier
    fun createCustomTextClassifier(): com.google.mediapipe.tasks.text.textclassifier.TextClassifier {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("my_text_classifier.tflite")
            .build()

        val options = com.google.mediapipe.tasks.text.textclassifier.TextClassifier
            .TextClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .build()

        return com.google.mediapipe.tasks.text.textclassifier.TextClassifier
            .createFromOptions(context, options)
    }
}
```

---

## CameraX Integration for Real-Time MediaPipe

```kotlin
class MediaPipeCameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var handLandmarker: HandLandmarker? = null

    fun startCamera(previewView: PreviewView, overlayView: OverlayView) {
        // Initialize MediaPipe in LIVE_STREAM mode
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .setDelegate(com.google.mediapipe.tasks.core.Delegate.GPU)
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)
            .setResultListener { result, inputImage ->
                // Update UI on main thread
                overlayView.post {
                    overlayView.setResults(result, inputImage.width, inputImage.height)
                    overlayView.invalidate()
                }
            }
            .setErrorListener { error ->
                Log.e("Camera", "MediaPipe error: ${error.message}")
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)

        // Set up CameraX
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(
                        java.util.concurrent.Executors.newSingleThreadExecutor()
                    ) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        val mpImage = BitmapImageBuilder(bitmap).build()
                        val timestampMs = imageProxy.imageInfo.timestamp / 1000

                        handLandmarker?.detectAsync(mpImage, timestampMs)
                        imageProxy.close()
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    fun close() = handLandmarker?.close()
}
```
