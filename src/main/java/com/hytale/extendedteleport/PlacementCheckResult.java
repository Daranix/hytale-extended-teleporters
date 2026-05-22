package com.hytale.extendedteleport;

public final class PlacementCheckResult {
    private final boolean allowed;
    private final String errorMessage;

    public PlacementCheckResult(boolean allowed, String errorMessage) {
        this.allowed = allowed; this.errorMessage = errorMessage;
    }

    public boolean allowed() { return this.allowed; }
    public String errorMessage() { return this.errorMessage; }

    public static final PlacementCheckResult ALLOWED = new PlacementCheckResult(true, null);

    public static PlacementCheckResult denied(String message) {
        return new PlacementCheckResult(false, message);
    }
}