package com.nasfinder.whattoeat.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.nasfinder.whattoeat.theme.WhattoEatTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MealShuffleAnimationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun normalRuntimeFrameKeepsMoving() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            WhattoEatTheme {
                MealShuffleAnimation(modifier = Modifier.testTag("shuffle"), freezeForCatalog = false)
            }
        }

        composeTestRule.mainClock.advanceTimeByFrame()
        val first = composeTestRule.onNodeWithTag("shuffle").captureToImage().asAndroidBitmap()
        composeTestRule.mainClock.advanceTimeBy(600)
        val second = composeTestRule.onNodeWithTag("shuffle").captureToImage().asAndroidBitmap()

        assertFalse(first.sameAs(second))
    }

    @Test
    fun catalogLoadingFrameStaysDeterministic() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            WhattoEatTheme {
                MealShuffleAnimation(modifier = Modifier.testTag("shuffle"), freezeForCatalog = true)
            }
        }

        composeTestRule.mainClock.advanceTimeByFrame()
        val first = composeTestRule.onNodeWithTag("shuffle").captureToImage().asAndroidBitmap()
        composeTestRule.mainClock.advanceTimeBy(600)
        val second = composeTestRule.onNodeWithTag("shuffle").captureToImage().asAndroidBitmap()

        assertTrue(first.sameAs(second))
    }
}
