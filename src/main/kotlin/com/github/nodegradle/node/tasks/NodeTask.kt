package com.github.nodegradle.node.tasks

import com.github.nodegradle.NodePlugin
import com.github.nodegradle.node.exec.NodeExecRunner
import com.moowork.gradle.node.task.SetupTask
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.File

open class NodeTask: DefaultTask() {
    @get:Nested
    var runner: NodeExecRunner

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var script: File? = null

    @get:Internal
    var result: ExecResult? = null

    @get:Input
    var args: MutableList<String> = mutableListOf()

    @get:Input
    var options: Iterable<String> = mutableListOf()

    init {
        group = NodePlugin.NODE_GROUP
        runner = NodeExecRunner(project)
        dependsOn(SetupTask.NAME)
    }

    @TaskAction
    fun exec()
    {
        checkNotNull(script) { "Required script property is not set." }

        val execArgs = mutableListOf<String>()
        execArgs.addAll(options)
        execArgs.add(script!!.absolutePath )
        execArgs.addAll(args)

        runner.arguments = execArgs
        result = runner.execute()
    }

    fun setEnvironment( value: Map<String, Nothing> ) {
        runner.environment.putAll(value)
    }

    fun setWorkingDir(workingDir: File) {
        runner.workingDir = workingDir
    }

    fun setIgnoreExitValue(value: Boolean) {
        runner.ignoreExitValue = value
    }

    fun setExecOverrides(closure: Action<ExecSpec>) {
        runner.execOverrides = closure
    }
}
