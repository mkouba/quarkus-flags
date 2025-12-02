package io.quarkiverse.flags.jpa.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.orm.FlagDefinition;
import io.quarkiverse.flags.hibernate.orm.FlagFeature;
import io.quarkiverse.flags.hibernate.orm.FlagMetadata;
import io.quarkiverse.flags.hibernate.orm.FlagValue;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagDefinition
@Entity
public class MyFlag extends PanacheEntity {

    @FlagFeature
    public String feature;

    @FlagValue
    public String value;

    @FlagMetadata
    @ElementCollection
    @CollectionTable
    public Map<String, String> metadata;

}
