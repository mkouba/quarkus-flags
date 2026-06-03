package io.quarkiverse.flags.jpa.test;

import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.FlagSource;
import io.quarkiverse.flags.hibernate.FlagValue;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagSource
@Entity
public class MyFlagNoFeature extends PanacheEntity {

    @FlagValue
    public String value;

}
