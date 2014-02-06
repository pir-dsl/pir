package edu.uwm.cs.pir.examples

import scala.collection.JavaConversions._
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import scala.util.control.Breaks
import edu.uwm.cs.pir.aws.AWSS3API._
import edu.uwm.cs.pir.spark.SparkObject._

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

object AmazonS3FileUploadExample {

  def main(args: Array[String]): Unit = {
    
    val amazonS3Client = new AmazonS3Client()
    val rangeObjectRequest = new GetObjectRequest(awsS3Config.getBucket_name(), "experiments/early_fusion/wikipedia_dataset/texts/training/warfare/fc66f418ce7c7855daed259ca643fb0a-4.5.xml");
    val objectPortion = amazonS3Client.getObject(rangeObjectRequest);
    val objectData = objectPortion.getObjectContent()
    displayTextInputStream(objectPortion.getObjectContent())
    objectData.close()

  }

  def displayTextInputStream(input: InputStream) {
    // Read one text line at a time and display.
    val reader = new BufferedReader(new InputStreamReader(input))
      var line = ""
      do {
         line = reader.readLine()
        println(line)
      } while (line != null)
  }
}