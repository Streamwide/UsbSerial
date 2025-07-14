/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Mon, 30 Oct 2023 12:19:45 +0100
 * @copyright  Copyright (c) 2023 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2023 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Mon, 30 Oct 2023 11:57:58 +0100
 */

rootProject.name = "USB_SERIAL_STW"

pluginManagement {

    includeBuild("build-logic")



    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()

    }

}

plugins {
    id("com.gradle.enterprise") version "3.9"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
include(":lib")



