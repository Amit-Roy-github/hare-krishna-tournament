package com.harekrishna.ui.util

// Insert thousands separators per the device's default locale (`1234567` → `1,234,567`
// or `12,34,567` for Indian locale). Used wherever a chant count is rendered.
fun Int.formatThousands(): String = "%,d".format(this)
