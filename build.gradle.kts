import org.apache.tools.ant.taskdefs.condition.Os
import org.flywaydb.gradle.FlywayExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask


plugins {
  id("org.jetbrains.kotlin.jvm") version "1.6.0" apply false
  id("org.jetbrains.kotlin.plugin.serialization") version "1.6.0" apply false
  id("org.jlleitschuh.gradle.ktlint") version "10.2.1" apply false
  id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1" apply false
  id("org.openapi.generator") version "5.4.0" apply false
  id("org.jetbrains.kotlin.plugin.spring") version "1.6.0" apply false
  id("org.springframework.boot") version "2.5.6" apply false
  id("org.flywaydb.flyway") version "8.0.2" apply false
}

allprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "org.jlleitschuh.gradle.ktlint-idea")
  repositories {
    mavenCentral()
  }
  configure<KtlintExtension> {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
      reporter(ReporterType.CHECKSTYLE)
      reporter(ReporterType.JSON)
      reporter(ReporterType.HTML)
    }
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "io.spring.dependency-management")
  apply(plugin = "org.jetbrains.kotlin.plugin.spring")

  group = "io.rsh"
  version = "1.0-SNAPSHOT"

  configurations {
    all {
      resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
          useVersion("1.6.0")
          because("use single kotlin version")
        }
      }
    }
  }
  val javaLauncher = project.extensions.getByType<JavaToolchainService>().launcherFor {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
  tasks.withType<KotlinCompile> {
    kotlinJavaToolchain.toolchain.use(javaLauncher)
    kotlinOptions {
      freeCompilerArgs = listOf(
        "-Xjsr305=strict",
        "-Xinline-classes",
        "-opt-in=kotlin.RequiresOptIn"
      )
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
    outputs.upToDateWhen { false }

    testLogging {
      events("passed", "failed", "skipped")
      exceptionFormat = TestExceptionFormat.FULL
    }
  }

  val implementation by configurations
  val testImplementation by configurations

  dependencies {
    implementation.let {
      it(platform("org.springframework.boot:spring-boot-dependencies:2.6.4"))
      it("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
      it("io.arrow-kt:arrow-core:1.0.0")
    }
    testImplementation.let {
      it("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
      }
//      it(platform("org.junit:junit-bom:5.8.2"))
      it("org.junit.jupiter:junit-jupiter:")
      it("org.mockito.kotlin:mockito-kotlin:4.0.0")
    }
  }
}

project("book-phone-app:web") {
  apply(plugin = "org.openapi.generator")
  apply(plugin = "org.springframework.boot")

  val implementation by configurations
  val runtimeOnly by configurations
  dependencies {
    implementation.let {
      it(project(":book-phone-domain"))
      it(project(":book-phone-infra:persistence"))
      it(project(":book-phone-infra:adapter"))
      it("org.springframework.boot:spring-boot-starter-actuator")
      it("org.springframework.boot:spring-boot-starter-validation")
      it("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")
      it("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(
          group = "org.springframework.boot",
          module = "spring-boot-starter-tomcat"
        )
      }
      it("com.fasterxml.jackson.module:jackson-module-kotlin")
      it("org.springframework.boot:spring-boot-starter-reactor-netty")
      it("org.springframework.boot:spring-boot-starter-security")
      it("com.nimbusds:nimbus-jose-jwt")
      it("org.springframework.security:spring-security-oauth2-resource-server")
      it("org.springframework.security:spring-security-oauth2-jose")

      it("org.jetbrains.exposed", "exposed-core", "0.37.3")
      it("it.skrape:skrapeit:1.2.1")

      it("org.apache.commons:commons-text:1.9")
    }
    runtimeOnly("org.postgresql:postgresql")
    if (Os.isFamily(Os.FAMILY_MAC)) {
      runtimeOnly(
        group = "io.netty",
        name = "netty-resolver-dns-native-macos",
        version = "4.1.70.Final",
        classifier = "osx-aarch_64"
      )
    }
  }
  tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set("kotlin-spring")
    skipOverwrite.set(false)
    inputSpec.set("$projectDir/src/main/resources/book-phone.v1.oas.yml")
    outputDir.set("$projectDir")
    apiPackage.set("io.bookphone.web.api")
    validateSpec.set(true)
    modelPackage.set("io.bookphone.web.api.model")
    configOptions.put("delegatePattern", "true")
    modelNameSuffix.set("Dto")
    configOptions.put("basePackage", "io.bookphone.web")
    configOptions.put("reactive", "true")
    configOptions.put("useBeanValidation", "false")
//    globalProperties.put("supportingFiles", "false")
//    configOptions.put("serviceInterface", "true")
  }
}

project("book-phone-domain") {
  dependencies {
    val implementation by configurations
    implementation.let {
    }
  }
}

project("book-phone-infra:persistence") {
  apply(plugin = "org.flywaydb.flyway")
  configure<FlywayExtension> {
    url = "jdbc:postgresql://localhost:5432/phone-book"
    user = "postgres"
    password = "secret"
  }

  val implementation by configurations
  val testImplementation by configurations
  dependencies {
    implementation.let {
      it(project(":book-phone-domain"))
      it(project(":book-phone-infra:adapter"))
      it("org.jetbrains.exposed", "exposed-core", "0.37.3")
      it("org.jetbrains.exposed", "exposed-dao", "0.37.3")
      it("org.jetbrains.exposed", "exposed-jdbc", "0.37.3")
      it("org.jetbrains.exposed", "exposed-kotlin-datetime", "0.37.3")
      it("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    }
    testImplementation.let {
      it("org.postgresql:postgresql")
    }
  }
}
project("book-phone-infra:adapter") {
  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
  val implementation by configurations
  dependencies {
    implementation.let {
      it(project(":book-phone-domain"))
      it("it.skrape:skrapeit:1.2.1")
      it("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    }
  }
}
