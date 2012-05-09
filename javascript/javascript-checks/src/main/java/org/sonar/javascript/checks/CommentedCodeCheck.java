/*
 * Sonar JavaScript Plugin
 * Copyright (C) 2011 Eriks Nukis and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.javascript.checks;

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.javascript.api.EcmaScriptGrammar;
import org.sonar.squid.recognizer.*;

import java.util.Set;
import java.util.regex.Pattern;

@Rule(
  key = "CommentedCode",
  priority = Priority.BLOCKER)
public class CommentedCodeCheck extends SquidCheck<EcmaScriptGrammar> implements AstAndTokenVisitor {

  private static final double THRESHOLD = 0.9;

  private final CodeRecognizer codeRecognizer = new CodeRecognizer(THRESHOLD, new JavaScriptRecognizer());
  private final Pattern regexpToDivideStringByLine = Pattern.compile("(\r?\n)|(\r)");

  private static class JavaScriptRecognizer implements LanguageFootprint {

    public Set<Detector> getDetectors() {
      // TODO copy-paste from org.sonar.plugins.javascript.squid.JavaScriptFootprint
      return ImmutableSet.of(
          new EndWithDetector(0.95, '}', ';', '{'),
          // https://developer.mozilla.org/en/JavaScript/Reference/Reserved_Words
          new KeywordsDetector(0.3, "break", "case", "catch", "continue", "default", "delete", "do", "else", "finally", "for",
              "function", "if", "in", "instanceof", "new", "return", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with"),
          // https://developer.mozilla.org/en/JavaScript/Reference
          new ContainsDetector(0.95, "++", "--"),
          new ContainsDetector(0.95, "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", ">>>=", "&=", "^=", "|="),
          new ContainsDetector(0.95, "==", "!=", "===", "!=="));
    }

  }

  public void visitToken(Token token) {
    for (Trivia trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        String lines[] = regexpToDivideStringByLine.split(getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue()));
        for (int lineOffset = 0; lineOffset < lines.length; lineOffset++) {
          if (codeRecognizer.isLineOfCode(lines[lineOffset])) {
            getContext().createLineViolation(this, "Sections of code should not be \"commented out\".", trivia.getToken().getLine() + lineOffset);
            break;
          }
        }
      }
    }
  }

}
