package de.mirb.pg.pojo;

/**
 * Created by mibo
 */
public abstract class JsonValue<T> {
  protected final T value;

  protected JsonValue(T value) {
    this.value = value;
  }

  public abstract T value();

  public String valueAsString() {
    if(value == null) {
      return "Invalid null value";
    }
    return value.toString();
  }

  public JsonObject asObject() {
    return (JsonObject) this;
  }
}
