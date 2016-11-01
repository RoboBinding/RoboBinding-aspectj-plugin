package org.robobinding.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.CompositeFileCollection;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author Cheng Wei
 *
 */
public class AspectJCompile extends AbstractCompile {
	private FileCollection aspectPath;
	private FileCollection inpath;
	private String bootClasspath;

	@TaskAction
	public void compile() {
		Logger log = getProject().getLogger();
		List<String> args = Lists.newArrayList(
				"-encoding", "UTF-8",
				"-source", getSourceCompatibility(),
				"-target", getTargetCompatibility(),
				"-d", getDestinationDir().getAbsolutePath(), 
				"-classpath", getClasspath().getAsPath(),
				"-sourceroots", getSourceRoots());
		
		if(!getAspectPath().isEmpty()) {
			args.add("-aspectpath");
			args.add(getAspectPath().getAsPath());
		}
		
		if(!Strings.isNullOrEmpty(bootClasspath)) {
			args.add("-bootclasspath");
			args.add(bootClasspath);
		}

		log.debug("Before executing: ajc {}", Joiner.on(" ").join(args));

		MessageHandler messageHandler = new MessageHandler(true);
		messageHandler.setInterceptor(new GradleMessageHandler(log));
		new Main().run(args.toArray(new String[0]), messageHandler);
		abartWhenError(messageHandler);
	}

	private void abartWhenError(MessageHandler handler) {
		IMessage[] errors = handler.getMessages(IMessage.ERROR, true);
		if (errors.length != 0) {
			IMessage firstMessage = errors[0];
			throw new GradleException(firstMessage.getMessage(),
					firstMessage.getThrown());
		}
	}

	private String getSourceRoots() {
		try {
			List<String> sourceRoots = new ArrayList<String>();
			Method method = CompositeFileCollection.class.getDeclaredMethod("getAsFileTrees", new Class<?>[0]);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			Collection<DirectoryFileTree> dirFileTrees = (Collection<DirectoryFileTree>)method.invoke(getSource(), new Object[0]);
			for (DirectoryFileTree dirFileTree : dirFileTrees) {
				String sourceRoot = dirFileTree.getDir().getAbsolutePath();
				File file = new File(sourceRoot);
				if (!file.exists()) {
					getLogger().warn("Skipped a non-existent source code root directory: " + sourceRoot);
					continue;
				}
				sourceRoots.add(sourceRoot);
			}
			return Joiner.on(File.pathSeparator).join(sourceRoots);
		} catch (NoSuchMethodException e) {
			throw new GradleException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new GradleException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new GradleException(e.getMessage(), e);
		}
	}

	@InputFiles
	public FileCollection getAspectPath() {
		return aspectPath;
	}

	public void setAspectPath(FileCollection aspectPath) {
		this.aspectPath = aspectPath;
	}

	@InputFiles
	public FileCollection getInpath() {
		return inpath;
	}

	public void setInpath(FileCollection inpath) {
		this.inpath = inpath;
	}

	@Input@Optional
	public String getBootClasspath() {
		return bootClasspath;
	}

	public void setBootClasspath(String bootClasspath) {
		this.bootClasspath = bootClasspath;
	}

	private static class GradleMessageHandler implements IMessageHandler {
		private final Logger log;

		public GradleMessageHandler(Logger log) {
			this.log = log;
		}

		public boolean handleMessage(IMessage message) throws AbortException {
			Kind messageKind = message.getKind();
			String messageText = message.toString();
			if (messageKind == IMessage.ABORT) {
				log.error(messageText);
			} else if (messageKind == IMessage.DEBUG) {
				log.debug(messageText);
			} else if (messageKind == IMessage.ERROR) {
				log.error(messageText);
			} else if (messageKind == IMessage.FAIL) {
				log.error(messageText);
			} else if (messageKind == IMessage.INFO) {
				log.info(messageText);
			} else if (messageKind == IMessage.WARNING) {
				log.warn(messageText);
			} else if (messageKind == IMessage.WEAVEINFO) {
				log.info(messageText);
			} else if (messageKind == IMessage.TASKTAG) {
				// ignore
			} else {
				throw new GradleException(
						"Unknown message kind from AspectJ compiler: "
								+ messageKind.toString());
			}
			return false;
		}

		public boolean isIgnoring(Kind kind) {
			return false;
		}

		public void dontIgnore(Kind kind) {
		}

		public void ignore(Kind kind) {
		}

	}
}