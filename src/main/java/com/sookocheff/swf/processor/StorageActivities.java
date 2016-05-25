package com.sookocheff.swf.processor;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

/**
 * Contract for S3 activities.
 */
@Activities(version = "1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 120)
public interface StorageActivities {

  /**
   * @param bucketName Name of the file to use on S3 bucket after upload
   * @param localName  Name of the file to upload from temporary directory
   * @param remoteName Machine name which has the file that needs to be uploaded
   * @return
   */
  @ExponentialRetry(initialRetryIntervalSeconds = 10, maximumAttempts = 10)
  void upload(String bucketName, String localName, String remoteName);

  /**
   * @param bucketName Name of the file to download from S3 bucket
   * @param remoteName Name of the remote machine to download from
   * @param localName  Name of the machine used locally after download
   */
  @ExponentialRetry(initialRetryIntervalSeconds = 10, maximumAttempts = 10)
  String download(String bucketName, String remoteName, String localName) throws Exception;

  /**
   * @param fileName Name of file to delete from temporary folder
   */
  @ExponentialRetry(initialRetryIntervalSeconds = 10)
  void deleteLocalFile(String fileName);

}
