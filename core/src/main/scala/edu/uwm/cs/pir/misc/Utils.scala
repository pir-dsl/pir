package edu.uwm.cs.pir.misc

import java.io._
import edu.uwm.cs.mir.prototypes.feature._
import edu.uwm.cs.mir.prototypes.aws.AWSS3Config
import edu.uwm.cs.mir.prototypes.aws.AWSS3API
import com.amazonaws.services.s3.AmazonS3

import edu.uwm.cs.mir.prototypes.utils.Utils._

object Utils {

  val formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

  def time[A](block: => A)(algorithm: String, storeResult: Boolean = false): A = {
    val now = System.currentTimeMillis
    val result = block
    if (storeResult) {
      val micros = (System.currentTimeMillis - now)
      val filenameHeader = if (!algorithm.contains("(")) algorithm
      else algorithm.substring(0, algorithm.indexOf("("))
      val writer = new PrintWriter(
        new File(
          "output/" + filenameHeader + "-" +
            formatter.format(new java.util.Date(System.currentTimeMillis)) + ".txt"))
      writer.write("Execution Time: %d microseconds\n".format(micros))
      writer.close()
    }
    result
  }

  def chunkList[In <: IFeature](list: List[In], chunkSize: Int): List[List[In]] = {
    if (list.size <= chunkSize) List(list)
    else {
      val (head, tail) = list.splitAt(chunkSize)
      head :: chunkList(tail, chunkSize)
    }
  }

  implicit def isDebug = true

  def log(msg: String)(implicit level: String = "DEBUG", isDebug: Boolean = false): Unit = {
    if (("INFO" == level) || (isDebug)) {
      println(level + ": " + msg);
    }
  }

  //throws IOException
  def storeObject(obj: Object, config: AWSS3Config, id: String, checkPersisted: Boolean): Unit = {
    if ((config.isIs_s3_storage)) {
      var result = false;
      val amazonS3Client = AWSS3API.getAmazonS3Client(config);
      if (AWSS3API.checkObjectExists(config, id, amazonS3Client, false)) {
        result = AWSS3API.deleteS3ObjectAsOutputStream(config, id, amazonS3Client, checkPersisted);
        if (!result) throw new RuntimeException("Cannnot delete AWS S3 file: " + id);
      }
      var is: InputStream = getObjectInputStream(obj);
      result = AWSS3API.putS3ObjectAsOutputStream(config, id, is, amazonS3Client, checkPersisted);
      if (!result) throw new RuntimeException("Cannnot create AWS S3 file: " + id);
    } else {
      val file = new File(id);
      if (!((file).exists)) {
        file.createNewFile
        serializeObjectToFile(obj, file)
      }
    }
  }

  def getObjectInputStream(obj: Object): InputStream = {
    val os = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(os)
    oos.writeObject(obj)
    oos.flush
    oos.close
    new ByteArrayInputStream(os.toByteArray)
  }

  def loadObject(id: String, config: AWSS3Config, checkPersisted: Boolean): Object = {
    val is = if (config.isIs_s3_storage) {
      val amazonS3Client = AWSS3API.getAmazonS3Client(config)
      AWSS3API.getS3ObjectAsInpuStream(config, id, amazonS3Client, checkPersisted)
    } else {
      new FileInputStream(id)
    }
    //val in = new ObjectInputStream(is)
    val in = new ClassLoaderObjectInputStream(is)
    val obj = in.readObject
    in.close
    obj
  }

  @throws(classOf[IOException])
  class ClassLoaderObjectInputStream(in: InputStream) extends ObjectInputStream(in) {
    @throws(classOf[IOException])
    @throws(classOf[ClassNotFoundException])
    override def resolveClass(desc: ObjectStreamClass): Class[_] =
      {
        try {
          super.resolveClass(desc)
        } catch {
          case e: ClassNotFoundException => {
            val classpath = System.getProperty("java.class.path") 
            log("classpath = " + classpath)("INFO")
            log("desc.getName = " + desc.getName)("INFO")
            val clazz = ClassLoader.getSystemClassLoader.loadClass(desc.getName);
            log("class = " + clazz)("INFO")
            clazz
          }
        }
      }
  }
}
