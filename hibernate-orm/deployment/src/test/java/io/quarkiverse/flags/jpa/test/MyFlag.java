package io.quarkiverse.flags.jpa.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.FlagFeature;
import io.quarkiverse.flags.hibernate.FlagMetadata;
import io.quarkiverse.flags.hibernate.FlagSource;
import io.quarkiverse.flags.hibernate.FlagValue;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagSource
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
