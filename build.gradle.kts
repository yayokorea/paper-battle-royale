plugins {
    java
}

version = providers.fileContents(layout.projectDirectory.file("version.txt")).asText.map(String::trim).get()

repositories { mavenCentral(); maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.60-beta")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
tasks.test { useJUnitPlatform() }
tasks.jar { archiveBaseName.set("WildBattleRoyale") }
val pluginVersion = version
tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to pluginVersion) }
}
