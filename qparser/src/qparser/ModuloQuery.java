package qparser;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;

public class ModuloQuery extends ExtendedQueryBase implements PostFilter {
	private int moduloX;
	
	public ModuloQuery(SolrParams localParams) {
		this.moduloX = localParams.getInt("modulo", 42);
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
				if (currentDocId % moduloX == 0) {
					super.collect(docNumber);
				}
			}
		};
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + moduloX;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuloQuery other = (ModuloQuery) obj;
		if (moduloX != other.moduloX)
			return false;
		return true;
	}
}
