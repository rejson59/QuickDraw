package com.example

import com.example.data.GestureRepository
import com.example.engine.GestureRecognizer
import com.example.model.ActionType
import com.example.model.GestureEntity
import com.example.model.GesturePointConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGestureRecognizerMatching() {
    val cPoints = GestureRepository.generateCPoints()
    val cSerialized = GesturePointConverter.serialize(cPoints)

    val template = GestureEntity(
      id = 1L,
      name = "Aparat C",
      pointsData = cSerialized,
      actionType = ActionType.SYSTEM.name,
      actionTarget = "OPEN_CAMERA",
      actionLabel = "Aparat",
      sensitivity = 0.65f
    )

    // Recognize with identical points
    val result = GestureRecognizer.recognize(cPoints, listOf(template))
    assertNotNull(result.matchedGesture)
    assertEquals(1L, result.matchedGesture?.id)
    assertTrue("Score should be > 0.90 for exact gesture", result.bestScore > 0.90f)
  }
}
