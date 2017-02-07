import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.OpenIE;
import edu.stanford.nlp.naturalli.SentenceFragment;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.spark_project.jetty.util.UrlEncoded.ENCODING;

public class CoreNLPOpenIEExt {

    private CoreNLPOpenIEExt() {} // static main

    public static void main(String[] args) throws Exception {
        // Create the Stanford CoreNLP pipeline
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie"
                // , "depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz"
                // "annotators", "tokenize,ssplit,pos,lemma,parse,natlog,openie"
                // , "parse.originalDependencies", "true"
        );
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Annotate an example document.
        String text;
        if (args.length > 0) {
            text = IOUtils.slurpFile(args[0]);
        } else {
            String mystr = null;
            Path path = Paths.get("output/researchArticlesTXT/A Dashboard of an Education Data Portal using Big Data Solutions.txt");
            try (BufferedReader reader = Files.newBufferedReader(path, ENCODING)){
                String line = null;
                while ((line = reader.readLine()) != null) {
                    //process each line in some way
                    mystr+=line;                }
            }
            text =mystr;
        }
        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        // Loop over sentences in the document
        int sentNo = 0;
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
//            System.out.println("Sentence #" + ++sentNo + ": " + sentence.get(CoreAnnotations.TextAnnotation.class));

            // Print SemanticGraph
  //          System.out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));

            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);

            // Print the triples
            for (RelationTriple triple : triples) {
                System.out.println(triple.confidence + "\t" +
                        triple.subjectLemmaGloss() + "\t" +
                        triple.relationLemmaGloss() + "\t" +
                        triple.objectLemmaGloss());
            }
            // Alternately, to only run e.g., the clause splitter:
            List<SentenceFragment> clauses = new OpenIE(props).clausesInSentence(sentence);
            for (SentenceFragment clause : clauses) {
              System.out.println(clause.parseTree.toString(SemanticGraph.OutputFormat.LIST));
            }
            System.out.println();
        }
    }

}