package com.github.drinkjava2.jsqlbox.compiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

@SuppressWarnings("all")
public class DynamicCompileEngine {
	public static final DynamicCompileEngine instance = new DynamicCompileEngine();
	private static final Map<String, Class<?>> compiledClassCache = new ConcurrentHashMap<String, Class<?>>();

	private URLClassLoader parentClassLoader;
	private String classpath;

	private DynamicCompileEngine() {
		this.parentClassLoader = (URLClassLoader) this.getClass().getClassLoader();
		this.buildClassPath();
	}

	private static String readFileToString(String filename) {
		String result = null;
		File file = new File(filename);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			result = new String(chars);
			reader.close();
		} catch (IOException e) {
			throw new SqlBoxException("Error happen when open file '" + filename + "'", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					throw new SqlBoxException("Error happen when closing file '" + filename + "'", e);
				}
		}
		return result;
	}

	private void buildClassPath() {
		this.classpath = null;
		StringBuilder sb = new StringBuilder();
		for (URL url : this.parentClassLoader.getURLs()) {
			String p = url.getFile();
			sb.append(p).append(File.pathSeparator);
		}
		this.classpath = sb.toString();
		// Deal class path issue caused by run "mvn test" command
		if (classpath.indexOf("surefire/surefirebooter") > 0) {
			String path = StrUtils.substringAfter(classpath, "/");
			path = StrUtils.substringBeforeLast(path, ";");
			String mavenJarPath = readFileToString(path);
			mavenJarPath = StrUtils.substringBetween(mavenJarPath, "Class-Path: ", "Main-Class: ");
			mavenJarPath = StrUtils.replace(mavenJarPath, "\r\n ", "").trim();
			mavenJarPath = StrUtils.replace(mavenJarPath, " ", ";");
			classpath = StrUtils.replace(mavenJarPath, "file:/", "");
		}
	}

	public Class<?> javaCodeToClass(String fullClassName, String javaCode) {
		if (StrUtils.isEmpty(fullClassName))
			throw new SqlBoxException("Can not compile class with empty name");
		if (StrUtils.isEmpty(javaCode))
			throw new SqlBoxException("Can not compile class " + fullClassName + " with empty Java source code");

		Class<?> result = compiledClassCache.get(fullClassName);
		if (result != null)
			return result;

		// long start = System.currentTimeMillis();
		Object instance = null;
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));

		List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
		jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));

		List<String> options = new ArrayList<String>();
		options.add("-encoding");
		options.add("UTF-8");
		options.add("-classpath");
		options.add(this.classpath);

		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
		boolean success = task.call();

		if (success) {
			JavaClassObject jco = fileManager.getJavaClassObject();
			DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(this.parentClassLoader);
			result = dynamicClassLoader.loadClass(fullClassName, jco);
			if (result != null)
				compiledClassCache.put(fullClassName, result);
			return result;
		} else {
			String error = "";
			for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
				error = error + compilePrint(diagnostic);
			}
			throw new SqlBoxException(error + " \r\r\r <<< Java Source Code Compile Error \r" + javaCode);
		}
	}

	public Object javaCodeToNewInstance(String fullClassName, String javaCode)
			throws InstantiationException, IllegalAccessException {
		Class<?> clazz = javaCodeToClass(fullClassName, javaCode);
		Object instance = clazz.newInstance();
		return instance;
	}

	private String compilePrint(Diagnostic diagnostic) {
		// System.out.println("Code:" + diagnostic.getCode());
		// System.out.println("Kind:" + diagnostic.getKind());
		// System.out.println("Position:" + diagnostic.getPosition());
		// System.out.println("Start Position:" + diagnostic.getStartPosition());
		// System.out.println("End Position:" + diagnostic.getEndPosition());
		// System.out.println("Source:" + diagnostic.getSource());
		// System.out.println("Message:" + diagnostic.getMessage(null));
		// System.out.println("LineNumber:" + diagnostic.getLineNumber());
		// System.out.println("ColumnNumber:" + diagnostic.getColumnNumber());
		StringBuffer res = new StringBuffer();
		res.append("Code:[" + diagnostic.getCode() + "]\n");
		res.append("Kind:[" + diagnostic.getKind() + "]\n");
		res.append("Position:[" + diagnostic.getPosition() + "]\n");
		res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
		res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
		res.append("Source:[" + diagnostic.getSource() + "]\n");
		res.append("Message:[" + diagnostic.getMessage(null) + "]\n");
		res.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
		res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
		return res.toString();
	}

}