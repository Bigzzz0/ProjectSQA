/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.resource.ResourceConstants;
import org.apache.commons.cli2.resource.ResourceHelper;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli2.option.ArgumentImpl
 *
 * 1. Decision & Branch Coverage:
 *    - Constructor:
 *      * name == null vs name != null (defaults to "arg")
 *      * minimum > maximum -> IllegalArgumentException (ARGUMENT_MIN_EXCEEDS_MAX)
 *      * valueDefaults != null && size > 0:
 *        - size < minimum -> IllegalArgumentException (ARGUMENT_TOO_FEW_DEFAULTS)
 *        - size > maximum -> IllegalArgumentException (ARGUMENT_TOO_MANY_DEFAULTS)
 *        - minimum <= size <= maximum -> valid
 *      * valueDefaults == null / empty -> valid
 *    - processValues():
 *      * argumentCount < maximum guard
 *      * allValuesQuoted.equals(consumeRemaining): absorbs following args up to maximum
 *      * commandLine.looksLikeOption(allValuesQuoted): backtracks iterator and terminates argument processing
 *      * subsequentSplit (subsequentSeparator != '\0'): tokenizes and adds values,
 *        throws OptionException(ARGUMENT_UNEXPECTED_VALUE) if token count