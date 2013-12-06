package com.github.hage1.gradle.tasks

import com.github.hage1.gradle.MakePluginSpec
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class GradlePluginProperty extends DefaultTask {
    Logger logger

    def GradlePluginProperty() {
        logger = Logging.getLogger(this.class)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    def exec() {
        def mainSourceSet = project.sourceSets.main
        def classes = project.fileTree(mainSourceSet.output.classesDir).files
        def dest = Paths.get(project.processResources.destinationDir.absolutePath)
        new MakePluginSpec(classes, dest, logger).execute(this)
    }
}
