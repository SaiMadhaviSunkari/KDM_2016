package sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

import java.io.InputStream;


public class QueryExecution1 {
	public static void main(String args[]){
		OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		InputStream in = FileManager.get().open("Data/SparqlRdf.owl");
		ontoModel.read(in, null);
		String queryString = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT  ?subject ?predicate  ?object WHERE {?subject foaf:predicate ?object } " ;
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, ontoModel);
		ResultSet results = qexec.execSelect() ;
		for ( ; results.hasNext() ; ){
			QuerySolution soln = results.nextSolution() ;
			System.out.println(soln.toString());
		}
	}
}
