package com.sbf.MediaManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ListFiles {

	// Uses Files.walk method
	public static List<File> listAllFiles(String path) throws IOException {
		Stream<Path> paths = Files.walk(Paths.get(path));
		List<File> filesFound = new ArrayList<File>();
		paths.forEach(filePath -> {
			filesFound.add(filePath.toFile());
		});
		paths.close();
		return filesFound;
	}

}
