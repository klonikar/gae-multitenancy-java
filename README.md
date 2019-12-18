# A sample project to demonstrate multitenancy on google appengine in Java

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/guestbook-cloud-datastore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

An App Engine guestbook using Java, Maven, and the Cloud Datastore API via
[google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java).

Please ask questions on [StackOverflow](http://stackoverflow.com/questions/tagged/google-app-engine).

## Running Locally

First, pick a project ID. You can create a project in the [Cloud Console] if you'd like, though this
isn't necessary unless you'd like to deploy the sample.

Second, modify `Persistence.java`: replace `your-project-id-here` with the project ID you picked.

Then start the [Cloud Datastore Emulator](https://cloud.google.com/datastore/docs/tools/datastore-emulator):

    gcloud beta emulators datastore start --project=YOUR_PROJECT_ID_HERE

Finally, in a new shell, [set the Datastore Emulator environmental variables](https://cloud.google.com/datastore/docs/tools/datastore-emulator#setting_environment_variables)
and run

    #mvn clean appengine:run ==> Does not work with emulator as it keeps throwing the following exception 
    #                            Exception when handling request: INVALID_ARGUMENT: "no_app_id" is an invalid project id.

    $(google-cloud-sdk-dir)/bin/java_dev_appserver.sh --application=YOUR_PROJECT_ID_HERE target/gae-multitenancy-j8-1.0-SNAPSHOT
    NOTE: Either set the google-cloud-sdk-dir env variable or use the directory of the cloud sdk install.
## Deploying

    mvn clean appengine:deploy
