package com.nasfinder.whattoeat.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.theme.WhattoEatTheme
import com.nasfinder.whattoeat.ui.components.AppBottomBar
import org.junit.Rule
import org.junit.Test

class AppBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingRegionTabInvokesCallbackWithRegionPage() {
        var selected: AppPage? = null

        composeTestRule.setContent {
            WhattoEatTheme {
                AppBottomBar(
                    currentPage = AppPage.HOME,
                    onTabSelected = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("지역").performClick()

        assert(selected == AppPage.REGION) { "Expected REGION but was $selected" }
    }

    @Test
    fun allFiveTabLabelsAreDisplayedInOrder() {
        composeTestRule.setContent {
            WhattoEatTheme {
                AppBottomBar(currentPage = AppPage.HOME, onTabSelected = {})
            }
        }

        composeTestRule.onNodeWithText("홈").assertExists()
        composeTestRule.onNodeWithText("지역").assertExists()
        composeTestRule.onNodeWithContentDescription("추천 다시 고르기").assertExists()
        composeTestRule.onNodeWithText("최근").assertExists()
        composeTestRule.onNodeWithText("찜").assertExists()
    }

    @Test
    fun androidDirectOverrideDimensionsAndSelectionSemanticsAreExposed() {
        composeTestRule.setContent {
            WhattoEatTheme {
                AppBottomBar(currentPage = AppPage.HOME, onTabSelected = {})
            }
        }

        composeTestRule.onNodeWithTag("bottom_bar").assertHeightIsEqualTo(126.dp)
        composeTestRule.onNodeWithTag("bottom_recommendCircle", useUnmergedTree = true)
            .assertWidthIsEqualTo(57.834.dp)
            .assertHeightIsEqualTo(57.834.dp)
        composeTestRule.onNodeWithText("홈").assertIsSelected().assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("추천 다시 고르기").assertHasClickAction()
    }

    @Test
    fun tappingRecommendInvokesFreshResultRequest() {
        var selected: AppPage? = null
        composeTestRule.setContent {
            WhattoEatTheme {
                AppBottomBar(currentPage = AppPage.HOME, onTabSelected = { selected = it })
            }
        }

        composeTestRule.onNodeWithContentDescription("추천 다시 고르기").performClick()

        assert(selected == AppPage.RESULT) { "Expected RESULT but was $selected" }
    }
}
