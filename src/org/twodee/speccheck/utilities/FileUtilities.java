package org.twodee.speccheck.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtilities {
  /**
   * Get the contents of a file as a byte array.
   * 
   * @param path
   * File to get as bytes.
   * 
   * @return Contents as an array of bytes.
   * 
   * @throws IOException
   * If file cannot be opened for reading.
   */
  public static byte[] getFileBytes(String path) throws IOException {
    RandomAccessFile f = new RandomAccessFile(new File(path), "r");
    byte[] bytes = new byte[(int) f.length()];
    f.read(bytes);
    f.close();
    return bytes;
  }

  public static boolean deleteHierarchy(File path) throws FileNotFoundException {
    if (!path.exists())
      throw new FileNotFoundException(path.getAbsolutePath());
    
    boolean isOkay = true;
    
    // Delete children.
    if (path.isDirectory()) {
      for (File f : path.listFiles()) {
        isOkay = isOkay && deleteHierarchy(f);
      }
    }
    
    // Delete root.
    return isOkay && path.delete();
  }

  /**
   * Get the contents of file at the specified path as text.
   * 
   * @param path
   * Location of file.
   * 
   * @return File contents as text.
   * 
   * @throws IOException
   * If file cannot be read.
   */
  public static String getFileText(String path) throws IOException {
    return getFileText(new File(path));
  }

  /**
   * Get the contents of the file as text.
   * 
   * @param file
   * File whose contents are to be retrieved.
   * 
   * @return File contents as text.
   * 
   * @throws IOException
   * If file cannot be read.
   */
  public static String getFileText(File file) throws IOException {
    Scanner in = new Scanner(file);
    in.useDelimiter("\\Z");
    String contents = in.next();
    in.close();
    return contents;
  }

  public static ArrayList<String> getZipEntries(ZipInputStream zis) throws IOException {
    ArrayList<String> entries = new ArrayList<String>();
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
      entries.add(entry.getName());
    }
    return entries;
  }

  private static final int TEMP_DIR_ATTEMPTS = 10000;

  public static File createTemporaryDirectory() {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = System.currentTimeMillis() + "-";

    for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
      File tempDir = new File(baseDir, baseName + counter);
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }
    throw new IllegalStateException("Failed to create directory within "
                                    + TEMP_DIR_ATTEMPTS + " attempts (tried "
                                    + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
  }
}
