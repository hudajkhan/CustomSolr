package edu.cornell.mannlib.vitro.webapp.apacheextension.solr.search;


import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.apacheextension.lucene.queries.function.*;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;
/*
 * Based on http://stackoverflow.com/questions/19528841/custom-functionquery-constvaluesource
 * That particular example states it is meant to return a constant value based on the key passed in
 * The example contains a hashmap 'data' which is not declared anywhere in the example, so I constructed my own here
 * At that point, we would probably be connecting to an external database instead
 */
public class TestFunctionValueSourceParser extends ValueSourceParser {
	  public void init(NamedList args) {
	  }

	  public ValueSource parse(FunctionQParser fqp) throws SyntaxError {
		  System.out.println("============>>>TestFubnctionValueSourceParser parse method");
		  //What does this give you? In some examples, we see this passed further on
		  //to the next one
		  //ValueSource vs = fqp.parseValueSource();
		  //System.out.println("Apres Value Source instantiation");
		  //This appears to be where you can actually get params
		  SolrParams parameters = fqp.getParams();
		  System.out.println("apres parameters retrieval");
		  //The example 
		  Map<String, Float> data = this.getDocDistanceInfo();
		 
		 
		  System.out.println("ValueSource parse: Set up hash map " + data.toString());
		  System.out.println("Qk parameter is " + parameters.get("qk"));
		  TestFunctionValueSource tfvs = new TestFunctionValueSource(data, parameters.get("qk"), "qk");
		  if(tfvs == null) {
			  System.out.println("TestFunctionValueSource returned is null");
		  }
		  return tfvs;
	  }
	  
	  private Map<String, Float> getDocDistanceInfo() {
		  Map<String, Float> data = new HashMap<String, Float>();
		  /*
		   *  //URI to example 'distance' from bounding box
		  //With an ascending sort order, you'd expect to see these individuals at the top
		  data.put("http://nyclimateclearinghouse.org/individual/n5639", new Float(1));
		  data.put("http://nyclimateclearinghouse.org/individual/n2148", new Float(2));
		  data.put("http://www.eionet.europa.eu/gemet/concept/1462", new Float(3));
		  data.put("http://nyclimateclearinghouse.org/individual/n4358", new Float(4));
		  data.put("http://nyclimateclearinghouse.org/individual/n7213", new Float(5));
		  
		   */
		  TestDocDistanceInfo distInfo = new TestDocDistanceInfo();
		  data = distInfo.processDocDistanceInfo();
		  System.out.println("Retrieved data in TestFunctionValueSourceParser getDocDistanceInfo");
		  return data;
	  }
	}