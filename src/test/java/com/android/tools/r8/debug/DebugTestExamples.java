// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import java.util.Collections;
import org.apache.harmony.jpda.tests.framework.jdwp.Value;
import org.junit.Test;

/**
 * Examples of debug test features.
 */
public class DebugTestExamples extends DebugTestBase {

  public static final String SOURCE_FILE = "Arithmetic.java";
  public static final String DEBUGGEE_CLASS = "Arithmetic";

  /**
   * Simple test that runs the debuggee until it exits.
   */
  @Test
  public void testRun() throws Throwable {
    runDebugTest(DEBUGGEE_CLASS, Collections.singletonList(run()));
  }

  /**
   * Tests that we do suspend on breakpoint then continue.
   */
  @Test
  public void testBreakpoint_Hit() throws Throwable {
    runDebugTest(DEBUGGEE_CLASS,
        breakpoint(DEBUGGEE_CLASS, "bitwiseInts"),
        run(),
        checkLine(SOURCE_FILE, 12),
        run());
  }

  /**
   * Tests that we can check local variables at a suspension point (breakpoint).
   */
  @Test
  public void testLocalsOnBreakpoint() throws Throwable {
    runDebugTest(DEBUGGEE_CLASS,
        breakpoint(DEBUGGEE_CLASS, "bitwiseInts"),
        run(),
        checkLine(SOURCE_FILE, 12),
        checkLocal("x", Value.createInt(12345)),
        checkLocal("y", Value.createInt(54321)),
        run());
  }

  /**
   * Tests that we can check local variables at different suspension points (breakpoint then step).
   */
  @Test
  public void testLocalsOnBreakpointThenStep() throws Throwable {
    runDebugTest(DEBUGGEE_CLASS,
        breakpoint(DEBUGGEE_CLASS, "bitwiseInts"),
        run(),
        checkLine(SOURCE_FILE, 12),
        checkLocal("x", Value.createInt(12345)),
        checkLocal("y", Value.createInt(54321)),
        stepOver(),
        checkLocal("x", Value.createInt(12345)),
        checkLocal("y", Value.createInt(54321)),
        run());
  }
}
