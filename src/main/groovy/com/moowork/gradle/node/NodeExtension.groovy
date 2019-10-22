package com.moowork.gradle.node

import com.moowork.gradle.node.variant.Variant
import org.gradle.api.Project

class NodeExtension
{
    public final static String NAME = 'node'
    public File workDir
    public File npmWorkDir
    public File yarnWorkDir
    public File nodeModulesDir
    public String version = '10.14.0'
    public String npmVersion = ''
    public String yarnVersion = ''
    public String distBaseUrl = 'https://nodejs.org/dist'
    public String npmCommand = 'npm'
    public String npxCommand = 'npx'
    public String npmInstallCommand = 'install'
    public String yarnCommand = 'yarn'
    public boolean download = false
    public Variant variant

    NodeExtension( final Project project )
    {
        def cacheDir = new File( project.projectDir, '.gradle' )
        this.workDir = new File( cacheDir, 'nodejs' )
        this.npmWorkDir = new File( cacheDir, 'npm' )
        this.yarnWorkDir = new File( cacheDir, 'yarn' )
        this.nodeModulesDir = project.projectDir
    }

    static NodeExtension get( final Project project )
    {
        return project.extensions.getByType( NodeExtension )
    }

    static NodeExtension create( final Project project )
    {
        return project.extensions.create( NAME, NodeExtension, project )
    }
}
