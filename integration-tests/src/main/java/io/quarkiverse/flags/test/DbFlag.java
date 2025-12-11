package io.quarkiverse.flags.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import io.quarkiverse.flags.hibernate.common.FlagDefinition;
import io.quarkiverse.flags.hibernate.common.FlagFeature;
import io.quarkiverse.flags.hibernate.common.FlagMetadata;
import io.quarkiverse.flags.hibernate.common.FlagValue;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@FlagDefinition
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
