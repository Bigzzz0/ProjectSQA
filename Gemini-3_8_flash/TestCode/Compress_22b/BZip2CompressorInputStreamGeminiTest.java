package org.apache.commons.compress.compressors.bzip2;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. Target Class: BZip2CompressorInputStream
 * 2. Static Methods:
 *    - matches(byte[] signature, int length):
 *      * length < 3 -> false
 *      * signature[0] != 'B' -> false
 *      * signature[1] != 'Z' -> false
 *      * signature[2] != 'h' -> false
 *      * valid "BZh" -> true
 * 3. Constructor & Initialization:
 *    - null InputStream -> IOException ("No InputStream") or NullPointerException
 *    - stream too short / non-bzip2 magic -> IOException ("Stream is not in the