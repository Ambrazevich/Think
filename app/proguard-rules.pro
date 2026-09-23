# Add project specific ProGuard rules here.
# By default, the flags in this file are applied to all build types.

# ProGuard rules for Gson
-keep class com.google.gson.annotations.** { *; }
-keep class * extends com.google.gson.TypeAdapter

# Keep all classes in the data package
-keep class io.github.ambrazevich.think.data.** { *; }

# Keep all enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn javax.lang.model.element.Modifier
