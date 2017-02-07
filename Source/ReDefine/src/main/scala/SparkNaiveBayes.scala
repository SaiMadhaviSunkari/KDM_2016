
package mlpipeline

import ontInterface.OwlReDefine
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.HashMap

object SparkNaiveBayes {


  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\winutils")

    val trainFolder = "data/Categories/*"
    val conf = new SparkConf().setAppName(s"NBExample").setMaster("local[*]").set("spark.driver.memory", "12g").set("spark.executor.memory", "12g")
    val sc = new SparkContext(conf)

    Logger.getRootLogger.setLevel(Level.WARN)

    // Load documents, and prepare them for NB.
    val preprocessStart = System.nanoTime()
    val (input, corpus) =
      preprocess(sc, trainFolder)

    var hm = new HashMap[String, Int]()
    val CATEGORIES = List("BigData", "MachineLearning","CloudComputing","ClusterComputing","DistributedComputing","SoftwareEngineering","KnowledgeDiscovery","ParallelComputing","MobileComputing","DatabaseComputing")
    var index = 0
    CATEGORIES.foreach(f => {
      hm += CATEGORIES(index) -> index
      index += 1
    })
    val mapping = sc.broadcast(hm)
    val data = input.zip(corpus)
    val featureVector = data.map(f => {
      val location_array = f._1._1.split("/")
      val class_name = location_array(location_array.length - 2)

      new LabeledPoint(hm.get(class_name).get.toDouble, f._2)
    })
    val splits = featureVector.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)
    val model = NaiveBayes.train(featureVector, lambda = 1.0, modelType = "multinomial")
    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))


    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

    val metrics = new MulticlassMetrics(predictionAndLabel)
    println("precision "+metrics.precision)
    println("accuracy "+accuracy)

    val testDir = "data/test/*"

    val topicData = SparkLDAMain.main(sc, testDir, 10, "em")

    val testFV = getTFIDFVector(sc, topicData)
//    testFV.saveAsTextFile("output/op.txt")
    val result = model.predict(testFV)

    result.foreach(f => println(f))

    //Ontology creation
    val owl = new OwlReDefine()

    CATEGORIES.foreach(f => {
      owl.createClass(f)
    })

    input.collect.foreach(f => {
      val location_array = f._1.split("/")
      val class_name = location_array(location_array.length - 2)

      owl.createIndividual(":" + "myname", class_name)

      /*
            val array = f._2.split("/")
            array.foreach(ff => {
              var temp = ""
              temp = temp+ff
              owl.createIndividual(":" + temp, class_name)

            })
      */


    })

    owl.createClass(":Topic")



    val resultData = topicData.zip(result.collect())
    var i = 1
    resultData.map(f => {
      owl.createIndividual(":Topic" + i, ":Topic")

      var className = ""
      if (f._2 == 0)
        className = ":BigData"

      else if (f._2==1)
        className = ":MachineLearning"
      else if (f._2==2)
        className = ":CloudComputing"
      else if (f._2==3)
        className = ":ClusterComputing"
      else if (f._2==4)
        className = ":DistributedComputing"
      else if (f._2==5)
        className = ":SoftwareEngineering"
      else if (f._2==6)
        className = ":KnowledgeDiscovery"
      else if (f._2==7)
        className = ":ParallelComputing"
      else if (f._2==8)
        className = ":MobileComputing"
      else if (f._2==9)
        className = ":DatabaseComputing"

      val array = f._1.split(" ")
      array.foreach(ff => {
        owl.createIndividual(":" + ff, className)
        owl.createObjectProperty(className, "isRelatedTo", ":" + ff)
        owl.createObjectProperty(":" + ff, "hasTopic", ":Topic" + i)
      })
      i = i + 1

    })

    owl.saveOntology()

    sc.stop()
  }

  private def preprocess(sc: SparkContext,
                         paths: String): (RDD[(String, String)], RDD[Vector]) = {

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._
    val df = sc.wholeTextFiles(paths).map(f => {
      var ff = f._2.replaceAll("[^a-zA-Z\\s:]", " ")
      ff = ff.replaceAll(":", "")
      // println(ff)
      (f._1, CoreNLP.returnLemma(ff))
    }).toDF("location", "docs")


    val tokenizer = new RegexTokenizer()
      .setInputCol("docs")
      .setOutputCol("rawTokens")
    val stopWordsRemover = new StopWordsRemover()
      .setInputCol("rawTokens")
      .setOutputCol("tokens")

    val tf = new org.apache.spark.ml.feature.HashingTF()
      .setInputCol("tokens")
      .setOutputCol("features")
    val idf = new org.apache.spark.ml.feature.IDF()
      .setInputCol("features")
      .setOutputCol("idfFeatures")

    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, stopWordsRemover, tf, idf))

    val model = pipeline.fit(df)
    val documents = model.transform(df)
      .select("idfFeatures")
      .rdd
      .map { case Row(features: Vector) => features }

    val input = model.transform(df).select("location", "docs").rdd.map { case Row(location: String, docs: String) => (location, docs) }
    println(model.transform(df).printSchema())
    (input, documents)
  }

  def getTFIDFVector(sc: SparkContext, input: Array[String]): RDD[Vector] = {

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._
    val df = sc.parallelize(input.toSeq).toDF("docs")


    val tokenizer = new RegexTokenizer()
      .setInputCol("docs")
      .setOutputCol("rawTokens")
    val stopWordsRemover = new StopWordsRemover()
      .setInputCol("rawTokens")
      .setOutputCol("tokens")

    val tf = new org.apache.spark.ml.feature.HashingTF()
      .setInputCol("tokens")
      .setOutputCol("features")
    val idf = new org.apache.spark.ml.feature.IDF()
      .setInputCol("features")
      .setOutputCol("idfFeatures")

    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, stopWordsRemover, tf, idf))

    val model = pipeline.fit(df)
    val documents = model.transform(df)
      .select("idfFeatures")
      .rdd
      .map { case Row(features: Vector) => features }

    documents
  }
}
