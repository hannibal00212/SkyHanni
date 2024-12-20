plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    this.mavenCentral()
    this.mavenLocal()
}
dependencies {
    this.implementation("com.google.code.gson:gson:2.10.1")
    this.implementation("com.google.guava:guava:33.2.1-jre")
}

sourceSets.main {
    this.kotlin.srcDir(file("src"))
}
gradlePlugin {
    this.plugins {
        this.create("simplePlugin") {
            this.id = "at.skyhanni.shared-variables"
            this.implementationClass = "at.skyhanni.sharedvariables.NoOp"
        }
    }
}
