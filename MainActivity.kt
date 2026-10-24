package com.vibecut.studio

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import android.widget.MediaController

class MainActivity : Activity() {
    private lateinit var videoView: VideoView
    private lateinit var statusText: TextView
    private var currentVideo: Uri? = null

    private val background = Color.rgb(15, 17, 29)
    private val panel = Color.rgb(28, 31, 49)
    private val accent = Color.rgb(139, 92, 246)
    private val textColor = Color.rgb(245, 245, 255)
    private val muted = Color.rgb(174, 178, 201)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
        window.decorView.systemUiVisibility = 0
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(background)
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        val brand = TextView(this).apply {
            text = "VibeCut Studio"
            textSize = 27f
            setTextColor(textColor)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(brand, matchWrap())

        val subtitle = TextView(this).apply {
            text = "Create something worth watching."
            textSize = 14f
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(18))
        }
        root.addView(subtitle, matchWrap())

        val previewCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(panel)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        videoView = VideoView(this).apply {
            setBackgroundColor(Color.BLACK)
            setMediaController(MediaController(this@MainActivity))
        }
        previewCard.addView(videoView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { height = dp(250) })

        statusText = TextView(this).apply {
            text = "Choose a video to start editing"
            textSize = 14f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }
        previewCard.addView(statusText, matchWrap())
        root.addView(previewCard, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(310)
        ))

        val importButton = makeButton("＋  Import video", true)
        importButton.setOnClickListener { chooseVideo() }
        root.addView(importButton, buttonParams())

        val section = TextView(this).apply {
            text = "QUICK TOOLS"
            textSize = 12f
            letterSpacing = 0.12f
            setTextColor(muted)
            setPadding(0, dp(18), 0, dp(8))
        }
        root.addView(section, matchWrap())

        val toolRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf("Trim", "Speed", "Filters").forEach { label ->
            val button = makeButton(label, false)
            button.isEnabled = currentVideo != null
            toolRow.addView(button, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(7)
            })
            button.setOnClickListener {
                statusText.text = "$label tool is planned for a future version."
            }
        }
        root.addView(toolRow, matchWrap())

        val note = TextView(this).apply {
            text = "Starter version • Import and preview videos. More editing tools can be added next."
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(18), 0, 0)
        }
        root.addView(note, matchWrap())

        setContentView(root)
    }

    private fun chooseVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, REQUEST_VIDEO)
    }

    @Deprecated("Use Activity Result APIs in a future update")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            currentVideo = uri
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some document providers do not grant persistable permissions.
            }
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                videoView.start()
            }
            statusText.text = "Video loaded • Use the player controls to preview"
        }
    }

    private fun makeButton(label: String, primary: Boolean): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(textColor)
            setBackgroundColor(if (primary) accent else panel)
            minimumHeight = dp(46)
            stateListAnimator = null
        }
    }

    private fun buttonParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
    ).apply { topMargin = dp(14) }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val REQUEST_VIDEO = 1001
    }
}
