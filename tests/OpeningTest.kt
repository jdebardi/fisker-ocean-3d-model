import ocean.twin.*
import kotlin.math.abs
class OpeningFake:OpeningSceneSink{
 val rotation=mutableMapOf<String,Float>();var offset=listOf(0f,0f,0f);val translations=mutableMapOf<String,List<Float>>()
 override fun rotationOffset(node:String,axisX:Float,axisY:Float,axisZ:Float,radians:Float){rotation[node]=radians}
 override fun translationOffset(node:String,x:Float,y:Float,z:Float){offset=listOf(x,y,z);translations[node]=offset}
}
fun main(){
 val f=OpeningFake();val c=VehicleOpeningController(f)
 c.update(OpeningState(frontLeft=1f),0f,true);check(f.rotation["Hinge_Door_FL"]!!<0);check(f.rotation["Hinge_Door_FR"]==0f)
 c.update(OpeningState(frontRight=1f,rearLeft=1f,rearRight=1f,tailgate=1f,sunroof=.18f),0f,true)
 check(f.rotation["Hinge_Door_FR"]!!>0&&f.rotation["Hinge_Door_RL"]!!<0&&f.rotation["Hinge_Door_RR"]!!>0)
 check(f.rotation["Hinge_Tailgate"]!!<0);check(abs(f.offset[1]-.06f)<.00001&&f.offset[2]==0f)
 c.update(OpeningState(sunroof=1f),0f,true);check(abs(f.offset[2]-.922f)<.00001)
 c.update(OpeningState(frontLeft=Float.NaN,sunroof=Float.POSITIVE_INFINITY),.05f,true);check(f.rotation.values.all{it.isFinite()});check(f.offset==listOf(0f,0f,0f))
 c.update(OpeningState(handlesExtended=1f),0f,true)
 for(key in listOf("FL","FR","RL","RR")){val expected=if(key.endsWith("L")) -.035f else .035f;check(abs(f.translations["Slide_Handle_$key"]!![0]-expected)<.000001f)}
 c.update(OpeningState(handlesExtended=.5f),0f,true);check(abs(f.translations["Slide_Handle_FL"]!![0]+.0175f)<.000001f)
 c.update(OpeningState(handlesExtended=Float.NaN),0f,true);check(f.translations.filterKeys{it.startsWith("Slide_Handle_")}.values.all{it.all{v->abs(v)<.000001f}})
 c.update(OpeningState(handlesExtended=1f),.05f);check(f.translations["Slide_Handle_FR"]!![0] in 0f.. .035f)
 c.update(OpeningState(),0f,true);check(f.translations.values.all{it.all{v->abs(v)<.000001f}})
 println("PASS: four handle extension signs, partial travel, finite values, smoothing and lock rest; independent doors, outward rotation signs, hatch axis, lift-before-slide, full roof travel, non-finite input and rest reset")
}
