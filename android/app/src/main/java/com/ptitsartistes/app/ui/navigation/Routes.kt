package com.ptitsartistes.app.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val HOME = "home"

    const val PARENT_LOGIN = "parent_login"
    const val PARENT_REGISTER = "parent_register"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val SETTINGS = "settings"

    const val ENFANT_FORM = "enfant_form?enfantId={enfantId}"
    fun enfantForm(enfantId: Int? = null) = "enfant_form?enfantId=${enfantId ?: -1}"

    const val DESSIN_UPLOAD_PARENT = "dessin_upload_parent?enfantId={enfantId}"
    fun dessinUploadParent(enfantId: Int? = null) = "dessin_upload_parent?enfantId=${enfantId ?: -1}"

    const val ENFANT_SELECT = "enfant_select"

    const val ENFANT_PIN = "enfant_pin/{enfantId}/{prenom}"
    fun enfantPin(enfantId: Int, prenom: String): String {
        val encoded = URLEncoder.encode(prenom, StandardCharsets.UTF_8.name())
        return "enfant_pin/$enfantId/$encoded"
    }

    const val ENFANT_DASHBOARD = "enfant_dashboard"
    const val ENFANT_DESSIN_UPLOAD = "enfant_dessin_upload"
}
