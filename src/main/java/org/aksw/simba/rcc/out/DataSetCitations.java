package org.aksw.simba.rcc.out;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataSetCitations extends Output {

  int citationID = 1;

  public DataSetCitations() {
    this("data_set_citations.json");
  }

  public DataSetCitations(final String fileName) {
    super(fileName);
  }

  public void addDataSetCitations(final int data_set_id, final int publication_id,
      final double score, final Set<String> mention_list) {
    final JSONArray a = new JSONArray();
    for (final String m : mention_list) {
      a.put(m);
    }
    addDataSetCitations(data_set_id, publication_id, score, a);
  }

  public void addDataSetCitations(final int data_set_id, final int publication_id,
      final double score, final String... mention_list) {
    final JSONArray a = new JSONArray();
    for (final String m : mention_list) {
      a.put(m);
    }
    addDataSetCitations(data_set_id, publication_id, score, a);
  }

  public void addDataSetCitations(final int data_set_id, final int publication_id,
      final double score, final JSONArray mention_list) {

    data.put(new JSONObject()//
        .put("citation_id", citationID++)//
        .put("data_set_id", data_set_id) //
        .put("publication_id", publication_id) //
        .put("mention_list", mention_list) //
        .put("score", score) //
    );
  }
}
