package com.sookocheff.swf.processor;

import java.io.IOException;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.GetState;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

/**
 * Flow framework Contract for the file processing workflow.
 */
@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 300, defaultTaskStartToCloseTimeoutSeconds = 10)
public interface FileProcessingWorkflow {

  /**
   * Process the file at inputBucketName.inputFileName.
   * Place the result at outputBucketName.outputFileName.
   *
   * @param inputBucketName input bucket to process from
   * @param inputFilename input file to process from
   * @param outputBucketName output bucket to put result to
   * @param outputFilename output file to put result to
   * @throws IOException
   */
  @Execute(name = "ProcessFile", version = "1.0")
  void processFile(String inputBucketName, String inputFilename, String outputBucketName, String outputFilename) throws IOException;

  /**
   * Get the current state of the workflow. This is reported to the SWF console and
   * through SWF APIs.
   *
   * When the decider is done processing a decision task, it fetches the latest state
   * using the @GetState annotation.
   *
   * @return current state of the workflow
   */
  @GetState
  String getState();

}