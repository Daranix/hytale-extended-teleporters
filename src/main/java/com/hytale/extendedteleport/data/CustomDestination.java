package com.hytale.extendedteleport.data;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public final class CustomDestination
{
    private final String name;
    private final String dimension;
    private final double x;
    private final double y;
    private final double z;
    private final long createdTimestamp;
    @Nullable
    private String createdBy;

    public CustomDestination(@Nonnull String name, @Nonnull String dimension, double x, double y, double z, long createdTimestamp, @Nullable String createdBy) {
        this.name = Objects.<String>requireNonNull(name, "name cannot be null");
        this.dimension = Objects.<String>requireNonNull(dimension, "dimension cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.createdTimestamp = createdTimestamp;
        this.createdBy = createdBy;
    }

    public CustomDestination(@Nonnull String name, @Nonnull String dimension, double x, double y, double z) {
        this(name, dimension, x, y, z, System.currentTimeMillis(), null);
    }

    public String name() {
        return this.name;
    }

    public String dimension() {
        return this.dimension;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public int blockX() {
        return (int)Math.floor(this.x);
    }

    public int blockY() {
        return (int)Math.floor(this.y);
    }

    public int blockZ() {
        return (int)Math.floor(this.z);
    }

    public long createdTimestamp() {
        return this.createdTimestamp;
    }
    @Nullable
    public String createdBy() {
        return this.createdBy;
    }

    public void setCreatedBy(@Nullable String createdBy) {
        this.createdBy = createdBy;
    }


    public String displayName() {
        return "%s (%s: %.1f, %.1f, %.1f)".formatted(new Object[] { this.name, this.dimension, Double.valueOf(this.x), Double.valueOf(this.y), Double.valueOf(this.z) });
    }


    public String toString() {
        return "CustomDestination[name=%s, dimension=%s, pos=(%.1f, %.1f, %.1f)]"
        .formatted(new Object[] { this.name, this.dimension, Double.valueOf(this.x), Double.valueOf(this.y), Double.valueOf(this.z) });
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomDestination that = (CustomDestination)o;
        return this.name.equalsIgnoreCase(that.name);
    }


    public int hashCode() {
        return this.name.toLowerCase().hashCode();
    }
}