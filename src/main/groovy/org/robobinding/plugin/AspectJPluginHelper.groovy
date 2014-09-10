package org.robobinding.plugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * @since 1.0
 * @version 
 * @author Cheng Wei
 *
 */
class AspectJPluginHelper {
	private final Project project
	
	AspectJPluginHelper(Project project) {
		this.project = project
	}
	
	void setupConfigurations() {
		ConfigurationContainer configurations = project.configurations
		
		for(configuration in ['aspectPath', 'ajInpath']) {
			if (configurations.findByName(configuration) == null) {
				configurations.create(configuration)
			}
		}
	}
	
	void createAspectJCompileTask(String taskVariant, 
		JavaCompile javaCompileToReplace, FileTree theSource, String theBootClasspath) {
	
		String variantName = taskVariant.capitalize()
		String taskName = "compile${variantName}AspectJ"
		
		project.tasks.create(name: taskName, , type: AspectJCompile) {
			description = 'Compiles source code with AspectJ Compiler';
			group = 'build'
			
			sourceCompatibility = javaCompileToReplace.sourceCompatibility
			targetCompatibility = javaCompileToReplace.targetCompatibility
			source = theSource
			destinationDir = javaCompileToReplace.destinationDir
			classpath = javaCompileToReplace.classpath
			bootClasspath = theBootClasspath
			aspectPath = project.configurations.aspectPath
			inpath = project.configurations.ajInpath
		}
		
		project.tasks."$taskName".setDependsOn(javaCompileToReplace.dependsOn)
		javaCompileToReplace.deleteAllActions()
		javaCompileToReplace.dependsOn project.tasks."$taskName"
	}

}
