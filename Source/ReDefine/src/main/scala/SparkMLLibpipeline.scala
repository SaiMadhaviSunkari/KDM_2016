import java.io.{FileNotFoundException, _}
import java.nio.file.{Files, Paths, StandardOpenOption}

import scala.io.Source
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.{HashingTF, IDF, StopWordsRemover, Tokenizer}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.io.BufferedSource


object SparkMLLibpipeline {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\winutils")

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")
    sparkConf.set("spark.hadoop.validateOutputSpecs", "false")
    val sc = new SparkContext(sparkConf)
    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true")

    val spark = SparkSession
      .builder
      .appName("TfIdfExample")
      .master("local[*]")
      .getOrCreate()
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import spark.implicits._
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    for (file <- new File("output/researchArticlesTXT").listFiles) {
      val input = spark.read.text(file.toString).as[String]
      val sentenceData = input.toDF("sentence")
      /* val sentenceData = spark.createDataFrame(Seq(
      (0, input)
      )).toDF("label")
      */
      val researchArticleName: String = FilenameUtils.getBaseName(file.toString)
      val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
      val wordsData = tokenizer.transform(sentenceData)

      val stopWordLines: Array[String] = Source.fromFile("resources/stopwords.txt").getLines.toArray
      val remover = new StopWordsRemover()
        .setInputCol("words")
        .setOutputCol("filteredWords")
        .setStopWords(stopWordLines)
      val processedWordData = remover.setStopWords(stopWordLines).transform(wordsData)

      //wordsData.rdd.saveAsTextFile("output/tokenizerOutput/paper1.txt")
      //processedWordData.rdd.saveAsTextFile("output/stopWordRemover/paper1.txt")

      val hashingTF = new HashingTF()
        .setInputCol("filteredWords").setOutputCol("rawFeatures").setNumFeatures(20)
      val featurizedData = hashingTF.transform(processedWordData)
      // get top TF words using the same function used to calculate Top TFIDF words

      val topTFWords: Array[(String, Double)] = TopTF.getTopTFWords(sc, featurizedData.select("filteredWords").rdd)
      // println("TOP tf WORDS: \n\n" + topTFWords.mkString("\n"))

      try {
        val fileTFIDF = new File("output/topTFWords/"+researchArticleName+".txt")
        fileTFIDF.getParentFile().mkdirs()
        val writer = new FileWriter(fileTFIDF)
        for (x <- topTFWords){
          writer.write(x.toString()+"\n")}
        writer.close()
      } catch {
        case ex: FileNotFoundException =>{
          println("Missing file exception")
        }

        case ex: IOException => {
          println("IO Exception")
        }
      }
      val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
      val idfModel = idf.fit(featurizedData)
      val rescaledData = idfModel.transform(featurizedData)
      //rescaledData.select("filteredWords","features", "sentence").take(10).foreach(println)
      //rescaledData.orderBy(desc("words")).take(10).foreach(println)
      //rescaledData.printSchema()
      val r2 = rescaledData.select("filteredWords", "features")
      //rescaledData.sort($"features".desc).rdd.saveAsTextFile("topTFIDF")
      //val rescaledData2=rescaledData.rdd
      //rescaledData2.sortBy(we=>we,false)
      val topTFIDFWords = TopTFIDF.getTopTFIDFWords(sc, rescaledData.select("filteredWords").rdd)
      //  println("TOP WORDS: \n\n" + topTFIDFWords.mkString("\n"))
      try {
        val fileTFIDF = new File("output/topTFIDFWords/"+researchArticleName+".txt")
        fileTFIDF.getParentFile().mkdirs()
        val writer = new FileWriter(fileTFIDF)
        for (x <- topTFIDFWords){
          writer.write(x.toString()+"\n")}
        writer.close()
      } catch {
        case ex: FileNotFoundException =>{
          println("Missing file exception")
        }

        case ex: IOException => {
          println("IO Exception")
        }
      }

//      rescaledData.createOrReplaceTempView("output1")
      //val r3= rescaledData.toDF()
////
      //r3.write.parquet("output1.parquet")


      //Spark SQL code
    //val parfile=sqlContext.read.parquet("output1.parquet")
    //parfile.createOrReplaceTempView("output2")


      //val parquetfile=output1.registerTemptable("")
      //r3.write.mode(SaveMode.Overwrite).saveAsTable("output1")
      //r3.createOrReplaceTempView("output1")
      //SaveMode(org.apache.spark.sql.SaveMode.valueOf("Overwrite"))
      //r3.write.mode(SaveMode.Overwrite).save("output/TFIDFout")

      //val hadoopConf = new org.apache.hadoop.conf.Configuration()
      //val hdfs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:9000"), hadoopConf)
      //try { hdfs.delete(new org.apache.hadoop.fs.Path("output/TFIDFOut"), true) } catch { case _ : Throwable => { } }
      // spark SQL code
 //   val temp1=sqlContext.sql("select words, features  from output2")
//    temp1.rdd.saveAsTextFile("queryout")
   // temp1.show(10)
   //

      //rescaledData.rdd.saveAsTextFile("output/TFIDFOut");
    }
    spark.stop()
  }

}
