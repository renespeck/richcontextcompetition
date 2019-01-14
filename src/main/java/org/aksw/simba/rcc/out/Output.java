package org.aksw.simba.rcc.out;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

public abstract class Output {

  final static Logger LOG = LogManager.getLogger(Output.class);

  public final JSONArray data = new JSONArray();

  final String fileName;

  public Output(final String fileName) {
    this.fileName = fileName;
  }

  public void write() {
    try {
      Files.write(Paths.get(fileName), cleanTextContent(data.toString(2)).getBytes());
    } catch (JSONException | IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  private static String cleanTextContent(String text) {
    // strips off all non-ASCII characters
    text = text.replaceAll("[^\\x00-\\x7F]", "");
    // erases all the ASCII control characters
    text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    // removes non-printable characters from Unicode
    text = text.replaceAll("\\p{C}", "");
    return text.trim();
  }
}
