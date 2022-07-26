package com.softawii.capivara.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class VoiceHive {

    @Embeddable
    public static class HiveKey implements Serializable {
        /**
         * every category has a snowflake id, it's used to identify the category
         */
        @Column
        Long categoryId;

        /**
         * This column is not necessary, but it's used to identify all hives in the guild.
         */
        @Column
        Long guildId;

        // region Constructors

        public HiveKey() {
        }

        public HiveKey(Long categoryId, Long guildId) {
            this.categoryId = categoryId;
            this.guildId = guildId;
        }

        // endregion

        // region Getters and Setters

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public Long getGuildId() {
            return guildId;
        }

        public void setGuildId(Long guildId) {
            this.guildId = guildId;
        }


        // endregion

        // region Overrides

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HiveKey that = (HiveKey) o;
            return Objects.equals(categoryId, that.categoryId) && Objects.equals(guildId, that.guildId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categoryId, guildId);
        }

        // endregion
    }

    @EmbeddedId
    HiveKey hiveKey;

    /**
     * Category needs to have a hive channel, this is the channel where the bot will listen
     */
    @Column
    private long hiveId;

    // region Constructors

    public VoiceHive() {
    }

    public VoiceHive(HiveKey hiveKey, long hiveId) {
        this.hiveKey = hiveKey;
        this.hiveId = hiveId;
    }

    // endregion

    // region Getters and Setters

    public HiveKey hiveKey() {
        return hiveKey;
    }

    public void setHiveKey(HiveKey hiveKey) {
        this.hiveKey = hiveKey;
    }

    public long hiveId() {
        return hiveId;
    }

    public void setHiveId(long hiveId) {
        this.hiveId = hiveId;
    }

    // endregion
}
