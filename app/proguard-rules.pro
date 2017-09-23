#all firebase methods and corresponding data has to deobfuscated for proper execution
#and not to mention the support libraries and stuff
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep public class com.client.shourya.periferi.viewholder.BroadCastViewHolder { *; }
-keep public class com.client.shourya.periferi.BroadCastFragment { *; }
-keep class com.client.shourya.periferi.data.** { *; }
-keep public class com.google.android.gms.** { *; }
-keep public class com.google.firebase.** { *; }
-keep public class com.firebase.** { *; }
-dontwarn com.google.android.gms.**