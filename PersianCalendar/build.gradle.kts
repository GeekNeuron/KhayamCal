
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    id("io.github.persiancalendar.appbuildplugin")
}

val javaVersion = JavaVersion.VERSION_17

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = javaVersion.majorVersion
        }
    }

    jvm("desktop")

    sourceSets {
        androidMain.dependencies {
            // Project owned libraries
            implementation(libs.persiancalendar.calendar)
            implementation(libs.persiancalendar.praytimes)
            implementation(libs.persiancalendar.calculator)
            implementation(libs.persiancalendar.qr)

            // https://github.com/cosinekitty/astronomy/releases/tag/v2.1.0
            implementation(libs.astronomy)

            // Google/JetBrains owned libraries (roughly platform libraries)
            implementation(libs.dynamicanimation)
            implementation(libs.androidx.core.ktx)
            implementation(libs.bundles.lifecycle)
            implementation(libs.browser)
            implementation(libs.work.manager.ktx)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlinx.html.jvm)
            implementation(libs.openlocationcode)
            implementation(libs.activity.ktx)

            implementation(libs.compose.activity)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.navigation)
            implementation(libs.compose.animation)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.runtime)
            implementation(libs.compose.material.icons.extended)
        }
        commonMain.dependencies {
        }
    }
}

operator fun File.div(child: String): File = File(this, child)
val generatedAppSrcDir =
    layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
android {
    compileSdk = 34

    sourceSets["main"].kotlin.srcDir(generatedAppSrcDir)
    sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/main/res")
    sourceSets["main"].resources.srcDirs("src/main/resources")

    buildFeatures {
        buildConfig = true
        compose = true
    }

    val gitInfo = providers.of(io.github.persiancalendar.gradle.GitInfoValueSource::class) {}.get()

    namespace = "com.byagowi.persiancalendar"

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 21
        targetSdk = 34
        versionCode = 915
        versionName = "9.1.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // It lowers the APK size and prevents crash in AboutScreen in API 21-23
        vectorDrawables.useSupportLibrary = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne", "zh-rCN", "ru"
        )
        setProperty("archivesBaseName", "PersianCalendar-$versionName-$gitInfo")
    }

    signingConfigs {
        create("nightly") {
            storeFile = rootProject.file("nightly.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    testOptions.unitTests.all { it.useJUnitPlatform() }

    buildTypes {
        create("nightly") {
            signingConfig = signingConfigs.getByName("nightly")
            versionNameSuffix = "-${defaultConfig.versionName}-$gitInfo-nightly"
            applicationIdSuffix = ".nightly"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "DEVELOPMENT", "true")
        }

        getByName("debug") {
            versionNameSuffix = "-${defaultConfig.versionName}-$gitInfo"
            buildConfigField("boolean", "DEVELOPMENT", "true")
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "DEVELOPMENT", "false")
        }
    }
    flavorDimensions += listOf("api")

    packaging {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    bundle {
        // We have in app locale change and don't want Google Play's dependency so better to disable
        language.enableSplit = false
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    dependencies {
        androidTestImplementation(libs.kotlinx.coroutines.test)
        implementation(platform(libs.compose.bom))
        androidTestImplementation(platform(libs.compose.bom))
        androidTestImplementation(libs.compose.ui.test.junit4)
        debugImplementation(libs.compose.ui.test.manifest)
        debugImplementation(libs.compose.ui.tooling)

        testImplementation(libs.junit)

        testImplementation(kotlin("test"))

        testImplementation(libs.junit.platform.runner)
        testImplementation(libs.junit.jupiter.api)
        testImplementation(libs.junit.jupiter.params)
        testRuntimeOnly(libs.junit.jupiter.engine)

        testImplementation(libs.bundles.mockito)

        testImplementation(libs.truth)

        androidTestImplementation(libs.test.runner)
        androidTestImplementation(libs.test.rules)
        androidTestImplementation(libs.test.core.ktx)
        androidTestImplementation(libs.androidx.test.ext.junit)
    }

    lint { disable += listOf("MissingTranslation") }
}

tasks.named("preBuild").configure { dependsOn(getTasksByName("codegenerators", false)) }
