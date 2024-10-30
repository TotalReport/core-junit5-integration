plugins {
    id("org.openapi.generator") version "7.9.0"
}

repositories {
    mavenCentral()
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("${rootDir}/specs/contract.yml")
    outputDir.set("${rootDir}/api")

    apiPackage.set("com.craftens.totalreport.openapi.api")
    invokerPackage.set("com.craftens.totalreport.openapi.invoker")
    modelPackage.set("com.craftens.totalreport.openapi.model")

    configOptions.put("dateLibrary", "java8")
    configOptions.put("useRuntimeException", "true")
    configOptions.put("groupId", "com.craftens.totalreport")
    configOptions.put("artifactId", "total-report-openapi-client")
    configOptions.put("artifactDescription", "Total Report OpenAPI client")
    configOptions.put("artifactVersion", "1.0.0")
}

tasks.create("cleanGeneratedApi") {
    doLast {
        fileTree("${rootDir}/api").forEach { it.delete() }
    }
}

tasks.get("openApiGenerate").dependsOn("cleanGeneratedApi")