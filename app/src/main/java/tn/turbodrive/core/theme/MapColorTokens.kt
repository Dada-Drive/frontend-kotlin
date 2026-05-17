package tn.turbodrive.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Map-specific color tokens NOT part of AppColorScheme (they represent
 * cartographic features: routes, water, terrain). Will be replaced by
 * proper map tokens (mapPath, mapRoad, mapWater) in R-4.5.
 *
 * Created in R-2.3 batch 3 as a transitional structure.
 */
object MapColorTokens {
    val routeActiveBlue = Color(0xFF2D79FF)
    val routeSecondLeg = Color(0xFF43A047)
    val scheduleAccent = Color(0xFF4FC3C8)
    val darkPanelSurface = Color(0xFF2C2C2C)
    val pinIntermediate = Color(0xFF1A1A1A)
    val connectorGrey = Color(0xFF8E8E93)
}
