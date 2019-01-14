package org.aksw.simba.rcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.aksw.simba.knowledgeextraction.commons.io.WebAppsUtil;
import org.aksw.simba.knowledgeextraction.commons.lang.MapUtil;
import org.aksw.simba.rcc.analysis.Datasets;
import org.aksw.simba.rcc.out.DataSetCitations;
import org.aksw.simba.rcc.process.ShortestPathProcessor;
import org.aksw.simba.rcc.training.Publications;
import org.aksw.simba.rcc.training.TrainingData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.devtools.common.options.OptionsParser;

import edu.stanford.nlp.ling.IndexedWord;

public class Rcc {
  final static Logger LOG = LogManager.getLogger(Rcc.class);

  /**
   * Parses the arguments, writes shut down file and calls the given command.
   *
   * @param args
   */
  public static void main(final String[] args) {

    final OptionsParser parser = OptionsParser.newOptionsParser(Options.class);
    parser.parseAndExitUponError(args);
    final Options options = parser.getOptions(Options.class);

    if (options.command.isEmpty() || options.input.isEmpty()) {
      printUsage(parser);
      return;
    }

    WebAppsUtil.writeShutDownFile("stop");

    Config.setInput(options.input);

    switch (options.command) {
      case Options.commandTraining:
        LOG.info("training...");
        training();
        break;
      case Options.commandAnalysis:
        LOG.info("analysis...");
        analysis();
        break;
      case Options.commandExecution:
        execution();
        LOG.info("execution...");
        break;
      default:
        printUsage(parser);
    }
  }

  private static void printUsage(final OptionsParser parser) {
    LOG.info("Usage: java -jar rcc.jar OPTIONS");
    LOG.info(parser.describeOptions(Collections.<String, String>emptyMap(),
        OptionsParser.HelpVerbosity.LONG));
  }

  public static void training() {
    final TrainingData trainingData = new TrainingData();

    final ShortestPathProcessor shortestPathProcessor = new ShortestPathProcessor();
    try {
      shortestPathProcessor.training(trainingData.getTrainingData());
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public static void analysis() {

    final Set<Integer> datasetIDs = Datasets.getDatasetIDs();
    LOG.info("Total datasetIDs: " + datasetIDs.size());

    int found = 0;

    final Map<IndexedWord, Integer> freq = new HashMap<>();
    final Map<List<IndexedWord>, Integer> freqSP = new HashMap<>();

    for (final Integer datasetID : datasetIDs) {

      final String f = ShortestPathProcessor.fileNameOfIndexedWords + datasetID;
      final String ff = ShortestPathProcessor.fileNameOfShortestPaths + datasetID;

      List<IndexedWord> indexedWords = null;
      if (FileUtil.fileExists(SerializationUtil.getRootFolder().concat("/").concat(f))) {
        indexedWords = SerializationUtil.deserialize(f, new ArrayList<>().getClass());
      }

      Map<String, List<List<IndexedWord>>> mentionToShortestpaths = null;
      if (FileUtil.fileExists(SerializationUtil.getRootFolder().concat("/").concat(ff))) {
        mentionToShortestpaths = SerializationUtil.deserialize(ff, new HashMap<>().getClass());
      }

      if (indexedWords != null && !indexedWords.isEmpty()) {
        LOG.info("Datasetid: " + datasetID);
        LOG.info("indexedWords: ");
        LOG.info(indexedWords);

        final Set<IndexedWord> w = new TreeSet<>();
        for (final IndexedWord indexedWord : indexedWords) {
          w.add(indexedWord);
          freq.put(indexedWord, freq.get(indexedWord) == null ? 1 : freq.get(indexedWord) + 1);
        }
        LOG.info(w);// sorted set

      }
      if (mentionToShortestpaths != null && !mentionToShortestpaths.isEmpty()) {
        found++;
        LOG.info("Datasetid: " + datasetID);
        LOG.info("mentionToShortestpaths: ");
        mentionToShortestpaths.entrySet().forEach(entry -> {
          for (final List<IndexedWord> e : entry.getValue()) {
            freqSP.put(e, freqSP.get(e) == null ? 1 : freqSP.get(e) + 1);
          }
        });
      }
    }

    final int limit = 100;
    LOG.info("Found sp for dataset: " + found);
    MapUtil.reverseSortByValue(freq).entrySet().stream().limit(limit).forEach(LOG::info);
    LOG.info("--");
    MapUtil.reverseSortByValue(freqSP).entrySet().stream().limit(limit).forEach(LOG::info);
    LOG.info(freq.size());
    LOG.info(freqSP.size());
  }

  // transform data to use in parallel stream. There is a better way to do that?
  private static Map<Integer, String> transformData() {
    final Map<Integer, String> pubToContent = new HashMap<>();
    for (int i = 0; i < Publications.publications.length(); i++) {
      final JSONObject o = Publications.publications.getJSONObject(i);
      final int publication_id = o.getInt("publication_id");
      final String content = Publications.getContent(publication_id);
      pubToContent.put(publication_id, content);
    }
    return pubToContent;
  }

  public static void execution() {

    // output
    final DataSetCitations dataSetCitations = new DataSetCitations();

    // all mentions which linked to one dataset
    final Map<Integer, Set<String>> datasetToMentions = Datasets.getDatasetToMentions();

    // publication ID to publication content
    final Map<Integer, String> pubToContent = transformData();

    final int threads = Config.cores;
    final ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
    try {
      // for each publication
      final AtomicInteger total = new AtomicInteger(pubToContent.entrySet().size());

      forkJoinPool.submit(() -> pubToContent.entrySet().parallelStream().forEach(//
          entry -> {
            final int publication_id = entry.getKey();
            final String content = entry.getValue();

            // for each dataset mention
            for (final Integer data_set_id : datasetToMentions.keySet()) {
              // find mentions
              final Set<String> foundmentions = new HashSet<>();
              for (final String mention : datasetToMentions.get(data_set_id)) {
                try {
                  final Pattern p;
                  p = Pattern.compile("\\b".concat(Pattern.quote(mention.trim())).concat("\\b"));
                  final Matcher matcher = p.matcher(content);
                  if (matcher.find()) {
                    foundmentions.add(mention);
                  }
                } catch (final Exception e) {
                  LOG.error(e.getLocalizedMessage());
                }
              } // end for all mentions

              // Add to output data
              if (!foundmentions.isEmpty()) {
                final double score = 0.9d;
                dataSetCitations.addDataSetCitations(data_set_id, publication_id, score,
                    foundmentions);
              }
            } // end for datasetToMentions

            LOG.info(total.decrementAndGet() + " left.");
          }// end foreach
      )).get();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    dataSetCitations.write();
  }
}
