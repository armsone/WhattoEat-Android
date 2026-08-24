package com.nasfinder.whattoeat.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri

internal fun sanitizedDialNumber(phone: String): String? {
    val trimmed = phone.trim()
    val digits = trimmed.filter { it.isDigit() }
    if (digits.length < 3) return null
    return (if (trimmed.startsWith("+")) "+" else "") + digits
}

fun openPhoneDialer(context: Context, phone: String) {
    val number = sanitizedDialNumber(phone) ?: return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
}
