package com.sookocheff.swf.processor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;

public class ActivityHost {

    public static void main(String[] args) throws Exception {
        ConfigHelper configHelper = ConfigHelper.createConfig();
        AmazonSimpleWorkflow swfService = configHelper.createSWFClient();
        AmazonS3 s3Client = configHelper.createS3Client();
        String domain = configHelper.getDomain();
        String localFolder = configHelper.getLocalFolder();
        String hostName = configHelper.getHostName();
        String taskList = configHelper.getActivitiesTaskList();

        // Start worker to poll the common task list
        final ActivityWorker workerForCommonTaskList = new ActivityWorker(swfService, domain, taskList);
        S3StorageActivities s3Activities = new S3StorageActivities(s3Client, localFolder, hostName);
        workerForCommonTaskList.addActivitiesImplementation(s3Activities);
        workerForCommonTaskList.start();
        System.out.println("Host Service Started for Task List: " + taskList);

        // Start worker to poll the host specific task list, executes tasks triggered for this particular host
        final ActivityWorker workerForHostSpecificTaskList = new ActivityWorker(swfService, domain, hostName);
        // add s3 implementation
        workerForHostSpecificTaskList.addActivitiesImplementation(s3Activities);

        // add file zip implementation
        ZipFileActivities zipFileActivities = new ZipFileActivities(localFolder);
        workerForHostSpecificTaskList.addActivitiesImplementation(zipFileActivities);
        workerForHostSpecificTaskList.start();
        System.out.println("Worker Started for Activity Task List: " + hostName);

        // Wait to close any running workers
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    workerForCommonTaskList.shutdown();
                    workerForHostSpecificTaskList.shutdown();
                    workerForCommonTaskList.awaitTermination(1, TimeUnit.MINUTES);
                    workerForHostSpecificTaskList.awaitTermination(1, TimeUnit.MINUTES);
                    System.out.println("Activity Workers Exited.");
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Press any key to terminate service.");

        try {
            System.in.read();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }

}
