tasks.register("Delete", Delete::class) {
    delete(rootProject.buildDir)
}

val androidCompileSdkVersion by extra(31)
val androidBuildToolsVersion by extra("31.0.0")
val androidTargetSdkVersion by extra(31)
val androidMinSdkVersion by extra(15)
val androidSourceCompatibility by extra(JavaVersion.VERSION_11)
val androidTargetCompatibility by extra(JavaVersion.VERSION_11)
