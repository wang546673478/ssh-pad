pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repository.apache.org/content/groups/public/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SSHPad"
include(":app")
