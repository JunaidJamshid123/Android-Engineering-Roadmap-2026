# On-Device NLP — Natural Language Processing

## Overview

On-device NLP enables text understanding and processing **without internet connectivity**, using lightweight models optimized for mobile inference.

```
On-Device NLP Architecture:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  ┌──────────┐    ┌──────────────┐    ┌────────────┐ │
│  │ Raw Text │───▶│ Tokenizer    │───▶│ Model      │ │
│  │ Input    │    │ (WordPiece/  │    │ (TFLite)   │ │
│  │          │    │  SentPiece)  │    │            │ │
│  └──────────┘    └──────────────┘    └─────┬──────┘ │
│                                            │        │
│                                     ┌──────▼──────┐ │
│                                     │ Post-process│ │
│                                     │ (Softmax,   │ │
│                                     │  ArgMax,    │ │
│                                     │  Decode)    │ │
│                                     └─────────────┘ │
│                                                      │
│  NLP Tasks:                                          │
│  ┌──────────────────────────────────────────────┐    │
│  │ • Named Entity Recognition (NER)             │    │
│  │ • Sentiment Analysis                         │    │
│  │ • Text Classification                        │    │
│  │ • Question Answering                         │    │
│  │ • Text Embedding / Similarity                │    │
│  │ • Token Classification                       │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Models:                                             │
│  ┌────────────────┬──────────┬──────────────────┐    │
│  │ Model          │ Size     │ Best For          │    │
│  ├────────────────┼──────────┼──────────────────┤    │
│  │ MobileBERT     │ ~25 MB   │ General NLP       │    │
│  │ DistilBERT     │ ~65 MB   │ Better accuracy   │    │
│  │ TinyBERT       │ ~15 MB   │ Smallest/fastest  │    │
│  │ ALBERT (tiny)  │ ~12 MB   │ Memory efficient  │    │
│  │ Avg Word Vec   │ ~2 MB    │ Simple classify   │    │
│  └────────────────┴──────────┴──────────────────┘    │
└──────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // TFLite Task Library (easiest)
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")
    
    // TFLite core (for custom models)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
}
```

---

## 1. BERT Models with TensorFlow Lite

### Theory

```
BERT Architecture (Simplified):
┌──────────────────────────────────────────────────────┐
│                                                      │
│  BERT = Bidirectional Encoder Representations         │
│         from Transformers                             │
│                                                      │
│  Input: "The movie was [MASK] good"                  │
│                                                      │
│  Tokenization:                                       │
│  ┌────┬─────┬─────┬──────┬──────┬─────┬─────┐       │
│  │[CLS]│The  │movie│was   │[MASK]│good │[SEP]│       │
│  └──┬──┴──┬──┴──┬──┴──┬───┴──┬───┴──┬──┴──┬──┘       │
│     │     │     │     │      │      │     │          │
│  ┌──▼─────▼─────▼─────▼──────▼──────▼─────▼──┐       │
│  │          Token Embeddings                  │       │
│  │        + Position Embeddings               │       │
│  │        + Segment Embeddings                │       │
│  └──────────────────┬─────────────────────────┘       │
│                     │                                │
│  ┌──────────────────▼─────────────────────────┐       │
│  │        Transformer Encoder Layers           │       │
│  │        (12 layers for base, 6 for mobile)   │       │
│  │                                             │       │
│  │  ┌─────────────────────────────────────┐   │       │
│  │  │ Multi-Head Self-Attention           │   │       │
│  │  │ Feed-Forward Network                │   │       │
│  │  │ Layer Normalization                 │   │       │
│  │  └─────────────────────────────────────┘   │       │
│  └──────────────────┬─────────────────────────┘       │
│                     │                                │
│  ┌──────────────────▼─────────────────────────┐       │
│  │        Output Representations               │       │
│  │  [CLS] → Classification (sentence-level)    │       │
│  │  Tokens → Token-level tasks (NER, etc.)     │       │
│  └─────────────────────────────────────────────┘       │
│                                                      │
│  MobileBERT: Optimized BERT for mobile               │
│  • 4.3x smaller than BERT-base                       │
│  • 5.5x faster                                       │
│  • 0.6% quality loss on GLUE benchmark               │
└──────────────────────────────────────────────────────┘

Tokenization Process (WordPiece):
  Input:  "unbelievably"
  Tokens: ["un", "##believe", "##ably"]
  IDs:    [4221, 6309, 24960]
  
  Special tokens:
  [CLS] = 101   (classification token, always first)
  [SEP] = 102   (separator, end of segment)
  [PAD] = 0     (padding)
  [UNK] = 100   (unknown token)
```

### Code — Custom BERT Inference

```kotlin
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BertModelHelper(private val context: Context) {

    private var interpreter: Interpreter? = null
    private lateinit var vocabulary: Map<String, Int>
    private val maxSeqLength = 128

    fun initialize(modelPath: String = "mobilebert.tflite", vocabPath: String = "vocab.txt") {
        // Load model
        val modelFile = loadModelFile(modelPath)
        interpreter = Interpreter(modelFile, Interpreter.Options().apply {
            setNumThreads(4)
        })

        // Load vocabulary
        vocabulary = context.assets.open(vocabPath)
            .bufferedReader()
            .readLines()
            .mapIndexed { index, word -> word to index }
            .toMap()
    }

    // WordPiece tokenization (simplified)
    fun tokenize(text: String): List<Int> {
        val tokens = mutableListOf(101) // [CLS]

        val words = text.lowercase().split("\\s+".toRegex())
        for (word in words) {
            val wordTokens = wordPieceTokenize(word)
            tokens.addAll(wordTokens)
        }

        tokens.add(102) // [SEP]

        // Pad or truncate to maxSeqLength
        while (tokens.size < maxSeqLength) tokens.add(0) // [PAD]
        return tokens.take(maxSeqLength)
    }

    private fun wordPieceTokenize(word: String): List<Int> {
        val tokens = mutableListOf<Int>()
        var remaining = word
        var isFirst = true

        while (remaining.isNotEmpty()) {
            var found = false
            for (end in remaining.length downTo 1) {
                val subword = if (isFirst) remaining.substring(0, end)
                else "##${remaining.substring(0, end)}"

                vocabulary[subword]?.let { id ->
                    tokens.add(id)
                    remaining = remaining.substring(end)
                    isFirst = false
                    found = true
                    return@let
                }
                if (found) break
            }
            if (!found) {
                tokens.add(100) // [UNK]
                break
            }
        }

        return tokens
    }

    // Prepare input tensors for BERT
    fun prepareInputs(tokenIds: List<Int>): Triple<IntArray, IntArray, IntArray> {
        val inputIds = tokenIds.toIntArray()
        val attentionMask = tokenIds.map { if (it != 0) 1 else 0 }.toIntArray()
        val tokenTypeIds = IntArray(maxSeqLength) { 0 } // Single segment

        return Triple(inputIds, attentionMask, tokenTypeIds)
    }

    private fun loadModelFile(path: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(path)
        return java.io.FileInputStream(fd.fileDescriptor).channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }

    fun close() = interpreter?.close()
}
```

---

## 2. Named Entity Recognition (NER)

### Theory

```
NER identifies and classifies named entities in text:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Input: "Tim Cook announced iPhone 16 in Cupertino"  │
│                                                      │
│  Output:                                             │
│  ┌──────────┬──────────┬───────────────────────┐     │
│  │ Token    │ Label    │ Entity Type           │     │
│  ├──────────┼──────────┼───────────────────────┤     │
│  │ Tim      │ B-PER    │ Person (Begin)        │     │
│  │ Cook     │ I-PER    │ Person (Inside)       │     │
│  │ announced│ O        │ Other                 │     │
│  │ iPhone   │ B-PROD   │ Product (Begin)       │     │
│  │ 16       │ I-PROD   │ Product (Inside)       │     │
│  │ in       │ O        │ Other                 │     │
│  │ Cupertino│ B-LOC    │ Location (Begin)      │     │
│  └──────────┴──────────┴───────────────────────┘     │
│                                                      │
│  Entity Types (BIO tagging):                         │
│  B-PER  = Begin Person         I-PER  = Inside Person│
│  B-LOC  = Begin Location       I-LOC  = Inside Loc   │
│  B-ORG  = Begin Organization   I-ORG  = Inside Org   │
│  B-MISC = Begin Miscellaneous  I-MISC = Inside Misc  │
│  O      = Other (not an entity)                      │
│                                                      │
│  NER Pipeline:                                       │
│  Text → Tokenize → BERT → Per-token logits → Labels │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class NERHelper(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val bertHelper = BertModelHelper(context)

    private val nerLabels = listOf(
        "O", "B-PER", "I-PER", "B-LOC", "I-LOC",
        "B-ORG", "I-ORG", "B-MISC", "I-MISC"
    )

    data class Entity(
        val text: String,
        val type: String,    // PER, LOC, ORG, MISC
        val startIdx: Int,
        val endIdx: Int,
        val confidence: Float
    )

    fun initialize() {
        bertHelper.initialize("ner_model.tflite", "vocab.txt")

        val modelFile = context.assets.openFd("ner_model.tflite").let { fd ->
            java.io.FileInputStream(fd.fileDescriptor).channel.map(
                java.nio.channels.FileChannel.MapMode.READ_ONLY,
                fd.startOffset, fd.declaredLength
            )
        }
        interpreter = Interpreter(modelFile)
    }

    fun recognizeEntities(text: String): List<Entity> {
        val words = text.split("\\s+".toRegex())
        val tokenIds = bertHelper.tokenize(text)
        val (inputIds, attentionMask, tokenTypeIds) = bertHelper.prepareInputs(tokenIds)

        // Run inference
        val maxSeqLen = 128
        val numLabels = nerLabels.size
        val output = Array(1) { Array(maxSeqLen) { FloatArray(numLabels) } }

        val inputs = mapOf(
            0 to arrayOf(inputIds),
            1 to arrayOf(attentionMask),
            2 to arrayOf(tokenTypeIds)
        )

        interpreter?.runForMultipleInputsOutputs(
            inputs.values.toTypedArray(),
            mapOf(0 to output)
        )

        // Decode predictions
        val entities = mutableListOf<Entity>()
        var currentEntity: MutableList<String>? = null
        var currentType = ""
        var startIdx = 0

        for (i in 1 until words.size + 1) { // Skip [CLS]
            val logits = output[0][i]
            val predictedIdx = logits.indices.maxByOrNull { logits[it] } ?: 0
            val label = nerLabels[predictedIdx]
            val confidence = softmax(logits)[predictedIdx]

            when {
                label.startsWith("B-") -> {
                    // Save previous entity
                    currentEntity?.let {
                        entities.add(Entity(it.joinToString(" "), currentType, startIdx, i - 1, confidence))
                    }
                    // Start new entity
                    currentEntity = mutableListOf(words[i - 1])
                    currentType = label.substring(2)
                    startIdx = i - 1
                }
                label.startsWith("I-") && currentEntity != null -> {
                    currentEntity.add(words[i - 1])
                }
                else -> {
                    currentEntity?.let {
                        entities.add(Entity(it.joinToString(" "), currentType, startIdx, i - 1, confidence))
                    }
                    currentEntity = null
                }
            }
        }

        // Save last entity
        currentEntity?.let {
            entities.add(Entity(it.joinToString(" "), currentType, startIdx, words.size - 1, 0.9f))
        }

        return entities
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.max()
        val exps = logits.map { Math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }

    fun close() {
        interpreter?.close()
        bertHelper.close()
    }
}
```

---

## 3. Sentiment Analysis

### Theory

```
Sentiment Analysis:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Input: "This restaurant has amazing food!"          │
│  Output: Positive (0.95)                             │
│                                                      │
│  Classification Pipeline:                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────┐ │
│  │Tokenize  │─▶│ BERT/    │─▶│ Softmax  │─▶│Label│ │
│  │          │  │ Model    │  │          │  │     │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────┘ │
│                                                      │
│  Classification Types:                               │
│  ┌──────────────────────────────────────────────┐    │
│  │ Binary:    Positive / Negative                │    │
│  │ 3-class:   Positive / Neutral / Negative      │    │
│  │ 5-class:   Very Neg / Neg / Neutral / Pos / + │    │
│  │ Fine-grained: Rating 1-5 stars               │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Model Options for Mobile:                           │
│  ┌─────────────────┬──────┬───────────┬───────┐      │
│  │ Model           │ Size │ Accuracy  │ Speed │      │
│  ├─────────────────┼──────┼───────────┼───────┤      │
│  │ Avg Word Vec    │ 2 MB │ ████      │ Fast  │      │
│  │ MobileBERT      │ 25MB │ ████████  │ Med   │      │
│  │ DistilBERT      │ 65MB │ █████████ │ Slow  │      │
│  └─────────────────┴──────┴───────────┴───────┘      │
└──────────────────────────────────────────────────────┘
```

### Code — Using Task Library (Easy)

```kotlin
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier

class SentimentAnalyzer(private val context: Context) {

    // --- Option 1: Average Word Vector model (fastest, smallest) ---
    private var simpleClassifier: NLClassifier? = null

    fun initializeSimple() {
        simpleClassifier = NLClassifier.createFromFile(
            context, "sentiment_model.tflite"
        )
    }

    fun analyzeSentimentSimple(text: String): SentimentResult {
        val results = simpleClassifier?.classify(text) ?: return SentimentResult("Unknown", 0f)

        val topResult = results.maxByOrNull { it.score }
        return SentimentResult(
            label = topResult?.label ?: "Unknown",
            score = topResult?.score ?: 0f
        )
    }

    // --- Option 2: BERT-based (more accurate) ---
    private var bertClassifier: BertNLClassifier? = null

    fun initializeBert() {
        bertClassifier = BertNLClassifier.createFromFile(
            context, "bert_sentiment.tflite"
        )
    }

    fun analyzeSentimentBert(text: String): SentimentResult {
        val results = bertClassifier?.classify(text) ?: return SentimentResult("Unknown", 0f)

        val topResult = results.maxByOrNull { it.score }
        return SentimentResult(
            label = topResult?.label ?: "Unknown",
            score = topResult?.score ?: 0f
        )
    }

    // Batch analysis
    fun analyzeBatch(texts: List<String>): List<SentimentResult> {
        return texts.map { analyzeSentimentBert(it) }
    }

    data class SentimentResult(
        val label: String,     // "Positive", "Negative", "Neutral"
        val score: Float       // 0.0 to 1.0
    ) {
        val emoji: String get() = when {
            label.contains("Positive", true) && score > 0.8 -> "😍"
            label.contains("Positive", true) -> "😊"
            label.contains("Neutral", true) -> "😐"
            label.contains("Negative", true) && score > 0.8 -> "😡"
            label.contains("Negative", true) -> "😞"
            else -> "🤔"
        }
    }

    fun close() {
        simpleClassifier?.close()
        bertClassifier?.close()
    }
}
```

---

## 4. Text Classification

### Theory

```
Text Classification Use Cases:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Spam Detection:                                     │
│  "Buy cheap pills now!" → Spam (0.98)                │
│  "Meeting at 3pm today" → Not Spam (0.95)            │
│                                                      │
│  Topic Classification:                               │
│  "Stock market crashes" → Finance (0.91)             │
│  "New vaccine approved" → Health (0.87)              │
│                                                      │
│  Intent Detection (Chatbots):                        │
│  "What time do you close?" → Hours_Inquiry (0.93)    │
│  "I want to return this"   → Return_Request (0.89)   │
│                                                      │
│  Language Detection:                                 │
│  "Bonjour le monde" → French (0.97)                  │
│  "Hola mundo"       → Spanish (0.95)                 │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class TextClassificationHelper(private val context: Context) {

    private var classifier: NLClassifier? = null

    fun initialize(modelPath: String = "text_classifier.tflite") {
        val options = NLClassifier.NLClassifierOptions.builder()
            .build()

        classifier = NLClassifier.createFromFileAndOptions(
            context, modelPath, options
        )
    }

    fun classify(text: String): List<ClassificationResult> {
        val results = classifier?.classify(text) ?: emptyList()
        return results.map {
            ClassificationResult(it.label, it.score)
        }.sortedByDescending { it.confidence }
    }

    // Multi-label classification
    fun classifyMultiLabel(text: String, threshold: Float = 0.5f): List<ClassificationResult> {
        return classify(text).filter { it.confidence >= threshold }
    }

    data class ClassificationResult(
        val category: String,
        val confidence: Float
    )

    fun close() = classifier?.close()
}
```

---

## 5. Question Answering

### Theory

```
Question Answering (Extractive):
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Given a context paragraph, extract the answer span. │
│                                                      │
│  Context: "Android was released in September 2008.   │
│           It was developed by Google and the Open    │
│           Handset Alliance."                         │
│                                                      │
│  Question: "When was Android released?"              │
│                                                      │
│  Model Output:                                       │
│  ┌──────────────────────────────────────────────┐    │
│  │ Start logits:                                 │    │
│  │ Android was released in [September 2008]...   │    │
│  │                          ↑ start              │    │
│  │ End logits:                                   │    │
│  │ ...released in [September 2008]. It was...    │    │
│  │                              ↑ end            │    │
│  │                                               │    │
│  │ Answer: "September 2008"                      │    │
│  │ Confidence: 0.92                              │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Pipeline:                                           │
│  [CLS] Question [SEP] Context [SEP] [PAD]...        │
│         ↓                                            │
│  BERT encoder                                        │
│         ↓                                            │
│  Start + End position logits for each token          │
│         ↓                                            │
│  Extract text between start and end positions        │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
import org.tensorflow.lite.task.text.qa.BertQuestionAnswerer

class QAHelper(private val context: Context) {

    private var answerer: BertQuestionAnswerer? = null

    fun initialize() {
        // MobileBERT fine-tuned on SQuAD
        answerer = BertQuestionAnswerer.createFromFile(
            context, "mobilebert_qa.tflite"
        )
    }

    data class Answer(
        val text: String,
        val score: Float
    )

    fun answer(contextText: String, question: String): List<Answer> {
        val results = answerer?.answer(contextText, question) ?: emptyList()

        return results.map { qaAnswer ->
            Answer(
                text = qaAnswer.text,
                score = qaAnswer.pos.logit
            )
        }
    }

    // Interactive Q&A with context
    fun interactiveQA(
        document: String,
        questions: List<String>
    ): Map<String, Answer> {
        return questions.associateWith { question ->
            val answers = answer(document, question)
            answers.firstOrNull() ?: Answer("No answer found", 0f)
        }
    }

    // FAQ Bot
    class FAQBot(private val qaHelper: QAHelper) {
        private val faqDatabase = mutableMapOf<String, String>()

        fun addFAQ(category: String, content: String) {
            faqDatabase[category] = content
        }

        fun askQuestion(question: String): Answer {
            // Search all FAQ categories
            var bestAnswer = QAHelper.Answer("No answer found", 0f)

            for ((_, content) in faqDatabase) {
                val answers = qaHelper.answer(content, question)
                val topAnswer = answers.firstOrNull() ?: continue

                if (topAnswer.score > bestAnswer.score) {
                    bestAnswer = topAnswer
                }
            }

            return bestAnswer
        }
    }

    fun close() = answerer?.close()
}

// Usage example
fun qaExample(context: Context) {
    val qaHelper = QAHelper(context)
    qaHelper.initialize()

    val document = """
        Jetpack Compose is Android's modern toolkit for building native UI.
        It simplifies and accelerates UI development with less code, powerful tools,
        and intuitive Kotlin APIs. Compose was first released as stable in July 2021.
        It uses a declarative programming model where you describe your UI as functions.
    """.trimIndent()

    val answers = qaHelper.answer(document, "When was Compose released?")
    // Expected: "July 2021"

    val answers2 = qaHelper.answer(document, "What programming language does Compose use?")
    // Expected: "Kotlin"

    qaHelper.close()
}
```

---

## Performance Comparison

```
┌──────────────────────────────────────────────────────┐
│ On-Device NLP Performance (Typical Flagship 2026)    │
│                                                      │
│ Task               │ Model        │ Latency │Accuracy│
│ ───────────────────┼──────────────┼─────────┼────────│
│ Sentiment (simple) │ Avg Word Vec │  5ms    │ 85%    │
│ Sentiment (BERT)   │ MobileBERT   │ 50ms    │ 92%    │
│ Text Classification│ MobileBERT   │ 50ms    │ 90%    │
│ NER                │ MobileBERT   │ 60ms    │ 88%    │
│ Question Answering │ MobileBERT   │ 80ms    │ 87%    │
│ Question Answering │ DistilBERT   │ 150ms   │ 91%    │
│                                                      │
│ Memory Usage:                                        │
│ Avg Word Vec:  ~10 MB RAM                            │
│ MobileBERT:    ~100 MB RAM                           │
│ DistilBERT:    ~250 MB RAM                           │
└──────────────────────────────────────────────────────┘
```
