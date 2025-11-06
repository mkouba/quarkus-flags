package io.quarkiverse.flags.jpa.test;

import jakarta.persistence.Entity;

import io.quarkiverse.flags.jpa.FlagDefinition;
import io.quarkiverse.flags.jpa.FlagFeature;
import io.quarkiverse.flags.jpa.FlagState;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagDefinition
@Entity
public class MyFlag extends PanacheEntity {

    @FlagFeature
    public String feature;

    @FlagState
    public String state;

}
