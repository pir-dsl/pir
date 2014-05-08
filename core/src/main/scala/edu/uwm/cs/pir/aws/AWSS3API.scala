package edu.uwm.cs.pir.aws

import edu.uwm.cs.pir.spark.SparkObject._
import edu.uwm.cs.pir.misc.Utils._

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest

import edu.uwm.cs.mir.prototypes.aws.AWSS3API._

object AWSS3API {

  def getIdList(prefix: String, extension: String, checkPersisted: Boolean = false): List[String] = {
    System.setProperty("com.amazonaws.services.s3.disableGetObjectMD5Validation", if (awsS3Config.isIs_s3_storage) "true" else "false")
    val bucketName = if (!checkPersisted) awsS3Config.getBucket_name else awsS3Config.getS3_persistence_bucket_name
    val amazonS3Client = new AmazonS3Client

    val request = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix)

    var allResultRetrieved = false

    var keyList = List[String]()
    do {
      val response = amazonS3Client.listObjects(request)
      val summaries = response.getObjectSummaries()
      keyList = keyList ::: {
        val newSummaries = summaries.map(summary => summary.getKey())
        log("newSummaries.size = " + newSummaries.size)("INFO")
        if (!extension.isEmpty) newSummaries.filter(key => {log("key = " + key)("INFO");key.endsWith(extension)}).toList else newSummaries.toList
      }
      if (response.isTruncated) {
        request.setMarker(response.getNextMarker())
      } else {
        allResultRetrieved = true
      }
    } while (!allResultRetrieved)
    keyList
  }

  def isExistingS3Location(S3String: String, hostname: String = ""): Boolean = {
    val amazonS3Client = getAmazonS3Client(awsS3Config);
    val result = checkObjectExists(awsS3Config, S3String + (if (hostname.isEmpty) "" else "/" + hostname), amazonS3Client, true)
    log("isExistingS3Location=" + result)("INFO")
    result
  }

  def getExistingHostname(S3String: String): String = {
    val amazonS3Client = getAmazonS3Client(awsS3Config);
    try {
      val result = getS3ObjectAsString(awsS3Config, S3String, amazonS3Client, true)
      log("isExistingS3Location=" + result)("INFO")
      result
    } catch {
      case _ : Throwable => ""
    }
  }

}