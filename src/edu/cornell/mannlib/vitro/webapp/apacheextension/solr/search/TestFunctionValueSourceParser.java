package edu.cornell.mannlib.vitro.webapp.apacheextension.solr.search;


import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.apacheextension.lucene.queries.function.*;

import org.apache.commons.lang.StringUtils;
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
		  //Get lat1, lon1, lat2, lon2
		  String lat1 = parameters.get("lat1");
		  String lon1 = parameters.get("lon1");
		  String lat2 = parameters.get("lat2");
		  String lon2 = parameters.get("lon2");
		  //If any of these are null, return ull
		  if(StringUtils.isEmpty(lat1) || StringUtils.isEmpty(lon1) || StringUtils.isEmpty(lat2) || StringUtils.isEmpty(lon2)) {
			  System.out.println("One of the paramters was empty and so returning null");
			  return null;
		  }
		  System.out.println("apres parameters retrieval: " + lat1 + ", " + lon1 + " - " + lat2 + "," + lon2);

		  //The example 
		  Map<String, Float> data = this.getDocDistanceInfo(lat1, lon1, lat2, lon2);
		 
		 
		  System.out.println("ValueSource parse: Set up hash map " + data.toString());
		
		  TestFunctionValueSource tfvs = new TestFunctionValueSource(data);
		  if(tfvs == null) {
			  System.out.println("TestFunctionValueSource returned is null");
		  }
		  return tfvs;
	  }
	  
	  private Map<String, Float> getDocDistanceInfo(String lat1, String lon1, String lat2, String lon2) {
		  Map<String, Float> data = new HashMap<String, Float>();
		  
		  TestDocDistanceInfo distInfo = new TestDocDistanceInfo(lat1, lon1, lat2, lon2);
		  data = distInfo.processDocDistanceInfo();
		  System.out.println("Retrieved data in TestFunctionValueSourceParser getDocDistanceInfo");
		  return data;
	  }
	}