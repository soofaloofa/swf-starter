package com.sookocheff.swf.processor;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a new workflow execution. Execution is managed by SWF and will send activities
 * and decisions to the ActivityHost and WorkflowHost.
 *
 * In a deployed application a workflow may be started after receiving a
 * message from SNS (for example).
 */
public class WorkflowExecutionStarter {

  private static final Logger LOG = LoggerFactory.getLogger(WorkflowExecutionStarter.class);

  public static void main(String[] args) throws Exception {
    // Load configuration
    Config config = Config.createConfig();

    // FileProcessingWorkflowClientExternalFactory is auto-generated through flow framework
    FileProcessingWorkflowClientExternalFactory clientFactory =
        new FileProcessingWorkflowClientExternalFactoryImpl(config.createSWFClient(), config.getSwfDomain());
    FileProcessingWorkflowClientExternal workflow = clientFactory.getClient();

    // Start workflow execution
    workflow.processFile(
        config.getWorkflowInputBucketName(),
        config.getWorkflowInputFileName(),
        config.getWorkflowOutputBucketName(),
        config.getWorkflowOutputFileName());

    WorkflowExecution workflowExecution = workflow.getWorkflowExecution();
    LOG.info("Started periodic workflow with workflowId=\"" + workflowExecution.getWorkflowId()
        + "\" and runId=\"" + workflowExecution.getRunId() + "\"");

    System.exit(0);
  }
}