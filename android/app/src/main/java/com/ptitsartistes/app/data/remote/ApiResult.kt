package com.ptitsartistes.app.data.remote

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String) : ApiResult<Nothing>
}

/**
 * Exécute un appel Retrofit `suspend` et convertit toute exception (réseau,
 * HTTP 4xx/5xx, timeout) en [ApiResult.Error] avec un message lisible,
 * plutôt que de laisser l'exception remonter jusqu'à l'UI.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: retrofit2.HttpException) {
        ApiResult.Error(parseHttpErrorMessage(e))
    } catch (e: java.io.IOException) {
        ApiResult.Error("Impossible de joindre le serveur. Vérifiez l'adresse dans les paramètres et votre connexion.")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Une erreur inattendue est survenue.")
    }
}

private fun parseHttpErrorMessage(e: retrofit2.HttpException): String {
    val body = e.response()?.errorBody()?.string()
    if (body.isNullOrBlank()) {
        return "Erreur serveur (${e.code()})."
    }
    return runCatching {
        val json = kotlinx.serialization.json.Json.parseToJsonElement(body)
        val obj = json as? kotlinx.serialization.json.JsonObject ?: return@runCatching null
        obj["detail"]?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
            ?: obj.entries.firstOrNull()?.let { (field, value) ->
                val message = when (value) {
                    is kotlinx.serialization.json.JsonArray ->
                        value.joinToString(" ") { (it as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "" }
                    is kotlinx.serialization.json.JsonPrimitive -> value.content
                    else -> null
                }
                message?.let { "$field : $it" }
            }
    }.getOrNull() ?: "Erreur serveur (${e.code()})."
}
