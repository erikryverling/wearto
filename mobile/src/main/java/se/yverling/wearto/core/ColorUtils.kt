package se.yverling.wearto.core

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat.getColor
import se.yverling.wearto.R

fun projectColorToColorHex(context: Context, code: Int): Int {
    when (code) {
        0 -> return getColor(context, R.color.lime)
        1 -> return getColor(context, R.color.salmon)
        2 -> return getColor(context, R.color.peach)
        3 -> return getColor(context, R.color.canary)
        4 -> return getColor(context, R.color.slate)
        5 -> return getColor(context, R.color.cafe)
        6 -> return getColor(context, R.color.orchid)
        7 -> return getColor(context, R.color.silver)
        8 -> return getColor(context, R.color.coral)
        9 -> return getColor(context, R.color.amber)
        10 -> return getColor(context, R.color.turquoise)
        11 -> return getColor(context, R.color.aqua)
        12 -> return getColor(context, R.color.raspberry)
        13 -> return getColor(context, R.color.cherry)
        14 -> return getColor(context, R.color.ruby)
        15 -> return getColor(context, R.color.pistachio)
        16 -> return getColor(context, R.color.teal)
        17 -> return getColor(context, R.color.lagoon)
        18 -> return getColor(context, R.color.sky)
        19 -> return getColor(context, R.color.sapphire)
        20 -> return getColor(context, R.color.onyx)
        21 -> return getColor(context, R.color.steel)
        else -> return Color.MAGENTA
    }
}