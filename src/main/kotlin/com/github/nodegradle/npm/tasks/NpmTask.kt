package com.github.nodegradle.npm.tasks

import com.github.nodegradle.NodePlugin
import com.github.nodegradle.npm.NpmExecRunner
import com.moowork.gradle.node.npm.NpmSetupTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.File

class NpmTask: DefaultTask() {
    @get:Nested
    val runner: NpmExecRunner

    @get:Input
    @get:Optional
    var args: MutableList<String>? = null

    @get:Input
    @get:Optional
    var npmCommand: MutableList<String>? = null

    @get:Internal
    var result: ExecResult? = null

    init {
        group = NodePlugin.NODE_GROUP
        runner = NpmExecRunner( this.project )
        dependsOn( NpmSetupTask.NAME )
    }


    fun setEnvironment(value: Map<String, Nothing>) {
        this.runner.environment.putAll(value)
    }

    fun setWorkingDir(workingDir: File) {
        this.runner.workingDir = workingDir
    }

    fun setIgnoreExitValue(value: Boolean) {
        this.runner.ignoreExitValue = value
    }

    fun setExecOverrides(closure: Action<ExecSpec>) {
        this.runner.execOverrides = closure
    }

    @TaskAction
    fun exec()
    {
        if (npmCommand != null) {
            runner.arguments.addAll(npmCommand!!)
        }

        args?.let { runner.arguments.addAll(it) }
        result = runner.execute()
    }
}