package org.twodee.speccheck.utilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class FileUtilities {
   /**
    * Get the contents of a file as a byte array.
    * 
    * @param path
    * File to get as bytes.
    * 
    * @return
    * Contents as an array of bytes.
    * 
    * @throws IOException
    * If file cannot be opened for reading.
    */
   public static byte[] getFileBytes(String path) throws IOException {
      RandomAccessFile f = new RandomAccessFile(new File(path), "r");
      byte[] bytes = new byte[(int) f.length()];
      f.read(bytes);
      return bytes;
   }
   
   /**
    * Get the contents of file at the specified path as text.
    * 
    * @param path
    * Location of file.
    * 
    * @return
    * File contents as text.
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
    * @return
    * File contents as text.
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
}
