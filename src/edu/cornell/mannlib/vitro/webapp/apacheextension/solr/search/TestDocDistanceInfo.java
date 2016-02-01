/*
 *
 *This class represents the call to the PostGres (or other APIs) that need to occur for this
 *particular TestFunctionValueSource/TestFunctionValueSourceParser to work.
 */

package edu.cornell.mannlib.vitro.webapp.apacheextension.solr.search;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Based on http://stackoverflow.com/questions/19528841/custom-functionquery-constvaluesource
 * That particular example states it is meant to return a constant value based on the key passed in
 * The example contains a hashmap 'data' which is not declared anywhere in the example, so I constructed my own here
 * At that point, we would probably be connecting to an external database instead
 */
public class TestDocDistanceInfo {
	private static final Log log = LogFactory.getLog(TestDocDistanceInfo.class);

	
	//Format for API call: http://frontierspatial.com/JanuarySprint/mapper/v1/documents.php?bbox=-75,42,-74,43
	private String apiURLBase = "http://frontierspatial.com/JanuarySprint/mapper/v1/documents.php?";
	private String uriKeyName = "vivo_uri";
	private String rankKeyName = "rank";
	private String lat1 = null;
	private String lon1 = null;
	private String lat2 = null;
	private String lon2 = null;
	//Could potentially pass in particular parameters as need be
	//SW NE -> y1,x1,y2,x2
	public TestDocDistanceInfo(String lat1, String lon1, String lat2, String lon2) {
		this.lat1 = lat1;
		this.lon1 = lon1;
		this.lat2 = lat2;
		this.lon2 = lon2;
	}

	// These will be API calls that will retrieve information from PostGres
	// Return JSON similar to what we expect
	// Read in JSON file and output
	public Map<String, Float> processDocDistanceInfo() {
		Map<String, Float> data = new HashMap<String, Float>();
		String JSON = this.getJSON();
		if (JSON != null) {
			try {
				data = this.processOutput(JSON);
			} catch (Exception ex) {
				log.error("Error occurring in processing JSON to data, processDocDistanceInfo", ex);
				//ex.printStackTrace();
			}
		} else {
			log.info("ProcessDocDistanceInfo: JSON null, returning nothing");
		}
		return data;
	}

	private String getJSON() {

		String results = null;
		//The API expects = BBOX=WSEN i.e. lon1, lat1, lon2, lat2
		String dataUrl = this.apiURLBase + "bbox=" + this.lon1 + "," + this.lat1 + "," + this.lon2 + "," + this.lat2;
		log.debug("Utilizing dataURL: " + dataUrl);
		try {

			StringWriter sw = new StringWriter();
			URL rss = new URL(dataUrl);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					rss.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				sw.write(inputLine);
			}
			in.close();

			results = sw.toString();
			// log.debug("results before processing: "+results);

		} catch (Exception ex) {
			log.error("Exception occurred in retrieving results", ex);
			
			return null;
		}
		return results;

	}

	//As of 4/8/15, returns {"type":"FeatureCollection",
	//						"features":[
	//								{"type":"Feature","geometry":null,
	//								  "properties":
	//									{"distance":"0","vivo_uri":..., "rank_bbox_area_diff":1}
	//								},
	//
	private Map<String, Float> processOutput(String results) throws Exception {

		Map<String, Float> docToRank = new HashMap<String, Float>();

		try {
			//
			JSONObject jsonResult = (JSONObject) JSONSerializer.toJSON(results);
			
			JSONArray featuresArray = jsonResult.getJSONArray("features");
			
			log.debug("Results to string " + featuresArray.toString());
			int size = featuresArray.size();
			log.debug("JSONArray size is " + size);

			int i;
			for (i = 0; i < size; i++) {
				JSONObject o = featuresArray.getJSONObject(i);
				JSONObject properties = o.getJSONObject("properties");
				if (properties.has(uriKeyName) && 
						StringUtils.isNotEmpty(properties.getString(uriKeyName)) &&
						properties.has(rankKeyName)) {
					// TODO: Check if this works or not
					String URI = properties.getString(uriKeyName);
					Float rank = new Float(properties.getInt(rankKeyName));
					//distance will always return as 0 since that translates to whether or not it overlaps with the
					//given bounding box
					//rank is determined by difference in area of the item and the bounding box
					docToRank.put(URI, rank);
					log.debug("Putting in rank for " + URI + " = "
							+ rank);
				}
			}

		} catch (Exception ex) {
			log.error("Error message in converting JSON to hashmap", ex);
			
			throw ex;
		}

		//
		return docToRank;

	}

}