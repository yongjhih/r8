// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.D8Command.Builder;
import com.android.tools.r8.errors.CompilationError;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;

public class D8RunExamplesAndroidNTest extends RunExamplesAndroidNTest<D8Command.Builder> {

  class D8TestRunner extends TestRunner {

    D8TestRunner(String testName, String packageName, String mainClass) {
      super(testName, packageName, mainClass);
    }

    @Override
    TestRunner withMinApiLevel(int minApiLevel) {
      return withBuilderTransformation(builder -> builder.setMinApiLevel(minApiLevel));
    }

    @Override
    void build(Path inputFile, Path out) throws Throwable {
      D8Command.Builder builder = D8Command.builder();
      for (UnaryOperator<Builder> transformation : builderTransformations) {
        builder = transformation.apply(builder);
      }
      builder.addLibraryFiles(Paths.get(ToolHelper.getAndroidJar(builder.getMinApiLevel())));
      D8Command command = builder.addProgramFiles(inputFile).setOutputPath(out).build();
      try {
        ToolHelper.runD8(command, this::combinedOptionConsumer);
      } catch (RuntimeException re) {
        throw re instanceof CompilationError ? re : re.getCause();
      }
    }
  }

  @Override
  TestRunner test(String testName, String packageName, String mainClass) {
    return new D8TestRunner(testName, packageName, mainClass);
  }
}
