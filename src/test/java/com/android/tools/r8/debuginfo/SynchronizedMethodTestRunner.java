// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.StringUtils;
import java.util.Arrays;
import org.junit.Test;

public class SynchronizedMethodTestRunner extends DebugInfoTestBase {

  static Class clazz = SynchronizedMethodTest.class;

  @Test
  public void testSynchronizedMethod() throws Exception {
    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = StringUtils.lines("42", "42", "2", "2");
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkSyncStatic(inspectMethod(d8App, clazz, "int", "syncStatic", "int"));
    checkSyncStatic(inspectMethod(dxApp, clazz, "int", "syncStatic", "int"));

    checkSyncInstance(inspectMethod(d8App, clazz, "int", "syncInstance", "int"));
    checkSyncInstance(inspectMethod(dxApp, clazz, "int", "syncInstance", "int"));

    checkThrowing(inspectMethod(d8App, clazz, "int", "throwing", "int"), false);
    checkThrowing(inspectMethod(dxApp, clazz, "int", "throwing", "int"), true);

    checkMonitorExitRegression(
        inspectMethod(d8App, clazz, "int", "monitorExitRegression", "int"), false);
    checkMonitorExitRegression(
        inspectMethod(dxApp, clazz, "int", "monitorExitRegression", "int"), true);
  }

  private void checkSyncStatic(DebugInfoInspector info) {
    info.checkStartLine(9);
    info.checkLineHasExactLocals(9, "x", "int");
    info.checkLineHasExactLocals(10, "x", "int");
    info.checkNoLine(11);
    info.checkLineHasExactLocals(12, "x", "int");
    info.checkNoLine(13);
  }

  private void checkSyncInstance(DebugInfoInspector info) {
    String[] locals = {"this", clazz.getCanonicalName(), "x", "int"};
    info.checkStartLine(16);
    info.checkLineHasExactLocals(16, locals);
    info.checkLineHasExactLocals(17, locals);
    info.checkNoLine(18);
    info.checkLineHasExactLocals(19, locals);
    info.checkNoLine(20);
  }

  private void checkThrowing(DebugInfoInspector info, boolean dx) {
    info.checkStartLine(23);
    if (!dx) {
      info.checkLineHasExactLocals(23, "cond", "int");
    }
    info.checkLineHasExactLocals(24, "cond", "int", "x", "int");
    info.checkLineHasExactLocals(25, "cond", "int", "x", "int");
    info.checkNoLine(26);
    info.checkLineHasExactLocals(27, "cond", "int", "x", "int");
  }

  private void checkMonitorExitRegression(DebugInfoInspector info, boolean dx) {
    info.checkStartLine(31);
    for (int line : Arrays.asList(32, 34, 36, 38, 40, 42, 44, 48, 50, 52)) {
      if (dx && line == 40) {
        continue;
      }
      info.checkLineHasExactLocals(line, "cond", "int", "x", "int");
    }
  }
}
