import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    application
    alias(libs.plugins.spotless)
    alias(libs.plugins.idea)
    alias(libs.plugins.pitest)
    jacoco
}

application {
    mainClass = "dev.chojo.Bootstrapper"
    applicationDefaultJvmArgs = listOf("--add-reads", "dev.chojo.elpis=ALL-UNNAMED")
}
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

    annotationProcessor(libs.ocular)
    implementation(libs.ocular)
    implementation(libs.bundles.config)

    annotationProcessor(libs.javalin.openapi.annotation)
    implementation(libs.bundles.javalin)

    implementation(libs.bundles.logback)
    implementation(libs.slf4j)

    implementation(libs.jspecify)

    testRuntimeOnly(libs.junit.platform)
    testImplementation(libs.sadu.testing)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testImplementation(libs.mockito)
}

tasks {
    compileJava {
        options.isIncremental = true
        options.compilerArgs.addAll(listOf("--add-reads", "dev.chojo.elpis=ALL-UNNAMED"))
    }

    processResources {
        val projectVersion = project.version.toString();
        inputs.property("projectVersion", projectVersion)
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                var version = projectVersion
                var workflow = (System.getenv("GITHUB_ACTIONS") ?: "false") == "true"
                if (workflow) {
                    val now = ZonedDateTime.now(ZoneOffset.UTC)
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val formattedDate = now.format(formatter)

                    version = when (System.getenv("GITHUB_REF_TYPE")) {
                        "branch" -> "$version ${System.getenv("GITHUB_REF_NAME")}-${
                            System.getenv("GITHUB_SHA").substring(0, 7)
                        } @ $formattedDate"

                        "tag" -> "$version ${System.getenv("GITHUB_REF_NAME").substring(1)} @ $formattedDate"
                        else -> "$version snapshot"
                    }
                }
                expand(
                    "version" to version
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    test {
        useJUnitPlatform {
            excludeTags("locale", "database")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register<Test>("testDatabase") {
        group = "verification"
        description = "Runs database validation tests"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        useJUnitPlatform {
            includeTags("database")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register("checkLicenseBackend") {
        group = "verification"
        description = "Checks license headers for backend Java files"
        dependsOn("spotlessJavaCheck")
    }

    register("checkLicenseFrontend") {
        group = "verification"
        description = "Checks license headers for frontend Vue and JavaScript files"
        dependsOn("spotlessJavascriptCheck", "spotlessVueCheck")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
    withJavadocJar()
}

idea {
    project {
        settings {
            var shared = listOf(
                "-Dbot.cleanup=false",
                "-Dbot.config=config.testing.yaml",
                "-Dlog4j2.configurationFile=docker/config/log4j2.testing.xml",
                "-Dbot.db.host=localhost,",
                "-Dbot.api.url=http://localhost:5173",
                "--sun-misc-unsafe-memory-access=allow",
                "--enable-native-access=ALL-UNNAMED"
            )
            runConfigurations {
                register<org.jetbrains.gradle.ext.Application>("App-Testing") {
                    mainClass = "dev.chojo.Bootstrapper"
                    jvmArgs = shared.joinToString(" ")
                    moduleName = "elpis.main"
                }
                register<org.jetbrains.gradle.ext.Application>("App-Testing - All SKUs") {
                    mainClass = "dev.chojo.Bootstrapper"
                    jvmArgs =
                        (shared + "-Dbot.grantallsku=true" + "-Dcjda.premium.skipEntitledCheck=true").joinToString(" ")
                    moduleName = "elpis.main"
                }
            }
        }
    }
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat("2.84.0")
            .formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        encoding("UTF-8")
    }

    format("javascript") {
        licenseHeaderFile(rootProject.file("HEADER.txt"), "(import|const|let|var|export|//)")
        target("frontend/src/**/*.js", "frontend/src/**/*.ts")
        targetExclude("frontend/node_modules/**", "frontend/dist/**")
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("vue") {
        licenseHeaderFile(rootProject.file("HEADER.txt"), "(<template|<script|<style)")
        target("frontend/src/**/*.vue")
        targetExclude("frontend/node_modules/**", "frontend/dist/**")
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("backendLocales") {
        encoding("UTF-8")
        target("src/main/resources/locale*.properties")
    }

    format("frontendLocales") {
        encoding("UTF-8")
        target("frontend/src/locales/*.json")
    }
}

pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    junit5PluginVersion = "1.2.3"    //or 0.15 for PIT <1.9.0
    threads = 4
    mutators.set(setOf("STRONGER"))
    mutationThreshold = 80
    targetClasses.set(setOf("dev.chojo.crypto.**"))
    targetTests.set(setOf("dev.chojo.crypto.**"))
}