package;

#if cpp
import cpp.Lib;
#elseif neko
import neko.Lib;
#end

#if (android && openfl)
import openfl.utils.JNI;
#end

@:keep
class Hardware 
{
    #if (android && openfl)
    private static var vibrate_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "vibrate", "(I)V");
    private static var wakeUp_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "wakeUp", "()V");
    private static var get_screen_width_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "getScreenWidth", "()I");
    private static var get_screen_height_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "getScreenHeight", "()I");
    private static var startTimer_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "startTimer", "(ILorg/haxe/lime/HaxeObject;)V");
    private static var startRepeatTimer_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "startRepeatTimer", "(IILorg/haxe/lime/HaxeObject;)V");
    private static var stopTimer_jni = JNI.createStaticMethod ("org.haxe.extension.Hardware", "stopTimer", "()V");
    #end

    private static var timeHandler:Void->Void;

    public static function vibrate(inputValue:Int)
    {
        #if (android && openfl)
        vibrate_jni(inputValue);
        #end
    }

    public static function wakeUp():Void
    {
        #if (android && openfl)
        wakeUp_jni();
        #end
    }

    public static function getScreenWidth():Int
    {
        var out = -1;
        #if (android && openfl)
        out = get_screen_width_jni();
        #end
        return out;
    }

    public static function getScreenHeight():Int
    {
        var out = -1;
        #if (android && openfl)
        out = get_screen_height_jni();
        #end
        return out;
    }

    /*
    repeatCount=-1 repeat
    */
    public static function startTimer(time:Int, hander:Void->Void):Void
    {
        timeHandler=hander;
        startTimer_jni(time, Hardware.getInstance());
    }

    public static function startRepeatTimer(time:Int, repeatCount:Int, hander:Void->Void):Void
    {
        timeHandler=hander;
        startRepeatTimer_jni(time, repeatCount, Hardware.getInstance());
    }

    public static function stopTimer():Void
    {
        stopTimer_jni();
    }

    public function completeTimer():Void
    {
        timeHandler();
    }

    private static var instance:Hardware=null;
    private static function getInstance():Hardware
    {
        if(instance==null) 
        {
            instance=new Hardware();
        }
        return instance;
    }

    private function new()
    {
    }
}
