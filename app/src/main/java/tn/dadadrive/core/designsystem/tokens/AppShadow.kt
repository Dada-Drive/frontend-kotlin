package tn.dadadrive.core.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import tn.dadadrive.core.designsystem.spacing.AppRadius

private val shadowAmbientSoft = Color.Black.copy(alpha = 0.08f)
private val shadowAmbientMedium = Color.Black.copy(alpha = 0.12f)

/**
 * Shadow tokens (design-system.md §6) — single-layer [Modifier.shadow] approximations.
 */
object AppShadow {
    fun card(shape: Shape = RoundedCornerShape(AppRadius.xl)): Modifier =
        Modifier.shadow(8.dp, shape, ambientColor = shadowAmbientSoft, spotColor = shadowAmbientSoft)

    fun sheet(shape: Shape = RoundedCornerShape(topStart = AppRadius.l, topEnd = AppRadius.l)): Modifier =
        Modifier.shadow(16.dp, shape, ambientColor = shadowAmbientMedium, spotColor = shadowAmbientSoft)

    fun search(shape: Shape = RoundedCornerShape(AppRadius.full)): Modifier =
        Modifier.shadow(6.dp, shape, ambientColor = shadowAmbientSoft, spotColor = shadowAmbientSoft)

    fun control(shape: Shape = RoundedCornerShape(AppRadius.full)): Modifier =
        Modifier.shadow(3.dp, shape, ambientColor = shadowAmbientSoft, spotColor = shadowAmbientSoft)

    fun toast(shape: Shape = RoundedCornerShape(AppRadius.m)): Modifier =
        Modifier.shadow(6.dp, shape, ambientColor = shadowAmbientSoft, spotColor = shadowAmbientSoft)

    fun pop(shape: Shape = RoundedCornerShape(topStart = AppRadius.l, topEnd = AppRadius.l)): Modifier =
        Modifier.shadow(18.dp, shape, ambientColor = shadowAmbientMedium, spotColor = shadowAmbientSoft)
}

fun Modifier.appShadowCard(shape: Shape = RoundedCornerShape(AppRadius.xl)): Modifier =
    this.then(AppShadow.card(shape))
