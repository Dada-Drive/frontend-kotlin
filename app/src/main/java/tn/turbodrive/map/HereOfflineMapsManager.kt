package tn.turbodrive.map

import android.content.Context
import android.util.Log
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.LayerConfiguration
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import java.io.File
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

internal object HereOfflineMapsManager {
    private const val TAG = "HereOfflineMaps"
    private val initGuard = AtomicBoolean(false)

    fun buildLayerConfiguration(): LayerConfiguration {
        val features =
            arrayListOf(
                LayerConfiguration.Feature.DETAIL_RENDERING,
                LayerConfiguration.Feature.RENDERING,
                LayerConfiguration.Feature.OFFLINE_SEARCH,
            )
        return LayerConfiguration().also { config ->
            config.enabledFeatures = features
            config.implicitlyPrefetchedFeatures = features
        }
    }

    fun cachePath(context: Context): String = File(context.cacheDir, "here-sdk/cache").apply { mkdirs() }.absolutePath

    fun persistentMapPath(context: Context): String = File(context.filesDir, "here-sdk/persistent-map").apply { mkdirs() }.absolutePath

    fun createSdkOptions(
        applicationContext: Context,
        authenticationMode: AuthenticationMode,
    ): SDKOptions {
        val options = SDKOptions(authenticationMode)
        tryInvokeSetter(options, "setCachePath", cachePath(applicationContext))
        tryInvokeSetter(options, "setPersistentMapStoragePath", persistentMapPath(applicationContext))
        tryInvokeSetter(options, "setLayerConfiguration", buildLayerConfiguration())
        return options
    }

    fun initializeOnce() {
        if (!initGuard.compareAndSet(false, true)) return
        val engine = SDKNativeEngine.getSharedInstance()
        if (engine == null) {
            Log.w(TAG, "SDKNativeEngine not ready, skip offline setup.")
            return
        }
        Log.i(
            TAG,
            "HERE paths cache=${engine.options.cachePath}, persistent=${engine.options.persistentMapStoragePath}",
        )
        ensureTunisiaOfflineDownloadedIfSupported(engine)
    }

    private fun tryInvokeSetter(
        target: Any,
        methodName: String,
        arg: Any,
    ) {
        runCatching {
            val method =
                target.javaClass.methods.firstOrNull { candidate ->
                    candidate.name == methodName && candidate.parameterTypes.size == 1
                } ?: return@runCatching
            method.invoke(target, arg)
            Log.i(TAG, "Applied HERE option via reflection: $methodName")
        }.onFailure { err ->
            Log.w(TAG, "Could not apply HERE option $methodName: ${err.message}")
        }
    }

    private fun ensureTunisiaOfflineDownloadedIfSupported(engine: SDKNativeEngine) {
        val mapDownloaderClass =
            runCatching {
                Class.forName("com.here.sdk.maploader.MapDownloader")
            }.getOrNull() ?: run {
                Log.w(TAG, "MapDownloader API unavailable in current HERE SDK edition.")
                return
            }
        val constructionCbClass =
            runCatching {
                Class.forName("com.here.sdk.maploader.MapDownloaderConstructionCallback")
            }.getOrNull() ?: return

        val constructionCb =
            Proxy.newProxyInstance(
                constructionCbClass.classLoader,
                arrayOf(constructionCbClass),
            ) { _, method, args ->
                if (method.name.contains("onMapDownloaderConstructed", ignoreCase = true)) {
                    val downloader = args?.firstOrNull() ?: return@newProxyInstance null
                    requestTunisiaDownload(downloader)
                }
                null
            }

        runCatching {
            val fromEngineAsync =
                mapDownloaderClass.methods.firstOrNull { candidate ->
                    candidate.name == "fromEngineAsync" && candidate.parameterTypes.size == 2
                } ?: return
            fromEngineAsync.invoke(null, engine, constructionCb)
        }.onFailure { err ->
            Log.w(TAG, "Failed to initialize MapDownloader: ${err.message}")
        }
    }

    private fun requestTunisiaDownload(mapDownloader: Any) {
        val downloadableCbClass =
            runCatching {
                Class.forName("com.here.sdk.maploader.DownloadableRegionsCallback")
            }.getOrNull() ?: return
        val languageCodeClass =
            runCatching {
                Class.forName("com.here.sdk.core.LanguageCode")
            }.getOrNull() ?: return
        val languageEnUs =
            runCatching {
                java.lang.Enum.valueOf(languageCodeClass as Class<out Enum<*>>, "EN_US")
            }.getOrNull() ?: return

        val callback =
            Proxy.newProxyInstance(
                downloadableCbClass.classLoader,
                arrayOf(downloadableCbClass),
            ) { _, method, args ->
                if (method.name == "onCompleted") {
                    val error = args?.getOrNull(0)

                    @Suppress("UNCHECKED_CAST")
                    val regions = args?.getOrNull(1) as? List<Any>
                    if (error != null) {
                        Log.w(TAG, "Downloadable regions query failed: $error")
                        return@newProxyInstance null
                    }
                    handleTunisiaRegions(mapDownloader, regions.orEmpty())
                }
                null
            }

        runCatching {
            val fn =
                mapDownloader.javaClass.methods.firstOrNull { candidate ->
                    candidate.name == "getDownloadableRegions" && candidate.parameterTypes.size == 2
                } ?: return
            fn.invoke(mapDownloader, languageEnUs, callback)
        }.onFailure { err ->
            Log.w(TAG, "Failed getDownloadableRegions: ${err.message}")
        }
    }

    private fun handleTunisiaRegions(
        mapDownloader: Any,
        topRegions: List<Any>,
    ) {
        val all = flattenRegions(topRegions)
        val tunisia =
            all.firstOrNull { region ->
                val name = regionString(region, "getName").trim().lowercase()
                name == "tunisia" || name == "tunisie" || name == "تونس"
            } ?: run {
                Log.w(TAG, "Tunisia region not found in HERE downloadable regions.")
                return
            }
        val regionId =
            runCatching { tunisia.javaClass.getMethod("getRegionId").invoke(tunisia) }.getOrNull()
                ?: return
        val regionIdValue = regionString(regionId, "getId")
        val installed = installedRegionIds(mapDownloader)
        if (installed.contains(regionIdValue)) {
            Log.i(TAG, "Tunisia already installed in offline maps.")
            return
        }
        startRegionDownload(mapDownloader, regionId)
    }

    private fun installedRegionIds(mapDownloader: Any): Set<String> {
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            val installed =
                mapDownloader.javaClass.getMethod("getInstalledRegions")
                    .invoke(mapDownloader) as? List<Any> ?: return emptySet<String>()
            installed.mapNotNull { item ->
                val regionId = runCatching { item.javaClass.getMethod("getRegionId").invoke(item) }.getOrNull()
                regionId?.let { regionString(it, "getId") }
            }.toSet()
        }.getOrElse { emptySet() }
    }

    private fun startRegionDownload(
        mapDownloader: Any,
        regionId: Any,
    ) {
        val statusCbClass =
            runCatching {
                Class.forName("com.here.sdk.maploader.DownloadRegionsStatusListener")
            }.getOrNull() ?: return
        val statusCb =
            Proxy.newProxyInstance(
                statusCbClass.classLoader,
                arrayOf(statusCbClass),
            ) { _, method, args ->
                when (method.name) {
                    "onProgress" -> {
                        val region = args?.getOrNull(0)
                        val percent = args?.getOrNull(1)
                        Log.d(TAG, "Tunisia offline download progress ${region?.let { regionString(it, "getId") }}: $percent%")
                    }
                    "onDownloadRegionsComplete" -> {
                        val error = args?.getOrNull(0)
                        if (error != null) {
                            Log.e(TAG, "Tunisia offline download failed: $error")
                        } else {
                            Log.i(TAG, "Tunisia offline download completed.")
                        }
                    }
                    "onPause" -> Log.w(TAG, "Tunisia offline download paused: ${args?.getOrNull(0)}")
                    "onResume" -> Log.i(TAG, "Tunisia offline download resumed.")
                }
                null
            }
        runCatching {
            val fn =
                mapDownloader.javaClass.methods.firstOrNull { candidate ->
                    candidate.name == "downloadRegions" && candidate.parameterTypes.size == 2
                } ?: return
            fn.invoke(mapDownloader, listOf(regionId), statusCb)
            Log.i(TAG, "Started Tunisia offline download.")
        }.onFailure { err ->
            Log.w(TAG, "Failed to start Tunisia download: ${err.message}")
        }
    }

    private fun flattenRegions(input: List<Any>): List<Any> {
        val out = ArrayList<Any>()
        val stack = ArrayDeque<Any>()
        stack.addAll(input)
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            out.add(current)
            val children =
                runCatching {
                    @Suppress("UNCHECKED_CAST")
                    current.javaClass.getMethod("getChildRegions").invoke(current) as? List<Any>
                }.getOrNull().orEmpty()
            stack.addAll(children)
        }
        return out
    }

    private fun regionString(
        target: Any,
        getter: String,
    ): String =
        runCatching { target.javaClass.getMethod(getter).invoke(target)?.toString().orEmpty() }
            .getOrDefault("")
}
