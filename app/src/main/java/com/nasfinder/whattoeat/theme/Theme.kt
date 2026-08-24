package com.nasfinder.whattoeat.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun WhattoEatTheme(
    content: @Composable () -> Unit
) {
    // Light mode fixed per spec (no dark mode)
    CompositionLocalProvider(
        content = content
    )
}
