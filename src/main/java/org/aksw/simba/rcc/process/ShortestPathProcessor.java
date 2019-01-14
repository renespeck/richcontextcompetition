package org.aksw.simba.rcc.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.aksw.simba.rcc.Config;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * Uses the training data to find shortest paths between the root and the mentions.
 *
 */
public class ShortestPathProcessor {

  final static Logger LOG = LogManager.getLogger(ShortestPathProcessor.class);

  final ShortestPath shortestPath = new ShortestPath();
  public final static String fileNameOfShortestPaths = "shortestpath";
  public final static String fileNameOfIndexedWords = "indexedWords";


  // data so serialize and to deserialize
  Map<Integer, List<List<IndexedWord>>> datasetIDToPaths = new HashMap<>();

  /**
   *
   */
  public ShortestPathProcessor() {}


  /**
   *
   * TODO: maybe it is faster to check first if the graph contains the words, without checking the
   * edges????
   *
   * Gets a sentence creates the semantic graph , reads the shortest paths for each dataset and
   * tries to match the paths in the graph of the sentence
   *
   *
   * returns dataset id
   *
   */
  public int match(final String sentence) {

    // creates semantic graph
    final SemanticGraph sg = shortestPath.getSemanticGraph(sentence);

    for (final Entry<Integer, List<List<IndexedWord>>> datasetIDToPath : datasetIDToPaths
        .entrySet()) {

      final int datasetid = datasetIDToPath.getKey();

      final List<List<IndexedWord>> shortestPaths = datasetIDToPath.getValue();

      for (final List<IndexedWord> path : shortestPaths) {
        final boolean found = shortestPath.findPath(path, sg);

        if (found) {
          return datasetid;
        }
      }
    }

    return -1;
  }

  /**
   * uses the training data with mentions to find shortest paths and stores the paths for later use.
   *
   * @throws ExecutionException
   * @throws InterruptedException
   *
   */
  public void training(final Map<Integer, Map<String, List<String>>> trainingsdata)
      throws InterruptedException, ExecutionException {

    final int threads = Config.shortestPathProcessor;

    final ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
    forkJoinPool.submit(() -> trainingsdata.keySet().parallelStream().forEach(//
        datasetID -> {

          final String f = fileNameOfIndexedWords + datasetID;
          final String ff = fileNameOfShortestPaths + datasetID;

          final Map<String, List<List<IndexedWord>>> mentionToShortestpaths = new HashMap<>();
          final List<IndexedWord> indexedWords = new ArrayList<>();

          if (!FileUtil.fileExists(SerializationUtil.getRootFolder().concat("/").concat(f))
              || !FileUtil.fileExists(SerializationUtil.getRootFolder().concat("/").concat(ff))) {
            // sentences to mentions
            for (final Entry<String, List<String>> entry : trainingsdata.get(datasetID)
                .entrySet()) {

              final String sentence = entry.getKey();
              final List<String> mentions = entry.getValue();

              final IndexedWord indexedWord = shortestPath.stanfordPipe.getRoot(sentence);
              if (indexedWord != null) {
                indexedWords.add(indexedWord);
              }

              final SemanticGraph sg = shortestPath.getSemanticGraph(sentence);
              if (sg == null) {
                LOG.error("Could not create semantic Graph of sentence: " + sentence);
                continue;
              }

              // for each mention
              for (final String mention : mentions) {
                final String[] splitedMentions = mention.split(" ");
                final String pattern = splitedMentions[splitedMentions.length - 1];
                final List<IndexedWord> path = shortestPath.getShortestPath(sg, pattern);
                if (!path.isEmpty()) {
                  if (mentionToShortestpaths.get(mention) == null) {
                    mentionToShortestpaths.put(mention, new ArrayList<>());
                  }
                  mentionToShortestpaths.get(mention).add(path);
                }
              } // mentions
            } // sentence
          }

          if (!FileUtil.fileExists(SerializationUtil.getRootFolder().concat("/").concat(f))) {
            try {
              SerializationUtil.serialize(f, indexedWords);
            } catch (final Exception e) {
              LOG.error(e.getLocalizedMessage(), e);
            }
          }
          if (!FileUtil.fileExists(SerializationUtil.getRootFolder().concat("/").concat(ff))) {
            try {
              SerializationUtil.serialize(ff, mentionToShortestpaths);
            } catch (final Exception e) {
              LOG.error(e.getLocalizedMessage(), e);
            }
          }
        })).get();
  }
}
