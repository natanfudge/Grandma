import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "com.fudge.lettuce"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation ("org.jetbrains.kotlin:kotlin-test")
    testImplementation ("org.jetbrains.kotlin:kotlin-test-junit")
    implementation ("com.jessecorbett:diskord:1.5.0")
    compile ("org.eclipse.jgit", "org.eclipse.jgit", "5.5.0.201909110433-r")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}