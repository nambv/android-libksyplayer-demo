package streaming.yeah1.com.streaming.model

import android.content.Context
import com.ksyun.media.player.KSYTextureView

class FloatingPlayer {

    var ksyTextureView: KSYTextureView? = null

    fun init(context: Context) {
        if (ksyTextureView != null) {
            ksyTextureView!!.release()
            ksyTextureView = null
        }
        ksyTextureView = KSYTextureView(context)
    }

    fun destroy() {
        ksyTextureView?.release()
        ksyTextureView = null
    }

    companion object {
        private var floatingPlayer: FloatingPlayer? = null

        fun getInstance(): FloatingPlayer {
            if (floatingPlayer == null) {
                synchronized(FloatingPlayer::class.java) {
                    if (null == floatingPlayer)
                        floatingPlayer = FloatingPlayer()
                }
            }

            return floatingPlayer as FloatingPlayer
        }
    }
}
