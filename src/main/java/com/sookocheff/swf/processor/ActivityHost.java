package com.sookocheff.swf.processor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ActivityHost processes work defined by @Activities annotations.
 *
 * For this application, work is divided into multiple task lists that are managed by SWF.
 * A common task list is for work that is processable by any worker.
 * Each worker also has a private task list that is unique to that host. By adding
 * work to this list, the worker will process it. This is used for workers to process
 * files that have been downloaded locally.
 */
public class ActivityHost {

  private static final Logger LOG = LoggerFactory.getLogger(ActivityHost.class);

  public static void main(String[] args) throws Exception {
    // Load config
    Config config = Config.createConfig();

    // Create clients
    AmazonSimpleWorkflow swfClient = config.createSWFClient();
    AmazonS3 s3Client = config.createS3Client();

    String hostName = Config.getHostName();
    String domain = config.getSwfDomain();
    String localFolder = config.getActivityWorkerLocalFolder();
    String taskList = config.getActivityWorkerTaskList();

    // Start worker to poll the common worker task list
    final ActivityWorker workerForCommonTaskList = new ActivityWorker(swfClient, domain, taskList);
    S3StorageActivities s3Activities = new S3StorageActivities(s3Client, hostName, localFolder);
    workerForCommonTaskList.addActivitiesImplementation(s3Activities);
    workerForCommonTaskList.start();
    LOG.info("Host Service Started for Task List: " + taskList);

    // Start worker to poll the host specific task list
    // Executes tasks specified for this particular host
    final ActivityWorker workerForHostSpecificTaskList = new ActivityWorker(swfClient, domain, hostName);
    // add s3 implementation
    workerForHostSpecificTaskList.addActivitiesImplementation(s3Activities);

    // add file zip implementation
    ZipFileActivities zipFileActivities = new ZipFileActivities(localFolder);
    workerForHostSpecificTaskList.addActivitiesImplementation(zipFileActivities);
    workerForHostSpecificTaskList.start();
    LOG.info("Worker Started for Activity Task List: " + hostName);

    // Close any running worker threads on VM shutdown
    Runtime.getRuntime().addShutdownHook(new Thread() {

      public void run() {
        try {
          workerForCommonTaskList.shutdown();
          workerForHostSpecificTaskList.shutdown();
          workerForCommonTaskList.awaitTermination(1, TimeUnit.MINUTES);
          workerForHostSpecificTaskList.awaitTermination(1, TimeUnit.MINUTES);
          LOG.info("Activity Workers Exited.");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    System.out.println("Press any key to terminate activity workers.");

    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.exit(0);

  }

}
