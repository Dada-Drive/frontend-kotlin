package tn.dadadrive.data.socket

import tn.dadadrive.core.constants.Constants

object SocketUrl {
    fun build(namespace: String): String {
        val ns = if (namespace.startsWith("/")) namespace else "/$namespace"
        val base = Constants.BASE_URL.trimEnd('/')
            .removeSuffix("/api/v1")
            .removeSuffix("/api")
        return base + ns
    }
}
