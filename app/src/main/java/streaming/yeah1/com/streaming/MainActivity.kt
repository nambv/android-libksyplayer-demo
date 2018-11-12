package streaming.yeah1.com.streaming

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import com.ksyun.media.player.IMediaPlayer
import com.ksyun.media.player.KSYHardwareDecodeWhiteList
import com.ksyun.media.player.KSYMediaPlayer
import kotlinx.android.synthetic.main.activity_main.*
import streaming.yeah1.com.streaming.model.FloatingPlayer
import streaming.yeah1.com.streaming.utils.Dialog
import streaming.yeah1.com.streaming.utils.Setting
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mTouching: Boolean = false

    private var lastSpan: Double = 0.toDouble()
    private var centerPointX: Float = 0.toFloat()
    private var centerPointY: Float = 0.toFloat()
    private var lastMoveX = -1f
    private var lastMoveY = -1f

    private var movedDeltaX: Float = 0.toFloat()
    private var movedDeltaY: Float = 0.toFloat()

    private var totalRatio: Float = 0.toFloat()
    private var deltaRatio: Float = 0.toFloat()

    private lateinit var videoFile: File
    private lateinit var imageFile: File

    private lateinit var mSettings: SharedPreferences
    private var editor: SharedPreferences.Editor? = null

    private var toFloatingWindow: Boolean = false

    private var startVol: Float = 0.toFloat()
    private var chooseDecode: String? = null
    private var bufferTime: Int = 0
    private var bufferSize: Int = 0
    private var prepareTimeout: Int = 0
    private var readTimeout: Int = 0
    private var useHwDecoder = false

    private var url: String = ""

    private lateinit var floatingPlayer: FloatingPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        floatingPlayer = FloatingPlayer.getInstance()

        url = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov"
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        editor = mSettings?.edit()

        initView()
        initFile()

        if (floatingPlayer.ksyTextureView == null)
            startToPlay()
        else
            resumeToPlay()
    }

    override fun onRestart() {
        super.onRestart()
        startToPlay()
    }

    override fun onPause() {
        super.onPause()
        if (toFloatingWindow) {
            video.removeView(floatingPlayer.ksyTextureView)
            if (floatingPlayer.ksyTextureView != null) {
                floatingPlayer.ksyTextureView.setOnTouchListener(null)
                floatingPlayer
                    .ksyTextureView.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            }
        } else {
            floatingPlayer.ksyTextureView.pause()
        }
    }

    private fun initFile() {

        videoFile = File(Environment.getExternalStorageDirectory(), "DCIM/video")
        imageFile = File(Environment.getExternalStorageDirectory(), "DCIM/image")

        if (!videoFile.exists()) videoFile.mkdir()
        if (!imageFile.exists()) imageFile.mkdir()
    }

    private fun initView() {

        verticalSeekBar.visibility = View.GONE
        verticalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
                floatingPlayer.ksyTextureView.setVolume(i.toFloat() / 100, i.toFloat() / 100)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
    }

    private fun startToPlay() {

        floatingPlayer.init(applicationContext)
        video.addView(floatingPlayer.ksyTextureView)

        floatingPlayer.ksyTextureView.setOnTouchListener(touchListener)
        floatingPlayer.ksyTextureView.setOnPreparedListener(preparedListener)
        floatingPlayer.ksyTextureView.setOnErrorListener(errorListener)
        floatingPlayer.ksyTextureView.setOnInfoListener(infoListener)
        floatingPlayer.ksyTextureView.setOnCompletionListener(completionListener)
        floatingPlayer.ksyTextureView.setVolume(1.0f, 1.0f)

        startVol = 1.0f
        chooseDecode = mSettings.getString("choose_decode", "undefined")
        bufferTime = mSettings.getInt("buffertime", 2)
        bufferSize = mSettings.getInt("buffersize", 15)
        prepareTimeout = mSettings.getInt("preparetimeout", 5)
        readTimeout = mSettings.getInt("readtimeout", 30)
        if (bufferTime > 0) {
            floatingPlayer.ksyTextureView.bufferTimeMax = bufferTime.toFloat()
        }

        if (bufferSize > 0) {
            floatingPlayer.ksyTextureView.setBufferSize(bufferSize)
        }
        if (prepareTimeout > 0 && readTimeout > 0) {
            floatingPlayer.ksyTextureView.setTimeout(prepareTimeout, readTimeout)
        }

        useHwDecoder = chooseDecode == Setting.USEHARD

        if (useHwDecoder) {
            if (KSYHardwareDecodeWhiteList.getInstance().currentStatus == KSYHardwareDecodeWhiteList.KSY_STATUS_OK) {
                if (KSYHardwareDecodeWhiteList.getInstance().supportHardwareDecodeH264() || KSYHardwareDecodeWhiteList.getInstance().supportHardwareDecodeH265())
                    floatingPlayer.ksyTextureView.setDecodeMode(KSYMediaPlayer.KSYDecodeMode.KSY_DECODE_MODE_AUTO)
            }
        }

        try {
            floatingPlayer.ksyTextureView.dataSource = url
            floatingPlayer.ksyTextureView.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        editor?.putBoolean("isPlaying", true)
        editor?.apply()
    }

    private fun resumeToPlay() {
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        editor = mSettings?.edit()
        video.addView(floatingPlayer.ksyTextureView)
        floatingPlayer.ksyTextureView.visibility = View.VISIBLE
        floatingPlayer.ksyTextureView.isComeBackFromShare = true
        editor?.putBoolean("isPlaying", true)
        editor?.apply()
        floatingPlayer.ksyTextureView.setOnTouchListener(touchListener)
    }

    private fun videoPlayEnd() {
        if (floatingPlayer.ksyTextureView != null) {
            floatingPlayer.destroy()
        }

        editor?.putBoolean("isPlaying", false)
        editor?.commit()
    }

    private fun getCurrentSpan(event: MotionEvent): Double {
        val disX = Math.abs(event.getX(0) - event.getX(1))
        val disY = Math.abs(event.getY(0) - event.getY(1))
        return Math.sqrt((disX * disX + disY * disY).toDouble())
    }

    private fun getFocusX(event: MotionEvent): Float {
        val xPoint0 = event.getX(0)
        val xPoint1 = event.getX(1)
        return (xPoint0 + xPoint1) / 2
    }

    private fun getFocusY(event: MotionEvent): Float {
        val yPoint0 = event.getY(0)
        val yPoint1 = event.getY(1)
        return (yPoint0 + yPoint1) / 2
    }

    private fun dealTouchEvent() {
        verticalSeekBar.visibility = View.GONE
        if (content.visibility == View.VISIBLE) {
            content.visibility = View.GONE
            hideStatusBar()
        } else if (content.visibility == View.GONE) {
            content.visibility = View.VISIBLE
            showStatusBar()
        }
    }

    private fun showStatusBar() {
        val params = window.attributes
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        window.attributes = params
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun hideStatusBar() {
        val params = window.attributes
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        window.attributes = params
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private val touchListener = View.OnTouchListener { v, event ->

        val videoView = floatingPlayer.ksyTextureView
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> mTouching = false
            MotionEvent.ACTION_POINTER_DOWN -> {
                mTouching = true
                if (event.pointerCount == 2) {
                    lastSpan = getCurrentSpan(event)
                    centerPointX = getFocusX(event)
                    centerPointY = getFocusY(event)
                }
            }
            MotionEvent.ACTION_MOVE -> if (event.pointerCount == 1) {
                val posX = event.x
                val posY = event.y
                if (lastMoveX == -1f && lastMoveX == -1f) {
                    lastMoveX = posX
                    lastMoveY = posY
                }
                movedDeltaX = posX - lastMoveX
                movedDeltaY = posY - lastMoveY

                if (Math.abs(movedDeltaX) > 5 || Math.abs(movedDeltaY) > 5) {
                    //判断调节音量和亮度 还是缩放画面
                    videoView?.moveVideo(movedDeltaX, movedDeltaY)
                    mTouching = true
                }
                lastMoveX = posX
                lastMoveY = posY

            } else if (event.pointerCount == 2) {
                val spans = getCurrentSpan(event)
                if (spans > 5) {
                    deltaRatio = (spans / lastSpan).toFloat()
                    totalRatio = videoView!!.videoScaleRatio * deltaRatio
                    videoView.setVideoScaleRatio(totalRatio, centerPointX, centerPointY)
                    lastSpan = spans
                }
            }
            MotionEvent.ACTION_POINTER_UP -> if (event.pointerCount == 2) {
                lastMoveX = -1f
                lastMoveY = -1f
            }
            MotionEvent.ACTION_UP -> {
                lastMoveX = -1f
                lastMoveY = -1f
                if (!mTouching) {
                    dealTouchEvent()
                }
            }
            else -> {
            }
        }
        return@OnTouchListener true
    }

    private val preparedListener = IMediaPlayer.OnPreparedListener {
        floatingPlayer
            .ksyTextureView.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        floatingPlayer.ksyTextureView.start()
    }

    private val errorListener = IMediaPlayer.OnErrorListener { _, what, _ ->
        Toast.makeText(
            this,
            "The player encountered an error, the playback has exited, the error code:$what",
            Toast.LENGTH_SHORT
        ).show()
        videoPlayEnd()
        return@OnErrorListener false
    }

    private val infoListener = IMediaPlayer.OnInfoListener { iMediaPlayer, i, i1 ->
        when (i) {
            IMediaPlayer.MEDIA_INFO_BUFFERING_START -> Dialog.show()
            IMediaPlayer.MEDIA_INFO_BUFFERING_END -> Dialog.dismiss()
        }
        return@OnInfoListener false
    }

    private val completionListener = IMediaPlayer.OnCompletionListener { }
}