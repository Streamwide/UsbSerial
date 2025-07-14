# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Program Files (x86)\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
-repackageclasses com.streamwide.smartms.lib.actuator
-keepattributes LineNumberTable
-keep public class com.streamwide.smartms.lib.actuator.api.** {*;}
-keep public class com.streamwide.smartms.lib.actuator.logger.** {*;}

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

-dontwarn java.lang.invoke.StringConcatFactory