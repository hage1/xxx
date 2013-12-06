package com.github.hage1.gradle.plugins

import com.github.hage1.gradle.GradlePlugin
import com.github.hage1.gradle.MakePluginSpec
import com.github.hage1.gradle.tasks.GradlePluginProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin

/**
 * Created by wataru on 13/12/06.
 */
@GradlePlugin(alias = 'plugin-dev')
class PluginPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def Task classes = project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME)
        project.tasks.create('makePluginSpec', GradlePluginProperty).with {
            project.tasks.getByPath('jar').dependsOn delegate
        }
    }
}
