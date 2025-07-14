/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Mon, 20 Jan 2025 09:43:36 +0100
 * @copyright  Copyright (c) 2025 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2025 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 17 Jan 2025 16:29:38 +0100
 */

package com.streamwide.buildlogic.internal

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.util.internal.TextUtil

/**
 * Holds configuration options specific to the build environment.
 * This class is used to configure various aspects of the build process, including:
 *  * Target SDK version (`targetSdkVersion`)
 *  * Compile SDK version (`compileSdkVersion`)
 *  * Compile options (`compileOptions`) including source and target compatibility for Java
 *  * Optional build variant (`buildVariant`) used to determine minimum SDK version
 *  * Application version information (`versionInfo`)
 *  * Kotlin options (`kotlinOptions`) including JVM target
 *
 * The `minSdkVersion` is dynamically determined based on the `buildVariant`.
 * If the variant's app dimension starts with "AgnetWorkOrange" (case insensitive),
 * it uses a minimum version of 31. Otherwise, it defaults to 23.
 */
data class EnvironmentConfig(
    val target: Project,
    val targetSdkVersion: Int = 35,
    val compileSdkVersion: Int = 35,
    val compileOptions: CompileOption = CompileOption(
        sourceCompatibility = JavaVersion.VERSION_17,
        targetCompatibility = JavaVersion.VERSION_17
    ),
    val buildVariant: AppBuildVariant = AppBuildVariant(target),
    val versionInfo: AppVersionInfo = AppVersionInfo(target),
    val kotlinOptions: KotlinOption = KotlinOption(
        jvmTarget = JavaVersion.VERSION_17
    ),
) {
    val minSdkVersion: Int
        get() = if (buildVariant.appDimension?.startsWith("AgnetWorkOrange", false) == true) {
            31
        } else {
            24
        }
}

/**
 * Represents options related to Java source and target compatibility for compilation.
 * This data class holds configurations that influence how the compiler treats the source code.
 *  * `sourceCompatibility`: The minimum Java version that the source code is written against.
 *  * `targetCompatibility`: The version of the Java bytecode that the compiler generates.
 */
data class CompileOption(
    val sourceCompatibility: JavaVersion,
    val targetCompatibility: JavaVersion,
)

/**
 * Represents a build variant configuration for the application.
 * This class captures various dimensions that influence the build behavior:
 *  * `appDimension`: Application dimension (e.g., "TOTR")
 *  * `certifiedDimension`: Certification dimension (e.g., "_uncertified" or "_certified")
 *  * `uiDimension`: UI dimension (e.g., "_stwUI")
 *  * `packageType`: Package type (e.g., "_saas")
 *  * `gMapType`: Google Maps inclusion type (e.g., "_gmapenabled" or "_gmapdisabled")
 *  * `alfrescoType`: Alfresco integration type (e.g., "_alfrescoenabled" or "_alfrescodisabled")
 *  * `jitsiDimension`: Jitsi integration (e.g., "_jitsienabled" or "_jitsidisabled")
 *  * `buildType`: Build type retrieved from project property ("debug" by default) (private)
 *  * `variantName`: Combination of all dimensions and capitalized build type for identification
 *
 * Dimension properties are retrieved from project properties using the specified keys.
 */
data class AppBuildVariant(
    val target: Project,
) {
    private val buildType: String?
        get() = target.getProperty("com.streamwide.variant.build_type", "debug")
    val appDimension: String?
        get() = target.getProperty("com.streamwide.variant.app_dimension", "TOTR")
    val certifiedDimension: String?
        get() = target.getProperty(
            "com.streamwide.variant.certified_dimension",
            "_uncertified"
        )
    private val uiDimension: String?
        get() = target.getProperty("com.streamwide.variant.ui_dimension", "_stwUI")
    private val packageType: String?
        get() = target.getProperty("com.streamwide.variant.package_type", "_saas")
    private val gMapType: String?
        get() = target.getProperty("com.streamwide.variant.gmap_type", "_gmapenabled")
    private val alfrescoType: String?
        get() = target.getProperty(
            "com.streamwide.variant.alfresco",
            "_alfrescoenabled"
        )
    private val jitsiType: String?
        get() = target.getProperty(
            "com.streamwide.variant.jitsi",
            "_jitsienabled"
        )

    val variantName: String
        get() = appDimension + certifiedDimension + uiDimension + packageType + gMapType + alfrescoType + jitsiType + TextUtil.capitalize(
            buildType
        )
}

/**
 * Represents information about the application version.
 * This data class retrieves version details from project properties and git.
 *  * `versionCode`: Version code retrieved from git build number (automatically generated).
 *  * `versionName`: Human-readable version name constructed from major, minor, and patch versions.
 *      - Major, minor, and patch versions are retrieved from project properties with specific keys (private).
 *      - If any of the version properties are missing from project properties, the corresponding part of the version name will be empty.
 */
data class AppVersionInfo(
    val target: Project,
    val versionMajor: String? = target.getProperty("MAIN_VERSION_MAJOR"),
    val versionMinor: String? = target.getProperty("MAIN_VERSION_MINOR"),
    val versionPatch: String? = target.getProperty("MAIN_VERSION_PATCH")
) {
    val versionCode: Int = target.gitBuildNumber()
    val versionName: String
        get() = "$versionMajor.$versionMinor.$versionPatch"

    val appVersionName: String
        get() = "$versionName.$versionCode"
}

/**
 * Represents options related to Kotlin compilation targeting the JVM.
 * This data class specifies the Java Virtual Machine (JVM) version that the generated bytecode should be compatible with.
 *  * `jvmTarget`: The version of the JVM that the compiled Kotlin code should target.
 */
data class KotlinOption(
    val jvmTarget: JavaVersion,
)
