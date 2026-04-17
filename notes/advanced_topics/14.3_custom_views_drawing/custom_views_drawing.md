# Custom Views and Drawing

## Overview

Custom Views let you create entirely new UI components by extending Android's `View` class and drawing directly on a `Canvas`. This is essential when built-in widgets don't meet your design needs.

```
┌─────────────────────────────────────────────────────────────┐
│                  Custom View Lifecycle                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Constructor(Context, AttributeSet)                          │
│       │                                                      │
│       ▼                                                      │
│  onAttachedToWindow()  ← View added to window               │
│       │                                                      │
│       ▼                                                      │
│  ┌─── onMeasure() ◄────────────────────────┐                │
│  │    (Determine size)                      │                │
│  │         │                                │                │
│  │         ▼                                │                │
│  │    onSizeChanged()                       │  requestLayout()
│  │    (Size finalized)                      │                │
│  │         │                                │                │
│  │         ▼                                │                │
│  │    onLayout()                            │                │
│  │    (Position children — ViewGroup)       │                │
│  │         │                                │                │
│  │         ▼                                │                │
│  └──► onDraw(canvas) ◄─── invalidate()     │                │
│       (Render the view)     (Redraw)        │                │
│            │                                 │                │
│            ▼                                 │                │
│       View Displayed                         │                │
│                                              │                │
│  onDetachedFromWindow() ← View removed       │                │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. Extending the View Class

### Basic Custom View Structure

```kotlin
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ---- Paints ----
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.LTGRAY
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.BLUE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 64f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    // ---- Properties ----
    private var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate() // Redraw when progress changes
        }

    private val rect = RectF()

    // ---- Read custom attributes ----
    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularProgressView,
            defStyleAttr,
            0
        ).apply {
            try {
                progress = getFloat(R.styleable.CircularProgressView_progress, 0f)
                progressPaint.color = getColor(
                    R.styleable.CircularProgressView_progressColor,
                    Color.BLUE
                )
                backgroundPaint.color = getColor(
                    R.styleable.CircularProgressView_trackColor,
                    Color.LTGRAY
                )
                progressPaint.strokeWidth = getDimension(
                    R.styleable.CircularProgressView_strokeWidth,
                    20f
                )
                backgroundPaint.strokeWidth = progressPaint.strokeWidth
            } finally {
                recycle() // Always recycle TypedArray!
            }
        }
    }

    // ---- Measurement ----
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 200 // default dp

        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        val size = minOf(width, height)

        setMeasuredDimension(size, size)
    }

    // ---- Size Changed ----
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = progressPaint.strokeWidth / 2
        rect.set(padding, padding, w - padding, h - padding)
    }

    // ---- Drawing ----
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background circle
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // Draw progress arc (start from top: -90°)
        val sweepAngle = (progress / 100f) * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Draw percentage text in center
        val text = "${progress.toInt()}%"
        val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, rect.centerX(), textY, textPaint)
    }

    // ---- Public API ----
    fun setProgress(value: Float) {
        progress = value
    }

    fun animateProgress(targetProgress: Float, duration: Long = 1000L) {
        ValueAnimator.ofFloat(progress, targetProgress).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                progress = animator.animatedValue as Float
            }
            start()
        }
    }
}
```

---

## 2. Canvas Drawing Operations

```
┌─────────────────────────────────────────────────────────────┐
│                  Canvas Coordinate System                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  (0,0) ──────────────── X+ ──────────────▶                  │
│    │                                                         │
│    │    ┌────────────────────────────┐                       │
│    │    │                            │                       │
│    │    │      Canvas Area           │                       │
│   Y+    │                            │                       │
│    │    │    drawRect()              │                       │
│    │    │    drawCircle()            │                       │
│    │    │    drawLine()              │                       │
│    │    │    drawText()              │                       │
│    │    │    drawPath()              │                       │
│    │    │    drawBitmap()            │                       │
│    │    │    drawArc()              │                       │
│    │    │                            │                       │
│    ▼    └────────────────────────────┘                       │
│                                                              │
│  Note: Y increases DOWNWARD (opposite to math convention)    │
└─────────────────────────────────────────────────────────────┘
```

### All Major Canvas Operations

```kotlin
class CanvasDemoView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. DRAW RECTANGLE
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        canvas.drawRect(50f, 50f, 250f, 150f, paint) // left, top, right, bottom

        // Rounded rectangle
        paint.color = Color.GREEN
        canvas.drawRoundRect(300f, 50f, 500f, 150f, 20f, 20f, paint)

        // 2. DRAW CIRCLE
        paint.color = Color.BLUE
        canvas.drawCircle(150f, 250f, 60f, paint) // cx, cy, radius

        // 3. DRAW OVAL
        paint.color = Color.MAGENTA
        canvas.drawOval(300f, 200f, 500f, 300f, paint)

        // 4. DRAW LINE
        paint.color = Color.BLACK
        paint.strokeWidth = 5f
        canvas.drawLine(50f, 350f, 500f, 350f, paint) // startX, startY, stopX, stopY

        // 5. DRAW ARC
        paint.color = Color.CYAN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        val arcRect = RectF(50f, 400f, 250f, 550f)
        canvas.drawArc(arcRect, 0f, 270f, false, paint) // rect, startAngle, sweep, useCenter

        // 6. DRAW TEXT
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.FILL
        paint.textSize = 48f
        canvas.drawText("Hello Canvas!", 300f, 480f, paint)

        // 7. DRAW PATH (custom shape)
        paint.color = Color.parseColor("#FF5722")
        paint.style = Paint.Style.FILL
        val path = Path().apply {
            moveTo(150f, 600f)    // Start point
            lineTo(250f, 750f)    // Line to
            lineTo(50f, 750f)     // Line to
            close()               // Close path (triangle)
        }
        canvas.drawPath(path, paint)

        // 8. DRAW BEZIER CURVE
        paint.color = Color.parseColor("#9C27B0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        val bezierPath = Path().apply {
            moveTo(300f, 600f)
            quadTo(400f, 550f, 500f, 700f)  // Quadratic bezier
            // cubicTo(x1, y1, x2, y2, x3, y3)  // Cubic bezier
        }
        canvas.drawPath(bezierPath, paint)

        // 9. CANVAS TRANSFORMATIONS
        canvas.save()           // Save current state
        canvas.translate(250f, 800f)
        canvas.rotate(45f)     // Rotate 45 degrees
        paint.color = Color.YELLOW
        paint.style = Paint.Style.FILL
        canvas.drawRect(-40f, -40f, 40f, 40f, paint)
        canvas.restore()        // Restore to saved state

        // 10. DRAW BITMAP
        // val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image)
        // canvas.drawBitmap(bitmap, 50f, 900f, null)

        // 11. CLIPPING
        canvas.save()
        canvas.clipRect(400f, 800f, 550f, 950f) // Only draw within this rect
        paint.color = Color.RED
        canvas.drawCircle(475f, 875f, 100f, paint) // Circle will be clipped
        canvas.restore()
    }
}
```

### Paint Styles and Effects

```kotlin
class PaintEffectsView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // FILL style
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLUE
        }
        canvas.drawCircle(100f, 100f, 60f, fillPaint)

        // STROKE style
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.RED
            strokeWidth = 8f
        }
        canvas.drawCircle(280f, 100f, 60f, strokePaint)

        // FILL_AND_STROKE
        val fillStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.GREEN
            strokeWidth = 4f
        }
        canvas.drawCircle(460f, 100f, 60f, fillStrokePaint)

        // GRADIENT (Linear)
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                50f, 250f, 550f, 250f,
                Color.RED, Color.BLUE,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(50f, 220f, 550f, 280f, gradientPaint)

        // GRADIENT (Radial)
        val radialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                300f, 400f, 100f,
                Color.YELLOW, Color.RED,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(300f, 400f, 100f, radialPaint)

        // SHADOW
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLUE
            setShadowLayer(10f, 5f, 5f, Color.GRAY)
        }
        // Must disable HW acceleration for shadow:
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint)
        canvas.drawRoundRect(100f, 550f, 500f, 650f, 20f, 20f, shadowPaint)

        // DASHED LINE
        val dashedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 4f
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f, 5f, 10f), 0f)
        }
        canvas.drawLine(50f, 700f, 550f, 700f, dashedPaint)
    }
}
```

---

## 3. Custom Attributes with TypedArray

### Step 1: Define Attributes in `res/values/attrs.xml`

```xml
<!-- res/values/attrs.xml -->
<resources>
    <declare-styleable name="CircularProgressView">
        <attr name="progress" format="float" />
        <attr name="progressColor" format="color" />
        <attr name="trackColor" format="color" />
        <attr name="strokeWidth" format="dimension" />
        <attr name="showText" format="boolean" />
        <attr name="textSize" format="dimension" />
        <attr name="animationDuration" format="integer" />
        <attr name="capStyle" format="enum">
            <enum name="round" value="0" />
            <enum name="square" value="1" />
            <enum name="butt" value="2" />
        </attr>
    </declare-styleable>

    <declare-styleable name="GaugeView">
        <attr name="minValue" format="float" />
        <attr name="maxValue" format="float" />
        <attr name="currentValue" format="float" />
        <attr name="needleColor" format="color" />
        <attr name="scaleColor" format="color" />
        <attr name="label" format="string" />
        <attr name="divisions" format="integer" />
    </declare-styleable>
</resources>
```

### Step 2: Read Attributes in Custom View

```kotlin
class GaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var minValue = 0f
    private var maxValue = 100f
    private var currentValue = 0f
    private var needleColor = Color.RED
    private var scaleColor = Color.BLACK
    private var label = ""
    private var divisions = 10

    init {
        // Read custom attributes
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GaugeView,
            defStyleAttr,
            0
        ).apply {
            try {
                minValue = getFloat(R.styleable.GaugeView_minValue, 0f)
                maxValue = getFloat(R.styleable.GaugeView_maxValue, 100f)
                currentValue = getFloat(R.styleable.GaugeView_currentValue, 0f)
                needleColor = getColor(R.styleable.GaugeView_needleColor, Color.RED)
                scaleColor = getColor(R.styleable.GaugeView_scaleColor, Color.BLACK)
                label = getString(R.styleable.GaugeView_label) ?: ""
                divisions = getInt(R.styleable.GaugeView_divisions, 10)
            } finally {
                recycle() // ALWAYS recycle TypedArray to avoid memory leaks
            }
        }
    }

    // ... onDraw implementation
}
```

### Step 3: Use in XML Layout

```xml
<!-- res/layout/activity_main.xml -->
<com.example.app.views.CircularProgressView
    android:id="@+id/progressView"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:progress="75"
    app:progressColor="@color/purple_500"
    app:trackColor="@color/grey_300"
    app:strokeWidth="12dp"
    app:showText="true"
    app:capStyle="round" />

<com.example.app.views.GaugeView
    android:id="@+id/gaugeView"
    android:layout_width="250dp"
    android:layout_height="250dp"
    app:minValue="0"
    app:maxValue="200"
    app:currentValue="120"
    app:needleColor="@color/red"
    app:label="Speed (km/h)"
    app:divisions="10" />
```

---

## 4. Complete Example: Bar Chart View

```kotlin
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class BarData(val label: String, val value: Float, val color: Int)

    private var bars: List<BarData> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
    }

    private val padding = 60f
    private val barSpacing = 16f

    fun setData(data: List<BarData>) {
        bars = data
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 600
        val desiredHeight = 400
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) return

        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        val maxValue = bars.maxOf { it.value }
        val barWidth = (chartWidth - (bars.size + 1) * barSpacing) / bars.size

        // Draw axes
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)

        // Draw bars
        bars.forEachIndexed { index, bar ->
            val left = padding + barSpacing + index * (barWidth + barSpacing)
            val barHeight = (bar.value / maxValue) * chartHeight
            val top = height - padding - barHeight
            val right = left + barWidth
            val bottom = height - padding

            barPaint.color = bar.color
            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, barPaint)

            // Value label on top of bar
            canvas.drawText(
                bar.value.toInt().toString(),
                left + barWidth / 2,
                top - 10f,
                textPaint
            )

            // Category label below axis
            canvas.drawText(
                bar.label,
                left + barWidth / 2,
                height - padding + 40f,
                textPaint
            )
        }
    }
}

// Usage:
// barChart.setData(listOf(
//     BarData("Mon", 45f, Color.BLUE),
//     BarData("Tue", 72f, Color.GREEN),
//     BarData("Wed", 30f, Color.RED),
//     BarData("Thu", 88f, Color.YELLOW),
//     BarData("Fri", 65f, Color.CYAN)
// ))
```

---

## 5. Touch Handling in Custom Views

```kotlin
class DraggableCircleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var circleX = 200f
    private var circleY = 200f
    private val circleRadius = 50f
    private var isDragging = false

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(circleX, circleY, circleRadius, circlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if touch is on the circle
                val dx = event.x - circleX
                val dy = event.y - circleY
                isDragging = (dx * dx + dy * dy) <= circleRadius * circleRadius
                if (isDragging) {
                    circlePaint.color = Color.RED
                    invalidate()
                }
                return isDragging
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    circleX = event.x.coerceIn(circleRadius, width - circleRadius)
                    circleY = event.y.coerceIn(circleRadius, height - circleRadius)
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    isDragging = false
                    circlePaint.color = Color.BLUE
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
```

---

## 6. Hardware Acceleration

```
┌──────────────────────────────────────────────────────────────┐
│               Hardware Acceleration Layers                    │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Application Level (AndroidManifest.xml)                      │
│  ┌──────────────────────────────────────────────────┐        │
│  │  <application android:hardwareAccelerated="true"> │        │
│  │  (Default: true for API 14+)                      │        │
│  └──────────────────┬───────────────────────────────┘        │
│                     │                                         │
│  Activity Level     ▼                                         │
│  ┌──────────────────────────────────────────────────┐        │
│  │  <activity android:hardwareAccelerated="true" />  │        │
│  └──────────────────┬───────────────────────────────┘        │
│                     │                                         │
│  Window Level       ▼                                         │
│  ┌──────────────────────────────────────────────────┐        │
│  │  window.setFlags(                                 │        │
│  │    FLAG_HARDWARE_ACCELERATED,                     │        │
│  │    FLAG_HARDWARE_ACCELERATED)                     │        │
│  └──────────────────┬───────────────────────────────┘        │
│                     │                                         │
│  View Level         ▼                                         │
│  ┌──────────────────────────────────────────────────┐        │
│  │  view.setLayerType(LAYER_TYPE_SOFTWARE, null)     │        │
│  │  // OR                                            │        │
│  │  view.setLayerType(LAYER_TYPE_HARDWARE, null)     │        │
│  └──────────────────────────────────────────────────┘        │
│                                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │  SW Rendering          │  HW Rendering             │      │
│  │  - CPU based            │  - GPU based              │      │
│  │  - All ops supported    │  - Faster for most ops    │      │
│  │  - Slower generally     │  - Some ops unsupported:  │      │
│  │                         │    • setShadowLayer()     │      │
│  │                         │    • setMaskFilter()      │      │
│  │                         │    • drawPicture()        │      │
│  │                         │    • PathDashPathEffect   │      │
│  └────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

### Checking and Handling HW Acceleration

```kotlin
class HwAwareView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        // Shadow requires software rendering
        setShadowLayer(10f, 5f, 5f, Color.GRAY)
    }

    init {
        // Force software rendering for this view if needed
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Check if HW accelerated
        if (canvas.isHardwareAccelerated) {
            // Use HW-compatible drawing operations
        } else {
            // Use any drawing operations (SW fallback)
        }

        canvas.drawRoundRect(50f, 50f, 300f, 200f, 20f, 20f, shadowPaint)
    }
}
```

### Performance Tips for Custom Views

```kotlin
class PerformantCustomView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // ✅ DO: Allocate objects in constructor/init, NOT in onDraw()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val path = Path()

    // ❌ DON'T: Allocate in onDraw (called 60+ times per second!)
    // override fun onDraw(canvas: Canvas) {
    //     val paint = Paint()        // BAD: Creates GC pressure
    //     val rect = RectF(...)      // BAD: Creates GC pressure
    // }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // ✅ Reuse pre-allocated objects
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        paint.color = Color.BLUE
        canvas.drawRect(rect, paint)

        // ✅ Use clipRect to limit drawing area (reduces overdraw)
        canvas.clipRect(0, 0, width / 2, height)
        canvas.drawColor(Color.RED)
    }

    // ✅ Use invalidate(rect) for partial redraw when possible
    fun updateSection(left: Int, top: Int, right: Int, bottom: Int) {
        invalidate(left, top, right, bottom)
    }

    // ✅ Override onDetachedFromWindow to clean up
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cancel animations, release resources
    }
}
```
