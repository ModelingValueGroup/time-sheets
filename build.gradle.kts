plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jira-rest-client"))
    implementation("org.freemarker:freemarker:2.3.31")
    implementation("com.google.code.gson:gson:2.8.6")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

application {
    mainClass.set("demo.App")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

if (gradle.startParameter.isBuildScan()) {
    println("TOMOMTOM ")
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            publishAlways()
        }
    }
}

tasks.withType<Test> {
    testLogging.showStandardStreams = true
}
