package com.sookocheff.swf.processor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The WorkflowHost processes work defined by @Workflow and @Execute annotations.
 *
 * WorkflowWorker instances process decisions given by the SWF service.
 */
public class WorkflowHost {

  private static final Logger LOG = LoggerFactory.getLogger(WorkflowHost.class);

  public static void main(String[] args) throws Exception {
    // Load config
    Config config = Config.createConfig();

    // Create a workflow worker
    WorkflowWorker worker =
        new WorkflowWorker(config.createSWFClient(), config.getSwfDomain(), config.getWorkflowWorkerTaskList());
    worker.addWorkflowImplementationType(ZipS3FileProcessingWorkflow.class);
    worker.start();
    LOG.info("Workflow Host Service Started...");

    // Close any running worker threads on VM shutdown
    Runtime.getRuntime().addShutdownHook(new Thread() {

      public void run() {
        try {
          worker.shutdownAndAwaitTermination(1, TimeUnit.MINUTES);
          LOG.info("Workflow Host Service Terminated...");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    System.out.println("Please press any key to terminate service.");

    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.exit(0);

  }

}