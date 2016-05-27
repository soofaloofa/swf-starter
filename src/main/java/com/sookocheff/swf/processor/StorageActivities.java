package com.sookocheff.swf.processor;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

/**
 * Flow framework contract for storage activities.
 */
@Activities(version = "1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 120)
public interface StorageActivities {

  /**
   * Upload a file to storage.
   *
   * @param bucketName Name of the S3 bucket to upload to
   * @param localName  Local name of the file to upload to S3
   * @param remoteName Name of the file to use when uploaded to S3
   */
  @ExponentialRetry(initialRetryIntervalSeconds = 10, maximumAttempts = 10)
  void upload(String bucketName, String localName, String remoteName);

  /**
   * Download a file from storage.
   *
   * @param bucketName Name of the S3 bucket to download from
   * @param remoteName Name of the file to download from S3
   * @param localName  Local name of the file to download to
   */
  @ExponentialRetry(initialRetryIntervalSeconds = 10, maximumAttempts = 10)
  String download(String bucketName, String remoteName, String localName) throws Exception;

  /**
   * Delete temporary local files.
   *
   * @param fileName Name of file to delete from temporary folder
   */
  @ExponentialRetry(initialRetryIntervalSeconds = 10)
  void deleteLocalFile(String fileName);

}
