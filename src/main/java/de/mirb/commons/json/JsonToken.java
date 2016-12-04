package de.mirb.pg.pojo;

public interface JsonToken {
  enum Token {
    OPEN_CB, CLOSE_CB, STRING, NUMBER, TRUE, FALSE, NULL, OPEN_BR, COMMA, COLON, CLOSE_BR
  }

  Token getToken();

  String getLiteral();
}