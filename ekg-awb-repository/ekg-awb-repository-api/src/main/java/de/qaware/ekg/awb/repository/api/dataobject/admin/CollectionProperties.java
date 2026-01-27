package de.qaware.ekg.awb.repository.api.dataobject.admin;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;

/**
 * A container that stores properties for a search index collection.
 */
public class CollectionProperties {

    private String collectionName;
    private String collectionAlias;
    private String configIdentifier;
    private String routerField;
    private int amountOfShards;
    private int amountOfReplicas;

    /**
     * Returns the name of the collection.
     *
     * @return the name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the collection.
     *
     * @param collectionName the name
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * The alias that will be assigned to the collection. Aliases work similar to a unix symbolic link.
     * An alias points to an existing collection and is used as an indirection when accessing the collection.
     * Aliases allow to quickly switch between collections in a way that is transparent for the user of the collection.
     *
     * @return the alias
     */
    public String getCollectionAlias() {
        return collectionAlias;
    }

    /**
     * Sets the alias that will be assigned to the collection. See {@link #getCollectionAlias()}.
     *
     * @param collectionAlias the alias
     */
    public void setCollectionAlias(String collectionAlias) {
        this.collectionAlias = collectionAlias;
    }

    /**
     * Returns the amount of pieces the collection is divided into.
     *
     * @return the amount of shards
     */
    public int getAmountOfShards() {
        return amountOfShards;
    }

    /**
     * Sets the amount of pieces the collection is divided into.
     *
     * @param amountOfShards the amount of shards
     */
    public void setAmountOfShards(int amountOfShards) {
        this.amountOfShards = amountOfShards;
    }

    /**
     * Returns the amount of copies of the collection. Replicas are used to improve availability and/or scalability.
     *
     * @return the number of replicas
     */
    public int getAmountOfReplicas() {
        return amountOfReplicas;
    }

    /**
     * Sets the amount of copies of the collection. Replicas are used to improve availability and/or scalability.
     *
     * @param amountOfReplicas the number of replicas
     */
    public void setAmountOfReplicas(int amountOfReplicas) {
        this.amountOfReplicas = amountOfReplicas;
    }

    /**
     * Returns the unique name of the config set that belongs to the collection.
     *
     * @return the unique key of the collection configuration
     */
    public String getConfigIdentifier() {
        return configIdentifier;
    }

    /**
     * Sets the unique name of the config set that belongs to the collection.
     *
     * @param configIdentifier the unique key of the collection configuration
     */
    public void setConfigIdentifier(String configIdentifier) {
        this.configIdentifier = configIdentifier;
    }

    /**
     * Returns the name of the router field.
     * <p>
     * If this field is set, the Collection will use this field for shard routing, i.e. the Search Index
     * will automatically route documents to shards based on the hash value of the document's value in this field.
     * <p>
     * If this field is not set, the sharding strategy is up to the Search Index.
     *
     * @return the router field
     */
    public Optional<String> getRouterField() {
        if (routerField == null) {
            return Optional.empty();
        } else {
            return Optional.of(routerField);
        }
    }

    /**
     * Sets the name of the router field.
     * <p>
     * If this field is set, the Collection will use this field for shard routing, i.e. the Search Index
     * will automatically route documents to shards based on the hash value of the document's value in this field.
     * <p>
     * If this field is not set, the sharding strategy is up to the Search Index.
     *
     * @param routerField the router field
     */
    public void setRouterField(String routerField) {
        this.routerField = routerField;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CollectionProperties rhs = (CollectionProperties) obj;
        return new EqualsBuilder()
                .append(this.collectionName, rhs.collectionName)
                .append(this.collectionAlias, rhs.collectionAlias)
                .append(this.configIdentifier, rhs.configIdentifier)
                .append(this.amountOfShards, rhs.amountOfShards)
                .append(this.amountOfReplicas, rhs.amountOfReplicas)
                .append(this.routerField, rhs.routerField)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.collectionName)
                .append(this.collectionAlias)
                .append(this.configIdentifier)
                .append(this.amountOfShards)
                .append(this.amountOfReplicas)
                .append(this.routerField)
                .toHashCode();
    }
}
