// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

allprojects {
    val home = System.getProperty("user.home")
    val customBuild = file("$home/.android-build")
    if (customBuild.exists()) {
        layout.buildDirectory.set(file("$home/.android-build/Tugas6/$name"))
    }
}
