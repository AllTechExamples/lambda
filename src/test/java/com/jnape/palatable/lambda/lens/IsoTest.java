package com.jnape.palatable.lambda.lens;

import org.junit.Test;

import static com.jnape.palatable.lambda.lens.Iso.iso;
import static java.util.Arrays.asList;
import static testsupport.assertion.LensAssert.assertLensLawfulness;

public class IsoTest {

    @Test
    public void isLawful() {
        Iso<String, String, Integer, Integer> iso = iso(Integer::parseInt, Object::toString);
        assertLensLawfulness(iso, asList("1", "2"), asList(1, 2));
        assertLensLawfulness(iso.reverse(), asList(1, 2), asList("1", "2"));
    }
}