package de.mirb.pg.pojo;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by michael on 01.12.16.
 */
public class JsonTokenizerTest {

  private JsonTokenizer jt = new JsonTokenizer();

//  public JsonTokenizerTest() {
//    jt = new JsonTokenizer();
//  }

  @Test
  public void emptyJson() throws JsonTokenizerException {
    List<JsonToken> result = jt.tokenize("{ }");

    Assert.assertEquals(2, result.size());
  }

  @Test
  public void jsonValue() throws JsonTokenizerException {
    List<JsonToken> result = jt.tokenize("\"Name\": \"Value\"");
    Assert.assertEquals(3, result.size());
    result = jt.tokenize("\"Name\": 42");
    Assert.assertEquals(3, result.size());
  }

  @Test
  public void jsonArray() throws JsonTokenizerException {
    List<JsonToken> result = jt.tokenize("[ \"Name\", \"Value\"]");
    Assert.assertEquals(5, result.size());
    result = jt.tokenize("[ 42, 4711 ] ");
    Assert.assertEquals(5, result.size());
  }

  @Test
  public void basicJson() throws JsonTokenizerException {
    List<JsonToken> result = jt.tokenize("{ \"Sample\" : \"Value\"}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : \"Value\", \"Sample2\" : \"Value2\"}");
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void basicNumbers() throws JsonTokenizerException {
    List<JsonToken> result;

    result = jt.tokenize("{ \"Sample\" : 4711}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : -4711}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : -47.11}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : 4711, \"Sample2\" : 1903}");
    Assert.assertEquals(9, result.size());

    result = jt.tokenize("{ \"Sample\" : 4711,\"Sample2\" : 1903  }");
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void expNumbers() throws JsonTokenizerException {
    List<JsonToken> result;

    result = jt.tokenize("{ \"Sample\" : 4711e1}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : -4711E1}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : -47.11e-2}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : 4711E+2, \"Sample2\" : 1903E-19}");
    Assert.assertEquals(9, result.size());

    result = jt.tokenize("{ \"Sample\" : 4711,\"Sample2\" : 1903E99  }");
    Assert.assertEquals(result.toString(), 9, result.size());
  }

  @Test
  public void fracNumbers() throws JsonTokenizerException {
    List<JsonToken> result;

    result = jt.tokenize("{ \"Sample\" : 47.11}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : -47.11}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : -4.711, \"Sample2\" : 190.3}");
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void jsonNull() throws JsonTokenizerException {
    List<JsonToken> result;

    result = jt.tokenize("{ \"Sample\" : NULL}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : null}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : NULL, \"Sample2\" : null}");
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void jsonFalse() throws JsonTokenizerException {
    List<JsonToken> result;

    result = jt.tokenize("{ \"Sample\" : FALSE}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : false}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : False, \"Sample2\" : fALse}");
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void jsonTrue() throws JsonTokenizerException {
    List<JsonToken> result;

    result = jt.tokenize("{ \"Sample\" : TRUE}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : true}");
    Assert.assertEquals(5, result.size());

    result = jt.tokenize("{ \"Sample\" : True, \"Sample2\" : trUE}");
    Assert.assertEquals(9, result.size());
  }


  @Test
  public void failNumbers() throws JsonTokenizerException {
    Assert.assertTrue(fail("{ \"Sample\" : 12-4711}"));
    Assert.assertTrue(fail("{ \"Sample\" : 12.}"));
    Assert.assertTrue(fail("{ \"Sample\" : 12.e}"));
    Assert.assertTrue(fail("{ \"Sample\" : 12.-12}"));
    Assert.assertTrue(fail("{ \"Sample\" : 12e}"));
  }

  private boolean fail(String json) {
    try {
      jt.tokenize("{ \"Sample\" : 12-4711}");
    } catch (JsonTokenizerException e) {
      return true;
    }
    return false;
  }

  @Test
  public void basicArray() throws JsonTokenizerException {
    List<JsonToken> result = jt.tokenize("{ \"Sample\" : [ 4711 ] }");
    System.out.println(result.toString());
    Assert.assertEquals(7, result.size());

    result = jt.tokenize("{ \"Sample\" : 4711, \"Sample2\" : [1903]}");
    Assert.assertEquals(11, result.size());

    result = jt.tokenize("{ \"Array\": [ \"Sample\",\"Sample2\" ] }");
    Assert.assertEquals(9, result.size());
  }

  @Test
  public void withInnerObject() throws JsonTokenizerException {
    List<JsonToken> result = jt.tokenize("{ \"Sample\" : \"Value\", \"InnerJson\": { \"Sample\" : \"Value\", "
        + "\"Sample2\" : \"Value2\"}}");
    Assert.assertEquals(17, result.size());
  }

}