package io.quarkiverse.flags.jpa.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import io.quarkiverse.flags.jpa.FlagDefinition;
import io.quarkiverse.flags.jpa.FlagFeature;
import io.quarkiverse.flags.jpa.FlagMetadata;
import io.quarkiverse.flags.jpa.FlagValue;
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
