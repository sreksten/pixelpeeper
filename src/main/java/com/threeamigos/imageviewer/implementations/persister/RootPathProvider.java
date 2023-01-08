package com.threeamigos.imageviewer.implementations.persister;

import java.io.File;

import javax.swing.JOptionPane;

public class RootPathProvider {

	private static final String EMPTY_PATH_SUPPLIED_FOR_PREFERENCES_DIRECTORY = "An empty path was supplied to the System property \"preferences.directory\".";
	private static final String PREFERENCES_PARENT_DIRECTORY_S_NOT_WRITEABLE = "Preferences parent directory %s is not writable.";
	private static final String PREFERENCES_PARENT_DIRECTORY_S_NOT_READABLE = "Preferences parent directory %s is not readable.";
	private static final String PREFERENCES_DIRECTORY_CANNOT_BE_WRITTEN = "Preferences directory cannot be written. Preferences cannot be saved.";
	private static final String PREFERENCES_DIRECTORY_CANNOT_BE_READ = "Preferences directory cannot be read. Preferences canot be read.";
	private static final String PREFERENCES_DIRECTORY_IS_A_FILE = "Preferences directory is actually a file. Preferences cannot be saved.";
	private static final String CLASS_WITH_EMPTY_PACKAGE = "A class with empty package name was passed to the RootPathProvider. Pass the main class and check it is contained in a proper package.";
	private static final String CLASS_WITH_NO_PACKAGE = "A class with no package was passed to the RootPathProvider. This means yo may be passing an array type, a primitive type or void. Pass the main class instead.";
	private static final String CLASS_WITH_NO_CANONICAL_NAME = "A class with no canonical name was passed to the RootPathProvider. This means you're passing either a local, an anonymous or a hidden class. Pass the main class instead.";
	
	private final String rootPath;
	private final boolean rootPathAccessible;

	public RootPathProvider(Object object) {
		this(object.getClass());
	}

	public RootPathProvider(Class<?> clazz) {

		String packageName = extractPackageName(clazz);

		String home = getPreferencesPath();

		File file = new File(home);
		if (!file.exists()) {
			if (checkParentPathIsAccessible(file)) {
				rootPathAccessible = true;
				rootPath = buildRootPath(home, packageName);
			} else {
				rootPathAccessible = false;
				rootPath = null;
			}
		} else { 
			if (checkFileIsAccessible(file)) {
				rootPathAccessible = true;
				rootPath = buildRootPath(home, packageName);
			} else {
				rootPathAccessible = false;
				rootPath = null;
			}
		}
	}

	public boolean isRootPathAccessible() {
		return rootPathAccessible;
	}

	public String getRootPath() {
		return rootPath;
	}

	private String extractPackageName(Class<?> clazz) {
		try {
			return extractPackageNameImpl(clazz);
		} catch (Exception e) {
			abort(e.getMessage());
			return null;
		}
	}

	String extractPackageNameImpl(Class<?> clazz) throws Exception {
		String canonicalName = clazz.getCanonicalName();
		if (canonicalName == null) {
			throw new IllegalArgumentException(CLASS_WITH_NO_CANONICAL_NAME);
		}
		Package classPackage = clazz.getPackage();
		if (classPackage == null) {
			throw new IllegalArgumentException(CLASS_WITH_NO_PACKAGE);
		}
		String packageName = classPackage.getName();
		if (packageName == null || packageName.trim().isEmpty()) {
			throw new IllegalArgumentException(CLASS_WITH_EMPTY_PACKAGE);
		}
		return packageName;
	}

	private String getPreferencesPath() {
		try {
			return getPreferencesPathImpl();
		} catch (Exception e) {
			abort(e.getMessage());
			return null;
		}
	}

	String getPreferencesPathImpl() throws Exception {
		String home = System.getProperty("preferences.directory");
		if (home == null) {
			home = System.getProperty("user.home");
		} else if (home.trim().isEmpty()) {
			throw new IllegalArgumentException(EMPTY_PATH_SUPPLIED_FOR_PREFERENCES_DIRECTORY);
		}
		return home;
	}
	
	private boolean checkParentPathIsAccessible(File file) {
		try {
			checkParentPathIsAccessibleImpl(file);
			return true;
		} catch (Exception e) {
			isOkToContinue(e.getMessage());
			return false;
		}
	}

	void checkParentPathIsAccessibleImpl(File file) throws Exception {
		File parentDirectory = file.getParentFile();
		while (parentDirectory != null) {
			if (parentDirectory.exists()) {
				if (!parentDirectory.canRead()) {
					throw new IllegalArgumentException(String.format(PREFERENCES_PARENT_DIRECTORY_S_NOT_READABLE, parentDirectory.getAbsolutePath()));
				}
				if (!parentDirectory.canWrite()) {
					throw new IllegalArgumentException(String.format(PREFERENCES_PARENT_DIRECTORY_S_NOT_WRITEABLE, parentDirectory.getAbsolutePath()));
				}
				return;
			}
			parentDirectory = parentDirectory.getParentFile();
		}
	}

	private boolean checkFileIsAccessible(File file) {
		try {
			checkFileIsAccessibleImpl(file);
			return true;
		} catch (Exception e) {
			isOkToContinue(e.getMessage());
			return false;
		}
	}
	
	void checkFileIsAccessibleImpl(File file) throws Exception {
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(PREFERENCES_DIRECTORY_IS_A_FILE);
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException(PREFERENCES_DIRECTORY_CANNOT_BE_READ);
		}
		if (!file.canWrite()) {
			throw new IllegalArgumentException(PREFERENCES_DIRECTORY_CANNOT_BE_WRITTEN);
		}
	}

	private void abort(String message) {
		JOptionPane.showMessageDialog(null, message, "Cannot start program", JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	
	private void isOkToContinue(String message) {
		if (JOptionPane.showConfirmDialog(null, message, "Do you want to continue?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
			System.exit(0);
		}
	}
	
	String buildRootPath(String home, String packageName) {
		String result = new StringBuilder(home).append(File.separator).append(".").append(packageName).toString();
		new File(result).mkdirs();
		return result;
	}

	String getUserHome() {
		return System.getProperty("user.home");
	}
}
