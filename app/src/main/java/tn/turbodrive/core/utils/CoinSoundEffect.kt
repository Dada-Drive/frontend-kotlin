package tn.turbodrive.core.utils

import android.content.Context
import android.media.MediaPlayer

fun playCoinSoundEffect(
    context: Context,
    assetName: String = "coinsound.mpeg",
) {
    runCatching {
        val app = context.applicationContext
        val afd = app.assets.openFd(assetName)
        val mp = MediaPlayer()
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mp.setOnCompletionListener { runCatching { it.release() } }
        mp.setOnErrorListener { player, _, _ ->
            runCatching { player.release() }
            true
        }
        mp.prepare()
        mp.start()
    }
}
