package com.sookocheff.swf.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An implementation of FileActivities that processes a file by zipping it.
 */
public class ZipFileActivities implements FileActivities {

  private static final Logger LOG = LoggerFactory.getLogger(ZipFileActivities.class);

  private final String localDirectory;

  /**
   * Create a new instance of ZipFileActivities.
   *
   * @param localDirectory the directory to store results.
   */
  public ZipFileActivities(String localDirectory) {
    this.localDirectory = localDirectory;
  }

  /**
   * Zips the file at inputFileName and output the result to outputFileName.
   * @param inputFileName the name of the file to process
   * @param outputFileName the name of the processed file
   * @throws Exception
   */
  @Override
  public void processFile(String inputFileName, String outputFileName) throws Exception {
    String inputFileNameFullPath = localDirectory + inputFileName;
    String outputFileNameFullPath = localDirectory + outputFileName;

    LOG.info("processFile activity begin.  fileName= " + inputFileNameFullPath + ", zipFileName= " + outputFileNameFullPath);
    final int BUFFER = 1024;
    BufferedInputStream origin = null;
    ZipOutputStream out = null;

    try {
      FileOutputStream dest = new FileOutputStream(outputFileNameFullPath);
      out = new ZipOutputStream(new BufferedOutputStream(dest));
      byte data[] = new byte[BUFFER];

      FileInputStream fi = new FileInputStream(inputFileNameFullPath);
      origin = new BufferedInputStream(fi, BUFFER);
      ZipEntry entry = new ZipEntry(inputFileName);
      out.putNextEntry(entry);
      int count;
      while ((count = origin.read(data, 0, BUFFER)) != -1) {
        out.write(data, 0, count);
      }
    } finally {
      if (origin != null)
        origin.close();
      if (out != null)
        out.close();
    }

    LOG.info("zipFileActivity done.");
  }
}
