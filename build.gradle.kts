plugins {
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("plugin.jpa") version "1.9.24"
	jacoco
	id("org.sonarqube") version "5.1.0.4882"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Configurações de Inicialização e Infra
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	
	// Banco de Dados e Kotlin
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	
	runtimeOnly("org.postgresql:postgresql")
	
	// Testes
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.cucumber:cucumber-spring:7.18.0")
	testImplementation("io.cucumber:cucumber-java:7.18.0")
	testImplementation("io.cucumber:cucumber-junit-platform-engine:7.18.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("com.h2database:h2")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// ---------- JaCoCo – cobertura mínima 80% ----------
tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.jacocoTestReport)
	violationRules {
		rule {
			excludes = listOf(
				"com.oficina_os_service.infra.repository.*",
				"com.oficina_os_service.infra.nosql.*",
				"com.oficina_os_service.OficinaOsServiceApplication*"
			)
			limit {
				minimum = BigDecimal("0.80")
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}

// ---------- SonarQube ----------
sonar {
	properties {
		property("sonar.projectKey", "rodriguessbarbara_oficina-os-service")
		property("sonar.organization", "rodriguessbarbara")
		property("sonar.projectName", "oficina-os-service")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.sources", "src/main/kotlin")
		property("sonar.tests", "src/test/kotlin")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
		property("sonar.qualitygate.wait", "true")
		property(
			"sonar.coverage.exclusions",
			"**/OficinaOsServiceApplication.kt," +
					"**/infra/repository/**," +
					"**/infra/nosql/**," +
					"**/domain/enum/**," +
					"**/domain/model/**," +
					"**/infra/dto/**"
		)
	}
}

tasks.named("sonar") {
	dependsOn(tasks.jacocoTestReport)
}
