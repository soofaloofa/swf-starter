package com.sookocheff.swf.processor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;

/**
 * This is the process which runs all SWF workflow deciders
 */
public class WorkflowHost {

  public static void main(String[] args) throws Exception {
    ConfigHelper configHelper = ConfigHelper.createConfig();
    AmazonSimpleWorkflow swfService = configHelper.createSWFClient();
    String domain = configHelper.getDomain();

    final WorkflowWorker worker = new WorkflowWorker(swfService, domain, configHelper.getDecisionTaskList());
    worker.addWorkflowImplementationType(ZipFileActivities.class);
    worker.start();
    System.out.println("Workflow Host Service Started...");

    // Wait to close any running workers
    Runtime.getRuntime().addShutdownHook(new Thread() {

      public void run() {
        try {
          worker.shutdownAndAwaitTermination(1, TimeUnit.MINUTES);
          System.out.println("Workflow Host Service Terminated...");
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