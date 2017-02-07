import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.Collection;

public class CoreNLPOpenIE {
    public static String returnTriplets(String sentence) {

        Document doc = new Document(sentence);
        String lemma="";
        for (Sentence sent : doc.sentences()) {
            Collection<RelationTriple> l=sent.openieTriples();
            for (int i = 0; i < l.toArray().length ; i++) {
                lemma+= l.toString();
            }
            System.out.println(lemma+"\n");
        }
        return lemma;
    }

}
