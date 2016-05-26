package com.sookocheff.swf.processor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the process which runs all SWF workflow deciders
 */
public class WorkflowHost {

  private static final Logger LOG = LoggerFactory.getLogger(WorkflowHost.class);

  public static void main(String[] args) throws Exception {
    Config config = Config.createConfig();
    AmazonSimpleWorkflow swfService = config.createSWFClient();
    String domain = config.getSwfDomain();

    final WorkflowWorker worker = new WorkflowWorker(swfService, domain, config.getWorkflowWorkerTaskList());
    worker.addWorkflowImplementationType(ZipS3FileProcessingWorkflow.class);
    worker.start();
    LOG.info("Workflow Host Service Started...");

    // Wait to close any running workers
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