package org.aksw.simba.rcc;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

/**
 * Command-line options for example server.
 */
public class Options extends OptionsBase {

  public final static String commandTraining = "training";
  public final static String commandExecution = "execution";
  public final static String commandAnalysis = "analysis";


  @Option(name = "help", abbrev = 'h', help = "Prints usage info.", defaultValue = "true")
  public boolean help;

  @Option(
      name = "command", abbrev = 'c', help = "The command to execute (\"" + commandTraining
          + "\" \"" + commandAnalysis + "\" or \"" + commandExecution + "\").",
      category = "startup", defaultValue = "")
  public String command;

  @Option(name = "input", abbrev = 'i', help = "input folder of the challenge",
      category = "startup", defaultValue = "")
  public String input;
}
