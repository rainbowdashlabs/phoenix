plugins {
    application
    id("com.gradleup.shadow") version "9.2.2"
}

application.mainClass = "dev.chojo.Main"
group = "dev.chojo"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(libs.jda) {
        exclude(module = "opus-java")
    }
    implementation(libs.jdacommands)

    implementation(libs.hikari)
    implementation(libs.postgres)
    implementation(libs.bundles.sadu)

    implementation(libs.bundles.logback)
    implementation(libs.slf4j)

    implementation(libs.jspecify)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    sourceCompatibility = "25"
}

tasks.withType<JavaExec>() {
    jvmArgs("--sun-misc-unsafe-memory-access=allow", "--enable-native-access=ALL-UNNAMED")
}