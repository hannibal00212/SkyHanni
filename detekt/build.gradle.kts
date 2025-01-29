import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    implementation("io.gitlab.arturbosch.detekt:detekt-api:1.23.7")
    ksp(libs.autoservice.ksp)
    implementation(libs.autoservice.annotations)
    implementation("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.7")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:1.23.7")
}

tasks.withType<Detekt>().configureEach {
    onlyIf { false }
}
