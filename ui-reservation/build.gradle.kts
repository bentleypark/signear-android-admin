plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id ("androidx.navigation.safeargs.kotlin")
}

listOf(
    "commonConfiguration.gradle",
    "libraryConfiguration.gradle"
).forEach { file ->
    apply(from = "${rootProject.projectDir}/gradle/${file}")
}

android {
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(project(":common:ui-common"))
    implementation(project(":ui-login"))
    implementation(project(":domain"))

    implementation(Dep.Kotlin.stdlibJvm)
    implementation(Dep.Kotlin.coroutines.core)
    implementation(Dep.Kotlin.coroutines.android)
    implementation(Dep.AndroidX.legacySupport)

    implementation(Dep.AndroidX.activity.ktx)
    implementation(Dep.AndroidX.fragment.ktx)
    implementation(Dep.AndroidX.lifecycle.viewModelKtx)
    implementation(Dep.AndroidX.lifecycle.liveDataKtx)
    implementation(Dep.AndroidX.UI.material)
    implementation(Dep.AndroidX.UI.recyclerview)

    implementation(Dep.Dagger.Hilt.android)
    kapt(Dep.Dagger.Hilt.compiler)

    implementation(Dep.AndroidX.Navigation.fragmentKtx)
    implementation(Dep.AndroidX.Navigation.uiKtx)

    implementation(Dep.timber)

    implementation(Dep.CustomLib.spinner)

    coreLibraryDesugaring(Dep.Tool.desugarJdk)
}

kapt {
    useBuildCache = true
    mapDiagnosticLocations = true
    arguments {
        arg("dagger.formatGeneratedSource", "disabled")
        arg("dagger.fastInit", "enabled")
        arg("dagger.experimentalDaggerErrorMessages", "enabled")
    }
}