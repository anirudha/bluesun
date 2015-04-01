import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;

import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngineFactory;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.Edge;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class GremlinQuery extends ExtendedQueryBase implements PostFilter{
    private String query;
    private TinkerGraph graph;
    
    public GremlinQuery(SolrParams localParams) {

        query = localParams.get("query", "");
        ScriptEngine engine = new GremlinGroovyScriptEngineFactory().getScriptEngine();
        try{
            graph = (TinkerGraph)engine.eval(query);
        } catch(ScriptException e){

        }

    }
    
    @Override
    public int getCost() {
        // We make sure that the cost is at least 100 to be a post filter
        return Math.max(super.getCost(), 100);
    }

    @Override
    public boolean getCache() {
        return false;
    }

    @Override
    public DelegatingCollector getFilterCollector(IndexSearcher idxS) {

        return new DelegatingCollector() {

            @Override
            public void collect(int docNumber) throws IOException {
                /* THE SLOW WAY
                // Our filter magic -> call super.collect()
                // To be able to get documents, we need the reader
                AtomicReader reader = context.reader();

                // From the reader we get the current document by the docNumber
                Document currentDoc = reader.document(docNumber);

                // We get the id field from our document
                Number currentDocId = currentDoc.getField("id").numericValue();
                */
                


                /* THE FAST WAY */
                long currentDocId = DocValues.getNumeric(context.reader(), "id_i").get(docNumber);
                
                // Filter magic
                if (isInGraph(currentDocId)) {
                    super.collect(docNumber);
                }
            }
        };
    }

    private boolean isInGraph(long docID){
        Iterator<Vertex> vertexIt = graph.vertexIterator();
        while(vertexIt.hasNext()){
            Vertex v = vertexIt.next();


        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
