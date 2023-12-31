plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.15.0"
}

group = "top.ffshaozi"
version = "1.0.5"
repositories{
    maven("https://maven.reposilite.com/snapshots")
    mavenCentral()
}
repositories{
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
repositories{
    maven("https://mirrors.cloud.tencent.com/repository/maven/public")
    mavenCentral()
}


dependencies{
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("io.javalin:javalin:5.6.1")

}

mirai {
    jvmTarget = JavaVersion.VERSION_16
}
