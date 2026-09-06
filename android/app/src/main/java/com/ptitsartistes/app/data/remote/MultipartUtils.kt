package com.ptitsartistes.app.data.remote

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toTextPart(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

/**
 * Lit le contenu d'un `content://` Uri sélectionné par le sélecteur de
 * photos et le transforme en partie multipart, prête à être envoyée à
 * l'API (upload d'un avatar ou d'un dessin).
 */
fun uriToImagePart(context: Context, uri: Uri, partName: String): MultipartBody.Part {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri) ?: "image/jpeg"
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw java.io.IOException("Impossible de lire l'image sélectionnée.")

    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, "upload.$extension", body)
}
