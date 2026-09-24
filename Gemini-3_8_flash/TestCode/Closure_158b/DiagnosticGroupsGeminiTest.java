package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.google.javascript.jscomp.DiagnosticGroups
 *
 * Decision / Branch Coverage Points:
 * 1. registerGroup(name, group):
 *    - Registers diagnostic group instance by key name in groupsByName map.
 * 2. registerGroup(name, types...):
 *    - Instantiates new DiagnosticGroup with varargs DiagnosticType, stores in map.