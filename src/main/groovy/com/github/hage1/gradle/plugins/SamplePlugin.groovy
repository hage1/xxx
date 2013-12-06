package com.github.hage1.gradle.plugins

import com.github.hage1.gradle.GradlePlugin
import org.gradle.api.Project
import org.gradle.api.Plugin

@GradlePlugin(alias = 'hoge')
class SamplePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.tasks.create('hoge').with {
            doFirst {
                println 'hoge!!!!!'
            }
            description = 'print hoge'
        }
    }
}
