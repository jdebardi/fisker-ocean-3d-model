package ocean.twin

import kotlin.math.*

enum class Headlights { OFF, DIPPED, HIGH }
enum class Indicators { OFF, LEFT, RIGHT, HAZARD }
data class Paint(val hex: String, val metallic: Float = .12f, val roughness: Float = .76f, val clearcoat: Float = 0f)
data class WheelPreset(val id: String, val family: String, val accentMaterial: String, val accentHex: String, val accentRoughness: Float, val radiusM: Float)
data class TwinState(
    val visualYawDeg: Float = 0f,
    val roadWheelSteerDeg: Float = 0f,
    val speedMps: Float = 0f,
    val headlights: Headlights = Headlights.OFF,
    val brake: Boolean = false,
    val indicators: Indicators = Indicators.OFF,
    val reverseLights: Boolean = false,
    val paint: Paint = Paint("#a5adb6", .12f, .76f, 0f),
    val wheelPreset: String = "F3A_22",
    val telemetryFresh: Boolean = true,
    val showBeamCards: Boolean = true,
    val tailLights: Boolean = false
)

/** Engine adapter. Invoke only on the rendering thread. Transform methods are absolute
 * overrides relative to the imported rest pose, not incremental matrix multiplication. */
interface TwinSceneSink {
    fun yawRadians(node: String, angle: Float)
    fun rollRadians(node: String, angle: Float)
    fun yOffsetFromRest(node: String, metres: Float)
    /** Restore the named group's unit scale; hide/show descendant renderables using layers. */
    fun visibleGroup(node: String, visible: Boolean)
    fun emissiveLinear(material: String, r: Float, g: Float, b: Float)
    fun paintLinear(material: String, r: Float, g: Float, b: Float, metallic: Float, roughness: Float, clearcoat: Float)
    fun wheelAccentLinear(material: String, r: Float, g: Float, b: Float, roughness: Float)
    fun beamMaterial(material: String, alpha: Float, emissionStrength: Float)
}

/** Independent of Android and Filament; a tested reference for telemetry-to-scene logic. */
class VehicleTwinController(private val sink: TwinSceneSink, presets: List<WheelPreset>) {
    private val wheels = presets.associateBy { it.id }
    private val familyNames = arrayOf("F3", "F5", "F6", "F7")
    private val corners = arrayOf("FL", "FR", "RL", "RR")
    private val rolls = corners.map { "Roll_$it" }
    private val lampCache = mutableMapOf<String, FloatArray>()
    private val visibleCache = mutableMapOf<String, Boolean>()
    private var yaw = 0f
    private var steer = 0f
    private var spin = 0.0
    private var wheelId: String? = null
    private var paint: Paint? = null
    private var lastDipped: Boolean? = null
    private var lastHigh: Boolean? = null

    private fun finite(v: Float, fallback: Float = 0f) = if (v.isFinite()) v else fallback
    private fun linear(channel: Int): Float {
        val x = channel / 255f
        return if (x <= .04045f) x / 12.92f else ((x + .055f) / 1.055f).pow(2.4f)
    }
    private fun rgb(hex: String): FloatArray {
        val h = hex.removePrefix("#")
        require(h.length == 6) { "Use a six-digit RGB colour" }
        return FloatArray(3) { linear(h.substring(it * 2, it * 2 + 2).toInt(16)) }
    }
    private fun show(name: String, on: Boolean) {
        if (visibleCache[name] != on) { sink.visibleGroup(name, on); visibleCache[name] = on }
    }
    private fun emit(name: String, r: Float, g: Float, b: Float, strength: Float) {
        val x = r * strength; val y = g * strength; val z = b * strength
        val old = lampCache[name]
        if (old == null || old[0] != x || old[1] != y || old[2] != z) {
            sink.emissiveLinear(name, x, y, z)
            if (old == null) lampCache[name] = floatArrayOf(x, y, z)
            else { old[0] = x; old[1] = y; old[2] = z }
        }
    }
    fun update(state: TwinState, elapsedSeconds: Double, dtSeconds: Float) {
        val dt = finite(dtSeconds).coerceIn(0f, .05f)
        val w = wheels[state.wheelPreset] ?: wheels["F3A_22"] ?: error("Default wheel preset is missing")
        if (wheelId != w.id) {
            for (corner in corners) for (family in familyNames) show("WheelStyle_${family}_$corner", family == w.family)
            val c = rgb(w.accentHex)
            sink.wheelAccentLinear(w.accentMaterial, c[0], c[1], c[2], w.accentRoughness)
            wheelId = w.id
        }
        if (paint != state.paint) {
            val c = rgb(state.paint.hex)
            sink.paintLinear("Paint_Body", .022f+.978f*c[0], .022f+.978f*c[1], .022f+.978f*c[2], state.paint.metallic.coerceIn(0f, .12f), state.paint.roughness.coerceIn(.70f, 1f), 0f)
            paint = state.paint
        }
        val blend = 1f - exp(-dt / .12f)
        yaw += (finite(state.visualYawDeg).coerceIn(-15f, 15f) - yaw) * blend
        steer += (finite(state.roadWheelSteerDeg).coerceIn(-35f, 35f) - steer) * blend
        sink.yawRadians("VehicleYaw", Math.toRadians(yaw.toDouble()).toFloat())
        sink.yOffsetFromRest("VehicleYaw", w.radiusM - .39415f)
        for (corner in arrayOf("FL", "FR")) sink.yawRadians("Steer_$corner", Math.toRadians(steer.toDouble()).toFloat())
        val speed = if (state.telemetryFresh) finite(state.speedMps).coerceIn(-30f, 90f) else 0f
        spin = (spin - speed * dt / w.radiusM) % (2 * PI)
        for (name in rolls) sink.rollRadians(name, spin.toFloat())

        val high = state.headlights == Headlights.HIGH
        val dipped = state.headlights != Headlights.OFF
        val phase = if (elapsedSeconds.isFinite()) ((elapsedSeconds * 1.4) % 1.0 + 1.0) % 1.0 < .5 else false
        val left = phase && (state.indicators == Indicators.LEFT || state.indicators == Indicators.HAZARD)
        val right = phase && (state.indicators == Indicators.RIGHT || state.indicators == Indicators.HAZARD)
        for (side in arrayOf("L", "R")) {
            emit("Mat_Head_$side", .90f, .96f, 1f, if (dipped) 2f + if (high) 5f else 0f else 0f)
            emit("Mat_Tail_$side", 1f, .007f, .012f, if (state.brake) 7f else if (state.tailLights) .40f else 0f)
            emit("Mat_Reverse_$side", .85f, .95f, 1f, if (state.reverseLights) 4f else 0f)
            emit("Mat_Indicator_$side", 1f, .025f, .001f, if (if (side == "L") left else right) 4f else 0f)
            show("Beam_Dipped_$side", dipped && state.showBeamCards)
            show("Beam_High_$side", high && state.showBeamCards)
        }
        emit("Mat_Brake_Centre", 1f, .007f, .012f, if (state.brake) 10f else 0f)
        if (lastDipped != dipped) { sink.beamMaterial("Beam_Dipped", if (dipped) .19f else 0f, if (dipped) .65f else 0f); lastDipped = dipped }
        if (lastHigh != high) { sink.beamMaterial("Beam_High", if (high) .24f else 0f, if (high) 1.7f else 0f); lastHigh = high }
    }
}
