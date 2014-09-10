package org.robobinding.plugin;

import org.gradle.api.DomainObjectCollection;
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.compile.JavaCompile

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant

/**
 * @author Cheng Wei
 *
 */
class AndroidAspectJPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		PluginContainer plugins = project.plugins;
		Plugin<?> plugin;
		DomainObjectCollection<?> variants;
		if (plugins.hasPlugin(AppPlugin)) {
            plugin = plugins.getPlugin(AppPlugin)
            variants = project.android.applicationVariants
        } else if (plugins.hasPlugin(LibraryPlugin)) {
            plugin = plugins.getPlugin(LibraryPlugin)
            variants = project.android.libraryVariants
        } else {
            throw new GradleException("The 'com.android.application' or 'com.android.library' plugin is required.")
        }
		
		AspectJPluginHelper helper = new AspectJPluginHelper(project)
		helper.setupConfigurations()
		
		project.afterEvaluate {
			variants.all { variant ->
				helper.createAspectJCompileTask(variant.name, variant.javaCompile, 
					variant.javaCompile.source, evaluateAndroidBootClasspath(plugin))
			}
		}
	}
	
	private String evaluateAndroidBootClasspath(Plugin<?> plugin) {
		List<String> bootClasspath
		if (plugin.properties['runtimeJarList']) {
			bootClasspath = plugin.runtimeJarList
		} else {
			bootClasspath = plugin.bootClasspath
		}
		
		return bootClasspath.join(File.pathSeparator)
	}
}