package com.smartresponsor.core.entitlement;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u001b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\n\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000b"}, d2 = {"Lcom/smartresponsor/core/entitlement/EntitlementDao;", "", "byKey", "Lcom/smartresponsor/core/entitlement/EntitlementEntity;", "k", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "", "e", "(Lcom/smartresponsor/core/entitlement/EntitlementEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "entitlement_debug"})
@androidx.room.Dao
public abstract interface EntitlementDao {
    
    @androidx.room.Query(value = "SELECT * FROM entitlement WHERE featureKey=:k LIMIT 1")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object byKey(@org.jetbrains.annotations.NotNull
    java.lang.String k, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.smartresponsor.core.entitlement.EntitlementEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull
    com.smartresponsor.core.entitlement.EntitlementEntity e, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}