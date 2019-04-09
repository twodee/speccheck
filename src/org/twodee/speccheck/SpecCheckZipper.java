package org.twodee.speccheck;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class SpecCheckZipper {
  public static void zip(boolean isPerfect,
                         String tag,
                         String... requiredFiles) {
    ArrayList<File> filesToZip = getFilesToZip(requiredFiles);

    // Create archive.
    boolean ok = false;
    try {
      String m = isPerfect ? "All tests pass." : "Not all tests pass.";
      int opt = JOptionPane.showConfirmDialog(null, m + " Create ZIP to submit?", "Create ZIP", JOptionPane.YES_NO_OPTION);
      if (opt == JOptionPane.YES_OPTION) {
        System.out.println("Choose a *directory* in which to store " + tag + ".zip.");
        ok = zip(tag, filesToZip);
        JOptionPane.getRootFrame().dispose();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Send them on.
    if (ok) {
      JOptionPane.showMessageDialog(null, "Open the ZIP file you created. Verify that it has your files in it.");
      System.out.println();
      System.out.println("Drop your archive named " + tag + ".zip into W:\\c s\\johnch\\cs245\\students\\<YOUR-USERNAME>.");
    } else {
      System.out.println("Archive not created!");
    }
  }

  private static ArrayList<File> getFilesToZip(String[] requiredFiles) {
    ArrayList<File> filesToZip = new ArrayList<File>();

    for (String path : requiredFiles) {
      File f = new File(path);
      if (f.isDirectory()) {
        addDirectory(f, filesToZip);
      } else {
        filesToZip.add(f);
      }
    }

    return filesToZip;
  }

  private static void addDirectory(File dir,
                                   ArrayList<File> files) {
    if (dir.exists()) {
      File[] contents = dir.listFiles();
      for (File file : contents) {
        if (file.isDirectory() && !file.getName().equals("speccheck")) {
          addDirectory(file, files);
        } else if (!files.contains(file) && !file.getName().startsWith("speccheck") && !file.getName().endsWith(".jar")) {
          files.add(file);
        }
      }
    } else {
      System.err.println("Directory " + dir + " doesn't exist.");
    }
  }

  /**
   * A help method for creating the archive tag.zip from the specified source
   * files.
   * 
   * @param tag
   * Root name of zip file.
   * 
   * @param sources
   * Paths to source files.
   * 
   * @return True if the archive was successfully created. False if the user
   * canceled the archiving because of a missing source file or a canceled save
   * dialog.
   * 
   * @throws IOException
   */
  private static boolean zip(String tag,
                             ArrayList<File> sources) throws IOException {
    JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = chooser.showOpenDialog(null);
    if (result != JFileChooser.APPROVE_OPTION) {
      return false;
    }
    String zipName = chooser.getSelectedFile().getAbsolutePath() + File.separatorChar + tag + ".zip";
    System.out.println("Archive to be saved at " + zipName + ".");

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(zipName);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Couldn't create ZIP file! Does the directory " + chooser.getSelectedFile().getAbsolutePath() + " actually exist?", e);
    }

    BufferedOutputStream bos = new BufferedOutputStream(fos);
    ZipOutputStream zos = new ZipOutputStream(bos);
    byte[] data = new byte[2048];
    boolean ok;

    System.out.println("Files in archive:");
    for (File source : sources) {
      ok = addSourceFile(source, zos, data);
      if (!ok) {
        return false;
      }
    }
    System.out.println();

    try {
      zos.close();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't close ZIP file! Was at least one source code file present?", e);
    }

    return true;
  }

  /**
   * A helper method from dumping a source file out an already-opened zip file.
   * 
   * @param source
   * Path to source file.
   * 
   * @param zos
   * Existing zip output stream.
   * 
   * @param data
   * Existing buffer for writing file contents.
   * 
   * @return True if archiving was not canceled due to missing file.
   * 
   * @throws IOException
   */
  private static boolean addSourceFile(File source,
                                       ZipOutputStream zos,
                                       byte[] data) throws IOException {
    BufferedInputStream bis;
    try {
      bis = new BufferedInputStream(new FileInputStream(source));
      ZipEntry entry = new ZipEntry(source.getPath().replace('\\', '/'));
      zos.putNextEntry(entry);
      int bytesRead;
      while ((bytesRead = bis.read(data, 0, data.length)) != -1) {
        zos.write(data, 0, bytesRead);
      }
      System.out.println(" - " + source);
    } catch (FileNotFoundException e) {
      int ok = JOptionPane.showConfirmDialog(null, "File " + source + " couldn't be found. Create archive anyway?", "Missing File", JOptionPane.YES_NO_OPTION);
      if (ok != JOptionPane.YES_OPTION) {
        return false;
      }
    } catch (IOException e) {
      System.out.println("Couldn't write or read file! Did you yank out your hard drive?");
      e.printStackTrace();
      throw e;
    }

    return true;
  }
}