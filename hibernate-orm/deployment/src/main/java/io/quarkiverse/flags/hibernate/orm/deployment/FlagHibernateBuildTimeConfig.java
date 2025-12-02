package io.quarkiverse.flags.hibernate.orm.deployment;

import static io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.flags.hibernate-orm")
public interface FlagHibernateBuildTimeConfig {

    /**
     * Selects the persistence unit.
     */
    @WithDefault(DEFAULT_PERSISTENCE_UNIT_NAME)
    String persistenceUnitName();

}
