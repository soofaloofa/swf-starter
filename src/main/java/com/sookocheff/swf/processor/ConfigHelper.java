package com.sookocheff.swf.processor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;


/**
 * Helper for creating AWS clients.
 */
public class ConfigHelper {

  private static final String SWF_ACTIVITIES_TASK_LIST = "FileProcessing";
  private static final String SWF_DECISION_TASK_LIST = "FileProcessing";
  private static final String SWF_LOCAL_FOLDER = "/tmp/swf-sample/";
  private static final String SWF_SERVICE_URL = "https://swf.us-west-2.amazonaws.com";
  private static final String SWF_DOMAIN = "anser-test";

  private static final String S3_SOURCE_BUCKET_NAME = "";
  private static final String S3_SOURCE_FILE_NAME = "";

  private static final String S3_TARGET_BUCKET_NAME = "";
  private static final String S3_TARGET_FILE_NAME = "";

  private static final int SOCKET_TIMEOUT = 60 * 1000;

  private ConfigHelper() {
  }

  public static ConfigHelper createConfig() throws IOException, IllegalArgumentException {
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.DEBUG);

    return new ConfigHelper();
  }

  public AmazonSimpleWorkflow createSWFClient() {
    AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
    AmazonSimpleWorkflow service =
        new AmazonSimpleWorkflowClient(credentials, new ClientConfiguration().withSocketTimeout(SOCKET_TIMEOUT));
    service.setEndpoint(SWF_SERVICE_URL);
    return service;
  }

  public AmazonS3 createS3Client() {
    AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
    return new AmazonS3Client(credentials, new ClientConfiguration().withSocketTimeout(SOCKET_TIMEOUT));
  }

  public String getDomain() {
    return SWF_DOMAIN;
  }

  public String getLocalFolder() {
    return SWF_LOCAL_FOLDER;
  }

  public String getActivitiesTaskList() {
    return SWF_ACTIVITIES_TASK_LIST;
  }

  public String getDecisionTaskList() {
    return SWF_DECISION_TASK_LIST;
  }

  public String getS3SourceBucketName() {
    return S3_SOURCE_BUCKET_NAME;
  }

  public String getS3SourceFileName() {
    return S3_SOURCE_FILE_NAME;
  }

  public String getS3TargetBucketName() {
    return S3_TARGET_BUCKET_NAME;
  }

  public String getS3TargetFileName() {
    return S3_TARGET_FILE_NAME;
  }

  static String getHostName() {
    try {
      InetAddress addr = InetAddress.getLocalHost();
      return addr.getHostName();
    } catch (UnknownHostException e) {
      throw new Error(e);
    }
  }
}
