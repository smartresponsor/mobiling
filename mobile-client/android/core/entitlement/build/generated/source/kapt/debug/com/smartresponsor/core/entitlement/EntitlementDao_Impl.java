package com.smartresponsor.core.entitlement;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EntitlementDao_Impl implements EntitlementDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EntitlementEntity> __insertionAdapterOfEntitlementEntity;

  public EntitlementDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEntitlementEntity = new EntityInsertionAdapter<EntitlementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `entitlement` (`id`,`featureKey`,`status`,`grantedUntil`,`grantedAt`,`source`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EntitlementEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getFeatureKey() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getFeatureKey());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getStatus());
        }
        if (entity.getGrantedUntil() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getGrantedUntil());
        }
        statement.bindLong(5, entity.getGrantedAt());
        if (entity.getSource() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSource());
        }
      }
    };
  }

  @Override
  public Object upsert(final EntitlementEntity e, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEntitlementEntity.insert(e);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object byKey(final String k, final Continuation<? super EntitlementEntity> $completion) {
    final String _sql = "SELECT * FROM entitlement WHERE featureKey=? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (k == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, k);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EntitlementEntity>() {
      @Override
      @Nullable
      public EntitlementEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFeatureKey = CursorUtil.getColumnIndexOrThrow(_cursor, "featureKey");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfGrantedUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "grantedUntil");
          final int _cursorIndexOfGrantedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "grantedAt");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final EntitlementEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpFeatureKey;
            if (_cursor.isNull(_cursorIndexOfFeatureKey)) {
              _tmpFeatureKey = null;
            } else {
              _tmpFeatureKey = _cursor.getString(_cursorIndexOfFeatureKey);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final Long _tmpGrantedUntil;
            if (_cursor.isNull(_cursorIndexOfGrantedUntil)) {
              _tmpGrantedUntil = null;
            } else {
              _tmpGrantedUntil = _cursor.getLong(_cursorIndexOfGrantedUntil);
            }
            final long _tmpGrantedAt;
            _tmpGrantedAt = _cursor.getLong(_cursorIndexOfGrantedAt);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            _result = new EntitlementEntity(_tmpId,_tmpFeatureKey,_tmpStatus,_tmpGrantedUntil,_tmpGrantedAt,_tmpSource);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
