package com.nasfinder.whattoeat.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchupLoadingPolicyTest {
    @Test
    fun `only debug loading fixture freezes the animation`() {
        assertTrue(shouldFreezeMatchupLoadingAnimation("loading", isDebug = true))
        assertFalse(shouldFreezeMatchupLoadingAnimation(null, isDebug = true))
        assertFalse(shouldFreezeMatchupLoadingAnimation("results", isDebug = true))
        assertFalse(shouldFreezeMatchupLoadingAnimation("loading", isDebug = false))
    }
}
