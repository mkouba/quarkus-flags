package io.quarkiverse.flags.jpa.test;

import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.FlagFeature;
import io.quarkiverse.flags.hibernate.FlagSource;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagSource
@Entity
public class MyFlagNoValue extends PanacheEntity {

    @FlagFeature
    public String feature;

}
