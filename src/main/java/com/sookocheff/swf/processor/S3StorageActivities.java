package com.sookocheff.swf.processor;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContext;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;

/**
 * This is an S3 Store implementation which provides Activities to
 * download/upload files from S3
 */
public class S3StorageActivities implements StorageActivities {

  private static final int HEARTBEAT_INTERVAL = 60000;

  private final ActivityExecutionContextProvider contextProvider = new ActivityExecutionContextProviderImpl();

  private final AmazonS3 s3Client;

  private final String localDirectory;

  private final String hostSpecificTaskList;

  public S3StorageActivities(AmazonS3 s3Client, String localDirectory, String taskList) {
    this.s3Client = s3Client;
    this.localDirectory = localDirectory;
    this.hostSpecificTaskList = taskList;
  }

  @Override
  public void upload(String bucketName, String localName, String remoteName) {
    System.out.println("upload begin remoteName=" + remoteName + ", localName=" + localName);
    File f = new File(localName);
    s3Client.putObject(bucketName, remoteName, f);
    System.out.println("upload done");
  }

  @Override
  public String download(String bucketName, String remoteName, String localName) throws Exception {
    System.out.println("download begin remoteName=" + remoteName + ", localName=" + localName);
    FileOutputStream f = new FileOutputStream(localName);
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
    System.out.println("download done");
    return hostSpecificTaskList;
  }

  @Override
  public void deleteLocalFile(String fileName) {
    System.out.println("deleteLocalFile begin fileName=" + fileName);
    File f = new File(fileName);
    f.delete();
    System.out.println("deleteLocal done");
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

}
