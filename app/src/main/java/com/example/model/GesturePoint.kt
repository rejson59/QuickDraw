package com.example.model

data class GesturePoint(
    val x: Float,
    val y: Float,
    val strokeId: Int = 0
)

object GesturePointConverter {
    fun serialize(points: List<GesturePoint>): String {
        return points.joinToString(";") { 
            "${"%.2f".format(java.util.Locale.US, it.x)},${"%.2f".format(java.util.Locale.US, it.y)},${it.strokeId}" 
        }
    }

    fun deserialize(data: String): List<GesturePoint> {
        if (data.isBlank()) return emptyList()
        return try {
            data.split(";").mapNotNull { part ->
                val coords = part.split(",")
                if (coords.size >= 2) {
                    val x = coords[0].toFloatOrNull()
                    val y = coords[1].toFloatOrNull()
                    val strokeId = if (coords.size >= 3) coords[2].toIntOrNull() ?: 0 else 0
                    if (x != null && y != null) GesturePoint(x, y, strokeId) else null
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
