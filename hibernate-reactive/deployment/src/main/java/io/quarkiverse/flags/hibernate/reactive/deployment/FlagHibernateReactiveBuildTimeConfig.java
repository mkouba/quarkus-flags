package io.quarkiverse.flags.hibernate.reactive.deployment;

import static io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.flags.hibernate.reactive")
public interface FlagHibernateReactiveBuildTimeConfig {

    /**
     * Selects the persistence unit.
     */
    @WithDefault(DEFAULT_PERSISTENCE_UNIT_NAME)
    String persistenceUnitName();

}
