plugins {
    `gradle-enterprise`
}
rootProject.name = "time-sheets"
include("jira-rest-client")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

