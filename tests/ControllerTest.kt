import ocean.twin.*
import kotlin.math.abs
class Fake : TwinSceneSink {
 val yaw=mutableMapOf<String,Float>();val roll=mutableMapOf<String,Float>();val on=mutableMapOf<String,Boolean>();val light=mutableMapOf<String,List<Float>>();val y=mutableMapOf<String,Float>();val paintNames=mutableListOf<String>();var finish=listOf<Float>();var colour=listOf<Float>()
 override fun yawRadians(node:String,angle:Float){yaw[node]=angle};override fun rollRadians(node:String,angle:Float){roll[node]=angle};override fun yOffsetFromRest(node:String,metres:Float){y[node]=metres};override fun visibleGroup(node:String,visible:Boolean){on[node]=visible};override fun emissiveLinear(material:String,r:Float,g:Float,b:Float){light[material]=listOf(r,g,b)}
 override fun paintLinear(material:String,r:Float,g:Float,b:Float,metallic:Float,roughness:Float,clearcoat:Float){paintNames+=material;colour=listOf(r,g,b);finish=listOf(metallic,roughness,clearcoat)}
 override fun wheelAccentLinear(material:String,r:Float,g:Float,b:Float,roughness:Float){}
 override fun beamMaterial(material:String,alpha:Float,emissionStrength:Float){}
}
fun main(){
 val f=Fake();val c=VehicleTwinController(f,listOf(WheelPreset("F3A_22","F3","Wheel_F3_Accent","#c7cdd4",.28f,.39415f),WheelPreset("F7_20","F7","Wheel_F7_Matte","#30353d",.63f,.3815f)))
 c.update(TwinState(headlights=Headlights.DIPPED),.1,.05f);check(f.light["Mat_Tail_L"]!![0]==0f);check(f.light["Mat_Head_L"]!![0]==1.8f);check(f.light["Mat_Brake_Centre"]!![0]==0f)
 c.update(TwinState(headlights=Headlights.HIGH,tailLights=true),.1,.05f);check(abs(f.light["Mat_Head_L"]!![0]-6.3f)<.001);check(f.light["Mat_Tail_L"]!![0]==.4f);check(f.on["Beam_Dipped_L"]==true&&f.on["Beam_High_L"]==true)
 c.update(TwinState(brake=true),.1,.05f);check(f.light["Mat_Tail_L"]!![0]==7f);check(f.light["Mat_Brake_Centre"]!![0]==10f)
 c.update(TwinState(indicators=Indicators.HAZARD),.1,.05f);check(f.light["Mat_Indicator_L"]==f.light["Mat_Indicator_R"]);check(f.light["Mat_Indicator_L"]!![0]==4f)
 c.update(TwinState(indicators=Indicators.HAZARD),.5,.05f);check(f.light["Mat_Indicator_L"]!![0]==0f)
 c.update(TwinState(speedMps=10f,visualYawDeg=8f,roadWheelSteerDeg=20f),.6,.05f);check(f.roll["Roll_FL"]!!<0);check(f.yaw["VehicleYaw"]!!>0&&f.yaw["Steer_FL"]!!>0)
 val spin=f.roll["Roll_FL"]!!;c.update(TwinState(wheelPreset="F7_20"),.7,0f);check(f.roll["Roll_FL"]==spin);check(abs(f.y["VehicleYaw"]!!+.01265f)<.00001);check(f.on["WheelStyle_F7_FL"]==true&&f.on["WheelStyle_F3_FL"]==false)
 c.update(TwinState(speedMps=Float.NaN,visualYawDeg=Float.NaN,roadWheelSteerDeg=Float.NaN),.8,.05f);check(f.roll.values.all{it.isFinite()});check(f.paintNames.all{it=="Paint_Body"})
 c.update(TwinState(paint=Paint("#ffffff",1f,.08f,1f)),.9,.05f);check(f.finish[0]<=.12f&&f.finish[1]>=.70f&&f.finish[2]==0f)
 c.update(TwinState(paint=Paint("#000000",0f,.8f,0f)),1.0,0f);check(f.colour.all{abs(it-.022f)<.00001f})
 c.update(TwinState(paint=Paint("#ffffff",0f,.8f,0f)),1.1,0f);check(f.colour.all{abs(it-1f)<.00001f})
 c.update(TwinState(tailLights=true),1.2,0f);check(f.light["Mat_Tail_L"]!![0]==.4f&&f.light["Mat_Head_L"]!![0]==0f)
 c.update(TwinState(headlights=Headlights.HIGH),1.3,0f);check(f.light["Mat_Tail_L"]!![0]==0f&&f.light["Mat_Head_L"]!![0]>6f)
 c.update(TwinState(tailLights=true,brake=true),1.4,0f);check(f.light["Mat_Tail_L"]!![0]==7f)
 println("PASS: independent tail/head controls; black-paint visibility floor and white endpoint; calm finish clamps; independent dipped/tail states, additive high beam, brighter brakes, centre brake, blink phase, wheel spin, steering, style switch, paint isolation and non-finite telemetry.")
}
