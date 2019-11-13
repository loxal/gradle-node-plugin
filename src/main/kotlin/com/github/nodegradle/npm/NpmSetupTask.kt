package com.github.nodegradle.npm

import com.github.nodegradle.NodePlugin
import com.github.nodegradle.node.tasks.SetupTask
import com.moowork.gradle.node.NodeExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult

class NpmSetupTask: DefaultTask() {
    lateinit var config: NodeExtension

    @get:Nested
    var runner: NpmExecRunner

    @get:Internal
    var result: ExecResult? = null

    @get:Input
    var args: MutableList<String> = mutableListOf()

    companion object {
        const val NAME = "npmSetup"
        fun proxySettings(): List<String> {
            arrayOf(arrayOf("http", "--proxy"), arrayOf("https", "--https-proxy")).forEach {
                proxySettings ->
                var proxyHost = System.getProperty(proxySettings[0] + ".proxyHost")
                val proxyPort = System.getProperty(proxySettings[0] + ".proxyPort")
                if (proxyHost != null && proxyPort != null) {
                    proxyHost = proxyHost.replace(Regex("^https?://"), "")
                    val proxyUser = System.getProperty(proxySettings[0] + ".proxyUser")
                    val proxyPassword = System.getProperty(proxySettings[0] + ".proxyPassword")
                    if (proxyUser != null && proxyPassword != null) {
                        return listOf("${proxySettings[1]} ${proxySettings[0]}://$proxyUser:$proxyPassword@$proxyHost:$proxyPort")
                    } else {
                        return listOf("${proxySettings[1]} ${proxySettings[0]}://$proxyHost:$proxyPort")
                    }
                }
            }
            return emptyList()
        }
    }

    init {
        group = NodePlugin.NODE_GROUP
        runner = NpmExecRunner(project)
        description = "Setup a specific version of npm to be used by the build."
        enabled = false
        dependsOn(SetupTask.NAME)
    }

    @TaskAction
    fun exec()
    {
        val execArgs = mutableListOf<String>()
        execArgs.addAll(args)

        runner.arguments = execArgs
        result = runner.execute()
    }

    @Input
    fun getInput(): Set<Any> {
        configureIfNeeded()

        val set = HashSet<Any>()
        set.add(this.config.download)
        set.add(this.config.npmVersion)
        set.add(this.config.npmWorkDir)
        return set
    }

    fun configureVersion(npmVersion: String) {
        if (npmVersion.isNotEmpty()) {
            logger.debug( "Setting npmVersion to ${npmVersion}" )
            args = mutableListOf("install", "--global", "--no-save")
            args.addAll(proxySettings())
            args.addAll(listOf("--prefix", config.variant.npmDir.absolutePath, "npm@$npmVersion"))

            enabled = true
        }
    }

    private fun configureIfNeeded() {
        if (this.config != null) {
            return
        }

        this.config = NodeExtension.get(this.project)
    }
}
