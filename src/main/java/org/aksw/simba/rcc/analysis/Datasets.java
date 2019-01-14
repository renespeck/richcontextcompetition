package org.aksw.simba.rcc.analysis;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.aksw.simba.rcc.Config;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * data_sets.json
 *
 * This is a dictionary for the whole challenge.
 */
public class Datasets {

  final static Logger LOG = LogManager.getLogger(Datasets.class);

  protected static String datasetsFiles = Config.datasetsFiles;

  protected static Map<Integer, JSONObject> idToDataset = new HashMap<>();

  static {
    try {

      final JSONArray datasets = FileUtil.readToJSONArray(new File("data_sets.json"));
      for (int i = 0; i < datasets.length(); i++) {
        final JSONObject o = datasets.getJSONObject(i);
        final int id = o.getInt("data_set_id");

        if (idToDataset.containsKey(id)) {
          throw new UnsupportedOperationException(
              "data_set_id already used. please check your data.");
        }
        idToDataset.put(id, o);
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  public static Set<Integer> getDatasetIDs() {
    return idToDataset.keySet();
  }

  public static Map<Integer, Set<String>> getDatasetToMentions() {
    final Map<Integer, Set<String>> mm = new HashMap<>();
    for (final Entry<String, Integer> entry : getMentionsToDatasets().entrySet()) {
      if (mm.get(entry.getValue()) == null) {
        mm.put(entry.getValue(), new HashSet<>());
      }
      mm.get(entry.getValue()).add(entry.getKey());
    }
    return mm;
  }

  /**
   * Gets all mentions which linked to one dataset.
   *
   * @return a map with mention to dataset id
   */
  public static Map<String, Integer> getMentionsToDatasets() {
    final Map<String, Integer> mentionToDataIDs = new HashMap<>();
    for (final JSONObject o : idToDataset.values()) {
      final JSONArray mentions = o.getJSONArray("mention_list");
      final Integer datasetID = o.getInt("data_set_id");
      if (mentions.length() > 0) {

        for (int i = 0; i < mentions.length(); i++) {
          final String m = mentions.getString(i);
          if (mentionToDataIDs.get(m) == null) {
            mentionToDataIDs.put(m, datasetID);
          } else {
            mentionToDataIDs.put(m, -1);
          }
        }
      }
    }
    final Iterator<Entry<String, Integer>> iter = mentionToDataIDs.entrySet().iterator();
    while (iter.hasNext()) {
      final Entry<String, Integer> e = iter.next();

      if (e.getValue().equals(new Integer(-1))) {
        iter.remove();
      }
    }
    return mentionToDataIDs;
  }

  static {

    final Map<String, Set<Integer>> mentionToDataIDs = new HashMap<>();
    int counter = 0;
    for (final JSONObject o : idToDataset.values()) {
      final JSONArray mentions = o.getJSONArray("mention_list");
      final Integer datasetID = o.getInt("data_set_id");
      if (mentions.length() > 0) {
        counter++;

        for (int i = 0; i < mentions.length(); i++) {
          final String m = mentions.getString(i);
          if (mentionToDataIDs.get(m) == null) {
            mentionToDataIDs.put(m, new HashSet<>());
          }
          mentionToDataIDs.get(m).add(datasetID);
        }
      }
    }
    LOG.info("Datasets with menthions: " + counter); // 1049
    LOG.info("Total number of datasets: " + idToDataset.size()); // 10348
    LOG.info("Mentions to datasts");

    mentionToDataIDs.entrySet().stream().sorted(//
        (o1, o2) -> o2.getValue().size() - o1.getValue().size()).limit(100).forEach(LOG::info);

    final Map<String, Integer> getMentionsToDatasets = getMentionsToDatasets();
    LOG.info(getMentionsToDatasets.size());
    getMentionsToDatasets.entrySet().stream().limit(10).forEach(LOG::info);
  }
}
