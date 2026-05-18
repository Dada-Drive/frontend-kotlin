package tn.turbodrive.core.designsystem.tokens

import androidx.annotation.DrawableRes
import com.turbodrive.R

/**
 * Design system v2 icon registry (R-4.3).
 *
 * Maps semantic icon names to vector drawable resource IDs. All 93 icons are
 * ported from `turbodrive_redesign/icons.jsx` :
 * - 87 stroke icons (Lucide-style, 24×24, stroke 2dp, round caps)
 * - 6 brand icons (Google, Facebook, WhatsApp, Apple, Mastercard, Visa)
 *
 * Usage in Compose :
 * ```
 * Icon(
 *     painter = painterResource(AppIcon.arrowLeft),
 *     contentDescription = "Back",
 *     tint = LocalAppColors.current.textPrimary,
 * )
 * ```
 *
 * Naming convention : lowerCamelCase matching the React source `name` attribute
 * (`arrow-left` -> `arrowLeft`). Brand icons are prefixed with `brand`
 * (`brandGoogle`, `brandFacebook`, etc.) and rendered with their fixed
 * brand colors -- do NOT apply theme tint.
 */
object AppIcon {
    // ── Navigation (12) ─────────────────────────────────────────────────────

    @DrawableRes val arrowLeft: Int = R.drawable.ic_arrow_left

    @DrawableRes val arrowRight: Int = R.drawable.ic_arrow_right

    @DrawableRes val arrowUp: Int = R.drawable.ic_arrow_up

    @DrawableRes val arrowDown: Int = R.drawable.ic_arrow_down

    @DrawableRes val chevronLeft: Int = R.drawable.ic_chevron_left

    @DrawableRes val chevronRight: Int = R.drawable.ic_chevron_right

    @DrawableRes val chevronUp: Int = R.drawable.ic_chevron_up

    @DrawableRes val chevronDown: Int = R.drawable.ic_chevron_down

    @DrawableRes val arrowUpRight: Int = R.drawable.ic_arrow_up_right

    @DrawableRes val arrowDownLeft: Int = R.drawable.ic_arrow_down_left

    @DrawableRes val arrowUpCircle: Int = R.drawable.ic_arrow_up_circle

    @DrawableRes val arrowDownCircle: Int = R.drawable.ic_arrow_down_circle

    // ── Actions (12) ────────────────────────────────────────────────────────

    @DrawableRes val close: Int = R.drawable.ic_close

    @DrawableRes val check: Int = R.drawable.ic_check

    @DrawableRes val plus: Int = R.drawable.ic_plus

    @DrawableRes val minus: Int = R.drawable.ic_minus

    @DrawableRes val moreH: Int = R.drawable.ic_more_h

    @DrawableRes val moreV: Int = R.drawable.ic_more_v

    @DrawableRes val menu: Int = R.drawable.ic_menu

    @DrawableRes val search: Int = R.drawable.ic_search

    @DrawableRes val filter: Int = R.drawable.ic_filter

    @DrawableRes val edit: Int = R.drawable.ic_edit

    @DrawableRes val trash: Int = R.drawable.ic_trash

    @DrawableRes val refresh: Int = R.drawable.ic_refresh

    // ── Visibility / Lock (4) ───────────────────────────────────────────────

    @DrawableRes val eye: Int = R.drawable.ic_eye

    @DrawableRes val eyeOff: Int = R.drawable.ic_eye_off

    @DrawableRes val lock: Int = R.drawable.ic_lock

    @DrawableRes val unlock: Int = R.drawable.ic_unlock

    // ── User & Communication (7) ────────────────────────────────────────────

    @DrawableRes val user: Int = R.drawable.ic_user

    @DrawableRes val users: Int = R.drawable.ic_users

    @DrawableRes val phone: Int = R.drawable.ic_phone

    @DrawableRes val mail: Int = R.drawable.ic_mail

    @DrawableRes val message: Int = R.drawable.ic_message

    @DrawableRes val mic: Int = R.drawable.ic_mic

    @DrawableRes val fingerprint: Int = R.drawable.ic_fingerprint

    // ── Home & Work (4) ─────────────────────────────────────────────────────

    @DrawableRes val home: Int = R.drawable.ic_home

    @DrawableRes val briefcase: Int = R.drawable.ic_briefcase

    @DrawableRes val shoppingBag: Int = R.drawable.ic_shopping_bag

    @DrawableRes val gift: Int = R.drawable.ic_gift

    // ── Time & Tagging (4) ──────────────────────────────────────────────────

    @DrawableRes val clock: Int = R.drawable.ic_clock

    @DrawableRes val calendar: Int = R.drawable.ic_calendar

    @DrawableRes val flag: Int = R.drawable.ic_flag

    @DrawableRes val tag: Int = R.drawable.ic_tag

    // ── Map & Location (6) ──────────────────────────────────────────────────

    @DrawableRes val mapPin: Int = R.drawable.ic_map_pin

    @DrawableRes val map: Int = R.drawable.ic_map

    @DrawableRes val navigation: Int = R.drawable.ic_navigation

    @DrawableRes val navigation2: Int = R.drawable.ic_navigation_2

    @DrawableRes val compass: Int = R.drawable.ic_compass

    @DrawableRes val globe: Int = R.drawable.ic_globe

    // ── Vehicle (1) ─────────────────────────────────────────────────────────

    @DrawableRes val car: Int = R.drawable.ic_car

    // ── Feedback / Status (8) ───────────────────────────────────────────────

    @DrawableRes val help: Int = R.drawable.ic_help

    @DrawableRes val info: Int = R.drawable.ic_info

    @DrawableRes val alert: Int = R.drawable.ic_alert

    @DrawableRes val alertTriangle: Int = R.drawable.ic_alert_triangle

    @DrawableRes val circle: Int = R.drawable.ic_circle

    @DrawableRes val circleCheck: Int = R.drawable.ic_circle_check

    @DrawableRes val circleX: Int = R.drawable.ic_circle_x

    @DrawableRes val plusCircle: Int = R.drawable.ic_plus_circle

    // ── Bookmarks / Notifications (5) ───────────────────────────────────────

    @DrawableRes val star: Int = R.drawable.ic_star

    @DrawableRes val heart: Int = R.drawable.ic_heart

    @DrawableRes val bookmark: Int = R.drawable.ic_bookmark

    @DrawableRes val bell: Int = R.drawable.ic_bell

    @DrawableRes val share: Int = R.drawable.ic_share

    // ── Files & Media (8) ───────────────────────────────────────────────────

    @DrawableRes val copy: Int = R.drawable.ic_copy

    @DrawableRes val download: Int = R.drawable.ic_download

    @DrawableRes val send: Int = R.drawable.ic_send

    @DrawableRes val play: Int = R.drawable.ic_play

    @DrawableRes val pause: Int = R.drawable.ic_pause

    @DrawableRes val video: Int = R.drawable.ic_video

    @DrawableRes val image: Int = R.drawable.ic_image

    @DrawableRes val camera: Int = R.drawable.ic_camera

    // ── Wallet & Payment (2) ────────────────────────────────────────────────

    @DrawableRes val wallet: Int = R.drawable.ic_wallet

    @DrawableRes val creditCard: Int = R.drawable.ic_credit_card

    // ── Security & Energy (4) ───────────────────────────────────────────────

    @DrawableRes val shield: Int = R.drawable.ic_shield

    @DrawableRes val shieldCheck: Int = R.drawable.ic_shield_check

    @DrawableRes val zap: Int = R.drawable.ic_zap

    @DrawableRes val crown: Int = R.drawable.ic_crown

    // ── Lists & Settings (4) ────────────────────────────────────────────────

    @DrawableRes val settings: Int = R.drawable.ic_settings

    @DrawableRes val sliders: Int = R.drawable.ic_sliders

    @DrawableRes val list: Int = R.drawable.ic_list

    @DrawableRes val grid: Int = R.drawable.ic_grid

    // ── Theme & Auth (4) ────────────────────────────────────────────────────

    @DrawableRes val sun: Int = R.drawable.ic_sun

    @DrawableRes val moon: Int = R.drawable.ic_moon

    @DrawableRes val logOut: Int = R.drawable.ic_log_out

    @DrawableRes val logIn: Int = R.drawable.ic_log_in

    // ── Misc (3) ────────────────────────────────────────────────────────────

    @DrawableRes val rotate: Int = R.drawable.ic_rotate

    @DrawableRes val qr: Int = R.drawable.ic_qr

    /** Custom (not in redesign icons.jsx) — used by network offline snackbar. */
    @DrawableRes val wifiOff: Int = R.drawable.ic_wifi_off

    // ── Brand icons (6) — multi-color, do not theme-tint ────────────────────

    @DrawableRes val brandGoogle: Int = R.drawable.ic_brand_google

    @DrawableRes val brandFacebook: Int = R.drawable.ic_brand_facebook

    @DrawableRes val brandWhatsApp: Int = R.drawable.ic_brand_whatsapp

    @DrawableRes val brandApple: Int = R.drawable.ic_brand_apple

    @DrawableRes val brandMastercard: Int = R.drawable.ic_brand_mastercard

    @DrawableRes val brandVisa: Int = R.drawable.ic_brand_visa
}
