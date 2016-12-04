package de.mirb.pg.pojo;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by mibo
 */
public class JsonParser {
  private class TokenList {
    final Iterator<JsonToken> tokens;

    TokenList(String json) throws JsonTokenizerException {
      JsonTokenizer jt = new JsonTokenizer();
      tokens = jt.tokenize(json).iterator();
    }

    boolean hasNext() {
      return tokens.hasNext();
    }

    JsonToken next() {
      return tokens.next();
    }


    JsonToken grant(JsonToken.Token token) throws JsonParserException {
      JsonToken t = next();
      if(t.getToken() == token) {
        return t;
      }
      throw new JsonParserException("Required token was not found (req: " + token + "; found: " + t + ")");
    }

    JsonToken some(JsonToken.Token... token) throws JsonParserException {
      JsonToken t = next();
      for (JsonToken.Token tok : token) {
        if(t.getToken() == tok) {
          return t;
        }
      }
      throw new JsonParserException("Required token was not found (req: " +
          Arrays.toString(token) + "; found: " + t + ")");
    }
  }


//  public JsonParser() {
//  }

  public JsonValue parse(String json) throws JsonParserException {
    try {
      TokenList tokens = new TokenList(json);
      JsonToken t = tokens.next();
      switch (t.getToken()) {
        case OPEN_CB:
          return handleObject(tokens);
//        case STRING:
//          return handleValue(tokens);
        default:
          return null;
      }

    } catch (JsonTokenizerException e) {
      throw new JsonParserException(e.getMessage());
    }
  }

  private JsonObject handleObject(TokenList tokens) throws JsonParserException {
    JsonObject.JsonObjectBuilder jo = JsonObject.start();

    JsonToken token = tokens.grant(JsonToken.Token.STRING);
    jo.add(handlePair(tokens, token));

    while (tokens.hasNext()) {
      token = tokens.some(JsonToken.Token.COMMA, JsonToken.Token.CLOSE_CB);
      switch (token.getToken()) {
        case COMMA:
          token = tokens.grant(JsonToken.Token.STRING);
          jo.add(handlePair(tokens, token));
          break;
        case CLOSE_CB:
          return jo.build();
        default:
          throw new JsonParserException("Unexpected token: " + token.toString());
      }
    }
    throw new JsonParserException("Unexpected token list end.");
  }

  private JsonPair handlePair(TokenList tokens, JsonToken t) throws JsonParserException {
    String literal = t.getLiteral();
    JsonPair.JsonPairBuilder pair = JsonPair.create();
    pair.name(literal.substring(1, literal.length()-1));
    tokens.grant(JsonToken.Token.COLON);
    JsonValue v = handleValue(tokens);
    return pair.value(v).build();
  }

  private JsonValue handleValue(TokenList tokens) throws JsonParserException {
    JsonToken t = tokens.next();

    switch (t.getToken()) {
      case STRING:
        String literal = t.getLiteral();
        return new JsonString(literal.substring(1, literal.length()-1));
      case NUMBER:
      case FALSE:
        throw new JsonParserException("Not yet implemented token: " + t.toString());
      case OPEN_CB:
        return handleObject(tokens);
      default:
        throw new JsonParserException("Unexpected token: " + t.toString());
    }
  }
}
