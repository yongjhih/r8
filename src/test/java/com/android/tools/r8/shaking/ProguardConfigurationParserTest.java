// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions.KeepAttributeOptions;
import com.android.tools.r8.utils.InternalOptions.PackageObfuscationMode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ProguardConfigurationParserTest extends TestBase {

  private static final String VALID_PROGUARD_DIR = "src/test/proguard/valid/";
  private static final String INVALID_PROGUARD_DIR = "src/test/proguard/invalid/";
  private static final String PROGUARD_SPEC_FILE = VALID_PROGUARD_DIR + "proguard.flags";
  private static final String MULTIPLE_NAME_PATTERNS_FILE =
      VALID_PROGUARD_DIR + "multiple-name-patterns.flags";
  private static final String ACCESS_FLAGS_FILE = VALID_PROGUARD_DIR + "access-flags.flags";
  private static final String WHY_ARE_YOU_KEEPING_FILE =
      VALID_PROGUARD_DIR + "why-are-you-keeping.flags";
  private static final String ASSUME_NO_SIDE_EFFECTS =
      VALID_PROGUARD_DIR + "assume-no-side-effects.flags";
  private static final String ASSUME_NO_SIDE_EFFECTS_WITH_RETURN_VALUE =
      VALID_PROGUARD_DIR + "assume-no-side-effects-with-return-value.flags";
  private static final String ASSUME_VALUES_WITH_RETURN_VALUE =
      VALID_PROGUARD_DIR + "assume-values-with-return-value.flags";
  private static final String INCLUDING =
      VALID_PROGUARD_DIR + "including.flags";
  private static final String INVALID_INCLUDING_1 =
      INVALID_PROGUARD_DIR + "including-1.flags";
  private static final String INVALID_INCLUDING_2 =
      INVALID_PROGUARD_DIR + "including-2.flags";
  private static final String LIBRARY_JARS =
      VALID_PROGUARD_DIR + "library-jars.flags";
  private static final String LIBRARY_JARS_WIN =
      VALID_PROGUARD_DIR + "library-jars-win.flags";
  private static final String SEEDS =
      VALID_PROGUARD_DIR + "seeds.flags";
  private static final String SEEDS_2 =
      VALID_PROGUARD_DIR + "seeds-2.flags";
  private static final String VERBOSE =
      VALID_PROGUARD_DIR + "verbose.flags";
  private static final String KEEPDIRECTORIES =
      VALID_PROGUARD_DIR + "keepdirectories.flags";
  private static final String DONT_OBFUSCATE =
      VALID_PROGUARD_DIR + "dontobfuscate.flags";
  private static final String PACKAGE_OBFUSCATION_1 =
      VALID_PROGUARD_DIR + "package-obfuscation-1.flags";
  private static final String PACKAGE_OBFUSCATION_2 =
      VALID_PROGUARD_DIR + "package-obfuscation-2.flags";
  private static final String PACKAGE_OBFUSCATION_3 =
      VALID_PROGUARD_DIR + "package-obfuscation-3.flags";
  private static final String PACKAGE_OBFUSCATION_4 =
      VALID_PROGUARD_DIR + "package-obfuscation-4.flags";
  private static final String PACKAGE_OBFUSCATION_5 =
      VALID_PROGUARD_DIR + "package-obfuscation-5.flags";
  private static final String PACKAGE_OBFUSCATION_6 =
      VALID_PROGUARD_DIR + "package-obfuscation-6.flags";
  private static final String APPLY_MAPPING =
      VALID_PROGUARD_DIR + "applymapping.flags";
  private static final String APPLY_MAPPING_WITHOUT_FILE =
      INVALID_PROGUARD_DIR + "applymapping-without-file.flags";
  private static final String DONT_SHRINK =
      VALID_PROGUARD_DIR + "dontshrink.flags";
  private static final String DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES =
      VALID_PROGUARD_DIR + "dontskipnonpubliclibraryclasses.flags";
  private static final String DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS =
      VALID_PROGUARD_DIR + "dontskipnonpubliclibraryclassmembers.flags";
  private static final String OVERLOAD_AGGRESIVELY =
      VALID_PROGUARD_DIR + "overloadaggressively.flags";
  private static final String DONT_OPTIMIZE =
      VALID_PROGUARD_DIR + "dontoptimize.flags";
  private static final String DONT_OPTIMIZE_OVERRIDES_PASSES =
      VALID_PROGUARD_DIR + "dontoptimize-overrides-optimizationpasses.flags";
  private static final String OPTIMIZATION_PASSES =
      VALID_PROGUARD_DIR + "optimizationpasses.flags";
  private static final String OPTIMIZATION_PASSES_WITHOUT_N =
      INVALID_PROGUARD_DIR + "optimizationpasses-without-n.flags";
  private static final String SKIP_NON_PUBLIC_LIBRARY_CLASSES =
      VALID_PROGUARD_DIR + "skipnonpubliclibraryclasses.flags";
  private static final String PARSE_AND_SKIP_SINGLE_ARGUMENT =
      VALID_PROGUARD_DIR + "parse-and-skip-single-argument.flags";
  private static final String PRINT_USAGE =
      VALID_PROGUARD_DIR + "printusage.flags";
  private static final String PRINT_USAGE_TO_FILE =
      VALID_PROGUARD_DIR + "printusage-to-file.flags";
  private static final String TARGET =
      VALID_PROGUARD_DIR + "target.flags";

  @Test
  public void parse() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser;

    // Parse from file.
    parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PROGUARD_SPEC_FILE));
    List<ProguardConfigurationRule> rules = parser.getConfig().getRules();
    assertEquals(24, rules.size());
    assertEquals(1, rules.get(0).getMemberRules().size());

    // Parse from strings.
    parser = new ProguardConfigurationParser(new DexItemFactory());
    List<String> lines = FileUtils.readTextFile(Paths.get(PROGUARD_SPEC_FILE));
    parser.parse(new ProguardConfigurationSourceStrings(lines));
    rules = parser.getConfig().getRules();
    assertEquals(24, rules.size());
    assertEquals(1, rules.get(0).getMemberRules().size());
  }

  @Test
  public void parseMultipleNamePatterns() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(MULTIPLE_NAME_PATTERNS_FILE));
    List<ProguardConfigurationRule> rules = parser.getConfig().getRules();
    assertEquals(1, rules.size());
    ProguardConfigurationRule rule = rules.get(0);
    assertEquals(1, rule.getMemberRules().size());
    assertEquals("com.company.hello.**,com.company.world.**", rule.getClassNames().toString());
    assertEquals(ProguardKeepRuleType.KEEP, ((ProguardKeepRule) rule).getType());
    assertTrue(rule.getInheritanceIsExtends());
    assertEquals("some.library.Class", rule.getInheritanceClassName().toString());
    ProguardMemberRule memberRule = rule.getMemberRules().iterator().next();
    assertTrue(memberRule.getAccessFlags().isProtected());
    assertEquals(ProguardNameMatcher.create("getContents"), memberRule.getName());
    assertEquals("java.lang.Object[][]", memberRule.getType().toString());
    assertEquals(ProguardMemberType.METHOD, memberRule.getRuleType());
    assertEquals(0, memberRule.getArguments().size());
  }

  @Test
  public void parseAccessFlags() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(ACCESS_FLAGS_FILE));
    List<ProguardConfigurationRule> rules = parser.getConfig().getRules();
    assertEquals(1, rules.size());
    ProguardConfigurationRule rule = rules.get(0);
    DexAccessFlags publicAndFinalFlags = new DexAccessFlags(0);
    publicAndFinalFlags.setPublic();
    publicAndFinalFlags.setFinal();
    assertTrue(rule.getClassAccessFlags().containsNoneOf(publicAndFinalFlags));
    assertTrue(rule.getNegatedClassAccessFlags().containsAllOf(publicAndFinalFlags));
    DexAccessFlags abstractFlags = new DexAccessFlags(0);
    abstractFlags.setAbstract();
    assertTrue(rule.getClassAccessFlags().containsAllOf(abstractFlags));
    assertTrue(rule.getNegatedClassAccessFlags().containsNoneOf(abstractFlags));
    for (ProguardMemberRule member : rule.getMemberRules()) {
      if (member.getRuleType() == ProguardMemberType.ALL_FIELDS) {
        DexAccessFlags publicFlags = new DexAccessFlags(0);
        publicAndFinalFlags.setPublic();
        assertTrue(member.getAccessFlags().containsAllOf(publicFlags));
        assertTrue(member.getNegatedAccessFlags().containsNoneOf(publicFlags));
        DexAccessFlags staticFlags = new DexAccessFlags(0);
        staticFlags.setStatic();
        assertTrue(member.getAccessFlags().containsNoneOf(staticFlags));
        assertTrue(member.getNegatedAccessFlags().containsAllOf(staticFlags));
      } else {
        assertTrue(member.getRuleType() == ProguardMemberType.ALL_METHODS);
        DexAccessFlags publicProtectedVolatileFlags = new DexAccessFlags(0);
        publicProtectedVolatileFlags.setPublic();
        publicProtectedVolatileFlags.setProtected();
        publicProtectedVolatileFlags.setVolatile();
        assertTrue(member.getAccessFlags().containsNoneOf(publicProtectedVolatileFlags));
        assertTrue(member.getNegatedAccessFlags().containsAllOf(publicProtectedVolatileFlags));
      }
    }
  }

  @Test
  public void parseWhyAreYouKeeping() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(WHY_ARE_YOU_KEEPING_FILE));
    List<ProguardConfigurationRule> rules = parser.getConfig().getRules();
    assertEquals(1, rules.size());
    ProguardConfigurationRule rule = rules.get(0);
    assertEquals(1, rule.getClassNames().size());
    assertEquals("*", rule.getClassNames().toString());
    assertTrue(rule.getInheritanceIsExtends());
    assertEquals("foo.bar", rule.getInheritanceClassName().toString());
  }

  @Test
  public void parseAssumeNoSideEffects() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(ASSUME_NO_SIDE_EFFECTS));
    List<ProguardConfigurationRule> assumeNoSideEffects = parser.getConfig().getRules();
    assertEquals(1, assumeNoSideEffects.size());
    assumeNoSideEffects.get(0).getMemberRules().forEach(rule -> {
      assertFalse(rule.hasReturnValue());
    });
  }

  @Test
  public void parseAssumeNoSideEffectsWithReturnValue()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(ASSUME_NO_SIDE_EFFECTS_WITH_RETURN_VALUE));
    List<ProguardConfigurationRule> assumeNoSideEffects = parser.getConfig().getRules();
    assertEquals(1, assumeNoSideEffects.size());
    assumeNoSideEffects.get(0).getMemberRules().forEach(rule -> {
      assertTrue(rule.hasReturnValue());
      if (rule.getName().matches("returnsTrue") || rule.getName().matches("returnsFalse")) {
        assertTrue(rule.getReturnValue().isBoolean());
        assertFalse(rule.getReturnValue().isValueRange());
        assertFalse(rule.getReturnValue().isField());
        assertEquals(rule.getName().matches("returnsTrue"), rule.getReturnValue().getBoolean());
      } else if (rule.getName().matches("returns1")) {
        assertFalse(rule.getReturnValue().isBoolean());
        assertTrue(rule.getReturnValue().isValueRange());
        assertFalse(rule.getReturnValue().isField());
        assertTrue(rule.getReturnValue().isSingleValue());
        assertEquals(1, rule.getReturnValue().getValueRange().getMin());
        assertEquals(1, rule.getReturnValue().getValueRange().getMax());
        assertEquals(1, rule.getReturnValue().getSingleValue());
      } else if (rule.getName().matches("returns2To4")) {
        assertFalse(rule.getReturnValue().isBoolean());
        assertTrue(rule.getReturnValue().isValueRange());
        assertFalse(rule.getReturnValue().isField());
        assertFalse(rule.getReturnValue().isSingleValue());
        assertEquals(2, rule.getReturnValue().getValueRange().getMin());
        assertEquals(4, rule.getReturnValue().getValueRange().getMax());
      } else if (rule.getName().matches("returnsField")) {
        assertFalse(rule.getReturnValue().isBoolean());
        assertFalse(rule.getReturnValue().isValueRange());
        assertTrue(rule.getReturnValue().isField());
        assertEquals("com.google.C", rule.getReturnValue().getField().clazz.toString());
        assertEquals("int", rule.getReturnValue().getField().type.toString());
        assertEquals("X", rule.getReturnValue().getField().name.toString());
      }
    });
  }

  @Test
  public void parseAssumeValuesWithReturnValue()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(ASSUME_VALUES_WITH_RETURN_VALUE));
    List<ProguardConfigurationRule> assumeValues = parser.getConfig().getRules();
    assertEquals(1, assumeValues.size());
    assumeValues.get(0).getMemberRules().forEach(rule -> {
      assertTrue(rule.hasReturnValue());
      if (rule.getName().matches("isTrue") || rule.getName().matches("isFalse")) {
        assertTrue(rule.getReturnValue().isBoolean());
        assertFalse(rule.getReturnValue().isValueRange());
        assertFalse(rule.getReturnValue().isField());
        assertEquals(rule.getName().matches("isTrue"), rule.getReturnValue().getBoolean());
      } else if (rule.getName().matches("is1")) {
        assertFalse(rule.getReturnValue().isBoolean());
        assertTrue(rule.getReturnValue().isValueRange());
        assertFalse(rule.getReturnValue().isField());
        assertTrue(rule.getReturnValue().isSingleValue());
        assertEquals(1, rule.getReturnValue().getValueRange().getMin());
        assertEquals(1, rule.getReturnValue().getValueRange().getMax());
        assertEquals(1, rule.getReturnValue().getSingleValue());
      } else if (rule.getName().matches("is2To4")) {
        assertFalse(rule.getReturnValue().isBoolean());
        assertTrue(rule.getReturnValue().isValueRange());
        assertFalse(rule.getReturnValue().isField());
        assertFalse(rule.getReturnValue().isSingleValue());
        assertEquals(2, rule.getReturnValue().getValueRange().getMin());
        assertEquals(4, rule.getReturnValue().getValueRange().getMax());
      } else if (rule.getName().matches("isField")) {
        assertFalse(rule.getReturnValue().isBoolean());
        assertFalse(rule.getReturnValue().isValueRange());
        assertTrue(rule.getReturnValue().isField());
        assertEquals("com.google.C", rule.getReturnValue().getField().clazz.toString());
        assertEquals("int", rule.getReturnValue().getField().type.toString());
        assertEquals("X", rule.getReturnValue().getField().name.toString());
      }
    });
  }

  @Test
  public void parseDontobfuscate() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(DONT_OBFUSCATE));
    ProguardConfiguration config = parser.getConfig();
    assertFalse(config.isObfuscating());
  }

  @Test
  public void parseRepackageClassesEmpty() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PACKAGE_OBFUSCATION_1));
    ProguardConfiguration config = parser.getConfig();
    assertEquals(PackageObfuscationMode.REPACKAGE, config.getPackageObfuscationMode());
    assertNotNull(config.getPackagePrefix());
    assertEquals("", config.getPackagePrefix());
  }

  @Test
  public void parseRepackageClassesNonEmpty() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PACKAGE_OBFUSCATION_2));
    ProguardConfiguration config = parser.getConfig();
    assertEquals(PackageObfuscationMode.REPACKAGE, config.getPackageObfuscationMode());
    assertNotNull(config.getPackagePrefix());
    assertEquals("p.q.r", config.getPackagePrefix());
  }

  @Test
  public void parseFlattenPackageHierarchyEmpty() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PACKAGE_OBFUSCATION_3));
    ProguardConfiguration config = parser.getConfig();
    assertEquals(PackageObfuscationMode.FLATTEN, config.getPackageObfuscationMode());
    assertNotNull(config.getPackagePrefix());
    assertEquals("", config.getPackagePrefix());
  }

  @Test
  public void parseFlattenPackageHierarchyNonEmpty() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PACKAGE_OBFUSCATION_4));
    ProguardConfiguration config = parser.getConfig();
    assertEquals(PackageObfuscationMode.FLATTEN, config.getPackageObfuscationMode());
    assertNotNull(config.getPackagePrefix());
    assertEquals("p.q.r", config.getPackagePrefix());
  }

  @Test
  public void flattenPackageHierarchyCannotOverrideRepackageClasses()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PACKAGE_OBFUSCATION_5));
    ProguardConfiguration config = parser.getConfig();
    assertEquals(PackageObfuscationMode.REPACKAGE, config.getPackageObfuscationMode());
    assertNotNull(config.getPackagePrefix());
    assertEquals("top", config.getPackagePrefix());
  }

  @Test
  public void repackageClassesOverridesFlattenPackageHierarchy()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PACKAGE_OBFUSCATION_6));
    ProguardConfiguration config = parser.getConfig();
    assertEquals(PackageObfuscationMode.REPACKAGE, config.getPackageObfuscationMode());
    assertNotNull(config.getPackagePrefix());
    assertEquals("top", config.getPackagePrefix());
  }

  @Test
  public void parseApplyMapping() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(APPLY_MAPPING));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.hasApplyMappingFile());
  }

  @Test
  public void parseApplyMappingWithoutFile() throws IOException, ProguardRuleParserException {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      parser.parse(Paths.get(APPLY_MAPPING_WITHOUT_FILE));
      fail("Expect to fail due to the lack of file name.");
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("File name expected"));
    }
  }

  @Test
  public void parseIncluding() throws IOException, ProguardRuleParserException {
    new ProguardConfigurationParser(new DexItemFactory()).parse(Paths.get(INCLUDING));
  }

  @Test
  public void parseInvalidIncluding1() throws IOException {
    try {
      new ProguardConfigurationParser(new DexItemFactory()).parse(Paths.get(INVALID_INCLUDING_1));
      fail();
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("6")); // line
      assertTrue(e.getMessage().contains("including-1.flags")); // file in error
      assertTrue(e.getMessage().contains("does-not-exist.flags")); // missing file
    }
  }

  @Test
  public void parseInvalidIncluding2() throws IOException {
    try {
      new ProguardConfigurationParser(new DexItemFactory()).parse(Paths.get(INVALID_INCLUDING_2));
      fail();
    } catch (ProguardRuleParserException e) {
      String message = e.getMessage();
      assertTrue(message, message.contains("6")); // line
      assertTrue(message, message.contains("including-2.flags")); // file in error
      assertTrue(message, message.contains("does-not-exist.flags")); // missing file
    }
  }

  @Test
  public void parseLibraryJars() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    if (!ToolHelper.isLinux() && !ToolHelper.isMac()) {
      parser.parse(Paths.get(LIBRARY_JARS_WIN));
    } else {
      parser.parse(Paths.get(LIBRARY_JARS));
    }
    assertEquals(4, parser.getConfig().getLibraryjars().size());
  }

  @Test
  public void parseInvalidFilePattern() throws IOException {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      parser.parse(new ProguardConfigurationSourceStrings(
          Collections.singletonList("-injars abc.jar(*.zip;*.class)")));
    } catch (ProguardRuleParserException e) {
      return;
    }
    fail();
  }

  @Test
  public void parseSeeds() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(SEEDS));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.isPrintSeeds());
    assertNull(config.getSeedFile());
  }

  @Test
  public void parseSeeds2() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(SEEDS_2));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.isPrintSeeds());
    assertNotNull(config.getSeedFile());
  }

  @Test
  public void parseVerbose() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(VERBOSE));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.isVerbose());
  }

  @Test
  public void parseKeepdirectories() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(KEEPDIRECTORIES));
  }

  @Test
  public void parseDontshrink() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(DONT_SHRINK));
    ProguardConfiguration config = parser.getConfig();
    assertFalse(config.isShrinking());
  }

  @Test
  public void parseDontSkipNonPublicLibraryClasses()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES));
  }

  @Test
  public void parseDontskipnonpubliclibraryclassmembers()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS));
  }

  @Test
  public void parseOverloadAggressively()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(OVERLOAD_AGGRESIVELY));
  }

  @Test
  public void parseDontOptimize() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(DONT_OPTIMIZE));
    ProguardConfiguration config = parser.getConfig();
  }

  @Test
  public void parseDontOptimizeOverridesPasses() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(DONT_OPTIMIZE_OVERRIDES_PASSES));
    ProguardConfiguration config = parser.getConfig();
  }

  @Test
  public void parseOptimizationPasses() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(OPTIMIZATION_PASSES));
    ProguardConfiguration config = parser.getConfig();
  }

  @Test
  public void parseOptimizationPassesError() throws IOException, ProguardRuleParserException {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      parser.parse(Paths.get(OPTIMIZATION_PASSES_WITHOUT_N));
      fail();
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("Missing n"));
    }
  }

  @Test
  public void parseSkipNonPublicLibraryClasses() throws IOException {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      parser.parse(Paths.get(SKIP_NON_PUBLIC_LIBRARY_CLASSES));
      fail();
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("Unsupported option: -skipnonpubliclibraryclasses"));
    }
  }

  @Test
  public void parseAndskipSingleArgument() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PARSE_AND_SKIP_SINGLE_ARGUMENT));
  }

  @Test
  public void parsePrintUsage() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PRINT_USAGE));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.isPrintUsage());
    assertNull(config.getPrintUsageFile());
  }

  @Test
  public void parsePrintUsageToFile() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(PRINT_USAGE_TO_FILE));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.isPrintUsage());
    assertNotNull(config.getPrintUsageFile());
  }

  @Test
  public void parseTarget()
      throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(Paths.get(TARGET));
  }

  @Test
  public void parseInvalidKeepClassOption() throws IOException, ProguardRuleParserException {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      Path proguardConfig = writeTextToTempFile(
          "-keepclassx public class * {  ",
          "  native <methods>;           ",
          "}                             "
      );
      parser.parse(proguardConfig);
      fail();
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("Unknown option at "));
    }
  }

  @Test
  public void parseCustomFlags() throws Exception {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    // Custom Proguard flags -runtype and -laststageoutput are ignored.
    Path proguardConfig = writeTextToTempFile(
        "-runtype FINAL                    ",
        "-laststageoutput /some/file/name  "
    );
    parser.parse(proguardConfig);
  }

  @Test
  public void testRenameSourceFileAttribute() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    String config1 = "-renamesourcefileattribute PG\n";
    String config2 = "-keepattributes SourceFile,SourceDir\n";
    parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(config1, config2)));
    ProguardConfiguration config = parser.getConfig();
    assertEquals("PG", config.getRenameSourceFileAttribute());
    assertTrue(config.getKeepAttributesPatterns().contains(KeepAttributeOptions.SOURCE_FILE));
    assertTrue(config.getKeepAttributesPatterns().contains(KeepAttributeOptions.SOURCE_DIR));
  }

  @Test
  public void testRenameSourceFileAttributeEmpty() throws IOException, ProguardRuleParserException {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    String config1 = "-renamesourcefileattribute\n";
    String config2 = "-keepattributes SourceFile\n";
    parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(config1, config2)));
    ProguardConfiguration config = parser.getConfig();
    assertEquals("", config.getRenameSourceFileAttribute());
    assertTrue(config.getKeepAttributesPatterns().contains(KeepAttributeOptions.SOURCE_FILE));
  }

  private void testKeepattributes(List<String> expected, String config) throws Exception {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(config)));
    assertEquals(expected, parser.getConfig().getKeepAttributesPatterns());
  }

  @Test
  public void parseKeepattributes() throws Exception {
    List<String> xxxYYY = ImmutableList.of("xxx", "yyy");
    testKeepattributes(xxxYYY, "-keepattributes xxx,yyy");
    testKeepattributes(xxxYYY, "-keepattributes xxx, yyy");
    testKeepattributes(xxxYYY, "-keepattributes xxx ,yyy");
    testKeepattributes(xxxYYY, "-keepattributes xxx   ,   yyy");
    testKeepattributes(xxxYYY, "-keepattributes       xxx   ,   yyy     ");
    testKeepattributes(xxxYYY, "-keepattributes       xxx   ,   yyy     \n");
    String config = "-keepattributes Exceptions,InnerClasses,Signature,Deprecated,\n" +
                    "                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod\n";
    List<String> expected = ImmutableList.of("Exceptions", "InnerClasses", "Signature", "Deprecated",
        "SourceFile", "LineNumberTable", "*Annotation*", "EnclosingMethod");
    testKeepattributes(expected, config);
  }

  @Test
  public void parseInvalidKeepattributes() throws Exception {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of("-keepattributes xxx,")));
      fail();
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("Expected list element at "));
    }
  }

  @Test
  public void parseUseUniqueClassMemberNames() throws Exception {
    try {
      ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
      parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(
          "-useuniqueclassmembernames"
      )));
      parser.getConfig();
      fail();
    } catch (ProguardRuleParserException e) {
      assertTrue(e.getMessage().contains("-useuniqueulassmembernames is not supported"));
    }
  }

  @Test
  public void parseUseUniqueClassMemberNamesWithoutMinification() throws Exception {
    ProguardConfigurationParser parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(
        "-useuniqueclassmembernames",
        "-dontobfuscate"
    )));
    ProguardConfiguration config = parser.getConfig();
    assertTrue(config.isUseUniqueClassMemberNames());

    parser = new ProguardConfigurationParser(new DexItemFactory());
    parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(
        "-useuniqueclassmembernames"
    )));
    parser.parse(new ProguardConfigurationSourceStrings(ImmutableList.of(
        "-dontobfuscate"
    )));
    config = parser.getConfig();
    assertTrue(config.isUseUniqueClassMemberNames());
  }
}
