package com.github.nodegradle.node.exec

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.variant.Variant
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.File

abstract class ExecRunner(val project: Project) {
    var ext: NodeExtension? = null
    var variant: Variant? = null

    @get:Input
    var environment = mutableMapOf<String, Any>()

    @get:Internal
    var workingDir: File? = null

    @get:Internal
    var arguments: MutableList<String> = mutableListOf()

    @get:Input
    var ignoreExitValue = false

    @get:Internal
    var execOverrides: Action<ExecSpec>? = null

    fun run(exec: String, args: MutableList<String> ): ExecResult {
        var realExec = exec
        var realArgs = args
        var execEnvironment = computeExecEnvironment()
        var execWorkingDir = computeWorkingDir()
        return project.exec {
            it.executable = realExec
            it.args = realArgs
            it.environment = execEnvironment
            it.setIgnoreExitValue(ignoreExitValue)
            it.workingDir = execWorkingDir

            if ( execOverrides != null )
            {
                execOverrides!!.execute(it)
            }
        }
    }

    fun computeWorkingDir(): File {
//        var directory = workingDir ?: project.node.nodeModulesDir
        val directory = workingDir ?: ext!!.nodeModulesDir
        directory.mkdirs()
        return directory
    }

    fun computeExecEnvironment(): Map<String, Any> {
        var environment = mutableMapOf<String, Any>()
        environment.putAll(System.getenv())
        environment.putAll(this.environment)
        var path = computeAdditionalBinPath()
        if (path != null)
        {
            // Take care of Windows environments that may contain "Path" OR "PATH" - both existing
            // possibly (but not in parallel as of now)
            if (environment["Path"] != null) {
                environment["Path"] = path + File.pathSeparator + environment["Path"]
            } else {
                environment["PATH"] = path + File.pathSeparator + environment["PATH"]
            }
        }
        return environment
    }

    abstract fun computeAdditionalBinPath(): String?

    fun execute(): ExecResult
    {
        ext = NodeExtension.get(project)
        variant = ext!!.variant
        return doExecute()
    }

    abstract fun doExecute(): ExecResult
}