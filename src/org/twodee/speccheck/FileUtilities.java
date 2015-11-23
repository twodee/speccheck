package org.twodee.speccheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
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
  public static String slurp(String path) throws IOException {
    return slurp(new File(path));
  }
  
  public static List<String> getFileLines(File file) throws IOException {
    ArrayList<String> lines = new ArrayList<String>();
    Scanner in = new Scanner(file);
    while (in.hasNextLine()) {
      lines.add(in.nextLine());
    }
    in.close();
    return lines;
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
  public static String slurp(File file) throws IOException {
    return slurp(new FileInputStream(file));
  }
  
  public static String slurp(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    byte[] buffer = new byte[1024];
    int nRead = 0;

    while ((nRead = is.read(buffer)) >= 0) {
      sb.append(new String(buffer, 0, nRead));
    }

    is.close();
    return sb.toString();
  }
  
  public static File resourceToTemporaryFile(Class<?> clazz, String path, String prefix, String suffix) throws IOException {
    File tmp = File.createTempFile(prefix, suffix);
    tmp.deleteOnExit();
    
    InputStream is = clazz.getResourceAsStream(path);
    PrintWriter out = new PrintWriter(tmp);
    Scanner in = new Scanner(is);
    while (in.hasNextLine()) {
      String line = in.nextLine();
      out.println(line);
    }
    in.close();
    out.close();
    
    return tmp;
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
