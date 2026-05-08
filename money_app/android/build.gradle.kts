allprojects {
    repositories {
        google()
        mavenCentral()
        // Tambahkan Maven address Huawei
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        // 🔽 TAMBAHKAN INI – Android Gradle Plugin versi 8.1.0
        classpath("com.android.tools.build:gradle:8.1.0")
        // Plugin AGConnect
        classpath("com.huawei.agconnect:agcp:1.9.1.300")
    }
}