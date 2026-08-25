plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.nasfinder.whattoeat"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nasfinder.whattoeat"
        minSdk = 26
        targetSdk = 37
        versionCode = 340540
        versionName = "0.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BUILD_STAMP", "\"202608251140\"")
        buildConfigField("String", "API_BASE_URL", "\"https://nasfinder.com\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

val copyHandoffAssets = tasks.register<Copy>("copyHandoffAssets") {
    val sourceDir = file("${project.rootDir}/android-handoff/source-assets")
    val targetDir = file("${project.projectDir}/src/main/res/drawable-nodpi")
    from(sourceDir) {
        include("**/*.png")
        eachFile {
            val rawName = name.substringBeforeLast(".")
            val mapped = when (rawName) {
                "AppIcon-1024" -> "ic_app_icon_1024"
                "Wordmark" -> "img_wordmark"
                "LunchHero" -> "img_lunch_hero"
                "PinWell" -> "img_pin_well"
                "EmptyRecent" -> "img_empty_recent"
                "EmptyFavorites" -> "img_empty_favorites"
                "FoodMain" -> "img_food_main"
                "FoodSide1" -> "img_food_side1"
                "FoodSide2" -> "img_food_side2"
                "FoodSide3" -> "img_food_side3"
                "MapApple" -> "img_map_apple"
                "MapNaver" -> "img_map_naver"
                "MapKakao" -> "img_map_kakao"
                "MapGoogle" -> "img_map_google"
                "AutoWell" -> "img_auto_well"
                "BackWell" -> "img_back_well"
                "ChartWell" -> "img_chart_well"
                "FoodBibimbap" -> "img_food_bibimbap"
                "LeatherTexture" -> "img_leather_texture"
                "LocationChoices" -> "img_location_choices"
                "MainHeartWell" -> "img_main_heart_well"
                "SearchWell" -> "img_search_well"
                else -> rawName.lowercase().replace("-", "_")
            }
            path = "$mapped.png"
        }
    }
    into(targetDir)
    includeEmptyDirs = false
}

tasks.named("preBuild") {
    dependsOn(copyHandoffAssets)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
