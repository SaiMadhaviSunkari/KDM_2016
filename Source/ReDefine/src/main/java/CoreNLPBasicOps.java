import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
public class CoreNLPBasicOps {
    public static void main(String args[]) throws IOException{
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        BufferedWriter outlemma = null;
        BufferedWriter outner = null;
        BufferedWriter outpos = null;
        try {
           //write ouput to a file
            File outlemmafile = new File("output/lemmatizationOutput/sampleLemmatizationOutput.txt");
            outlemma = new BufferedWriter(new FileWriter(outlemmafile));
            File outnerfile = new File("output/NEROutput/sampleNEROutput.txt");
            outner = new BufferedWriter(new FileWriter(outnerfile));
            File outposfile = new File("output/POSOutput/samplePOSOutput.txt");
            outpos = new BufferedWriter(new FileWriter(outposfile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileInputStream fstream = new FileInputStream("output/researchArticlesTXT/A Large-Scale Empirical study on software reuse in mobile apps.txt");
        try(BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
            for (String line; (line = br.readLine()) != null; ) {
                Annotation document = new Annotation(line);
                pipeline.annotate(document);
                List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        if (outlemma != null) {
                            outlemma.write(lemma + " ");
                        }
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        if (outpos != null) {
                            outpos.write(token + ":"+ pos + " ");
                        }
                        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        if (outner != null) {
                            outner.write(token + ":" + ne + " ");
                        }
        }
                    Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                    SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                    Map<Integer, CorefChain> graph =
                            document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
                    }
            }
        }
       try {
           outlemma.close();
           outner.close();
           outpos.close();
       } catch (IOException e) {
        e.printStackTrace();
    }
    }
}
