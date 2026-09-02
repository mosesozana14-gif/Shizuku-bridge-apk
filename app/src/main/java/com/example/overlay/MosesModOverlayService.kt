package com.example.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MosesModOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingPillView: View? = null
    private var hudOverlayView: View? = null

    private var pillParams: WindowManager.LayoutParams? = null
    private var hudParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        const val CHANNEL_ID = "moses_mod_overlay_channel"
        const val NOTIFICATION_ID = 9991
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        setupFloatingPill()
        setupHudOverlay()

        MosesModRepository.setOverlayActive(true)

        serviceScope.launch {
            MosesModRepository.isHudExpanded.collectLatest { expanded ->
                if (expanded) {
                    showHud()
                } else {
                    hideHud()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Moses Mod Menu Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating mod menu overlay for BitLife"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Moses Mod Menu Overlay Active")
            .setContentText("Moses is the GOAT - BitLife Mod HUD running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun getOverlayWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingPill() {
        val layoutType = getOverlayWindowType()
        pillParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        val pillLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 20, 32, 20)
            gravity = Gravity.CENTER_VERTICAL

            // Gradient pill background
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 60f
                colors = intArrayOf(Color.parseColor("#43664E"), Color.parseColor("#1B4D3E"))
                setStroke(3, Color.parseColor("#C4EED0"))
            }
            background = shape
            elevation = 20f
        }

        val iconTv = TextView(this).apply {
            text = "⚡"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 14, 0)
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleTv = TextView(this).apply {
            text = "MOSES MOD MENU"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F5FAF4"))
            letterSpacing = 0.05f
        }

        val subTv = TextView(this).apply {
            text = "Moses is the GOAT • Tap for HUD"
            textSize = 10f
            setTextColor(Color.parseColor("#C4EED0"))
        }

        textLayout.addView(titleTv)
        textLayout.addView(subTv)

        pillLayout.addView(iconTv)
        pillLayout.addView(textLayout)

        // Dragging & Click touch listener
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        pillLayout.setOnTouchListener { _, event ->
            val params = pillParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(pillLayout, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        MosesModRepository.setHudExpanded(true)
                    }
                    true
                }
                else -> false
            }
        }

        floatingPillView = pillLayout
        try {
            windowManager.addView(floatingPillView, pillParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupHudOverlay() {
        val layoutType = getOverlayWindowType()
        hudParams = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            (resources.displayMetrics.heightPixels * 0.82).toInt(),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val rootCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 48f
                setColor(Color.parseColor("#15241C"))
                setStroke(3, Color.parseColor("#43664E"))
            }
            background = bg
            elevation = 30f
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 20)
        }

        val headerTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val hTitle = TextView(this).apply {
            text = "⚡ MOSES MOD MENU (THE GOAT)"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#7AD59F"))
        }

        val hSub = TextView(this).apply {
            text = "Live BitLife Power-Up & Followers Engine"
            textSize = 11f
            setTextColor(Color.parseColor("#A0BAA7"))
        }

        headerTextLayout.addView(hTitle)
        headerTextLayout.addView(hSub)

        val closeBtn = Button(this).apply {
            text = "✕"
            textSize = 14f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#2A3C31"))
            }
            layoutParams = LinearLayout.LayoutParams(90, 90)
            setOnClickListener {
                MosesModRepository.setHudExpanded(false)
            }
        }

        header.addView(headerTextLayout)
        header.addView(closeBtn)
        rootCard.addView(header)

        // Scrollable content
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Section 1: Social Media Hub
        addSectionHeader(contentLayout, "📱 SOCIAL MEDIA & SUBSCRIBERS HUB")
        val curStats = MosesModRepository.activeStats.value
        val curSoc = curStats.socialStats

        val ytInput = addNumberRow(contentLayout, "YouTube Subscribers", curSoc.youtubeSubscribers.toString(), listOf("+1M", "+10M", "+100M", "1 Billion")) { newVal ->
            MosesModRepository.updateSocialStats { it.copy(youtubeSubscribers = newVal) }
        }

        val tiktokInput = addNumberRow(contentLayout, "TikTok Followers", curSoc.tiktokFollowers.toString(), listOf("+500K", "+5M", "+50M", "100 Million")) { newVal ->
            MosesModRepository.updateSocialStats { it.copy(tiktokFollowers = newVal) }
        }

        val igInput = addNumberRow(contentLayout, "Instagram Followers", curSoc.instagramFollowers.toString(), listOf("+1M", "+10M", "+50M", "100 Million")) { newVal ->
            MosesModRepository.updateSocialStats { it.copy(instagramFollowers = newVal) }
        }

        val xInput = addNumberRow(contentLayout, "Twitter / X Followers", curSoc.twitterFollowers.toString(), listOf("+500K", "+2M", "+10M", "50 Million")) { newVal ->
            MosesModRepository.updateSocialStats { it.copy(twitterFollowers = newVal) }
        }

        val twitchInput = addNumberRow(contentLayout, "Twitch Followers", curSoc.twitchFollowers.toString(), listOf("+250K", "+1M", "+5M", "20 Million")) { newVal ->
            MosesModRepository.updateSocialStats { it.copy(twitchFollowers = newVal) }
        }

        addToggleRow(contentLayout, "Verified Checkmark on all Socials", curSoc.isVerified) { isChecked ->
            MosesModRepository.updateSocialStats { it.copy(isVerified = isChecked) }
        }

        addToggleRow(contentLayout, "100% Viral Boost Multiplier", curSoc.viralBoost) { isChecked ->
            MosesModRepository.updateSocialStats { it.copy(viralBoost = isChecked) }
        }

        // Section 2: Godly Power-Ups & In-Game Perks
        addSectionHeader(contentLayout, "👑 GODLY POWER-UPS & PERKS")
        val curPwr = curStats.powerUps

        addToggleRow(contentLayout, "🎰 Lottery Auto-Win 100% (Instant Jackpot)", curPwr.lotteryAutoWin) {
            MosesModRepository.updatePowerUps { p -> p.copy(lotteryAutoWin = it) }
        }

        addToggleRow(contentLayout, "🃏 Casino & Blackjack 100% Win Rate", curPwr.casino100Win) {
            MosesModRepository.updatePowerUps { p -> p.copy(casino100Win = it) }
        }

        addToggleRow(contentLayout, "🗡️ 100% Crime/Murder Success & 0% Arrest", curPwr.crime100Success) {
            MosesModRepository.updatePowerUps { p -> p.copy(crime100Success = it) }
        }

        addToggleRow(contentLayout, "🏃 100% Prison Escape (Ghost Inmate)", curPwr.prisonEscape100) {
            MosesModRepository.updatePowerUps { p -> p.copy(prisonEscape100 = it) }
        }

        addToggleRow(contentLayout, "💉 Complete Disease Immunity & Cure All", curPwr.diseaseImmunity) {
            MosesModRepository.updatePowerUps { p -> p.copy(diseaseImmunity = it) }
        }

        addToggleRow(contentLayout, "💅 Flawless Plastic Surgery (Zero Botch)", curPwr.plasticSurgeryFlawless) {
            MosesModRepository.updatePowerUps { p -> p.copy(plasticSurgeryFlawless = it) }
        }

        addToggleRow(contentLayout, "👔 Instant Promotion to CEO / Godfather", curPwr.instantPromotionCEO) {
            MosesModRepository.updatePowerUps { p -> p.copy(instantPromotionCEO = it) }
        }

        addToggleRow(contentLayout, "👶 100% Fertility + Twins/Triplets Guarantee", curPwr.fertilityTwinsTriplets) {
            MosesModRepository.updatePowerUps { p -> p.copy(fertilityTwinsTriplets = it) }
        }

        addToggleRow(contentLayout, "⏳ Unlimited Free Time Machine", curPwr.unlimitedTimeMachine) {
            MosesModRepository.updatePowerUps { p -> p.copy(unlimitedTimeMachine = it) }
        }

        scrollView.addView(contentLayout)
        rootCard.addView(scrollView)

        // Bottom Actions Bar
        val actionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }

        val statusTv = TextView(this).apply {
            text = "Ready to patch active BitLife session"
            textSize = 11f
            setTextColor(Color.parseColor("#A0BAA7"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 10)
        }

        val saveBtn = Button(this).apply {
            text = "⚡ APPLY & SAVE TO BITLIFE"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#15241C"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
                setColor(Color.parseColor("#7AD59F"))
            }
            setPadding(0, 24, 0, 24)
            setOnClickListener {
                statusTv.text = "Patching BitLife save via Shizuku..."
                MosesModRepository.applyToBitLife { success, msg ->
                    statusTv.text = msg
                    Toast.makeText(this@MosesModOverlayService, if (success) "Applied to BitLife!" else msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val unlockAllBtn = Button(this).apply {
            text = "🌟 UNLOCK ALL EXPANSIONS (GOD MODE)"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(Color.parseColor("#2A3C31"))
                setStroke(2, Color.parseColor("#43664E"))
            }
            setPadding(0, 18, 0, 18)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 12
            }
            layoutParams = params
            setOnClickListener {
                statusTv.text = "Unlocking all BitLife packs & God Mode..."
                MosesModRepository.unlockAllGodModeAndExpansions { success, msg ->
                    statusTv.text = msg
                    Toast.makeText(this@MosesModOverlayService, if (success) "All BitLife Expansions Unlocked!" else msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        actionsLayout.addView(statusTv)
        actionsLayout.addView(saveBtn)
        actionsLayout.addView(unlockAllBtn)

        rootCard.addView(actionsLayout)

        hudOverlayView = rootCard
    }

    private fun addSectionHeader(parent: LinearLayout, title: String) {
        val tv = TextView(this).apply {
            text = title
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#7AD59F"))
            setPadding(0, 24, 0, 12)
            letterSpacing = 0.05f
        }
        parent.addView(tv)
    }

    private fun addNumberRow(
        parent: LinearLayout,
        label: String,
        currentVal: String,
        presetChips: List<String>,
        onValChanged: (Long) -> Unit
    ): EditText {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#1B2E24"))
            }
            background = bg
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 14
            }
            layoutParams = p
        }

        val labelTv = TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }

        val input = EditText(this).apply {
            setText(currentVal)
            textSize = 14f
            setTextColor(Color.parseColor("#7AD59F"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f
                setColor(Color.parseColor("#15241C"))
                setStroke(1, Color.parseColor("#43664E"))
            }
            setPadding(18, 12, 18, 12)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            val ip = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 10
                bottomMargin = 10
            }
            layoutParams = ip
            addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    s?.toString()?.toLongOrNull()?.let { onValChanged(it) }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Quick chip scroll
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }
        val chipsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        presetChips.forEach { chipText ->
            val btn = Button(this).apply {
                text = chipText
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#C4EED0"))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(Color.parseColor("#2A3C31"))
                }
                setPadding(16, 6, 16, 6)
                val bp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 75).apply {
                    rightMargin = 10
                }
                layoutParams = bp
                setOnClickListener {
                    val num = when (chipText) {
                        "+250K" -> (input.text.toString().toLongOrNull() ?: 0L) + 250_000L
                        "+500K" -> (input.text.toString().toLongOrNull() ?: 0L) + 500_000L
                        "+1M" -> (input.text.toString().toLongOrNull() ?: 0L) + 1_000_000L
                        "+2M" -> (input.text.toString().toLongOrNull() ?: 0L) + 2_000_000L
                        "+5M" -> (input.text.toString().toLongOrNull() ?: 0L) + 5_000_000L
                        "+10M" -> (input.text.toString().toLongOrNull() ?: 0L) + 10_000_000L
                        "+50M" -> (input.text.toString().toLongOrNull() ?: 0L) + 50_000_000L
                        "+100M" -> (input.text.toString().toLongOrNull() ?: 0L) + 100_000_000L
                        "20 Million" -> 20_000_000L
                        "50 Million" -> 50_000_000L
                        "100 Million" -> 100_000_000L
                        "1 Billion" -> 1_000_000_000L
                        else -> 10_000_000L
                    }
                    input.setText(num.toString())
                    onValChanged(num)
                }
            }
            chipsRow.addView(btn)
        }
        scroll.addView(chipsRow)

        card.addView(labelTv)
        card.addView(input)
        card.addView(scroll)
        parent.addView(card)

        return input
    }

    private fun addToggleRow(parent: LinearLayout, title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 14, 18, 14)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#1B2E24"))
            }
            background = bg
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 10
            }
            layoutParams = p
        }

        val titleTv = TextView(this).apply {
            text = title
            textSize = 12f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val cb = CheckBox(this).apply {
            this.isChecked = isChecked
            setOnCheckedChangeListener { _, checked ->
                onCheckedChange(checked)
            }
        }

        row.addView(titleTv)
        row.addView(cb)
        parent.addView(row)
    }

    private fun showHud() {
        val hud = hudOverlayView ?: return
        if (hud.windowToken == null) {
            try {
                windowManager.addView(hud, hudParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun hideHud() {
        val hud = hudOverlayView ?: return
        if (hud.windowToken != null) {
            try {
                windowManager.removeView(hud)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MosesModRepository.setOverlayActive(false)
        MosesModRepository.setHudExpanded(false)

        floatingPillView?.let {
            if (it.windowToken != null) {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        hudOverlayView?.let {
            if (it.windowToken != null) {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
