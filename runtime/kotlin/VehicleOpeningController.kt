package ocean.twin

import kotlin.math.exp

data class OpeningState(
    val frontLeft: Float = 0f, val frontRight: Float = 0f,
    val rearLeft: Float = 0f, val rearRight: Float = 0f,
    val tailgate: Float = 0f, val sunroof: Float = 0f,
    val handlesExtended: Float = 0f
)
/** All calls are absolute offsets from saved imported rest transforms, on the renderer thread. */
interface OpeningSceneSink {
    fun rotationOffset(node: String, axisX: Float, axisY: Float, axisZ: Float, radians: Float)
    fun translationOffset(node: String, x: Float, y: Float, z: Float)
}
/** The visual limits match contract v2.0; they are not OEM actuator calibrations. */
class VehicleOpeningController(private val sink: OpeningSceneSink) {
    private val values = FloatArray(7)
    private fun fraction(x: Float) = if (x.isFinite()) x.coerceIn(0f, 1f) else 0f
    fun update(state: OpeningState, dtSeconds: Float, snap: Boolean = false) {
        val target = floatArrayOf(state.frontLeft,state.frontRight,state.rearLeft,state.rearRight,state.tailgate,state.sunroof,state.handlesExtended)
        val dt = if (dtSeconds.isFinite()) dtSeconds.coerceIn(0f,.05f) else 0f
        val blend = if (snap) 1f else 1f-exp(-dt/.18f)
        for (i in values.indices) values[i] += (fraction(target[i])-values[i])*blend
        val names = arrayOf("FL","FR","RL","RR")
        for (i in names.indices) {
            val degrees = if (names[i].endsWith("L")) -68f else 68f
            sink.rotationOffset("Hinge_Door_${names[i]}",0f,1f,0f,Math.toRadians((degrees*values[i]).toDouble()).toFloat())
        }
        sink.rotationOffset("Hinge_Tailgate",1f,0f,0f,Math.toRadians((-82f*values[4]).toDouble()).toFloat())
        for (name in names) {
            val x = (if (name.endsWith("L")) -.035f else .035f) * values[6]
            sink.translationOffset("Slide_Handle_$name",x,0f,0f)
        }
        val lift = (values[5]/.18f).coerceIn(0f,1f)
        val slide = ((values[5]-.18f)/.82f).coerceIn(0f,1f)
        sink.translationOffset("Slide_Sunroof_Front",0f,.060f*lift,.922f*slide)
    }
}
