package org.aksw.simba.rcc.training;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.aksw.simba.knowledgeextraction.commons.cache.InMemoryCache;
import org.aksw.simba.rcc.Config;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * example <code>
[
{
  "publication_id": 143,
  "unique_identifier": "bbk-42",
  "title": "The determinants of service imports: The role of cost pressure and financial constraints",
  "pub_date": "1969-01-01",
  "pdf_file_name": "143.pdf",
  "text_file_name": "143.txt"
},
...
] </code>
 *
 */
public class Publications {

  public final static Logger LOG = LogManager.getLogger(Publications.class);

  private static final int timerInterval = 10 * 60 * 1 * 1000; // 10min
  protected static InMemoryCache<String, String> cache = new InMemoryCache<>(//
      Long.MAX_VALUE, timerInterval);


  protected static String publicationsFile = Config.publications;

  public static JSONArray publications = new JSONArray();

  static {
    try {
      publications = new JSONArray(//
          new String(Files.readAllBytes(Paths.get(publicationsFile)))//
      );
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  public static void main(final String[] a) {
    final Publications input = new Publications();
    input.hashCode();
  }


  public static String getTitle(final JSONObject o) {
    return o.getString("title");
  }

  public static int getUniqueID(final JSONObject o) {
    return o.getInt("unique_identifier");
  }

  public static int getPublicationID(final JSONObject o) {
    return o.getInt("publication_id");
  }

  public static String getContent(final int o) {
    return read(o + ".txt");
  }

  public static String getContent(final JSONObject o) {
    return read(o.getString("text_file_name"));
  }

  private static String read(final String path) {
    String content = cache.get(path);
    if (content == null) {
      try {
        content = new String(Files.readAllBytes(Paths.get(//
            Config.getInput().concat("/files/text/").concat(path))));

        cache.put(path, content);

      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
        content = "";
      }
    }
    return content;
  }
}
