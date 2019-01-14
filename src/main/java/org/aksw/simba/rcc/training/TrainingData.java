package org.aksw.simba.rcc.training;

import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;
import org.aksw.simba.rcc.Config;
import org.aksw.simba.rcc.process.tools.StanfordForRCC;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.stanford.nlp.util.CoreMap;

/**
 * Reads data and stores data_set_id to sentences and mentions to each sentence
 *
 */
public class TrainingData {

  final static Logger LOG = LogManager.getLogger(TrainingData.class);

  public final static String fileName = "store";

  /**
   * data_set_id to sentences and mentions to each sentence
   */
  protected Map<Integer, Map<String, List<String>>> datasetsToSentences = null;

  @SuppressWarnings("unchecked")
  public TrainingData() {

    datasetsToSentences = SerializationUtil.deserialize(fileName, HashMap.class);
    if (datasetsToSentences == null) {
      preprocessToSentences();
      serialize();
    }
    statistiscs(datasetsToSentences);
  }

  public Map<Integer, Map<String, List<String>>> getTrainingData() {
    return datasetsToSentences;
  }

  public Map<String, List<String>> getSentences(final Integer datasetID) {
    return datasetsToSentences.get(datasetID);
  }

  protected void statistiscs(final Map<Integer, Map<String, List<String>>> store) {
    LOG.info("File loaded.");

    LOG.info("dataset size: " + store.size());

    int sen = 0;
    for (final Integer datasetID : store.keySet()) {
      sen += store.get(datasetID).keySet().size();
    }
    LOG.info("Total sentences: " + sen);
  }

  /**
   * Initialized the {@link #datasetsToSentences}. First reads dataset citations file and for each
   * dataset
   */
  protected void preprocessToSentences() {

    final StanfordPipe stanfordPipe =
        StanfordForRCC.getStanfordPipe(StanfordForRCC.getPropertiesSsplit());

    datasetsToSentences = new HashMap<>();

    // reads dataset citations file
    for (final Integer data_set_id : DataSetCitations.getDatasetCitations().keySet()) {

      datasetsToSentences.put(data_set_id, new HashMap<>());

      // for each citations collect all mentions to datasets
      for (final JSONObject citation : DataSetCitations.getDatasetCitations().get(data_set_id)) {

        // read mentions
        final List<String> mentionList = new ArrayList<>();
        final JSONArray m = citation.getJSONArray("mention_list");
        for (int i = 0; i < m.length(); i++) {
          mentionList.add(m.getString(i));
        }
        if (mentionList.isEmpty()) {
          LOG.info("mentions empty!");
          continue;
        }

        // reads file
        final String content = Publications.getContent(citation.getInt("publication_id"))//
            .replace("\r\n", " ").replace("\n", " ");

        // find sentences
        final List<CoreMap> coreMaps = stanfordPipe.getSentences(content);

        // removes too long and too short sentences
        {
          final Iterator<CoreMap> iter = coreMaps.iterator();
          while (iter.hasNext()) {
            final CoreMap element = iter.next();
            final int size = element.size();
            if (size < Config.sentenceMinTokenSize || size > Config.sentenceMaxTokenSize) {
              LOG.info("removes: " + element);
              iter.remove();
            }
          }
        }

        // cast sentences to strings
        final List<String> sentences = coreMaps.stream()//
            .map(CoreMap::toString).collect(Collectors.toList());

        // find sentences with mentions
        for (final String sentence : sentences) {
          final Map<String, List<String>> sentenceToMentions = datasetsToSentences.get(data_set_id);

          for (final String mention : mentionList) {
            try {
              final Pattern p = Pattern.compile(//
                  "\\b".concat(Pattern.quote(mention.trim())).concat("\\b"));
              final Matcher matcher = p.matcher(sentence);
              if (matcher.find()) {
                if (sentenceToMentions.get(sentence) == null) {
                  sentenceToMentions.put(sentence, new ArrayList<>());
                }
                sentenceToMentions.get(sentence).add(mention);
              }
            } catch (final Exception e) {
              LOG.error(e.getLocalizedMessage());
            }
          } // mentions
        } // sentences

      } // citations
    } // datasetcitations
  }

  private void serialize() {
    if (datasetsToSentences.size() > 0) {
      try {
        SerializationUtil.serialize(fileName, datasetsToSentences);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    } else {
      LOG.warn("Nothing to serialize in: " + fileName);
    }
  }
}
