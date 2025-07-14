
/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 13 Jul 2023 12:15:03 +0100
 * @copyright  Copyright (c) 2023 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2023 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 13 Jul 2023 12:14:50 +0100
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
   /* ext {
        minSdkVersionSamsic = 18
        minSdkVersionNfcWriter = 21
        targetSdkVersionSonim = 30
        targetSdkVersionSamsic = 29
        targetSdkVersionNfcWriter = 31
    }*/
}


apply(plugin = "android-reporting")

project.subprojects {
    configurations.all {

        resolutionStrategy.dependencySubstitution.all {
            requested.let {
                if (it is ModuleComponentSelector && it.group == "com.streamwide") {
                    val targetProject = findProject(":application:${it.module}")

                    if (targetProject != null) {
                        useTarget(targetProject)
                    }
                }
            }
        }

    }
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false

}

task("clean", Delete::class) {
    delete(layout.buildDirectory.get().asFile)
}