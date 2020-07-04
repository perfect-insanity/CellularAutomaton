plugins {
    id("org.jetbrains.kotlin.js") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.70"
}

repositories {
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains:kotlin-react:16.13.0-pre.94-kotlin-1.3.70")
    implementation("org.jetbrains:kotlin-react-dom:16.13.0-pre.94-kotlin-1.3.70")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.109-kotlin-1.3.72")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
    implementation(npm("styled-components"))
    implementation(npm("inline-style-prefixer"))
    implementation(npm("@material-ui/core", "4.10.2"))
}

kotlin.target.browser { }