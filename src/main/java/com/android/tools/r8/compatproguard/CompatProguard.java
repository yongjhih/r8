// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.compatproguard;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8;
import com.android.tools.r8.R8Command;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class CompatProguard {
  public static class CompatProguardOptions {
    public final String output;
    public final int minApi;
    public final List<String> proguardConfig;

    CompatProguardOptions(List<String> proguardConfig, String output, int minApi) {
      this.output = output;
      this.minApi = minApi;
      this.proguardConfig = proguardConfig;
    }

    public static CompatProguardOptions parse(String[] args) {
      String output = null;
      int minApi = 1;
      ImmutableList.Builder<String> builder = ImmutableList.builder();
      if (args.length > 0) {
        StringBuilder currentLine = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
          String arg = args[i];
          if (arg.charAt(0) == '-') {
            if (arg.equals("--min-api")) {
              minApi = Integer.valueOf(args[++i]);
            } else if (arg.equals("-outjars")) {
              output = args[++i];
            } else {
              builder.add(currentLine.toString());
              currentLine = new StringBuilder(arg);
            }
          } else {
            currentLine.append(' ').append(arg);
          }
        }
        builder.add(currentLine.toString());
      }
      return new CompatProguardOptions(builder.build(), output, minApi);
    }
  }

  private static void run(String[] args) throws IOException, CompilationException {
    System.out.println("CompatProguard " + String.join(" ", args));
    // Run R8 passing all the options from the command line as a Proguard configuration.
    CompatProguardOptions options = CompatProguardOptions.parse(args);
    R8.run(R8Command.builder()
        .setOutputPath(Paths.get(options.output))
        .addProguardConfiguration(options.proguardConfig)
        .setMinApiLevel(options.minApi)
        .build());
  }

  public static void main(String[] args) throws IOException {
    try {
      run(args);
    } catch (CompilationException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
