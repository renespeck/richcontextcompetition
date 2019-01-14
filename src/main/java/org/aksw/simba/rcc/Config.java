package org.aksw.simba.rcc;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Config {

  final static Logger LOG = LogManager.getLogger(Config.class);

  private static String input = "/home/rspeck/Data/data/input";

  public static String data_set_citations = input.concat("/data_set_citations.json");
  public static String publications = input.concat("/publications.json");

  public static String getInput() {
    return input;
  }

  public static void setInput(final String f) {
    input = f;
    data_set_citations = input.concat("/data_set_citations.json");
    publications = input.concat("/publications.json");
  }

  public static String datasetsFiles = "data_sets.json";

  public static int sentenceMinTokenSize = 4;
  public static int sentenceMaxTokenSize = 55;

  public static int stanfordThreads = 4;

  // public static int maxThreads = ThreadInfo.getFreeCount();
  final static int cores = Runtime.getRuntime().availableProcessors();

  @Deprecated
  public static int verbFreq = cores / 4;
  public static int shortestPathProcessor = cores / 4 < 1 ? 1 : cores / 4;

  static {
    LOG.info("cores: ".concat(String.valueOf(cores)));
    LOG.info("shortestPathProcessor: ".concat(String.valueOf(shortestPathProcessor)));
  }

  // DEBUG
  public static int limit = Integer.MAX_VALUE;
}
