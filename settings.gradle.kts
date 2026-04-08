pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StoryBuilder"

// Include all modules with their source paths
include(":app")
project(":app").projectDir = file("src/app")

include(":domain")
project(":domain").projectDir = file("src/domain")

include(":data:local")
project(":data:local").projectDir = file("src/data/local")

include(":data:ai")
project(":data:ai").projectDir = file("src/data/ai")

include(":data:tts")
project(":data:tts").projectDir = file("src/data/tts")

include(":data:stt")
project(":data:stt").projectDir = file("src/data/stt")

include(":data:remote")
project(":data:remote").projectDir = file("src/data/remote")

include(":feature:chat-player")
project(":feature:chat-player").projectDir = file("src/feature/chat-player")

include(":feature:option-selection")
project(":feature:option-selection").projectDir = file("src/feature/option-selection")

include(":feature:text-input")
project(":feature:text-input").projectDir = file("src/feature/text-input")

include(":feature:genre-select")
project(":feature:genre-select").projectDir = file("src/feature/genre-select")

include(":feature:character-create")
project(":feature:character-create").projectDir = file("src/feature/character-create")

include(":feature:story-library")
project(":feature:story-library").projectDir = file("src/feature/story-library")

include(":feature:user-dashboard")
project(":feature:user-dashboard").projectDir = file("src/feature/user-dashboard")

include(":core:ui")
project(":core:ui").projectDir = file("src/core/ui")

include(":core:common")
project(":core:common").projectDir = file("src/core/common")
