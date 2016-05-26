package com.sookocheff.swf.processor;

import java.io.IOException;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.GetState;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

/**
 * Contract for file processing workflow.
 */
@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 300, defaultTaskStartToCloseTimeoutSeconds = 10)
public interface FileProcessingWorkflow {

    @Execute(name = "ProcessFile", version = "1.0")
    void processFile(String inputBucketName, String inputFilename, String outputBucketName, String outputFilename) throws IOException;

    @GetState
    String getState();

}