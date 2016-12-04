package de.mirb.pg.pojo;

/**
 * Created by mibo.
 */
public class JsonPair {
  private String name;
  private JsonValue value;

  private JsonPair(String name, JsonValue value) {
    this.name = name;
    this.value = value;
  }

  public static JsonPairBuilder with(String name) {
    return new JsonPairBuilder(name);
  }

  public static JsonPairBuilder create() {
    return new JsonPairBuilder();
  }

  public String getName() {
    return name;
  }

  public JsonValue getValue() {
    return value;
  }

  static class JsonPairBuilder {
    private String name;
    private JsonValue value;

    public JsonPairBuilder(String name) {
      this.name = name;
    }

    public JsonPairBuilder() {

    }

    public JsonPairBuilder name(String name) {
      this.name = name;
      return this;
    }

    public JsonPairBuilder value(JsonValue v) {
      value = v;
      return this;
    }

    public JsonPair build() {
      return new JsonPair(name, value);
    }
  }
}
