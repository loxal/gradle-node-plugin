package com.github.nodegradle

import com.github.nodegradle.node.tasks.NodeTask
import com.github.nodegradle.node.tasks.SetupTask
import com.github.nodegradle.npm.NpmSetupTask
import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.npm.NpmInstallTask
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.npm.NpxTask
import com.moowork.gradle.node.variant.VariantBuilder
import com.moowork.gradle.node.yarn.YarnInstallTask
import com.moowork.gradle.node.yarn.YarnSetupTask
import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class NodePlugin: Plugin<Project> {
    companion object {
        const val NODE_GROUP = "Node"
    }

    override fun apply(project: Project) {
        val config = NodeExtension.create(project)

        project.extensions.extraProperties.set(NodeTask::class.simpleName!!, NodeTask::class)
        project.extensions.extraProperties.set(NpmTask::class.simpleName!!, NpmTask::class)
        project.extensions.extraProperties.set(NpxTask::class.simpleName!!, NpxTask::class)
        project.extensions.extraProperties.set(YarnTask::class.simpleName!!, YarnTask::class)

        project.tasks.create(NpmInstallTask.NAME, NpmInstallTask::class.java)
        project.tasks.create(YarnInstallTask.NAME, YarnInstallTask::class.java)
        val setupTask = project.tasks.create(SetupTask.NAME, SetupTask::class.java)
        val npmSetupTask = project.tasks.create(NpmSetupTask.NAME, NpmSetupTask::class.java)
        val yarnSetupTask = project.tasks.create(YarnSetupTask.NAME, YarnSetupTask::class.java)

        addNpmRule(project)
        addYarnRule(project)

        project.afterEvaluate {
            config.variant = VariantBuilder(config).build()
            setupTask.isEnabled = config.download
            npmSetupTask.configureVersion(config.npmVersion)
            yarnSetupTask.configureVersion(config.yarnVersion)
        }
    }

    private fun addNpmRule(project: Project) {
        // note this rule also makes it possible to specify e.g. "dependsOn npm_install"
        project.tasks.addRule("Pattern: \"npm_<command>\": Executes an NPM command.") { taskName ->
            if (taskName.startsWith( "npm_" )) {
                val npmTask = project.tasks.create(taskName, NpmTask::class.java)
                val tokens = taskName.split( '_' ).drop(1) // all except first
                npmTask.npmCommand = tokens.toTypedArray()

                if (tokens.first().toLowerCase() == "run") {
                    npmTask.dependsOn( NpmInstallTask.NAME )
                }
            }
        }
    }

    private fun addYarnRule(project: Project) {
        // note this rule also makes it possible to specify e.g. "dependsOn yarn_install"
        project.tasks.addRule("Pattern: \"yarn_<command>\": Executes an Yarn command." ) { taskName: String ->
            if (taskName.startsWith( "yarn_" )) {
                val yarnTask = project.tasks.create(taskName, YarnTask::class.java)
                val tokens = taskName.split( '_' ).drop(1) // all except first
                yarnTask.yarnCommand = tokens.toTypedArray()

                if (tokens.first().toLowerCase() == "run") {
                    yarnTask.dependsOn(YarnInstallTask.NAME)
                }
            }
        }
    }

}
