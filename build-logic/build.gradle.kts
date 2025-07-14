/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Mon, 21 Oct 2024 12:06:35 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Mon, 21 Oct 2024 10:43:38 +0100
 */

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.streamwide"
version = "0.1.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)

    implementation(libs.android.gradlePlugin)
    implementation(libs.jsonSimple)
    implementation(libs.com.squareup.okhttp3)
    implementation(libs.okHttpInterceptor)

}

tasks.test {
    useJUnitPlatform()
}


gradlePlugin {
    plugins {
        create("AndroidLibraryConventionPlugin") {
            id = "com.streamwide.android-library-convention"
            implementationClass = "com.streamwide.buildlogic.AndroidLibraryConventionPlugin"
        }

    }
}


