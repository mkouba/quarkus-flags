package io.quarkiverse.flags.jpa.test;

import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.orm.FlagDefinition;
import io.quarkiverse.flags.hibernate.orm.FlagFeature;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagDefinition
@Entity
public class MyFlagNoValue extends PanacheEntity {

    @FlagFeature
    public String feature;

}
