package com.github.nodegradle.npm

import com.github.nodegradle.node.exec.ExecRunner
import org.gradle.api.Project
import org.gradle.process.ExecResult
import java.io.File

open class NpmExecRunner(project: Project) : ExecRunner(project) {
    override fun doExecute(): ExecResult {
        var exec = variant!!.npmExec
//        var arguments: MutableList<String> = mutableListOf()
        var arguments = this.arguments

        if ( ext!!.download ) {
            val localNpm = project.file(File(this.ext!!.nodeModulesDir, "node_modules/npm/bin/npm-cli.js"))
            if (localNpm.exists()) {
                exec = variant!!.nodeExec
                arguments = mutableListOf(localNpm.absolutePath)
                arguments.addAll(this.arguments)
            }
            else if (!File(exec).exists()) {
                exec = variant!!.nodeExec
                arguments = mutableListOf(variant!!.npmScriptFile)
                arguments.addAll(this.arguments)
            }
        }
        project.logger.warn("Running ${exec.toString()} ${arguments.toString()}")
        return run( exec, arguments )
    }

    override fun computeAdditionalBinPath(): String? {
        if (ext!!.download) {
            val npmBinDir = this.variant!!.npmBinDir.absolutePath;
            val nodeBinDir = this.variant!!.nodeBinDir.absolutePath;
            return npmBinDir + File.pathSeparator + nodeBinDir
        }
        return null
    }
}
