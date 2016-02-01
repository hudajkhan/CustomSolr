package edu.cornell.mannlib.vitro.webapp.apacheextension.solr.search;


import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.apacheextension.lucene.queries.function.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static final Log log = LogFactory.getLog(TestFunctionValueSourceParser.class);

	  public void init(NamedList args) {
	  }

	  public ValueSource parse(FunctionQParser fqp) throws SyntaxError {
		  log.debug("============>>>TestFubnctionValueSourceParser parse method");
		  //What does this give you? In some examples, we see this passed further on
		  //to the next one
		  //ValueSource vs = fqp.parseValueSource();
		  //log.debug("Apres Value Source instantiation");
		  //This appears to be where you can actually get params
		  SolrParams parameters = fqp.getParams();
		  //Method getParams did not return an array even with a comma delimited string
		  String bbox = parameters.get("bbox");
		  if(StringUtils.isEmpty(bbox) || !bbox.contains(",")) {
			  log.debug("Error: Bounding box paramter does not exist or does not contain comma, returning null");
			  return null;
		  }
		  String[] bboxCoords = bbox.split(",");
		  //Expect four parameters
		  if(bboxCoords.length < 4) {
			  log.debug("Insufficient number of bounding box parameters = ");
			  return null;
		  }
		  //WSEN = i.e., lon1, lat1, lon2, lat2
		  //Get lat1, lon1, lat2, lon2
		  String lon1 = bboxCoords[0];
		  String lat1 = bboxCoords[1];
		  String lon2 = bboxCoords[2];
		  String lat2 = bboxCoords[3];
		  //If any of these are null, return ull
		  if(StringUtils.isEmpty(lat1) || StringUtils.isEmpty(lon1) || StringUtils.isEmpty(lat2) || StringUtils.isEmpty(lon2)) {
			  log.debug("One of the paramters was empty and so returning null");
			  return null;
		  }
		  log.debug("apres parameters retrieval: lat1,lon1 - lat2,lon2= " + lat1 + ", " + lon1 + " - " + lat2 + "," + lon2);

		  //The example 
		  Map<String, Float> data = this.getDocDistanceInfo(lat1, lon1, lat2, lon2);
		 
		 
		  log.debug("ValueSource parse: Set up hash map " + data.toString());
		
		  TestFunctionValueSource tfvs = new TestFunctionValueSource(data);
		  if(tfvs == null) {
			  log.debug("TestFunctionValueSource returned is null");
		  }
		  return tfvs;
	  }
	  
	  //SW NE -> y1,x1, y2,x2
	  private Map<String, Float> getDocDistanceInfo(String lat1, String lon1, String lat2, String lon2) {
		  Map<String, Float> data = new HashMap<String, Float>();
		  
		  TestDocDistanceInfo distInfo = new TestDocDistanceInfo(lat1, lon1, lat2, lon2);
		  data = distInfo.processDocDistanceInfo();
		  log.debug("Retrieved data in TestFunctionValueSourceParser getDocDistanceInfo");
		  return data;
	  }
	}