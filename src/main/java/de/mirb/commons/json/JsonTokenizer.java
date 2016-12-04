/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.mirb.pg.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * object    = ( OPEN_CB CLOSE | OPEN_CB member CLOSE )
 * member  =  ( pair | pair COMMA member )
 * pair = ( string DC value )
 * array  = ( OPEN_B CLOSE_B | OPEN_B elements CLOSE_B )
 * elements = ( value | value DC elements )
 * value = ( string | number | object | array | TRUE | FALSE | NULL )
 *
 *
 * string    = [ 'NOT' RWS ] ( searchPhrase / searchWord )
 * number  = quotation-mark 1*qchar-no-AMP-DQUOTE quotation-mark
 * </pre>
 *
 */
public class JsonTokenizer {


  private static abstract class State implements JsonToken {
    private Token token = null;
    private boolean finished = false;

    static final char CHAR_DOT = '.';
    static final char QUOTATION_MARK = '\"';
    static final char ESCAPE_CHAR = '\\';
    static final char CHAR_A = 'A';
    static final char CHAR_E = 'E';
    static final char CHAR_F = 'F';
    static final char CHAR_R = 'R';
    static final char CHAR_L = 'L';
    static final char CHAR_S = 'S';
    static final char CHAR_T = 'T';
    static final char CHAR_U = 'U';
    static final char CHAR_N = 'N';
    static final char CHAR_LOW_A = 'a';
    static final char CHAR_LOW_E = 'e';
    static final char CHAR_LOW_F = 'f';
    static final char CHAR_LOW_N = 'n';
    static final char CHAR_LOW_R = 'r';
    static final char CHAR_LOW_S = 's';
    static final char CHAR_LOW_T = 't';
    static final char CHAR_LOW_U = 'u';
    static final char CHAR_LOW_L = 'l';
    static final char CHAR_CLOSE_CB = '}';
    static final char CHAR_OPEN_CB = '{';
    static final char CHAR_CLOSE_BR = ']';
    static final char CHAR_OPEN_BR = '[';
    static final char COLON = ':';
    static final char CHAR_DASH = '-';
    static final char COMMA = ',';

    public State() {}

    public State(final Token t) {
      token = t;
    }

    public State(final Token t, final boolean finished) {
      this(t);
      this.finished = finished;
    }

    protected abstract State nextChar(char c) throws JsonTokenizerException;

    /** @param c allowed character */
    public State allowed(final char c) {
      return this;
    }

    public State forbidden(final char c) throws JsonTokenizerException {
      throw new JsonTokenizerException("Forbidden character in state " + token + "->" + c,
          JsonTokenizerException.MessageKeys.FORBIDDEN_CHARACTER, "" + c);
    }

    public State invalid() throws JsonTokenizerException {
      throw new JsonTokenizerException("Token " + token + " is in invalid state.",
          JsonTokenizerException.MessageKeys.INVALID_TOKEN_STATE);
    }

    public State finish() {
      finished = true;
      return this;
    }

    public State finishAs(final Token token) {
      finished = true;
      return changeToken(token);
    }

    public boolean isFinished() {
      return finished;
    }

    @Override
    public Token getToken() {
      return token;
    }

    public String getTokenName() {
      if (token == null) {
        return "NULL";
      }
      return token.name();
    }

    public State close() throws JsonTokenizerException {
      return this;
    }

    protected State changeToken(final Token token) {
      this.token = token;
      return this;
    }

    static boolean isAllowedString(final char character) {
      return isDigit(character) || Character.isUnicodeIdentifierStart(character);
    }

    static boolean isSome(final char expected, char ... chars) {
      for (char aChar : chars) {
        if(aChar == expected) {
          return true;
        }
      }
      return false;
    }


    static boolean isAllowedEscapedChar(char character) {
      return character == QUOTATION_MARK
          || character == ESCAPE_CHAR
          || character == 'b'
          || character == 'f'
          || character == 'n'
          || character == 'r'
          || character == 't';
      // FIXME: Support for 4-digit unicode
//          || character == 'u';
    }


    static boolean isDigit(final char character) {
      return '0' <= character && character <= '9'; // case 0..9
    }

    // BWS = *( SP / HTAB / "%20" / "%09" ) ; "bad" whitespace
    // RWS = 1*( SP / HTAB / "%20" / "%09" ) ; "required" whitespace
    static boolean isWhitespace(final char character) {
      return character == ' ' || character == '\t';
    }

    static boolean isSomeClose(final char character) {
      return character == COMMA
          || character == CHAR_CLOSE_BR
          || character == CHAR_CLOSE_CB
          || isWhitespace(character);
    }

    @Override
    public String getLiteral() {
      return token.toString();
    }

    @Override
    public String toString() {
      return token + "=>{" + getLiteral() + "}";
    }
  }

  private static abstract class LiteralState extends State {
    final StringBuilder literal = new StringBuilder();

    private LiteralState() {
      super();
    }

    LiteralState(final Token t, final char c) throws JsonTokenizerException {
      super(t);
      init(c);
    }

    public LiteralState(final Token t, final String initLiteral) {
      super(t);
      literal.append(initLiteral);
    }

    @Override
    public State allowed(final char c) {
      literal.append(c);
      return this;
    }

    @Override
    public String getLiteral() {
      return literal.toString();
    }

    public State init(final char c) throws JsonTokenizerException {
      if (isFinished()) {
        throw new JsonTokenizerException(toString() + " is already finished.",
            JsonTokenizerException.MessageKeys.ALREADY_FINISHED, getTokenName());
      }
      literal.append(c);
      return this;
    }
  }

  private class StartState extends LiteralState {
    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (c == CHAR_OPEN_CB) {
        return new OpenCbState();
      } else if (c == QUOTATION_MARK) {
        return new StringState(c);
      } else if (c == CHAR_OPEN_BR) {
        return new OpenBrState();
      } else if (isWhitespace(c)) {
        return allowed(c);
      } else {
        return forbidden(c);
      }
    }

    @Override
    public State init(final char c) throws JsonTokenizerException {
      return nextChar(c);
    }
  }



  private class StringState extends LiteralState {
    private boolean closed = false;
    private boolean escaped = false;

    public StringState(char c) throws JsonTokenizerException {
      super(Token.STRING, c);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (closed) {
        if (isWhitespace(c)) {
          return this;
        } else if (isSomeClose(c)) {
          finish();
          return new SomeCloseState().init(c);
        } else if (c == COLON) {
          finish();
          return new ColonState();
        }
      } else if (escaped) {
        escaped = false;
        if (isAllowedEscapedChar(c)) {
          return allowed(c);
        } else {
          return forbidden(c);
        }
      } else if (c == ESCAPE_CHAR) {
        escaped = true;
        return this;
      } else if (isWhitespace(c)) {
        return allowed(c);
      } else if (c == QUOTATION_MARK) {
        if (literal.length() == 1) {
          return invalid();
        }
        closed = true;
        return allowed(c);
      } else if (isAllowedString(c)) {
        return allowed(c);
      }
      return forbidden(c);
    }

    @Override
    public State close() throws JsonTokenizerException {
      if (closed) {
        return finish();
      }
      return invalid();
    }
  }


  private class OpenCbState extends State {
    public OpenCbState() {
      super(Token.OPEN_CB);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (isWhitespace(c)) {
        return allowed(c);
      } else if(c == CHAR_CLOSE_CB) {
        finish();
        return new CloseCbState();
      } else if(c == QUOTATION_MARK) {
        finish();
        return new StringState(c);
      }
      return forbidden(c);
    }
  }


  private class CloseCbState extends State {
    public CloseCbState() {
      super(Token.CLOSE_CB, true);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if(isSomeClose(c)) {
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }
  }

  private class ValueState extends State {
    public State init(final char c) throws JsonTokenizerException {
      if (c == QUOTATION_MARK) {
        return new StringState(c);
      } else if (c == CHAR_OPEN_CB) {
        return new OpenCbState();
      } else if (c == CHAR_OPEN_BR) {
        return new OpenBrState();
      } else if (c == CHAR_DASH || isDigit(c)) {
        return new NumberState(c);
      } else if (isSome(c, CHAR_T, CHAR_LOW_T)) {
        return new TrueState(c);
      } else if (isSome(c, CHAR_F, CHAR_LOW_F)) {
        return new FalseState(c);
      } else if (isSome(c, CHAR_N, CHAR_LOW_N)) {
        return new NullState(c);
      } else {
        return forbidden(c);
      }
    }

    @Override
    protected State nextChar(char c) throws JsonTokenizerException {
      return null;
    }
  }

  private class OpenBrState extends State {
    public OpenBrState() {
      super(Token.OPEN_BR);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if(c == CHAR_CLOSE_BR) {
        finish();
        return new CloseBrState();
      } else if(isWhitespace(c)) {
        return allowed(c);
      } else {
        finish();
        return new ValueState().init(c);
      }
    }
  }

  private class CloseBrState extends State {
    public CloseBrState() {
      super(Token.CLOSE_BR);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if(c == CHAR_CLOSE_CB) {
        finish();
        return new CloseCbState();
      } else if(c == COMMA) {
        finish();
        return new CommaState();
      } else if(isWhitespace(c)) {
        return allowed(c);
      }
      return forbidden(c);
    }

    @Override
    public State close() throws JsonTokenizerException {
      // FIXME: Remove this if simple json value parsing is not allowed
      finish();
      return super.close();
    }
  }

  private class TrueState extends LiteralState {
    public TrueState(final char c) throws JsonTokenizerException {
      super(Token.TRUE, c);
      if (c != CHAR_T && c != CHAR_LOW_T) {
        forbidden(c);
      }
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (literal.length() == 1 && isSome(c, CHAR_R, CHAR_LOW_R)) {
        return allowed(c);
      } else if (literal.length() == 2 && isSome(c, CHAR_U, CHAR_LOW_U)) {
        return allowed(c);
      } else if (literal.length() == 3 && isSome(c, CHAR_E, CHAR_LOW_E)) {
        return allowed(c);
      } else if (literal.length() == 4 && (isSomeClose(c))) {
        finish();
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }
  }

  private class NumberState extends LiteralState {
    public NumberState(final char c) throws JsonTokenizerException {
      super(Token.NUMBER, c);
      if (c != '-' && !isDigit(c)) {
        forbidden(c);
      }
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (isDigit(c)) {
        return allowed(c);
      } else if (c == CHAR_DOT) {
        return new NumberFracState(c, getLiteral());
      } else if (c == 'e' || c == 'E') {
        return new NumberExpState(c, getLiteral());
      } else if (isSomeClose(c)) {
        finish();
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }

    @Override
    public State close() throws JsonTokenizerException {
      // FIXME: Remove this if simple json value parsing is not allowed
      finish();
      return super.close();
    }
  }

  private class NumberFracState extends LiteralState {

    public NumberFracState(final char c, final String consume) throws JsonTokenizerException {
      super(Token.NUMBER, consume);
      if (c != '.') {
        forbidden(c);
      }
      allowed(c);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (isDigit(c)) {
        return allowed(c);
      } else if (c == CHAR_LOW_E || c == CHAR_E) {
        return new NumberExpState(c, getLiteral());
      } else if (isSomeClose(c)) {
        finish();
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }
  }

  private class NumberExpState extends LiteralState {
    int exponent = 1;
    public NumberExpState(final char c, final String consume) throws JsonTokenizerException {
      super(Token.NUMBER, consume);
      if (c != 'e' && c != 'E') {
        forbidden(c);
      }
      allowed(c);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if(exponent == 1) {
        if(c == '+' || c == '-') {
          exponent++;
          return allowed(c);
        } else if(isDigit(c)) {
          exponent = 0;
          return allowed(c);
        }
      } else if (exponent == 2) {
        if(isDigit(c)) {
          exponent = 0;
          return allowed(c);
        }
      } else if (isDigit(c)) {
        return allowed(c);
      } else if (isSomeClose(c)) {
        finish();
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }
  }

  private class FalseState extends LiteralState {
    public FalseState(final char c) throws JsonTokenizerException {
      super(Token.FALSE, c);
      if (c != CHAR_F && c != CHAR_LOW_F) {
        forbidden(c);
      }
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (literal.length() == 1 && isSome(c, CHAR_A, CHAR_LOW_A)) {
        return allowed(c);
      } else if (literal.length() == 2 && isSome(c, CHAR_L, CHAR_LOW_L)) {
        return allowed(c);
      } else if (literal.length() == 3 && isSome(c, CHAR_S, CHAR_LOW_S)) {
        return allowed(c);
      } else if (literal.length() == 4 && isSome(c, CHAR_E, CHAR_LOW_E)) {
        return allowed(c);
      } else if (literal.length() == 5 && (isSomeClose(c))) {
        finish();
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }
  }

  private class NullState extends LiteralState {
    public NullState(final char c) throws JsonTokenizerException {
      super(Token.NULL, c);
      if (c != CHAR_N && c != CHAR_LOW_N) {
        forbidden(c);
      }
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (literal.length() == 1 && (c == CHAR_U || c == CHAR_LOW_U)) {
        return allowed(c);
      } else if (literal.length() == 2 && (c == CHAR_L || c == CHAR_LOW_L)) {
        return allowed(c);
      } else if (literal.length() == 3 && (c == CHAR_L || c == CHAR_LOW_L)) {
        return allowed(c);
      } else if (literal.length() == 4 && (isSomeClose(c))) {
        finish();
        return new SomeCloseState().init(c);
      }
      return forbidden(c);
    }
  }

  private class SomeCloseState extends State {

    @Override
    protected State nextChar(char c) throws JsonTokenizerException {
      return init(c);
    }

    public State init(char c) throws JsonTokenizerException {
      if(c == COMMA) {
        return new CommaState();
      } else if(isWhitespace(c)) {
        return allowed(c);
      } else if(c == CHAR_CLOSE_BR) {
        return new CloseBrState();
      } else if(c == CHAR_CLOSE_CB) {
        return new CloseCbState();
      }
      return forbidden(c);
    }
  }

  private class CommaState extends State {
    public CommaState() {
      super(Token.COMMA);
    }

    @Override
    protected State nextChar(char c) throws JsonTokenizerException {
      if(isWhitespace(c)) {
        return allowed(c);
      } else {
        finish();
        return new ValueState().init(c);
      }
    }
  }

  private class ColonState extends State {
    public ColonState() {
      super(Token.COLON);
    }

    @Override
    public State nextChar(final char c) throws JsonTokenizerException {
      if (isWhitespace(c)) {
        return allowed(c);
      } else {
        finish();
        return new ValueState().init(c);
      }
    }
  }

  /**
   * Takes the search query and splits it into a list of corresponding {@link JsonToken}s.
   * Before splitting it into tokens, leading and trailing whitespace in the given search query string is removed.
   *
   * @param json search query to be tokenized
   * @return list of tokens
   * @throws JsonTokenizerException if something in query is not valid (based on OData search query ABNF)
   */
  public List<JsonToken> tokenize(final String json) throws JsonTokenizerException {

    char[] chars = json.trim().toCharArray();

    State state = new StartState();
    List<JsonToken> states = new ArrayList<>();
    for (char aChar : chars) {
      State next = state.nextChar(aChar);
      if (state.isFinished()) {
        states.add(state);
      }
      state = next;
    }

    if (state.close().isFinished()) {
      states.add(state);
    } else {
      throw new JsonTokenizerException("Last parsed state '" + state.toString() + "' is not finished.",
          JsonTokenizerException.MessageKeys.NOT_FINISHED_QUERY, state.getTokenName());
    }

    return states;
  }
}
