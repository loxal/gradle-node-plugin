@file:Suppress("UnstableApiUsage")

package com.github.nodegradle.node.tasks

import com.github.nodegradle.NodePlugin
import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

class SetupTask: DefaultTask() {
    lateinit var config: NodeExtension
    lateinit var variant: Variant

    companion object {
        const val NAME = "nodeSetup"
    }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Download and install a local node/npm version."
        enabled = false
    }

    @Input
    fun getInput(): Set<Any> {
        configureIfNeeded()

        var set = HashSet<Any>()
        set.add(this.config.download)
        set.add(this.variant.archiveDependency)
        set.add(this.variant.exeDependency)
        return set
    }

    @OutputDirectory
    fun getNodeDir(): File {
        configureIfNeeded()
        return this.variant.nodeDir
    }

    fun configureIfNeeded()
    {
        if (this.config != null) {
            return
        }

        this.config = NodeExtension.get(this.project)
        this.variant = this.config.variant
    }

    @TaskAction
    fun exec() {
        configureIfNeeded()
        addRepositoryIfNeeded()

        if (this.variant.exeDependency != null) {
            copyNodeExe()
        }

        deleteExistingNode()
        unpackNodeArchive()
        setExecutableFlag()
    }

    fun copyNodeExe() {
        this.project.copy {
            it.from(getNodeExeFile())
            it.into(variant.nodeBinDir)
            it.rename("node.+\\.exe", "node.exe")
        }
    }

    fun deleteExistingNode()
    {
        this.project.delete(getNodeDir().parent)
    }

    fun unpackNodeArchive()
    {
        if (getNodeArchiveFile().getName().endsWith("zip")) {
            this.project.copy {
                it.from(project.zipTree(getNodeArchiveFile()))
                it.into(getNodeDir().parent)
            }
        } else if (variant.exeDependency != null) {
            //Remap lib/node_modules to node_modules (the same directory as node.exe) because that"s how the zip dist does it
            this.project.copy {
                it.from(project.tarTree(getNodeArchiveFile()))
                it.into(variant.nodeBinDir)
                it.eachFile {
                    val m = Regex("""^.*?[\\/]lib[\\/](node_modules.*$)""").matchEntire(it.path)
                    if (m != null && m.groups[1] != null) {
                        // remap the file to the root
                        it.path = m.groups[1]!!.value
                    } else {
                        it.exclude()
                    }
                }
                it.includeEmptyDirs = false
            }
        } else {
            this.project.copy {
                it.from(project.tarTree(getNodeArchiveFile()))
                it.into(getNodeDir().parent)
            }
            // Fix broken symlink
            val npm = Paths.get(variant.nodeBinDir.path, "npm")
            if (Files.deleteIfExists(npm)) {
                Files.createSymbolicLink(npm, Paths.get(variant.npmScriptFile))
            }
        }
    }

    fun setExecutableFlag() {
        if (!this.variant.windows) {
            File(this.variant.nodeExec).setExecutable(true)
        }
    }

    @Internal
    fun getNodeExeFile(): File {
        return resolveSingle(this.variant.exeDependency)
    }

    @Internal
    fun getNodeArchiveFile(): File {
        return resolveSingle(this.variant.archiveDependency)
    }

    fun resolveSingle(name: String): File {
        val dep = this.project.dependencies.create(name)
        val conf = this.project.configurations.detachedConfiguration(dep)
        conf.isTransitive = false
        return conf.resolve().iterator().next();
    }

    fun addRepositoryIfNeeded() {
        if (config.distBaseUrl != null) {
            addRepository(config.distBaseUrl)
        }
    }

    fun addRepository(distUrl: String) {
        this.project.repositories.ivy {
            it.url = URI(distUrl)
            it.patternLayout {
                it.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
                it.ivy("v[revision]/ivy.xml")
            }
            it.metadataSources {
                it.artifact()
            }
        }
    }
}