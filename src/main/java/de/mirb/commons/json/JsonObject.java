package de.mirb.pg.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by michael on 04.12.16.
 */
public class JsonObject extends JsonValue<List<JsonPair>> {
  private List<JsonPair> values;

  private JsonObject(List<JsonPair> jsonPairs) {
    super(jsonPairs);
    values = jsonPairs;
  }

  public static JsonObjectBuilder with(JsonPair ... jp) {
    JsonObjectBuilder b = new JsonObjectBuilder();
    for (JsonPair jsonPair : jp) {
      b.add(jsonPair);
    }
    return b;
  }

  public static JsonObjectBuilder start() {
    return new JsonObjectBuilder();
  }

  @Override
  public List<JsonPair> value() {
    return Collections.unmodifiableList(values);
  }

  public static class JsonObjectBuilder {
    private List<JsonPair> jsonPairs = new ArrayList<>();

    public JsonObjectBuilder add(JsonPair jp) {
      jsonPairs.add(jp);
      return this;
    }

    public JsonObject build() {
      return new JsonObject(jsonPairs);
    }
  }
}
