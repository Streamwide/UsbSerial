/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 6 Nov 2024 10:34:19 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 6 Nov 2024 10:28:11 +0100
 */

package com.streamwide.buildlogic

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.streamwide.buildlogic.internal.EnvironmentConfig
import com.streamwide.buildlogic.internal.configureFlavors
import com.streamwide.buildlogic.internal.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.hasPlugin

/*
 * The plugin applied with `com.streamwide.android-library-convention`
 *
 *
 * It can be apply either on android application or library.
 * It configure the version name and the version code.
 * It configure the library default config compileSDK, MinSDK, TargetSDK
 * It create maven publication for the given library.
 *
 */

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val environmentConfig = EnvironmentConfig(
            target = project
        )

        with(project) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                
            }
            /*
             * configure the project :
             * - version name.
             * - version code.
             * - targetSDK.
             * - MinSdk.
             * - compile Sdk.
             * - Source compatibility.
             * - target compatibility.
             */
            extensions.configure<LibraryExtension> {
                val hasUnCertifiedDirectory = project.file("src/config/_uncertified").exists()
                // Configure flavor only for modules containing certified/uncertified
                if (hasUnCertifiedDirectory) {
                    configureFlavors(this)
                }

                defaultConfig.versionCode = environmentConfig.versionInfo.versionCode
                defaultConfig.versionName = environmentConfig.versionInfo.versionName
                configureKotlinAndroid(
                    this,
                    environmentConfig
                )
                defaultConfig.targetSdk = environmentConfig.targetSdkVersion

                lint {
                    // Add abortOnError to Continue build even if errors are found
                    abortOnError = false
                    // if true, check all issues, including those that are off by default
                    checkAllWarnings = true
                    //Add this line to check all libraries
                    checkDependencies = true
                }

            }
        }

        /*
         * Do not continue working if "com.android.library" is not applied correctly
         */
        if (!project.plugins.hasPlugin(LibraryPlugin::class)) {
            throw IllegalStateException("com.android.library needs to be applied")
        }


        project.extensions.configure<LibraryAndroidComponentsExtension> {


            /**
             * Ignore the generation of unnecessary dimensions . If the chosen dimension
             * is uncertified then all libraries will be generation as uncertified. Idem for the certified
             * dimension
             */
            val certifiedDimension = environmentConfig.buildVariant.certifiedDimension
            val toIgnore =
                if (certifiedDimension == "_uncertified") "_certified" else "_uncertified"
            val variantForCertified = selector().withFlavor(Pair("certified_dimension", toIgnore))
            beforeVariants(variantForCertified) { variantBuilder ->
                variantBuilder.enable = false
            }

        }
    }
}