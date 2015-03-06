package org.robobinding.plugin;

import org.gradle.api.DomainObjectCollection;
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.compile.JavaCompile

import com.android.build.gradle.BaseExtension
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
					variant.javaCompile.source, evaluateAndroidBootClasspath(plugin,project))
			}
		}
	}
	
	private String evaluateAndroidBootClasspath(Plugin<?> basePlugin, Project project) {
		List<String> bootClasspath
		BaseExtension baseExtension = project.getExtensions().getByName("android") as BaseExtension

		if (basePlugin.getMetaClass().getMetaMethod("getRuntimeJarList")) {
	      bootClasspath = basePlugin.getRuntimeJarList()
	    }
	    else if (baseExtension.getMetaClass().getMetaMethod("getBootClasspath")) {
	      bootClasspath = baseExtension.getBootClasspath()
	    }
	    else {
	      bootClasspath= basePlugin.getBootClasspath()
	    }
		
		return bootClasspath.join(File.pathSeparator)
	}
}