package com.sookocheff.swf.processor;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;

/**
 * Starts a new workflow execution.
 */
public class WorkflowExecutionStarter {

  public static void main(String[] args) throws Exception {
    // Load configuration
    Config config = Config.createConfig();

    // Start Workflow client
    AmazonSimpleWorkflow swfClient = config.createSWFClient();
    FileProcessingWorkflowClientExternalFactory clientFactory =
        new FileProcessingWorkflowClientExternalFactoryImpl(swfClient, config.getSwfDomain());
    FileProcessingWorkflowClientExternal workflow = clientFactory.getClient();
    workflow.processFile(
        config.getWorkflowInputBucketName(),
        config.getWorkflowInputFileName(),
        config.getWorkflowOutputBucketName(),
        config.getWorkflowOutputFileName());

    // Start workflow execution
    WorkflowExecution workflowExecution = workflow.getWorkflowExecution();
    System.out.println("Started periodic workflow with workflowId=\"" + workflowExecution.getWorkflowId()
        + "\" and runId=\"" + workflowExecution.getRunId() + "\"");

    System.exit(0);
  }
}