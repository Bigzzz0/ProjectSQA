package org.apache.commons.cli;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Method                     | Branch / Condition                          | Target Test Case
 * ----------------------------------------------------------------------------------------------------
 * addOptionGroup             | group.isRequired() == true / false         | testAddOptionGroupRequired/NonRequired
 *                            | option in group.getOptions()               | testAddOptionGroupContents
 *                            | group options forced required=false        | testAddOptionGroupResetsOptionRequired
 * getOptionGroups            | empty / multiple groups                    | testGetOptionGroups
 * addOption(opt, desc)       | delegate to full addOption                 | testAddOptionShortOnly
 * addOption(opt, hasArg, d)  | delegate to full addOption                 | testAddOptionWithArgFlag
 * addOption(o, l, hasArg, d) | delegate with Option instance creation     | testAddOptionFullVariants
 * addOption(Option)          | opt.hasLongOpt() == true / false           | testAddOptionWithAndWithoutLongOpt
 *                            | opt.isRequired() == true / false          | testAddOptionRequiredHandling
 *                            | requiredOpts already contains key          | testAddOptionDuplicateRequiredReplaces
 * getOptions                 | unmodifiable collection verification       | testGetOptionsImmutability
 * helpOptions                | returns list of shortOpts values           | testHelpOptions
 * getRequiredOptions         | unmodifiable list verification             | testGetRequiredOptionsImmutability
 * getOption(opt)             | shortOpts.containsKey(opt) == true         | testGetOptionShortMatch
 *                            |