
import java.io.{File, FileNotFoundException, FileWriter, IOException}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

object Ngram_Word2Vec {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\winutils")

    // Configuration
    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val spark = SparkSession
      .builder
      .appName("SparkW2VML")
      .master("local[*]")
      .getOrCreate()


    // Turn off Info Logger for Console
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);

    // Read the file into RDD[String]
    val input = sc.textFile("output/temp/sample.txt", 500).map(line => {
      //Getting Lemmatized Form of the word using CoreNLP
      val lemma = CoreNLPReturnLemma.returnLemma(line)
      (0, lemma)

    })


    //Creating DataFrame from RDD

    val sentenceData = spark.createDataFrame(input).toDF("labels", "sentence")

    //Tokenizer
    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val wordsData = tokenizer.transform(sentenceData)
    val stopWordLines: Array[String] = Source.fromFile("resources/stopwords.txt").getLines.toArray
    //Stop Word Remover
    val remover = new StopWordsRemover()
      .setInputCol("words")
      .setOutputCol("filteredWords")
      .setStopWords(stopWordLines)
    val processedWordData = remover.transform(wordsData)

    //NGram
    val ngram = new NGram().setInputCol("filteredWords").setOutputCol("ngrams")
    val ngramDataFrame = ngram.transform(processedWordData)
    ngramDataFrame.take(3).foreach(println)
    println(ngramDataFrame.printSchema())

    //TFIDF TopWords
    val topWords = TopTFIDF.getTopTFIDFWords(sc, ngramDataFrame.select("ngrams").rdd)
    println("TOP WORDS: \n\n" + topWords.mkString("\n"))

    val newDataFrame = ngramDataFrame.na.drop()

    println(newDataFrame.printSchema())

    //Word2Vec Model Generation


    val word2Vec = new Word2Vec()
      .setInputCol("ngrams")
      .setOutputCol("result")
      .setVectorSize(3)
      .setMinCount(0)

    val struct = StructType(StructField("labels", IntegerType, false) ::
      StructField("sentence", StringType, true) ::
      StructField("words", ArrayType(StringType,true), true)::
      StructField("filteredWords",ArrayType(StringType,true), true)::
      StructField("ngrams", ArrayType(StringType,true), true) :: Nil
    )
    val ss=spark.createDataFrame(sc.parallelize(ngramDataFrame.collect()),struct)

    val model = word2Vec.fit(ss)


    //Finding synonyms for TOP TFIDF Words using Word2Vec Model
    topWords.foreach(f => {
      println(f._1 + "  : ")
      val result = model.findSynonyms(f._1, 3)
      //result.take(3).foreach(println)
      println
    })

    spark.stop()
  }

}
