package org.aksw.simba.rcc.process;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;
import org.aksw.simba.rcc.process.tools.StanfordForRCC;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class ShortestPath {

  final static Logger LOG = LogManager.getLogger(ShortestPath.class);

  public StanfordPipeExtended stanfordPipe =
      StanfordForRCC.getStanfordPipe(StanfordForRCC.getPropertiesParse());

  /**
   *
   * @param sentence
   * @return
   */
  public SemanticGraph getSemanticGraph(final String sentence) {
    return stanfordPipe.getSemanticGraph(sentence);
  }

  /**
   *
   * @param sentence
   * @param pattern a word or regex in the sentence
   * @return
   */
  public List<IndexedWord> getShortestPath(final SemanticGraph sg, final String pattern) {

    final IndexedWord source = stanfordPipe.getRoot(sg);
    final List<IndexedWord> words = sg.getAllNodesByWordPattern(pattern);
    List<IndexedWord> shortestPath = new ArrayList<>();
    if (words != null && words.size() > 0) {
      // TODO: loop over all words and find the longest paths?
      shortestPath = getShortestUndirectedPathNodes(sg, source, words.get(0));
    }
    return shortestPath;
  }

  /**
   * Checks if the given shortest path is in the semantic graph.
   *
   * @param shortestPath
   * @param semanticGraph
   * @return true in case the path is in the graph
   */
  public boolean findPath(final List<IndexedWord> shortestPath, final SemanticGraph semanticGraph) {
    Boolean hasChildren = null;
    IndexedWord last = null;
    for (int i = 0; i < shortestPath.size(); i++) {
      final IndexedWord word = shortestPath.get(i);

      if (hasChildren == null) {
        hasChildren = semanticGraph.hasChildren(word);
        last = word;
      } else if (hasChildren) {
        // 2nd word
        final SemanticGraphEdge e = semanticGraph.getEdge(last, word);
        if (e == null) {
          LOG.info("not found");
          break;
        } else {
          last = word;
        }
      }
      if (i + 1 == shortestPath.size()) {
        LOG.info("match");
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the shortest path from source to the first root in the graph to target.
   *
   * @param sg
   * @param source
   * @param target
   * @return
   */
  public static List<IndexedWord> getShortestUndirectedPathNodes(//
      final SemanticGraph sg, final IndexedWord source, final IndexedWord target) {
    final List<IndexedWord> shortestPath = new ArrayList<>();
    List<IndexedWord> list = null;
    list = sg.getShortestUndirectedPathNodes(source, target);
    if (list != null && !list.isEmpty()) {
      shortestPath.addAll(list);
    }
    // shortestPath.remove(source);
    // cshortestPath.remove(target);
    return shortestPath;
  }
}
