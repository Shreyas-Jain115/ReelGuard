package com.reelguard

import android.content.Context
import java.time.LocalDate

/** Private storage (MODE_PRIVATE): unreadable by other apps. Backup disabled in manifest. */
object Prefs {
    const val IG = "ig"; const val YT = "yt"
    const val TRACK = 0; const val LIMIT = 1; const val BLOCK = 2

    private fun sp(c: Context) = c.applicationContext.getSharedPreferences("rg_private", Context.MODE_PRIVATE)
    private fun day() = LocalDate.now().toString()

    fun mode(c: Context, k: String) = sp(c).getInt("mode_$k", TRACK)
    fun setMode(c: Context, k: String, v: Int) = sp(c).edit().putInt("mode_$k", v).apply()

    fun maxReels(c: Context, k: String) = sp(c).getInt("maxn_$k", 30)
    fun maxMins(c: Context, k: String) = sp(c).getInt("maxm_$k", 15)
    fun setLimits(c: Context, k: String, n: Int, m: Int) =
        sp(c).edit().putInt("maxn_$k", n).putInt("maxm_$k", m).apply()

    fun count(c: Context, k: String) = sp(c).getInt("n_${k}_${day()}", 0)
    fun secs(c: Context, k: String) = sp(c).getInt("s_${k}_${day()}", 0)
    fun addCount(c: Context, k: String) = sp(c).edit().putInt("n_${k}_${day()}", count(c, k) + 1).apply()
    fun addSecs(c: Context, k: String) = sp(c).edit().putInt("s_${k}_${day()}", secs(c, k) + 1).apply()

    fun limitReached(c: Context, k: String) = when (mode(c, k)) {
        BLOCK -> true
        LIMIT -> count(c, k) >= maxReels(c, k) || secs(c, k) >= maxMins(c, k) * 60
        else -> false
    }
}
