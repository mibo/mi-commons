package de.mirb.pg.pojo;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by mibo.
 */
public class JsonParserTest {

  @Test
  public void basic() throws JsonParserException {
    JsonParser jp = new JsonParser();
    JsonValue value = jp.parse("{ \"name\": \"value\" }");

    JsonObject object = value.asObject();
    List<JsonPair> pair = object.value();
    Assert.assertEquals(1, pair.size());
    Assert.assertEquals("name", pair.get(0).getName());
    Assert.assertEquals("value", pair.get(0).getValue().valueAsString());
  }

  @Test
  public void basicTwoValues() throws JsonParserException {
    JsonParser jp = new JsonParser();
    JsonValue value = jp.parse("{ \"name\": \"value\", \"name2\": \"value2\" }");

    JsonObject object = value.asObject();
    List<JsonPair> pair = object.value();
    Assert.assertEquals(2, pair.size());
    Assert.assertEquals("name", pair.get(0).getName());
    Assert.assertEquals("value", pair.get(0).getValue().valueAsString());
    Assert.assertEquals("name2", pair.get(1).getName());
    Assert.assertEquals("value2", pair.get(1).getValue().valueAsString());
  }

  @Test
  public void basicObjectValues() throws JsonParserException {
    JsonParser jp = new JsonParser();
    JsonValue value = jp.parse("{ \"object\" : { \"name\": \"value\", \"name2\": \"value2\" } }");

    JsonObject object = value.asObject();
    List<JsonPair> pair = object.value();
    Assert.assertEquals(1, pair.size());
    JsonPair innerPair = pair.get(0);
    Assert.assertEquals("object", innerPair.getName());
    object = innerPair.getValue().asObject();
    pair = object.value();
    Assert.assertEquals("name", pair.get(0).getName());
    Assert.assertEquals("value", pair.get(0).getValue().valueAsString());
    Assert.assertEquals("name2", pair.get(1).getName());
    Assert.assertEquals("value2", pair.get(1).getValue().valueAsString());
  }

}