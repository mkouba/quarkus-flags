package io.quarkiverse.flags.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import io.quarkiverse.flags.hibernate.FlagFeature;
import io.quarkiverse.flags.hibernate.FlagMetadata;
import io.quarkiverse.flags.hibernate.FlagSource;
import io.quarkiverse.flags.hibernate.FlagValue;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagSource
@Entity
@Table(name = "db_flag")
public class DbFlag extends PanacheEntity {

    @FlagFeature
    public String feature;

    @FlagValue
    public String value;

    @FlagMetadata
    @ElementCollection
    @CollectionTable(name = "db_flag_meta")
    public Map<String, String> metadata;

}
