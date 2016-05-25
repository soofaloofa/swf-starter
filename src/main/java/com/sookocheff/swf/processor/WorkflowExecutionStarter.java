package com.sookocheff.swf.processor;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;

/**
 * Starts a new workflow execution.
 */
public class WorkflowExecutionStarter {

  private static AmazonSimpleWorkflow swfService;
  private static String domain;

  public static void main(String[] args) throws Exception {

    // Load configuration
    ConfigHelper configHelper = ConfigHelper.createConfig();

    // Create the client for Simple Workflow Service
    swfService = configHelper.createSWFClient();
    domain = configHelper.getDomain();

    // Start Workflow instance
    String sourceBucketName = configHelper.getS3SourceBucketName();
    String sourceFilename = configHelper.getS3SourceFileName();
    String targetBucketName = configHelper.getS3TargetBucketName();
    String targetFilename = configHelper.getS3TargetFileName();

    FileProcessingWorkflowClientExternalFactory clientFactory = new FileProcessingWorkflowClientExternalFactoryImpl(swfService, domain);
    FileProcessingWorkflowClientExternal workflow = clientFactory.getClient();
    workflow.processFile(sourceBucketName, sourceFilename, targetBucketName, targetFilename);

    // WorkflowExecution is available after workflow creation
    WorkflowExecution workflowExecution = workflow.getWorkflowExecution();
    System.out.println("Started periodic workflow with workflowId=\"" + workflowExecution.getWorkflowId()
        + "\" and runId=\"" + workflowExecution.getRunId() + "\"");

    System.exit(0);
  }
}