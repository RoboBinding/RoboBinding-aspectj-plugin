package org.robobinding.plugin;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * @author Cheng Wei
 *
 */
class AspectJPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.plugins.apply(JavaPlugin)

		AspectJPluginHelper helper = new AspectJPluginHelper(project)
		helper.setupConfigurations()

		JavaCompile compileJava = project.tasks.compileJava
		if (isMainSourceSetsNotEmpty(project)) {
			helper.createAspectJCompileTask("", compileJava, compileJava.source, "")
		}

		if (isTestSourceSetsNotEmpty(project)) {
			JavaCompile compileTestJava = project.tasks.compileTestJava;
			FileTree actualSource = compileJava.source + compileTestJava.source
			helper.createAspectJCompileTask("test", compileTestJava, actualSource, "")
		}
	}
	
	private boolean isMainSourceSetsNotEmpty(Project project) {
		return !project.sourceSets.main.allJava.isEmpty()
	}
	
	private boolean isTestSourceSetsNotEmpty(Project project) {
		return !project.sourceSets.test.allJava.isEmpty();
	}
}