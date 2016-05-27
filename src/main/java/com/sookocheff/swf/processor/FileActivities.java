package com.sookocheff.swf.processor;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 * Flow framework contract for file processing activities.
 */
@Activities(version="1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 60, defaultTaskStartToCloseTimeoutSeconds = 60)
public interface FileActivities {

  /**
   * Process the file at inputFileName and output the result to outputFileName.
   * @param inputFileName the name of the file to process
   * @param outputFileName the name of the processed file
   * @throws Exception
   */
  void processFile(String inputFileName, String outputFileName) throws Exception;

}
