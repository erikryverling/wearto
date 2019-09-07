package se.yverling.wearto.core

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat.getColor
import se.yverling.wearto.R

fun projectColorToColorHex(context: Context, code: Int): Int {
    when (code) {
        30 -> return getColor(context, R.color.berry_red)
        31 -> return getColor(context, R.color.red)
        32 -> return getColor(context, R.color.orange)
        33 -> return getColor(context, R.color.yellow)
        34 -> return getColor(context, R.color.olive_green)
        35 -> return getColor(context, R.color.lime_green)
        36 -> return getColor(context, R.color.green)
        37 -> return getColor(context, R.color.mint_green)
        38 -> return getColor(context, R.color.teal)
        39 -> return getColor(context, R.color.sky_blue)
        40 -> return getColor(context, R.color.light_blue)
        41 -> return getColor(context, R.color.blue)
        42 -> return getColor(context, R.color.grape)
        43 -> return getColor(context, R.color.violet)
        44 -> return getColor(context, R.color.lavender)
        45 -> return getColor(context, R.color.magneta)
        46 -> return getColor(context, R.color.salomon)
        47 -> return getColor(context, R.color.charcoal)

        // Inbox
        else -> return Color.DKGRAY
    }
}