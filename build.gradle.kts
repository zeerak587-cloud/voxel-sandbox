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
    // LWJGL 3 with OpenGL bindings
    val lwjglVersion = "3.3.3"
    val jomlVersion = "1.10.5"
    val lwjglNatives = "natives-windows"
    
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
    
    // JOML for math operations
    implementation("org.joml:joml:$jomlVersion")
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
