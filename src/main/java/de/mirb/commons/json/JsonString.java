package de.mirb.pg.pojo;

/**
 * Created by mibo
 */
public class JsonString extends JsonValue {
  public JsonString(String value) {
    super(value);
  }

  @Override
  public String value() {
    return (String) value;
  }
}
