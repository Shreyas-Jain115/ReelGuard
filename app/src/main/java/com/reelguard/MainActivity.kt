package com.reelguard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var stats: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 40) }
        root.addView(TextView(this).apply { text = "ReelGuard"; textSize = 26f })
        root.addView(Button(this).apply {
            text = "1. Enable accessibility service"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(section("Instagram Reels", Prefs.IG))
        root.addView(section("YouTube Shorts", Prefs.YT))
        stats = TextView(this).apply { textSize = 16f; setPadding(0, 40, 0, 0) }
        root.addView(stats)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() { super.onResume(); showStats() }

    private fun showStats() {
        fun line(n: String, k: String) = "$n: ${Prefs.count(this, k)} scrolled, ${Prefs.secs(this, k) / 60} min today"
        stats.text = "Today\n" + line("Instagram Reels", Prefs.IG) + "\n" + line("YouTube Shorts", Prefs.YT)
    }

    private fun section(title: String, k: String): View {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 40, 0, 0) }
        box.addView(TextView(this).apply { text = title; textSize = 18f })
        val rg = RadioGroup(this)
        listOf("Track only", "Limit", "Block").forEachIndexed { i, t ->
            rg.addView(RadioButton(this).apply { text = t; id = 100 + i })
        }
        rg.check(100 + Prefs.mode(this, k))
        val n = EditText(this).apply { hint = "Max reels/day"; setText("${Prefs.maxReels(this@MainActivity, k)}"); inputType = 2 }
        val m = EditText(this).apply { hint = "Max minutes/day"; setText("${Prefs.maxMins(this@MainActivity, k)}"); inputType = 2 }
        val save = Button(this).apply {
            text = "Save"
            setOnClickListener {
                Prefs.setMode(this@MainActivity, k, rg.checkedRadioButtonId - 100)
                Prefs.setLimits(this@MainActivity, k, n.text.toString().toIntOrNull() ?: 30, m.text.toString().toIntOrNull() ?: 15)
                Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
            }
        }
        listOf(rg, n, m, save).forEach { box.addView(it) }
        return box
    }
}
