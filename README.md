# swf-starter

A starter Maven project for learning the [Amazon Flow
Framework](https://aws.amazon.com/swf/details/flow/) for Java. This
project downloads an image from S3 to an Activity Worker. The Activity
Worker zips the file and uploads it back to S3 when complete.

A more detailed explanation of this code is available on [my
website](https://sookocheff.com/aws/getting-started-with-amazon-flow-framework/).

Adapted from https://github.com/aws/aws-sdk-java/tree/master/src/samples/AwsFlowFramework

## Dependencies

Flow framework has dependencies on annotation processing (Java 1.6 and up)
and AspectJ. To get these working for Maven and IntelliJ requires some
configuration. A lot of the steps to get this working I adapted from [this
stackoverflow
thread](http://stackoverflow.com/questions/9392655/how-to-consume-amazon-swf);
things should work using the `pom.xml` file present in this repo and, if
not, that thread has excellent troubleshooting information.

You can verify the dependencies are correct by using Maven to compile the
code and checking for auto-generated classes.

```bash
> # compile annotations and aspects
> mvn verify
> # grep for auto-generated aspect classes
> find target/classes -type f | grep Ajc
```

## Running the Application

Activity and Workflow Workers can be run locally or on remote hosts.

### Prerequisites

1. Create an S3 bucket for hosting the image files.
2. Upload an image to the bucket.
3. Create an [SWF
   Domain](http://docs.aws.amazon.com/amazonswf/latest/developerguide/swf-dev-domain.html)
   to run your workflow through the AWS UI.
4. Update the `src/main/resources/processor.properties` file with your S3
   and SWF configuration.
5. A valid AWS credentials file located at `~/.aws/credentials`

### Running the workers

These commands can be run locally or on remote hosts, depending on your
deployment.

Using Maven:

```bash
> mvn -e compile exec:java -Dexec.mainClass=com.sookocheff.swf.processor.ActivityHost
> mvn -e compile exec:java -Dexec.mainClass=com.sookocheff.swf.processor.WorkflowHost
```

From the bundled Jar file:

```bash
> mvn package
> java -cp target/swf-starter-bundled-1.0-SNAPSHOT.jar com.sookocheff.swf.processor.ActivityHost
> java -cp target/swf-starter-bundled-1.0-SNAPSHOT.jar com.sookocheff.swf.processor.WorkflowHost
```

### Starting a workflow execution

An execution of the workflow can be started via the SWF UI or through the
included `WorkflowExecutionStarter` application.

Using Maven:

```bash
> mvn -e compile exec:java -Dexec.mainClass=com.sookocheff.swf.processor.WorkflowExecutionStarter
```

From the bundled Jar file:

```bash
> mvn package
> java -cp target/swf-starter-bundled-1.0-SNAPSHOT.jar com.sookocheff.swf.processor.WorkflowExecutionStarter
```
