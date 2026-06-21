package com.smartresponsor.core.entitlement;

/**
 * Legacy-compatible Android entry point bridged to canonical system/entitlement slices.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/smartresponsor/core/entitlement/Entitlement;", "", "gateway", "Lcom/smartresponsor/mobile/client/data/system/entitlement/EntitlementSnapshotGateway;", "(Lcom/smartresponsor/mobile/client/data/system/entitlement/EntitlementSnapshotGateway;)V", "refreshEntitlementSnapshotUseCase", "Lcom/smartresponsor/mobile/client/usecase/system/entitlement/RefreshEntitlementSnapshotUseCase;", "refresh", "Lcom/smartresponsor/mobile/client/contract/system/entitlement/EntitlementSnapshot;", "subjectId", "", "entitlement_debug"})
public final class Entitlement {
    @org.jetbrains.annotations.NotNull
    private final com.smartresponsor.mobile.client.data.system.entitlement.EntitlementSnapshotGateway gateway = null;
    @org.jetbrains.annotations.NotNull
    private final com.smartresponsor.mobile.client.usecase.system.entitlement.RefreshEntitlementSnapshotUseCase refreshEntitlementSnapshotUseCase = null;
    
    public Entitlement(@org.jetbrains.annotations.NotNull
    com.smartresponsor.mobile.client.data.system.entitlement.EntitlementSnapshotGateway gateway) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.smartresponsor.mobile.client.contract.system.entitlement.EntitlementSnapshot refresh(@org.jetbrains.annotations.NotNull
    java.lang.String subjectId) {
        return null;
    }
    
    public Entitlement() {
        super();
    }
}