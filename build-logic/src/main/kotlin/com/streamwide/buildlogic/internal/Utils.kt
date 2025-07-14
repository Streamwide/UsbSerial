/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Mon, 21 Oct 2024 10:18:54 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Mon, 21 Oct 2024 10:18:35 +0100
 */

package com.streamwide.buildlogic.internal

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    environmentConfig: EnvironmentConfig,
) {
    commonExtension.apply {
        compileSdk = environmentConfig.compileSdkVersion

        defaultConfig {
            minSdk = environmentConfig.minSdkVersion

            /*
             * only for sw core add the version code and version name in the build config
             */
            if (project.name == "swcore") {
                buildConfigField(
                    "int",
                    "LIBRARY_VERSION_CODE",
                    "${environmentConfig.versionInfo.versionCode}"
                )
                buildConfigField(
                    "String",
                    "LIBRARY_VERSION_NAME",
                    "\"${environmentConfig.versionInfo.versionName}\""
                )
            }
        }

        compileOptions {
            // Up to Java 17 APIs are available through desugaring
            // https://developer.android.com/studio/write/java11-minimal-support-table
            //https://stackoverflow.com/questions/73453524/what-is-causing-this-error-com-android-tools-r8-internal-nc-sealed-classes-are
            sourceCompatibility = environmentConfig.compileOptions.sourceCompatibility
            targetCompatibility = environmentConfig.compileOptions.targetCompatibility

            //isCoreLibraryDesugaringEnabled = true
        }
    }
    configureKotlin(environmentConfig.kotlinOptions.jvmTarget.toString())
}


internal fun Project.configureProtoKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    environmentConfig: EnvironmentConfig,
) {
    commonExtension.apply {
        compileSdk = environmentConfig.compileSdkVersion

        defaultConfig {
            minSdk = environmentConfig.minSdkVersion


        }

        compileOptions {
            sourceCompatibility = environmentConfig.compileOptions.sourceCompatibility
            targetCompatibility = environmentConfig.compileOptions.targetCompatibility

            //isCoreLibraryDesugaringEnabled = true
        }
    }
    configureProtoKotlin(environmentConfig.kotlinOptions.jvmTarget.toString())
}

private fun Project.configureProtoKotlin(jvmVersion: String) {
    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            // Set JVM target to 17
            jvmTarget = jvmVersion
        }

    }

}

/**
 * Configure base Kotlin options
 */
private fun Project.configureKotlin(jvmVersion: String) {
    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = jvmVersion
        }

    }

}

/**
 * Extension function to retrieve the git build number
 * @return Int the build number
 */

internal fun Project.gitBuildNumber(): Int {
    try {
        val stdout = ByteArrayOutputStream()
        rootProject.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        var buildNumber = stdout.toString().trim().toInt()
        if (buildNumber == 0) {
            buildNumber = project.getProperty("com.streamwide.build_number")?.toInt() ?: 0
        }
        return buildNumber + 100000
    } catch (e: Exception) {
        println("Exception : $e")
        return getLibraryVersion("sdkCore")
    }
}


fun extractRevision(version: String): Int? {
    val regex = "-r(\\d+)$".toRegex()  // Regular expression to match the number after "-r"
    val matchResult = regex.find(version)
    return matchResult?.groupValues?.get(1)?.toInt()  // Extract the number after "-r"
}

fun Project.getLibraryVersion(libraryName: String): Int {
    val libsTomlFile = File("${project.rootDir}/gradle/libs.versions.toml")
    // Read the file and parse it
    val lines = libsTomlFile.readLines()
    for (line in lines) {
        if (line.startsWith(libraryName)) {
            return extractRevision(line.split("=")[1]) ?: 1
        }
    }
    return 1
}


/**
 * Extension function to retrieve the project version
 * @return String the project version format Example 4.0.0-r123456
 */
internal fun Project.getModuleVersion(): String {
    val major = getProperty("MAIN_VERSION_MAJOR")
    val minor = getProperty("MAIN_VERSION_MINOR")
    val patch = getProperty("MAIN_VERSION_PATCH")
    val buildNumber = gitBuildNumber()

    return "$major.$minor.$patch-r$buildNumber"
}

internal fun Project.getHashCode(version: Int): String {

    val process = ProcessBuilder("git", "rev-list", "--reverse", "HEAD")
        .start()

    val gitOutput = process.inputStream.bufferedReader().use(BufferedReader::readText)
    val commitList = gitOutput.lines().toList()

    println("Commit hash code  = ${commitList[version]}  for the version $version")
    return commitList[version]
}

internal fun Project.getHeadOfCommit(): String {
    val process = ProcessBuilder("git", "rev-list", "--reverse", "HEAD")
        .start()

    val gitOutput = process.inputStream.bufferedReader().use(BufferedReader::readText)
    val commitList = gitOutput.lines().toList()

    println("End commit hash code  = ${commitList.last()}")

    return commitList.last()

}

/**
 * Extension function to retrieve the value of the given property
 * @param name of the property
 * @return the value of the property or null
 */
internal fun Project.getProperty(name: String, default: String? = null) =
    providers.gradleProperty(name).orNull ?: default


/**
 * The capitalize function is used in many time in the project
 * and it is deprecated so, instead of replace it we create
 * an extension function that just implement the new method
 */
internal fun String.toCapitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}


internal fun String.decapitalize() = replaceFirstChar { it.lowercase(Locale.getDefault()) }

internal fun Project.configureNFCWriterAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    environmentConfig: EnvironmentConfig,
) {
    commonExtension.apply {
        compileSdk = environmentConfig.compileSdkVersion

        defaultConfig {
            minSdk = environmentConfig.minSdkVersion

        }

        compileOptions {
            sourceCompatibility = environmentConfig.compileOptions.sourceCompatibility
            targetCompatibility = environmentConfig.compileOptions.targetCompatibility
        }
    }
}


val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
