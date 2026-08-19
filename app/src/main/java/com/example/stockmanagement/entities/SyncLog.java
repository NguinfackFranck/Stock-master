package com.example.stockmanagement.entities;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.stockmanagement.database.TimeConverters;
import com.example.stockmanagement.enums.Action;

import java.util.Date;

@Entity(tableName = "sync_log")
@TypeConverters({TimeConverters.class})
public class SyncLog {
    public String getTable_name() {
        return table_name;
    }

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String table_name;
    private Long record_id;
    private Action action;
    private Date synced_at;
    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecord_id() {
        return record_id;
    }

    public void setRecord_id(Long record_id) {
        this.record_id = record_id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Date getSynced_at() {
        return synced_at;
    }

    public void setSynced_at(Date synced_at) {
        this.synced_at = synced_at;
    }}


