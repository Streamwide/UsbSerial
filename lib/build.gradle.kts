/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 27 Jul 2023 08:31:53 +0100
 * @copyright  Copyright (c) 2023 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2023 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 27 Jul 2023 08:29:20 +0100
 */

plugins {
    id("com.streamwide.android-library-convention")
    id("kotlin-kapt")
}


android {

    group = "com.felhr.usbserial"
    namespace = "com.streamwide.smartms.lib.actuator"


    defaultConfig {
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled  = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {
    implementation (libs.androidx.annotation)
    implementation (libs.okio.okio)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.junit.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
