java -cp spark/spark-assembly_2.10-0.9.0-incubating-hadoop1.0.4.jar:assembly/target/scala-2.10/pir-assembly-0.1.0.jar -Dspark.akka.frameSize=128 -Dconfig.file=env.conf edu.uwm.cs.pir.examples.NaiveIndexQuery se 2

