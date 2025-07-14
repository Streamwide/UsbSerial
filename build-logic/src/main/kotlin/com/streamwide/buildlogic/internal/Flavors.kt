/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 2 Nov 2023 08:27:40 +0100
 * @copyright  Copyright (c) 2023 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2023 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 2 Nov 2023 08:27:39 +0100
 */

package com.streamwide.buildlogic.internal

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureFlavors(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        flavorDimensions += "certified_dimension"

        productFlavors {
            create("_certified") {
                dimension = "certified_dimension"
                minSdk = 31
            }
            create("_uncertified") {
                dimension = "certified_dimension"
                //isDefault = true
            }
        }
        sourceSets {
            getByName("_certified") {
                java.srcDirs("src/config/_certified/java")
            }
            getByName("_uncertified") {
                java.srcDirs("src/config/_uncertified/java")
            }
        }
    }
}