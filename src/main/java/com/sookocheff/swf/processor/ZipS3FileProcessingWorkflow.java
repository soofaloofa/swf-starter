package com.sookocheff.swf.processor;

import java.io.File;
import java.io.IOException;

import com.amazonaws.services.simpleworkflow.flow.ActivitySchedulingOptions;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.WorkflowContext;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.Settable;
import com.amazonaws.services.simpleworkflow.flow.core.TryCatchFinally;

/**
 * Downloads a file from S3, zips it uploads it back to S3.
 */
public class ZipS3FileProcessingWorkflow implements FileProcessingWorkflow {

  /**
   * Storage client is auto-generated using AspectJ.
   */
  private final StorageActivitiesClient storageClient;

  /**
   * File client is auto-generated using AspectJ.
   */
  private final FileActivitiesClient fileClient;


  private final WorkflowContext workflowContext;

  private String state = "Started";


  public ZipS3FileProcessingWorkflow() {
    // Create instances of auto-generated activity clients
    storageClient = new StorageActivitiesClientImpl();
    fileClient = new FileActivitiesClientImpl();
    workflowContext = (new DecisionContextProviderImpl()).getDecisionContext().getWorkflowContext();
  }

  @Override
  public void processFile(final String sourceBucketName, final String sourceFilename, final String targetBucketName,
                          final String targetFilename) throws IOException {
    // Settable to store the worker specific task list returned by the activity
    final Settable<String> taskList = new Settable<String>();

    // Use runId as a way to ensure that downloaded files do not get name collisions
    String workflowRunId = workflowContext.getWorkflowExecution().getRunId();
    File localSource = new File(sourceFilename);
    final String localSourceFilename = workflowRunId + "_" + localSource.getName();
    File localTarget = new File(targetFilename);
    final String localTargetFilename = workflowRunId + "_" + localTarget.getName();
    new TryCatchFinally() {

      @Override
      protected void doTry() throws Throwable {
        Promise<String> activityWorkerTaskList = storageClient.download(sourceBucketName, sourceFilename, localSourceFilename);
        // chaining is a way for one promise to get assigned the value of another
        taskList.chain(activityWorkerTaskList);
        // Call processFile activity to zip the file
        Promise<Void> fileProcessed = processFileOnHost(localSourceFilename, localTargetFilename, activityWorkerTaskList);
        // Call upload activity to upload zipped file
        upload(targetBucketName, targetFilename, localTargetFilename, taskList, fileProcessed);
      }

      @Override
      protected void doCatch(Throwable e) throws Throwable {
        state = "Failed: " + e.getMessage();
        throw e;
      }

      @Override
      protected void doFinally() throws Throwable {
        if (taskList.isReady()) { // File was downloaded

          // Set option to schedule activity in worker specific task list
          ActivitySchedulingOptions options = new ActivitySchedulingOptions().withTaskList(taskList.get());

          // Call deleteLocalFile activity using the host specific task list
          storageClient.deleteLocalFile(localSourceFilename, options);
          storageClient.deleteLocalFile(localTargetFilename, options);
        }
        if (!state.startsWith("Failed:")) {
          state = "Completed";
        }
      }

    };
  }

  @Asynchronous
  private Promise<Void> processFileOnHost(String fileToProcess, String fileToUpload, Promise<String> taskList) {
    state = "Downloaded to " + taskList.get();
    // Call the activity to process the file using worker specific task list
    ActivitySchedulingOptions options = new ActivitySchedulingOptions().withTaskList(taskList.get());
    return fileClient.processFile(fileToProcess, fileToUpload, options);
  }

  @Asynchronous
  private void upload(final String targetBucketName, final String targetFilename, final String localTargetFilename,
                      Promise<String> taskList, Promise<Void> fileProcessed) {
    state = "Processed at " + taskList.get();
    ActivitySchedulingOptions options = new ActivitySchedulingOptions().withTaskList(taskList.get());
    storageClient.upload(targetBucketName, localTargetFilename, targetFilename, options);
  }

  @Override
  public String getState() {
    return state;
  }

}