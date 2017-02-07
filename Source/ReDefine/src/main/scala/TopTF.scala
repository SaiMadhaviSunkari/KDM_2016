import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import scala.collection.immutable.HashMap

/**
  * Created by Mayanka on 22-Jun-16.
  */
object TopTF {

  def getTopTFWords(sc: SparkContext, input:RDD[Row]): Array[(String, Double)] = {

    input.foreach(f=>println(f))
    val documentseq = input.map(_.getList(0).toString.replace("[WrappedArray(","").replace(")]","").replace(", ",",").split(",").toSeq)

    val strData = sc.broadcast(documentseq.collect())
    val hashingTF = new HashingTF()
    val tf = hashingTF.transform(documentseq)
    tf.cache()

    val tfidfvalues = tf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val values = ff(2).replace("]", "").replace(")","").split(",")
      values
    })
    val tfidfindex = tf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val indices = ff(1).replace("]", "").replace(")","").split(",")
      indices
    })
    tf.foreach(f => println(f))

    val tfidfData = tfidfindex.zip(tfidfvalues)
    var hm = new HashMap[String, Double]
    tfidfData.collect().foreach(f => {
      hm += f._1 -> f._2.toDouble
    })
    val mapp = sc.broadcast(hm)

    val documentData = documentseq.flatMap(_.toList)
    val dd = documentData.map(f => {
      val i = hashingTF.indexOf(f)
      val h = mapp.value
      (f, h(i.toString))
    })

    val dd1=dd.distinct().sortBy(_._2,false)
    dd1.foreach(f=>{
      //println(f)
    })
  return dd1.take(100)
  }

}
