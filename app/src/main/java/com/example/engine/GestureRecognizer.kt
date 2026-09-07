package com.example.engine

import com.example.model.GestureEntity
import com.example.model.GesturePoint
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object GestureRecognizer {
    const val RESAMPLE_POINTS = 32
    private const val REFERENCE_SIZE = 100.0f
    private const val MAX_DISTANCE_FACTOR = 55.0f

    data class RecognitionResult(
        val matchedGesture: GestureEntity?,
        val bestScore: Float,
        val topCandidates: List<CandidateScore>
    )

    data class CandidateScore(
        val gesture: GestureEntity,
        val score: Float,
        val meetsThreshold: Boolean
    )

    fun recognize(
        rawPoints: List<GesturePoint>,
        templates: List<GestureEntity>
    ): RecognitionResult {
        if (rawPoints.size < 3 || templates.isEmpty()) {
            return RecognitionResult(null, 0f, emptyList())
        }

        val candidateNormalized = normalize(rawPoints)
        if (candidateNormalized.isEmpty()) {
            return RecognitionResult(null, 0f, emptyList())
        }

        val candidates = mutableListOf<CandidateScore>()

        for (template in templates) {
            val templatePoints = template.getPoints()
            if (templatePoints.size < 2) continue

            val templateNormalized = normalize(templatePoints)
            if (templateNormalized.isEmpty()) continue

            val forwardDist = pathDistance(candidateNormalized, templateNormalized)
            val reversedDist = pathDistanceReversed(candidateNormalized, templateNormalized)
            val cloudDist = cloudDistance(candidateNormalized, templateNormalized)

            // Combined evaluation: supports both sequential paths and multi-stroke point clouds
            val seqDist = min(forwardDist, reversedDist * 1.05f)
            val bestDist = min(seqDist, cloudDist * 1.08f)

            val score = max(0.0f, min(1.0f, 1.0f - (bestDist / MAX_DISTANCE_FACTOR)))
            val meetsThreshold = score >= template.sensitivity

            candidates.add(
                CandidateScore(
                    gesture = template,
                    score = score,
                    meetsThreshold = meetsThreshold
                )
            )
        }

        candidates.sortByDescending { it.score }

        val bestCandidate = candidates.firstOrNull()
        val match = if (bestCandidate != null && bestCandidate.meetsThreshold) {
            bestCandidate.gesture
        } else {
            null
        }

        return RecognitionResult(
            matchedGesture = match,
            bestScore = bestCandidate?.score ?: 0f,
            topCandidates = candidates.take(4)
        )
    }

    fun normalize(points: List<GesturePoint>): List<GesturePoint> {
        if (points.size < 2) return emptyList()

        val resampled = resample(points, RESAMPLE_POINTS)
        if (resampled.size < RESAMPLE_POINTS) return emptyList()

        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        for (p in resampled) {
            if (p.x < minX) minX = p.x
            if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
        }

        val width = max(maxX - minX, 1.0f)
        val height = max(maxY - minY, 1.0f)
        val centerX = (minX + maxX) / 2.0f
        val centerY = (minY + maxY) / 2.0f

        val scale = REFERENCE_SIZE / max(width, height)

        return resampled.map { p ->
            GesturePoint(
                x = (p.x - centerX) * scale,
                y = (p.y - centerY) * scale
            )
        }
    }

    private fun resample(points: List<GesturePoint>, n: Int): List<GesturePoint> {
        val totalLength = pathLength(points)
        if (totalLength <= 0.001f) return emptyList()

        val interval = totalLength / (n - 1)
        var accumulatedDistance = 0.0f
        val resampled = mutableListOf<GesturePoint>()
        resampled.add(points[0])

        val mutablePoints = points.toMutableList()
        var i = 1

        while (i < mutablePoints.size) {
            val p0 = mutablePoints[i - 1]
            val p1 = mutablePoints[i]
            val d = distance(p0, p1)

            if ((accumulatedDistance + d) >= interval) {
                val t = (interval - accumulatedDistance) / d
                val newX = p0.x + t * (p1.x - p0.x)
                val newY = p0.y + t * (p1.y - p0.y)
                val q = GesturePoint(newX, newY)

                resampled.add(q)
                mutablePoints.add(i, q)
                accumulatedDistance = 0.0f
            } else {
                accumulatedDistance += d
            }
            i++
        }

        // Pad if needed to guarantee exactly n points
        while (resampled.size < n) {
            resampled.add(points.last())
        }

        return resampled.take(n)
    }

    private fun pathLength(points: List<GesturePoint>): Float {
        var len = 0.0f
        for (i in 1 until points.size) {
            len += distance(points[i - 1], points[i])
        }
        return len
    }

    private fun distance(p1: GesturePoint, p2: GesturePoint): Float {
        return hypot(p2.x - p1.x, p2.y - p1.y)
    }

    private fun pathDistance(pts1: List<GesturePoint>, pts2: List<GesturePoint>): Float {
        val n = min(pts1.size, pts2.size)
        if (n == 0) return Float.MAX_VALUE
        var sum = 0.0f
        for (i in 0 until n) {
            sum += distance(pts1[i], pts2[i])
        }
        return sum / n
    }

    private fun pathDistanceReversed(pts1: List<GesturePoint>, pts2: List<GesturePoint>): Float {
        val n = min(pts1.size, pts2.size)
        if (n == 0) return Float.MAX_VALUE
        var sum = 0.0f
        for (i in 0 until n) {
            sum += distance(pts1[pts1.size - 1 - i], pts2[i])
        }
        return sum / n
    }

    private fun cloudDistance(pts1: List<GesturePoint>, pts2: List<GesturePoint>): Float {
        if (pts1.isEmpty() || pts2.isEmpty()) return Float.MAX_VALUE
        var sum1 = 0.0f
        for (p1 in pts1) {
            var minD = Float.MAX_VALUE
            for (p2 in pts2) {
                val d = distance(p1, p2)
                if (d < minD) minD = d
            }
            sum1 += minD
        }
        val avg1 = sum1 / pts1.size

        var sum2 = 0.0f
        for (p2 in pts2) {
            var minD = Float.MAX_VALUE
            for (p1 in pts1) {
                val d = distance(p2, p1)
                if (d < minD) minD = d
            }
            sum2 += minD
        }
        val avg2 = sum2 / pts2.size

        return (avg1 + avg2) / 2.0f
    }
}
