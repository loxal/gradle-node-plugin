package com.github.nodegradle.node.exec

import org.gradle.api.Project
import org.gradle.process.ExecResult

open class NodeExecRunner(project: Project) : ExecRunner(project) {
    override fun doExecute(): ExecResult {
        var exec = "node"
        if (ext!!.download) {
            exec = variant!!.nodeExec
        }
        return run(exec, arguments)
    }

    override fun computeAdditionalBinPath(): String? {
        if (ext!!.download) {
            return variant!!.nodeBinDir.absolutePath
        }
        return null
    }
}
