# Release Notes

## 2019-01-29 1.0.15
  * Make S3 item name use / instead of _, to take advantage of S3 "folders"

## 2019-01-29 1.0.14
  * Handle command line args properly in the S3 store

## 2019-01-28 1.0.13
  * Fix Docker image name for snapshotter (was haystack-service-snapshotter, is now haystack-service-graph-snapshotter)

## 2019-01-25 1.0.12
  * Fix typo in Docker image name for snapshotter

## 2019-01-25 1.0.11
  * Publish snapshotter to Docker

## 2019-01-23 1.0.10
  * Names of S3 service graph snapshot items should terminate in ".csv"

## 2019-01-23 1.0.9
  * Make the parameter for listObjectsBatchSize in S3SnapshotStore optional, as it's only needed when calling write

## 2019-01-23 1.0.8
  * Remove Main companion object (it wasn't really needed) 
  * Allow URL to be specified as a parameter instead of being hard coded
  * More unit tests

## 2019-01-23 1.0.7 
  * Add Main companion class to Main object so that it can be instantiated by the Java JVM
  * Add this ReleaseNotes.md file
