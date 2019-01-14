package org.aksw.simba.rcc.training;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.rcc.Config;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * data_set_citations.json
 *
 *
 * this file for training only, not part in the challenge run.
 *
 */
public class DataSetCitations {

  protected final static Logger LOG = LogManager.getLogger(DataSetCitations.class);

  private final static String data_set_citations = Config.data_set_citations;

  /**
   * Dataset IDs to the list of citations
   */
  private static Map<Integer, List<JSONObject>> idToDatasetCitations = null;

  /**
   * All citations for a dataset id given in the training data {@link #data_set_citations}.
   *
   * @return dataset IDs to citations
   */
  public static Map<Integer, List<JSONObject>> getDatasetCitations() {
    if (idToDatasetCitations == null) {
      idToDatasetCitations = new HashMap<>();
      init();
    }
    return idToDatasetCitations;
  }

  private static void init() {
    try {
      final JSONArray datasets = new JSONArray(//
          new String(Files.readAllBytes(Paths.get(data_set_citations)))//
      );

      for (int i = 0; i < datasets.length(); i++) {
        final JSONObject o = datasets.getJSONObject(i);
        final int data_set_id = o.getInt("data_set_id");

        if (!idToDatasetCitations.containsKey(data_set_id)) {
          idToDatasetCitations.put(data_set_id, new ArrayList<>());
        }
        idToDatasetCitations.get(data_set_id).add(o);
      }
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    LOG.info("Number of datasets: " + idToDatasetCitations.size()); // 1028
  }
}
