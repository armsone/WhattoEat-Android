package com.nasfinder.whattoeat.ui.screens

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.nasfinder.whattoeat.model.SituationFilter
import com.nasfinder.whattoeat.theme.WhattoEatTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SituationFilterRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allSituationChoicesAreReachableAndSelectionInvokesCallback() {
        var selected: SituationFilter? = null
        composeTestRule.setContent {
            WhattoEatTheme {
                SituationFilterRow(
                    selectedFilter = SituationFilter.ALL,
                    onSelectFilter = { selected = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("home_situationFilterRow").assertIsDisplayed()
        composeTestRule.onNodeWithText("전체").assertHasClickAction()
        composeTestRule.onNodeWithText("든든한 식사").assertHasClickAction()
        composeTestRule.onNodeWithText("간단하게").assertHasClickAction()
        composeTestRule.onNodeWithText("패스트푸드").assertHasClickAction()
        composeTestRule.onNodeWithText("디저트·카페").assertHasClickAction()
        composeTestRule.onNodeWithText("회식·모임").assertHasClickAction()
        composeTestRule.onNodeWithText("야식").performScrollTo().performClick()

        assertEquals(SituationFilter.LATE_NIGHT, selected)
    }
}
