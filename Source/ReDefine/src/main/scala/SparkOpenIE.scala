import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}


object SparkOpenIE {

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "C:\\winutils");

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val spark = SparkSession
      .builder
      .appName("SparkOpenIE")
      .master("local[*]")
      .getOrCreate()

    //Logger.getLogger("org").setLevel(Level.OFF)
    //Logger.getLogger("akka").setLevel(Level.OFF)

    val input = sc.textFile("output/researchArticlesTXT/A Large-Scale Empirical study on software reuse in mobile apps.txt").map(line => {
      val t=CoreNLPOpenIE.returnTriplets(line)
      t
    })

    println(input.collect().mkString("\n")+"\n")

  val temp = "just for debugging"

  }

}
