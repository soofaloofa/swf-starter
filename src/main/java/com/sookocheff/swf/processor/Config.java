package com.sookocheff.swf.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;


/**
 * Helper class for storing application configuration and creating AWS clients.
 *
 * You can change the configuration of this application by editing the
 * processor.properties file.
 */
public class Config {

  // Default location of the properties file to load configuration values from.
  public static final String DEFAULT_PROPERTIES_FILE = "processor.properties";

  // Keys in the property file containing your configuration
  public static final String SWF_SERVICE_URL = "service.url";
  public static final String SWF_SERVICE_DOMAIN = "service.domain";

  public static final String ACTIVITY_WORKER_LOCAL_FOLDER = "activity.worker.localFolder";
  public static final String ACTIVITY_WORKER_TASK_LIST = "activity.worker.taskList";

  public static final String WORKFLOW_WORKER_TASK_LIST = "workflow.worker.taskList";

  public static final String WORKFLOW_INPUT_FILE_NAME = "workflow.input.fileName";
  public static final String WORKFLOW_INPUT_BUCKET_NAME = "workflow.input.bucketName";

  public static final String WORKFLOW_OUTPUT_FILE_NAME = "workflow.output.fileName";
  public static final String WORKFLOW_OUTPUT_BUCKET_NAME = "workflow.output.bucketName";

  // Timeout period for AWS clients
  private static final int SOCKET_TIMEOUT = 60 * 1000;

  private final String activityWorkerTaskList;
  private final String activityWorkerLocalFolder;
  private final String workflowWorkerTaskList;
  private final String workflowInputFileName;
  private final String workflowInputBucketName;
  private final String workflowOutputFileName;
  private final String workflowOutputBucketName;
  private final String swfServiceUrl;
  private final String swfDomain;

  private Config() {
    this(DEFAULT_PROPERTIES_FILE);
  }

  private Config(String propertiesFile) {
    Properties config = loadProperties(propertiesFile);

    swfServiceUrl = config.getProperty(SWF_SERVICE_URL);
    swfDomain = config.getProperty(SWF_SERVICE_DOMAIN);

    activityWorkerTaskList = config.getProperty(ACTIVITY_WORKER_TASK_LIST);
    activityWorkerLocalFolder = config.getProperty(ACTIVITY_WORKER_LOCAL_FOLDER);

    workflowWorkerTaskList = config.getProperty(WORKFLOW_WORKER_TASK_LIST);

    workflowInputFileName = config.getProperty(WORKFLOW_INPUT_FILE_NAME);
    workflowInputBucketName = config.getProperty(WORKFLOW_INPUT_BUCKET_NAME);

    workflowOutputFileName = config.getProperty(WORKFLOW_OUTPUT_FILE_NAME);
    workflowOutputBucketName = config.getProperty(WORKFLOW_OUTPUT_BUCKET_NAME);
  }

  /**
   * Create a new configuration from the given properties file
   * @param propertiesFile location of the properties file
   * @return new Config instance
   * @throws IOException
   * @throws IllegalArgumentException
   */
  static Config createConfig(String propertiesFile) throws IOException, IllegalArgumentException {
    return new Config(propertiesFile);
  }

  /**
   * Create a new configuration using the default properties file
   * @return new Config instance
   * @throws IOException
   * @throws IllegalArgumentException
   */
  static Config createConfig() throws IOException, IllegalArgumentException {
    return createConfig(DEFAULT_PROPERTIES_FILE);
  }

  /**
   * Create a new SWF client given the current configuration.
   *
   * @return an AmazonSimpleWorkflow client
   */
  AmazonSimpleWorkflow createSWFClient() {
    AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
    AmazonSimpleWorkflow service =
        new AmazonSimpleWorkflowClient(credentials, new ClientConfiguration().withSocketTimeout(SOCKET_TIMEOUT));
    service.setEndpoint(swfServiceUrl);
    return service;
  }

  /**
   * Create a new S3 client given the current configuration.
   *
   * @return an AmazonS3 client
   */
  AmazonS3 createS3Client() {
    AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
    return new AmazonS3Client(credentials, new ClientConfiguration().withSocketTimeout(SOCKET_TIMEOUT));
  }

  /**
   * Get the host name of the current machine.
   *
   * @return this computer's machine
   */
  static String getHostName() {
    try {
      InetAddress addr = InetAddress.getLocalHost();
      return addr.getHostName();
    } catch (UnknownHostException e) {
      throw new Error(e);
    }
  }

  String getActivityWorkerTaskList() {
    return activityWorkerTaskList;
  }

  String getActivityWorkerLocalFolder() {
    return activityWorkerLocalFolder;
  }

  String getWorkflowWorkerTaskList() {
    return workflowWorkerTaskList;
  }

  String getWorkflowInputFileName() {
    return workflowInputFileName;
  }

  String getWorkflowInputBucketName() {
    return workflowInputBucketName;
  }

  String getWorkflowOutputFileName() {
    return workflowOutputFileName;
  }

  String getWorkflowOutputBucketName() {
    return workflowOutputBucketName;
  }

  String getSwfDomain() {
    return swfDomain;
  }

  private Properties loadProperties(String propertiesFile) {
    URL url = Resources.getResource(propertiesFile);
    ByteSource byteSource = Resources.asByteSource(url);
    Properties properties = new Properties();
    try (InputStream inputStream = byteSource.openBufferedStream()) {
      properties.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }
}
