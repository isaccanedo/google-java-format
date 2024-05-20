/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.googlejavaformat.java;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import com.google.common.base.Joiner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** {@link StringWrapper}Test */
@RunWith(JUnit4.class)
public class StringWrapperTest {
  @Test
  public void testAwkwardLineEndWrapping() throws Exception {
    String input =
        lines(
            "class T {",
            // This is a wide line, but has to be split in code because of 100-char limit.
            "  String s = someMethodWithQuiteALongNameThatWillGetUsUpCloseToTheColumnLimit() "
                + "+ \"foo bar foo bar foo bar\";",
            "",
            "  String someMethodWithQuiteALongNameThatWillGetUsUpCloseToTheColumnLimit() {",
            "    return null;",
            "  }",
            "}");
    String output =
        lines(
            "class T {",
            "  String s =",
            "      someMethodWithQuiteALongNameThatWillGetUsUpCloseToTheColumnLimit()",
            "          + \"foo bar foo bar foo bar\";",
            "",
            "  String someMethodWithQuiteALongNameThatWillGetUsUpCloseToTheColumnLimit() {",
            "    return null;",
            "  }",
            "}");

    assertThat(StringWrapper.wrap(100, input, new Formatter())).isEqualTo(output);
  }

  @Test
  public void textBlock() throws Exception {
    assumeTrue(Runtime.version().feature() >= 15);
    String input =
        lines(
            "package com.mypackage;",
            "public class ReproBug {",
            "  private String myString;",
            "  private ReproBug() {",
            "    String str =",
            "        \"\"\"",
            "{\"sourceEndpoint\":\"ri.something.1-1.object-internal.1\",\"targetEndpoint"
                + "\":\"ri.something.1-1.object-internal.2\",\"typeId\":\"typeId\"}\"\"\";",
            "    myString = str;",
            "  }",
            "}");
    assertThat(StringWrapper.wrap(100, input, new Formatter())).isEqualTo(input);
  }

  // Test that whitespace handling on text block lines only removes spaces, not other control
  // characters.
  @Test
  public void textBlockControlCharacter() throws Exception {
    assumeTrue(Runtime.version().feature() >= 15);
    // We want an actual control character in the Java source being formatted, not a unicode escape,
    // i.e. the escape below doesn't need to be double-escaped.
    String input =
        lines(
            "package p;",
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      \u0007lorem",
            "      \u0007",
            "      ipsum",
            "      \"\"\";",
            "}");
    String actual = StringWrapper.wrap(100, input, new Formatter());
    assertThat(actual).isEqualTo(input);
  }

  @Test
  public void textBlockTrailingWhitespace() throws Exception {
    assumeTrue(Runtime.version().feature() >= 15);
    String input =
        lines(
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      lorem   ",
            "      ipsum",
            "      \"\"\";",
            "}");
    String expected =
        lines(
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      lorem",
            "      ipsum",
            "      \"\"\";",
            "}");
    String actual = StringWrapper.wrap(100, input, new Formatter());
    assertThat(actual).isEqualTo(expected);
  }

  // It would be neat if the formatter could remove the trailing whitespace here, but in general
  // it preserves unicode escapes from the original text.
  @Test
  public void textBlockTrailingWhitespaceUnicodeEscape() throws Exception {
    assumeTrue(Runtime.version().feature() >= 15);
    // We want a unicode escape in the Java source being formatted, so it needs to be escaped
    // in the string literal in this test.
    String input =
        lines(
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      lorem\\u0020",
            "      ipsum",
            "      \"\"\";",
            "}");
    String expected =
        lines(
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      lorem\\u0020",
            "      ipsum",
            "      \"\"\";",
            "}");
    String actual = StringWrapper.wrap(100, input, new Formatter());
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void textBlockSpaceTabMix() throws Exception {
    assumeTrue(Runtime.version().feature() >= 15);
    String input =
        lines(
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      lorem",
            "     \tipsum",
            "      \"\"\";",
            "}");
    String expected =
        lines(
            "public class T {",
            "  String s =",
            "      \"\"\"",
            "      lorem",
            "      ipsum",
            "      \"\"\";",
            "}");
    String actual = StringWrapper.wrap(100, input, new Formatter());
    assertThat(actual).isEqualTo(expected);
  }

  private static String lines(String... line) {
    return Joiner.on('\n').join(line) + '\n';
  }
}
