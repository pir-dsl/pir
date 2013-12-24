package edu.uwm.cs.pir.aws

import edu.uwm.cs.pir.spark.SparkObject._

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest

object AWSS3API {

  def getIdList(prefix : String, extension : String) : List[String] = {
    System.setProperty("com.amazonaws.services.s3.disableGetObjectMD5Validation", if (awsS3Config.isIs_s3_storage) "true" else "false")
    val bucketName = awsS3Config.getBucket_name()
    val amazonS3Client = new AmazonS3Client()

    val request = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix)

    var allResultRetrieved = false
    
    var keyList = List[String]()
    do {    
      val response = amazonS3Client.listObjects(request);
      val summaries = response.getObjectSummaries()
      keyList = keyList ::: summaries.map(summary => summary.getKey()).filter(key => key.endsWith(extension)).toList
      if (response.isTruncated) {
        request.setMarker(response.getNextMarker())
      } else {
        allResultRetrieved = true
      }
    } while (!allResultRetrieved) 
    keyList
  }
}