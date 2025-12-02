package io.quarkiverse.flags.runtime;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.event.Event;
import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.smallrye.mutiny.Uni;

@Singleton
public class InMemoryFlagProviderImpl extends AbstractFlagProvider implements InMemoryFlagProvider {

    private final ConcurrentMap<String, Flag> flags = new ConcurrentHashMap<>();

    private final Event<FlagAdded> flagAdded;

    private final Event<FlagRemoved> flagRemoved;

    public InMemoryFlagProviderImpl(FlagManager manager, Event<FlagAdded> flagAdded, Event<FlagRemoved> flagRemoved) {
        super(manager);
        this.flagAdded = flagAdded;
        this.flagRemoved = flagRemoved;
    }

    @Override
    public Uni<Collection<Flag>> getFlags() {
        return Uni.createFrom().item(flags.values());
    }

    @Override
    public boolean addFlag(Flag flag) {
        Flag existing = flags.putIfAbsent(flag.feature(), flag);
        if (existing == null) {
            flagAdded.fire(new FlagAdded(flag));
            return true;
        }
        return false;
    }

    @Override
    public Flag removeFlag(String feature) {
        Flag removed = flags.remove(feature);
        if (removed != null) {
            flagRemoved.fire(new FlagRemoved(removed));
        }
        return removed;
    }

}
