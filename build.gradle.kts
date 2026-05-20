plugins {
    java
    application
}

group = "com.voxelsandbox"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.3")
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.3")
    implementation("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-windows")
    implementation("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux")
    implementation("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-osx")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("com.voxelsandbox.VoxelSandbox")
}

tasks.run.configure {
    standardInput = System.`in`
}
