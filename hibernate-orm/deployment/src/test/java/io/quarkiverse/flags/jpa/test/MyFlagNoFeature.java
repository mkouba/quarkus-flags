package io.quarkiverse.flags.jpa.test;

import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.common.FlagDefinition;
import io.quarkiverse.flags.hibernate.common.FlagValue;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagDefinition
@Entity
public class MyFlagNoFeature extends PanacheEntity {

    @FlagValue
    public String value;

}
