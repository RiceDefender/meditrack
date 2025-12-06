package kr.ac.cau.team3.meditrack

import android.content.res.Resources

/**
 * Extension function to convert DP to Pixels for dynamic UI calculations.
 */
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}