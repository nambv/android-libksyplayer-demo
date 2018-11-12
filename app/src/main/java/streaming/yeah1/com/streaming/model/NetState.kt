package streaming.yeah1.com.streaming.model

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by liubohua on 2017/1/6.
 */
@Suppress("DEPRECATION")
object NetState {

    val NETWORK_NONE = 997
    val NETWORK_WIFI = NETWORK_NONE + 1
    val NETWORK_MOBILE = NETWORK_WIFI + 1

    fun getNetWorkState(context: Context): Int {

        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        val dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected) {
            return NETWORK_WIFI
        }
        return if (dataNetworkInfo != null && dataNetworkInfo.isConnected) {
            NETWORK_MOBILE
        } else NETWORK_NONE
    }
}
