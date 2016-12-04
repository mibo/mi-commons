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

import java.util.Arrays;

public class JsonTokenizerException extends Throwable {

  private static final long serialVersionUID = -8295456415309640166L;
  private final String message;
  private final MessageKeys messageKey;
  private final String[] parameters;

  public enum MessageKeys {
    /** parameter: character, TOKEN */
    FORBIDDEN_CHARACTER,
    /** parameter: TOKEN */
    NOT_EXPECTED_TOKEN,
    /** parameter: TOKEN */
    NOT_FINISHED_QUERY,
    /** parameter: TOKEN */
    INVALID_TOKEN_STATE,
    /** parameter: TOKEN */
    ALREADY_FINISHED;

    public String getKey() {
      return name();
    }
  }

  public JsonTokenizerException(final String message, final MessageKeys messageKey,
                                final String... parameters) {
    this.message = message;
    this.messageKey = messageKey;
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return "JsonTokenizerException{" +
        "message='" + message + '\'' +
        ", messageKey=" + messageKey +
        ", parameters=" + Arrays.toString(parameters) +
        '}';
  }
}
