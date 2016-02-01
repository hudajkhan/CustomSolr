package edu.cornell.mannlib.vitro.webapp.apacheextension.lucene.queries.function;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.Fields;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.util.BytesRef;


/*
 * TestFunctionValueSource - responsible for instantiating function value for particular query
 * Based on http://stackoverflow.com/questions/19528841/custom-functionquery-constvaluesource
 * Return constant value based on key
 */
public class TestFunctionValueSource extends ValueSource {
	private static final Log log = LogFactory.getLog(TestFunctionValueSource.class);

	final Map<String, Float> constants;

	

	// final ValueSource vs;

	public TestFunctionValueSource(Map<String, Float> constants) {
		

		this.constants = constants;
		
	}

	public FunctionValues getValues(Map context,
			AtomicReaderContext readerContext) throws IOException {
		// Returns function values instance, this appears to actually implement
		// the methods
		// of the abstract class and return them at the same time
		log.debug("=====>>>TestFunctionValueSource getValues method");
		// Not sure why this is 'experimental' and what all that means
		final Fields fields = readerContext.reader().fields();
		log.debug("Retrieved Fields ");
		final AtomicReader reader = readerContext.reader();
		log.debug("Retrieved Reader");
		// Not sure what to get from fields here or if we should get anything
		// Test out what we can do with VS here
		// final FunctionValues fv = this.vs.getValues(context, readerContext);
		// log.debug("Retrieved function values from the input VS");

		// Trying field cache-change boolean to false, true will return number
		// of docs with that field
		final BinaryDocValues values = FieldCache.DEFAULT.getTerms(reader,
				"URI", true);

		return new FunctionValues() {

			public float floatVal(int doc) {
				String uriFromCache = null;
				//Assume maximum value? Not sure how to actually do a sort in this fashion
				//Q: What should we return to indicate that NO distance is associated and this document
				//can therefore not even be used?
				//We need a 'neutral' value to indicate that there is no location here and we shouldn't
				//be using this value at all
				//Float distance = Float.MAX_VALUE;
				Float distance = null;
				try {
					// Leaving this in for referencem in case we need to utilize
					// the document itself
					// Using the cache is probably better for performance
					/*
					 * Document document = reader.document(doc); String docId =
					 * document.get("DocId"); if(docId != null) {
					 * log.debug("Doc id is " + docId); } String URI =
					 * document.get("URI"); if(URI != null) {
					 * log.debug("URI is  " + URI);
					 * 
					 * }
					 */
					// Trying caching
					uriFromCache = this.getURIFromCache(doc);
					distance = this.getDistanceForURI(uriFromCache);
					if (uriFromCache != null) {
						log.debug("Result for URI FROM Cache for "
								+ doc + " is " + uriFromCache);
						if(distance != null) {
							log.debug("Distance is " + distance);
						} else {
							log.debug("Distance is null, resetting to a max value here just to get past the code");
							//Not sure how to return null or enable the sort to 'skip' over the items that don't have any recorded distance
							distance = Float.MAX_VALUE;
						}
					} else {
						log.debug("URIFromCache does not exist");
					}

					// Get the value based on the URI from the hash
				} catch (Exception ex) {
					log.error("Error occurred in floatVal of FunctionValues in TestFunctionValueSource", ex);
					//ex.printStackTrace();
				}
				log.debug("Returning distance:" + distance);
				return distance;
			}

			public int intVal(int doc) {
				return (int) floatVal(doc);
			}

			public long longVal(int doc) {
				return (long) floatVal(doc);
			}

			public double doubleVal(int doc) {
				return (double) floatVal(doc);
			}

			public String strVal(int doc) {
				return Float.toString(floatVal(doc));
			}

			public String toString(int doc) {
				return description();
			}

			private String getURIFromCache(int doc) {
				BytesRef result = new BytesRef();
				values.get(doc, result);
				String uri = result.utf8ToString();
				log.debug("getURIFromCache:URI From Cache for " + doc);
				if(uri != null){
					log.debug("getURIFromCache:URI is " + uri);
				} else {
					
				}
				return uri;

			}

			private Float getDistanceForURI(String uri) {
				// if uri is empty or if the uri is not within our set of URIs
				// with associated distance information
				if (StringUtils.isEmpty(uri) || !constants.containsKey(uri)) {
					log.debug("getDistanceForURI: uri is empty or the hash does not contain this particular uri");
					return null;
				}
				log.debug("getDistanceForURI: hash contains URI " + uri);
				return constants.get(uri);
			}
			
			public boolean exists(int doc) {
				log.debug("testfunctionvaluesource exists: " + doc);
				String uriFromCache = this.getURIFromCache(doc);
				//if uri exists
				if(StringUtils.isEmpty(uriFromCache)) {
					log.debug("URI is empty");
					return false;
				}
				//Check if the URI exists in the distance hash
				if(!constants.containsKey(uriFromCache)) {
					log.debug("testfunctionvaluesource exists: hash does not contain URI");
					return false;
				}
				log.debug("testfunctionvaluesource exists is true ");
				return true;
			}
		};

	}

	@Override
	public String description() {
		return "Overriding TestFunctionValueSource description" 
				+ "";
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TestFunctionValueSource))
			return false;
		TestFunctionValueSource other = (TestFunctionValueSource) o;
		return this.constants.equals(other.constants) ;
	}

	@Override
	public int hashCode() {
		//Not sure what to do with hashcodes
		return constants.hashCode();
	}
	
	
}