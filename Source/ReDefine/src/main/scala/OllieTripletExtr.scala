
package ollie

/*import edu.knowitall.ollie.Ollie
//import edu.knowitall.tool.parse.MaltParser

import scala.io.Source
import edu.knowitall.ollie.confidence.OllieConfidenceFunction
import org.maltparser.{Malt, MaltParserService}
import org.maltparser.parser.Parser*/

/** This is an example project that takes lines as input from stdin,
  * parses them, runs the Ollie extractor on them, scores the
  * extractions with a confidence function, and then prints the results.
  *
  * You can run this project with the following command:
  *   mvn clean compile exec:java -Dexec.mainClass=ollie.Example
  *
  * You will need to have engmalt.linear-1.7.mco in the base directory
  * of this example for the program to work.  You can download this
  * file from the MaltParser website:
  *
  *   http://www.maltparser.org/mco/english_parser/engmalt.html
  */
object OllieTripletExtr extends App {
  /*val parser = new MaltParserService()

  val ollie = new Ollie
  val confidence = OllieConfidenceFunction.loadDefaultClassifier()
  //for (line <- Source.stdin.getLines; if !line.trim.isEmpty) {
  val str: Array[String] = Array("for to in the","obama is real good")
    val parsed = parser.toDependencyStructure(str)
    //val extractionInstances = ollie.extract(parsed)

    println("Extractions:")
    //for (inst <- extractionInstances) {
      //val conf = confidence(inst)
      //println(("%.2f" format conf) + "\t" + inst.extraction)
    //}
    //println("Waiting for next input...")
  //}*/
}
