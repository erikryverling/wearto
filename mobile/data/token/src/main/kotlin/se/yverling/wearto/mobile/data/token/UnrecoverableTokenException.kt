package se.yverling.wearto.mobile.data.token

class UnrecoverableTokenException(
    message: String = "Stored Todoist credential could not be read or decrypted",
    cause: Throwable? = null,
) : Exception(message, cause)
