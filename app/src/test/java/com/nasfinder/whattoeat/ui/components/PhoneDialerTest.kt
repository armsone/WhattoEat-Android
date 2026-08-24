package com.nasfinder.whattoeat.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneDialerTest {
    @Test
    fun preservesInternationalPrefixAndRemovesDisplayPunctuation() {
        assertEquals("+821012345678", sanitizedDialNumber(" +82 10-1234-5678 "))
    }

    @Test
    fun keepsDomesticDigits() {
        assertEquals("0311234567", sanitizedDialNumber("031-123-4567"))
    }

    @Test
    fun rejectsMissingOrInvalidNumbers() {
        assertNull(sanitizedDialNumber("--"))
        assertNull(sanitizedDialNumber("12"))
    }
}
