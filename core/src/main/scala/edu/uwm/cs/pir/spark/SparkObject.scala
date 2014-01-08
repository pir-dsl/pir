package edu.uwm.cs.pir.spark

import com.typesafe.config._

import org.apache.spark._
import SparkContext._

import edu.uwm.cs.mir.prototypes.aws.AWSS3Config

import edu.uwm.cs.pir.misc.Utils._

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator

object SparkObject { 
  
  val conf = ConfigFactory.load	

  var sparkContext: SparkContext = null
  
  var awsS3Config: AWSS3Config = initAWSS3Config
  
  val isS3Storage = conf.getString("aws_env.is_s3_storage")	
  
  val sparkPartitionSize = conf.getString("spark_env.spark_partition_size")		
  
  val inputDataRoot = if ("true" == isS3Storage) "" else conf.getString("input_data_root")
			
  def initAWSS3Config : AWSS3Config = {	
		val isMd5Validation = conf.getString("aws_env.s3_disable_get_object_md5_validation")
		val bucketName = conf.getString("aws_env.bucket_name")
		log("bucketName = " + bucketName)("INFO")
		val s3_root = conf.getString("aws_env.s3_root")
		val isS3Storage = conf.getString("aws_env.is_s3_storage")	
		log("isS3Storage = " + isS3Storage)("INFO")
		new AWSS3Config(if ("true" == isS3Storage) true else false, 
				if ("true" == isMd5Validation) true else false, bucketName, s3_root)	  
  }
  
  def initSparkConf : SparkContext = {
			
		//log("conf = " + conf)("INFO")  
		val hostName = conf.getString("spark_env.host_name")
		val port = conf.getString("spark_env.port")
		val numCore = conf.getString("spark_env.num_core")
		val appName = conf.getString("spark_env.host_name")
		val sparkHome = conf.getString("spark_env.spark_home")
		val pirAssembly = conf.getString("pir_env.pir_assembly")
		setSparkContext(hostName, port, numCore, appName, sparkHome, pirAssembly)  
  }
  
  def setSparkContext(hostname: String, port: String, numCore: String, appName: String, sparkHome: String, pirAssembly: String) : SparkContext = {
	  
	  val sparkHostString = if ("local" == hostname) hostname + "[" + numCore + "]" else "spark://" + hostname + ":" + port
	  //System.clearProperty("spark.master.port")
		  
	  //System.setProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer")		  
	  System.setProperty("spark.scheduler.mode", "FAIR")
	  System.setProperty("spark.task.maxFailures", "1")
	  new SparkContext(sparkHostString, appName, "/" + sparkHome, Seq(pirAssembly))  
  }
}