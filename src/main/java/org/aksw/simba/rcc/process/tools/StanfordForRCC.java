package org.aksw.simba.rcc.process.tools;

import java.util.Properties;

import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;
import org.aksw.simba.rcc.Config;

public class StanfordForRCC {

  private static int threadsUsed = Config.stanfordThreads;// ThreadInfo.getFreeCount();

  public static StanfordPipeExtended getStanfordPipe(final Properties properties) {
    return StanfordPipeExtended.newInstance(properties);
  }

  public static Properties getPropertiesSsplit() {
    final Properties properties = new Properties();
    properties.setProperty("annotators", "tokenize, ssplit");
    properties.setProperty("tokenize.language", "en");

    if (threadsUsed > 0) {
      properties.put("threads", String.valueOf(threadsUsed));
    }

    return properties;
  }

  public static Properties getPropertiesParse() {
    final Properties properties = new Properties();

    properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
    // props.setProperty("ner.applyNumericClassifiers", "false");
    // props.setProperty("ner.useSUTime", "false");
    properties.setProperty("ner.model",
        "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
    properties.setProperty("tokenize.language", "en");

    if (threadsUsed > 0) {
      properties.put("threads", String.valueOf(threadsUsed));
    }
    return properties;
  }
}
