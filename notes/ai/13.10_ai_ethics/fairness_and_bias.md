# Fairness and Bias in AI — Detection, Metrics & Mitigation

## Overview

AI bias occurs when models produce systematically unfair outcomes for certain groups. In Android apps serving diverse users, fairness is critical.

```
Sources of Bias in ML Pipeline:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Data Collection    Labeling      Model Training  Deployment │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐ │
│  │Selection│    │Annotator│    │Algorithm│    │Feedback │ │
│  │  Bias   │───▶│  Bias   │───▶│  Bias   │───▶│  Loop   │ │
│  └─────────┘    └─────────┘    └─────────┘    └─────────┘ │
│                                                              │
│  Examples:                                                  │
│  • Selection: Training face recognition mostly on one      │
│    ethnicity → poor performance on others                   │
│  • Annotator: Labelers' cultural assumptions affect labels  │
│  • Algorithm: Model amplifies existing data skews          │
│  • Feedback: Biased predictions → biased user behavior     │
│    → more biased training data                              │
│                                                              │
│  Types of Bias:                                             │
│  ┌─────────────────────┬───────────────────────────────┐   │
│  │ Historical bias     │ Real-world inequality         │   │
│  │                     │ reflected in training data     │   │
│  ├─────────────────────┼───────────────────────────────┤   │
│  │ Representation bias │ Some groups under-represented │   │
│  │                     │ in training data               │   │
│  ├─────────────────────┼───────────────────────────────┤   │
│  │ Measurement bias    │ Features measured differently │   │
│  │                     │ across groups                  │   │
│  ├─────────────────────┼───────────────────────────────┤   │
│  │ Aggregation bias    │ One model for all groups when │   │
│  │                     │ subgroups need different ones  │   │
│  ├─────────────────────┼───────────────────────────────┤   │
│  │ Evaluation bias     │ Benchmark not representative  │   │
│  │                     │ of real-world deployment       │   │
│  └─────────────────────┴───────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Bias Detection

```kotlin
// Bias detection framework for Android ML models
class BiasDetector {

    data class GroupMetrics(
        val groupName: String,
        val totalSamples: Int,
        val truePositives: Int,
        val falsePositives: Int,
        val trueNegatives: Int,
        val falseNegatives: Int
    ) {
        val accuracy: Float get() = (truePositives + trueNegatives).toFloat() / totalSamples
        val precision: Float get() = truePositives.toFloat() / (truePositives + falsePositives).coerceAtLeast(1)
        val recall: Float get() = truePositives.toFloat() / (truePositives + falseNegatives).coerceAtLeast(1)
        val falsePositiveRate: Float get() = falsePositives.toFloat() / (falsePositives + trueNegatives).coerceAtLeast(1)
        val falseNegativeRate: Float get() = falseNegatives.toFloat() / (falseNegatives + truePositives).coerceAtLeast(1)
        val positiveRate: Float get() = (truePositives + falsePositives).toFloat() / totalSamples
    }

    // Evaluate model across demographic groups
    fun evaluateByGroup(
        predictions: List<Boolean>,
        labels: List<Boolean>,
        groups: List<String>  // group membership for each sample
    ): Map<String, GroupMetrics> {
        val groupData = mutableMapOf<String, MutableList<Pair<Boolean, Boolean>>>()

        for (i in predictions.indices) {
            groupData.getOrPut(groups[i]) { mutableListOf() }
                .add(predictions[i] to labels[i])
        }

        return groupData.mapValues { (name, data) ->
            var tp = 0; var fp = 0; var tn = 0; var fn = 0
            for ((pred, label) in data) {
                when {
                    pred && label -> tp++
                    pred && !label -> fp++
                    !pred && label -> fn++
                    else -> tn++
                }
            }
            GroupMetrics(name, data.size, tp, fp, tn, fn)
        }
    }

    // Detect significant performance differences
    fun detectBias(
        groupMetrics: Map<String, GroupMetrics>,
        threshold: Float = 0.1f  // 10% difference threshold
    ): List<BiasReport> {
        val reports = mutableListOf<BiasReport>()
        val groups = groupMetrics.values.toList()

        for (i in groups.indices) {
            for (j in i + 1 until groups.size) {
                val a = groups[i]
                val b = groups[j]

                val accuracyDiff = kotlin.math.abs(a.accuracy - b.accuracy)
                val fprDiff = kotlin.math.abs(a.falsePositiveRate - b.falsePositiveRate)
                val fnrDiff = kotlin.math.abs(a.falseNegativeRate - b.falseNegativeRate)

                if (accuracyDiff > threshold || fprDiff > threshold || fnrDiff > threshold) {
                    reports.add(BiasReport(
                        groupA = a.groupName,
                        groupB = b.groupName,
                        accuracyDifference = accuracyDiff,
                        fprDifference = fprDiff,
                        fnrDifference = fnrDiff,
                        severity = when {
                            maxOf(accuracyDiff, fprDiff, fnrDiff) > 0.2f -> Severity.HIGH
                            maxOf(accuracyDiff, fprDiff, fnrDiff) > 0.1f -> Severity.MEDIUM
                            else -> Severity.LOW
                        }
                    ))
                }
            }
        }
        return reports
    }

    data class BiasReport(
        val groupA: String,
        val groupB: String,
        val accuracyDifference: Float,
        val fprDifference: Float,
        val fnrDifference: Float,
        val severity: Severity
    )

    enum class Severity { LOW, MEDIUM, HIGH }
}
```

---

## 2. Fairness Metrics

```
Common Fairness Metrics:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  1. Demographic Parity (Statistical Parity):               │
│     P(Ŷ=1 | A=a) = P(Ŷ=1 | A=b)                         │
│     "Positive prediction rate same across groups"           │
│     Example: Loan approval rate same for all ethnicities    │
│                                                              │
│  2. Equalized Odds:                                        │
│     P(Ŷ=1 | Y=y, A=a) = P(Ŷ=1 | Y=y, A=b) for y∈{0,1}  │
│     "FPR and TPR same across groups"                        │
│     Example: Cancer detection equally accurate for all      │
│                                                              │
│  3. Equal Opportunity:                                      │
│     P(Ŷ=1 | Y=1, A=a) = P(Ŷ=1 | Y=1, A=b)               │
│     "TPR (recall) same across groups"                       │
│     Relaxed version of equalized odds                       │
│                                                              │
│  4. Predictive Parity:                                     │
│     P(Y=1 | Ŷ=1, A=a) = P(Y=1 | Ŷ=1, A=b)               │
│     "Precision same across groups"                          │
│                                                              │
│  5. Calibration:                                           │
│     E(Y | Ŷ=p, A=a) = p for all groups                    │
│     "Predicted probability matches actual probability"      │
│                                                              │
│  ⚠️ Impossibility Theorem:                                 │
│  Cannot satisfy all metrics simultaneously                  │
│  (except in trivial cases). Must choose based on           │
│  application context.                                       │
│                                                              │
│  Choosing Metrics by Use Case:                             │
│  ┌───────────────────┬────────────────────────────┐        │
│  │ Content recommend │ Demographic Parity         │        │
│  │ Medical diagnosis │ Equal Opportunity           │        │
│  │ Criminal justice  │ Equalized Odds              │        │
│  │ Credit scoring    │ Predictive Parity           │        │
│  │ Risk assessment   │ Calibration                 │        │
│  └───────────────────┴────────────────────────────┘        │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class FairnessMetrics {

    // Demographic Parity: positive rate difference
    fun demographicParity(
        groupMetrics: Map<String, BiasDetector.GroupMetrics>
    ): Map<String, Float> {
        val rates = groupMetrics.mapValues { it.value.positiveRate }
        val maxRate = rates.values.maxOrNull() ?: 0f
        val minRate = rates.values.minOrNull() ?: 0f

        return mapOf(
            "max_difference" to (maxRate - minRate),
            "disparate_impact_ratio" to if (maxRate > 0) minRate / maxRate else 0f
            // Disparate impact ratio < 0.8 → potential bias (80% rule)
        )
    }

    // Equalized Odds: FPR and TPR differences across groups
    fun equalizedOdds(
        groupMetrics: Map<String, BiasDetector.GroupMetrics>
    ): Map<String, Float> {
        val tprs = groupMetrics.mapValues { it.value.recall }
        val fprs = groupMetrics.mapValues { it.value.falsePositiveRate }

        return mapOf(
            "tpr_max_diff" to ((tprs.values.maxOrNull() ?: 0f) - (tprs.values.minOrNull() ?: 0f)),
            "fpr_max_diff" to ((fprs.values.maxOrNull() ?: 0f) - (fprs.values.minOrNull() ?: 0f))
        )
    }

    // Equal Opportunity: TPR difference only
    fun equalOpportunity(
        groupMetrics: Map<String, BiasDetector.GroupMetrics>
    ): Float {
        val tprs = groupMetrics.mapValues { it.value.recall }
        return (tprs.values.maxOrNull() ?: 0f) - (tprs.values.minOrNull() ?: 0f)
    }

    // Generate full fairness report
    fun generateFairnessReport(
        groupMetrics: Map<String, BiasDetector.GroupMetrics>
    ): FairnessReport {
        return FairnessReport(
            demographicParity = demographicParity(groupMetrics),
            equalizedOdds = equalizedOdds(groupMetrics),
            equalOpportunity = equalOpportunity(groupMetrics),
            groupDetails = groupMetrics.map { (name, m) ->
                GroupDetail(name, m.accuracy, m.precision, m.recall, m.falsePositiveRate, m.totalSamples)
            }
        )
    }

    data class FairnessReport(
        val demographicParity: Map<String, Float>,
        val equalizedOdds: Map<String, Float>,
        val equalOpportunity: Float,
        val groupDetails: List<GroupDetail>
    )

    data class GroupDetail(
        val group: String,
        val accuracy: Float,
        val precision: Float,
        val recall: Float,
        val fpr: Float,
        val sampleCount: Int
    )
}
```

---

## 3. Diverse Training Data

```
Building Fair Datasets:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Strategy 1: Balanced Sampling                              │
│  ┌──────┐                                                   │
│  │Group │  Original    Target     Action                    │
│  │  A   │   60%    →   33%   →   Undersample               │
│  │  B   │   30%    →   33%   →   Keep                      │
│  │  C   │   10%    →   33%   →   Oversample / augment      │
│  └──────┘                                                   │
│                                                              │
│  Strategy 2: Data Augmentation for Underrepresented Groups  │
│  • Synthetic data generation                                │
│  • Style transfer across demographics                       │
│  • Careful duplication with variation                       │
│                                                              │
│  Strategy 3: Re-weighting                                   │
│  • Assign higher loss weight to underrepresented groups     │
│  • w_k = 1 / (K × p_k)  where p_k = proportion of group k │
│                                                              │
│  Strategy 4: Adversarial Debiasing                         │
│  ┌──────┐    ┌──────────┐    ┌──────────┐                  │
│  │Input │───▶│Predictor │───▶│ŷ (task)  │  Maximize        │
│  │  x   │    └──────────┘    └──────────┘  task accuracy   │
│  │      │    ┌──────────┐    ┌──────────┐                  │
│  │      │───▶│Adversary │───▶│â (group) │  Minimize        │
│  │      │    └──────────┘    └──────────┘  group predict.  │
│  └──────┘                                                   │
│  Train predictor to maximize task accuracy while making     │
│  it impossible for adversary to predict group membership.   │
│                                                              │
│  Strategy 5: Post-Processing                               │
│  • Adjust decision thresholds per group                    │
│  • Calibrate probabilities per group                       │
│  • Reject option: abstain on uncertain near-boundary cases │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class FairDataPipeline {

    // Re-weight samples for balanced representation
    fun computeSampleWeights(
        groups: List<String>
    ): Map<String, Float> {
        val counts = groups.groupingBy { it }.eachCount()
        val total = groups.size.toFloat()
        val numGroups = counts.size

        // Inverse frequency weighting
        return counts.mapValues { (_, count) ->
            total / (numGroups * count)
        }
    }

    // Balanced batch sampler for training
    fun createBalancedBatches(
        data: List<Pair<FloatArray, FloatArray>>,
        groups: List<String>,
        batchSize: Int
    ): List<List<Pair<FloatArray, FloatArray>>> {
        val groupedData = data.zip(groups)
            .groupBy { it.second }
            .mapValues { it.value.map { pair -> pair.first } }

        val samplesPerGroup = batchSize / groupedData.size
        val batches = mutableListOf<List<Pair<FloatArray, FloatArray>>>()

        val maxBatches = groupedData.values.maxOf { it.size } / samplesPerGroup

        for (batchIdx in 0 until maxBatches) {
            val batch = mutableListOf<Pair<FloatArray, FloatArray>>()

            for ((_, groupSamples) in groupedData) {
                // Sample with replacement if group is small
                repeat(samplesPerGroup) {
                    val idx = (batchIdx * samplesPerGroup + it) % groupSamples.size
                    batch.add(groupSamples[idx])
                }
            }

            batches.add(batch.shuffled())
        }

        return batches
    }

    // Post-processing: adjust thresholds per group for equal opportunity
    fun calibrateThresholds(
        predictions: List<Float>,   // raw probabilities
        labels: List<Boolean>,
        groups: List<String>,
        targetTPR: Float = 0.8f
    ): Map<String, Float> {
        val groupData = predictions.indices
            .groupBy { groups[it] }

        return groupData.mapValues { (_, indices) ->
            // Find threshold that achieves target TPR for this group
            val groupPreds = indices.map { predictions[it] }
            val groupLabels = indices.map { labels[it] }

            var bestThreshold = 0.5f
            var bestDiff = Float.MAX_VALUE

            for (t in (1..99).map { it / 100f }) {
                val tp = groupPreds.zip(groupLabels).count { (p, l) -> p >= t && l }
                val fn = groupPreds.zip(groupLabels).count { (p, l) -> p < t && l }
                val tpr = if (tp + fn > 0) tp.toFloat() / (tp + fn) else 0f

                val diff = kotlin.math.abs(tpr - targetTPR)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestThreshold = t
                }
            }
            bestThreshold
        }
    }
}
```
