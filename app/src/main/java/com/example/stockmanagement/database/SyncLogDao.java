package com.example.stockmanagement.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.stockmanagement.entities.SyncLog;

import java.util.Date;
import java.util.List;

@Dao
public interface SyncLogDao {
    @Insert
    void insertSyncLog(SyncLog syncLog);

    @Query("SELECT * FROM sync_log WHERE table_name = :tableName AND record_id = :recordId")
    SyncLog getSyncLog(String tableName, int recordId);

    @Query("SELECT * FROM sync_log WHERE synced_at IS NULL")
    List<SyncLog> getPendingSyncLogs();

    @Query("UPDATE sync_log SET synced_at = :syncTime WHERE id = :logId")
    void markLogSynced(long logId, Date syncTime);

    @Query("DELETE FROM sync_log WHERE synced_at IS NOT NULL")
    void clearSyncedLogs();

    @Delete
    void deleteSyncLog(SyncLog syncLog);
}
