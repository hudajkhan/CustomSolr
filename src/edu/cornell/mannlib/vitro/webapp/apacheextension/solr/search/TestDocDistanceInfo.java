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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.apacheextension.lucene.queries.function.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

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
public class TestDocDistanceInfo {
	// The API base - we will read from a file available on the server
	// This will include the JSON we expect to get from the actual API
	private String apiURLBase = "http://localhost:8080/nyccscvivo/TestData.json";
	//Could potentially pass in particular parameters as need be
	public TestDocDistanceInfo() {

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
				System.out
						.println("Error occurring in processing JSON to data, processDocDistanceInfo");
				ex.printStackTrace();
			}
		} else {
			System.out
					.println("ProcessDocDistanceInfo: Error, JSON null, returning nothing");
		}
		return data;
	}

	private String getJSON() {

		String results = null;
		String dataUrl = this.apiURLBase;

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
			// System.out.println("results before processing: "+results);

		} catch (Exception ex) {
			System.out.println("Exception occurred in retrieving results");
			ex.printStackTrace();
			return null;
		}
		return results;

	}

	private Map<String, Float> processOutput(String results) throws Exception {

		Map<String, Float> docToDistance = new HashMap<String, Float>();

		try {
			JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(results);
			System.out.println("Results to string " + jsonArray.toString());
			int size = jsonArray.size();
			System.out.println("JSONArray size is " + size);

			int i;
			for (i = 0; i < size; i++) {
				JSONObject o = jsonArray.getJSONObject(i);
				if (o.has("URI") && o.has("distance")) {
					// TODO: Check if this works or not
					String URI = o.getString("URI");
					Float distance = new Float(o.getInt("distance"));
					docToDistance.put(URI, distance);
					System.out.println("Putting in distance for " + URI + " = "
							+ distance);
				}
			}

		} catch (Exception ex) {
			System.out.println("Error message in converting JSON to hashmap");
			ex.printStackTrace();

			throw ex;
		}

		//
		return docToDistance;

	}

}