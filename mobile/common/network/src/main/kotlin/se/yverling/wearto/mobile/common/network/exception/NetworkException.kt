package se.yverling.wearto.mobile.common.network.exception

class NetworkException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
