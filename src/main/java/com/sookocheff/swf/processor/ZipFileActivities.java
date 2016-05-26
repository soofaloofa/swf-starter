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
 * This implementation of FileProcessingActivities zips the file
 */
public class ZipFileActivities implements FileActivities {

  private static final Logger LOG = LoggerFactory.getLogger(ZipFileActivities.class);

  private final String localDirectory;

  public ZipFileActivities(String localDirectory) {
    this.localDirectory = localDirectory;
  }

  /**
   * This is the Activity implementation that zips the file
   *
   * @param fileName    Name of file to zip
   * @param zipFileName Filename after zip
   */
  @Override
  public void processFile(String fileName, String zipFileName) throws Exception {
    String fileNameFullPath = localDirectory + fileName;
    String zipFileNameFullPath = localDirectory + zipFileName;

    LOG.info("processFile activity begin.  fileName= " + fileNameFullPath + ", zipFileName= " + zipFileNameFullPath);
    final int BUFFER = 1024;
    BufferedInputStream origin = null;
    ZipOutputStream out = null;

    try {
      FileOutputStream dest = new FileOutputStream(zipFileNameFullPath);
      out = new ZipOutputStream(new BufferedOutputStream(dest));
      byte data[] = new byte[BUFFER];

      FileInputStream fi = new FileInputStream(fileNameFullPath);
      origin = new BufferedInputStream(fi, BUFFER);
      ZipEntry entry = new ZipEntry(fileName);
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
