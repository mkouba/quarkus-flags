package io.quarkiverse.flags.jpa.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class FlagEntityValueNotSetTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> root.addClasses(MyFlagNoValue.class))
            .setExpectedException(IllegalArgumentException.class);

    @Test
    public void testFailure() {
        fail();
    }

}
