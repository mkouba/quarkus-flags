package io.quarkiverse.flags.qute.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.qute.Qute;
import io.quarkus.test.QuarkusUnitTest;

public class FlagNamespaceResolverTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withEmptyApplication()
            .overrideRuntimeConfigKey("quarkus.flags.runtime.alpha.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.bravo.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.value", "5")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.meta.foo", "true");

    @Test
    public void testFlag() {
        assertEquals("true", Qute.fmt("{flag:bool('alpha')}").render());
        assertEquals("false", Qute.fmt("{flag:disabled('alpha')}").render());
        assertEquals("true", Qute.fmt("{flag:string(data.0)}", "bravo"));
        assertEquals("5", Qute.fmt("{flag:int(\"charlie\")}").render());
        assertEquals("ok", Qute.fmt("{#if flag:enabled('alpha')}ok{/if}").render());
        assertEquals("true", Qute.fmt("{flag:meta('charlie').get('foo')}").render());
        assertEquals("true", Qute.fmt("{flag:find('charlie').metadata.get('foo')}").render());
        String allFlags = Qute.fmt("""
                {#for flag in flag:flags}{flag.feature}{#if flag_hasNext}:{/if}{/for}
                """).render();
        assertTrue(allFlags.contains("alpha"));
        assertTrue(allFlags.contains("bravo"));
        assertTrue(allFlags.contains("charlie"));
    }

}
