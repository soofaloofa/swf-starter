package com.sookocheff.swf.processor;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContext;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is an S3 storage implementation which provides Activities to
 * download/upload files from S3.
 */
public class S3StorageActivities implements StorageActivities {

  private static final Logger LOG = LoggerFactory.getLogger(WorkflowHost.class);

  private static final int HEARTBEAT_INTERVAL = 60000;

  private final ActivityExecutionContextProvider contextProvider = new ActivityExecutionContextProviderImpl();
  private final AmazonS3 s3Client;
  private final String localTaskList;
  private final String localFolder;

  /**
   * Create a new S3StorageActivities object.
   *
   * @param s3Client s3 client to use
   * @param localTaskList SWF task list to add activities for this host to process
   * @param localFolder folder to store temporary work in
   */
  public S3StorageActivities(AmazonS3 s3Client, String localTaskList, String localFolder) {
    this.s3Client = s3Client;
    this.localTaskList = localTaskList;
    this.localFolder = localFolder;
  }

  /**
   * Upload a file to storage.
   *
   * @param bucketName Name of the S3 bucket to upload to
   * @param localName  Local name of the file to upload to S3
   * @param remoteName Name of the file to use when uploaded to S3
   */
  @Override
  public void upload(String bucketName, String localName, String remoteName) {
    String fileNameFullPath = getFullPath(localName);
    LOG.info("upload begin remoteName=" + remoteName + ", localName=" + fileNameFullPath);
    s3Client.putObject(bucketName, remoteName, new File(fileNameFullPath));
    LOG.info("upload done");
  }

  /**
   * Download a file from storage.
   *
   * @param bucketName Name of the S3 bucket to download from
   * @param remoteName Name of the file to download from S3
   * @param localName  Local name of the file to download to
   */
  @Override
  public String download(String bucketName, String remoteName, String localName) throws Exception {
    String fileNameFullPath = getFullPath(localName);
    LOG.info("download begin remoteName=" + remoteName + ", localName=" + fileNameFullPath);
    FileOutputStream f = new FileOutputStream(fileNameFullPath);
    try {
      S3Object obj = s3Client.getObject(bucketName, remoteName);
      InputStream inputStream = obj.getObjectContent();
      long totalSize = obj.getObjectMetadata().getContentLength();

      try {
        long totalRead = 0;
        int read = 0;
        byte[] bytes = new byte[1024];
        long lastHeartbeatTime = System.currentTimeMillis();
        while ((read = inputStream.read(bytes)) != -1) {
          totalRead += read;
          f.write(bytes, 0, read);
          int progress = (int) (totalRead / totalSize * 100);
          lastHeartbeatTime = heartbeat(lastHeartbeatTime, progress);
        }
      } finally {
        inputStream.close();
      }
    } finally {
      f.close();
    }

    // Return hostname file was downloaded to
    LOG.info("download done");
    return localTaskList;
  }

  /**
   * Delete temporary local files.
   *
   * @param fileName Name of file to delete from temporary folder
   */
  @Override
  public void deleteLocalFile(String fileName) {
    String fileNameFullPath = getFullPath(fileName);
    LOG.info("deleteLocalFile begin fileName=" + fileNameFullPath);
    File f = new File(fileNameFullPath);
    f.delete();
    LOG.info("deleteLocal done");
  }

  /**
   * Heartbeat every 5 minutes. It is not a good idea to heartbeat too
   * frequently as each noteActivityProgress event ends up eating history
   * events count.
   *
   * @return time of the last heartbeat
   */
  private long heartbeat(long lastHeartbeatTime, int progress) {
    if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_INTERVAL) {
      ActivityExecutionContext context = contextProvider.getActivityExecutionContext();
      context.recordActivityHeartbeat(Integer.toString((progress)));
      lastHeartbeatTime = System.currentTimeMillis();
    }
    return lastHeartbeatTime;
  }

  /**
   * Return the full path to file by appending the temporary folder name
   * @param path file to get the full path of
   * @return full path of the file, including temporary folder
   */
  private String getFullPath(String path) {
    File f = new File(localFolder + path);
    return f.getAbsolutePath();
  }
}
