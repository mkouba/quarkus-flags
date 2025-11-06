package io.quarkiverse.flags.jpa.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.FlagManager;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.TestTransaction;

public class FlagDefinitionTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> root.addClass(MyFlag.class));

    @Inject
    FlagManager manager;

    @TestTransaction
    @Test
    public void testFlagDefinition() {
        assertEquals(0, manager.getFlags().size());

        MyFlag alpha = new MyFlag();
        alpha.feature = "alpha";
        alpha.state = "true";
        alpha.persist();

        Flag alphaFlag = manager.getFlag("alpha").orElseThrow();
        Flag.Value alphaState = alphaFlag.computeAndAwait();
        assertTrue(alphaState.asBoolean());
        assertEquals("true", alphaState.asString());
    }

}
