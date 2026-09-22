package org.apache.commons.math.complex;

import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.apache.commons.math.complex.Complex
 *
 * DEFECT TARGET (Ground Truth: ComplexTest::testAddNaN):
 * - Method: Complex.add(Complex rhs)
 * - Defect: Missing NaN validation check (`if (isNaN || rhs.isNaN) return NaN;`).
 *           When adding a finite Complex and a Complex with a single NaN part (e.g., 1.0 + NaNi),
 *           the definitional arithmetic results in real = finite + 1.0 (non-NaN) instead of returning
 *           Complex.