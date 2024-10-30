plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.craftens.totalreport"
            artifactId = "total-report-junit5"
            version = "1.0.0-SNAPSHOT"

            from(components["java"])

        }
    }
}

val embeddedJars by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    implementation(project(":api"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.junit.jupiter:junit-jupiter:5.11.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.11.2")

    embeddedJars(project(":api"))

    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("ch.qos.logback:logback-classic:1.5.9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.2")
    testImplementation("org.junit.platform:junit-platform-testkit:1.11.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
//    from(project(":api").sourceSets.main.get().output)
    from( configurations["embeddedJars"] )
}


tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
