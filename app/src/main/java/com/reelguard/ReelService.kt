package com.reelguard

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast

class ReelService : AccessibilityService() {

    // NOTE: view IDs can change with app updates. Update these lists if detection stops working.
    private val igIds = listOf("clips_viewer_view_pager", "clips_viewer_container", "clips_tab")
    private val ytIds = listOf("reel_recycler", "reel_player_page_container", "shorts_player")

    private val h = Handler(Looper.getMainLooper())
    private var cur: String? = null
    private var overlay: TextView? = null
    private var lastScroll = 0L
    private var lastCheck = 0L

    private val tick = object : Runnable {
        override fun run() {
            cur?.let { Prefs.addSecs(this@ReelService, it); enforce(it); refresh(it) }
            h.postDelayed(this, 1000)
        }
    }

    override fun onServiceConnected() { h.post(tick) }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val key = when (e.packageName?.toString()) {
            "com.instagram.android" -> Prefs.IG
            "com.google.android.youtube" -> Prefs.YT
            else -> { leave(); return }
        }
        val now = System.currentTimeMillis()
        when (e.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED ->
                if (cur == key && now - lastScroll > 800) {
                    lastScroll = now
                    Prefs.addCount(this, key); enforce(key); refresh(key)
                }
            else -> if (now - lastCheck > 400) {
                lastCheck = now
                if (inReels(key, rootInActiveWindow)) enter(key) else leave()
            }
        }
    }

    private fun inReels(key: String, root: AccessibilityNodeInfo?): Boolean {
        root ?: return false
        val pkg = if (key == Prefs.IG) "com.instagram.android" else "com.google.android.youtube"
        val ids = if (key == Prefs.IG) igIds else ytIds
        return ids.any { root.findAccessibilityNodeInfosByViewId("$pkg:id/$it").isNotEmpty() }
    }

    private fun enter(key: String) {
        if (cur != key) { cur = key; showOverlay() }
        enforce(key); refresh(key)
    }

    private fun leave() { if (cur != null) { cur = null; hideOverlay() } }

    private fun enforce(key: String) {
        if (Prefs.limitReached(this, key)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this, "ReelGuard: Reels/Shorts blocked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refresh(key: String) {
        val s = Prefs.secs(this, key)
        overlay?.text = "▶ %d scrolled • %02d:%02d".format(Prefs.count(this, key), s / 60, s % 60)
    }

    private fun showOverlay() {
        if (overlay != null) return
        val tv = TextView(this).apply {
            setTextColor(Color.WHITE); setBackgroundColor(0xCC000000.toInt())
            textSize = 14f; setPadding(28, 12, 28, 12)
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, // no extra permission needed
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; y = 160 }
        (getSystemService(WINDOW_SERVICE) as WindowManager).addView(tv, lp)
        overlay = tv
    }

    private fun hideOverlay() {
        overlay?.let { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
        overlay = null
    }

    override fun onInterrupt() { leave() }
    override fun onDestroy() { h.removeCallbacksAndMessages(null); leave(); super.onDestroy() }
}
