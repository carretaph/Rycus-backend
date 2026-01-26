package com.rycus.Rycus_backend.milestone;

public class MilestoneProgressDto {

    /**
     * Tipo de milestone (enum name)
     */
    private String milestoneType;

    /**
     * Total de customers calificados acumulados
     * (cap implícito en el service: máx 30)
     */
    private int qualifiedCustomers;

    /**
     * Cuántas veces ya se otorgó el reward
     * (0..3 → meses gratis)
     */
    private int timesAwarded;

    /**
     * Próxima meta visible:
     * 10, 20 o 30
     */
    private int nextRewardAt;

    /**
     * Cuántos faltan para llegar a la próxima meta
     */
    private int remaining;

    // =========================
    // Constructors
    // =========================

    public MilestoneProgressDto() {
    }

    public MilestoneProgressDto(
            String milestoneType,
            int qualifiedCustomers,
            int timesAwarded,
            int nextRewardAt,
            int remaining
    ) {
        this.milestoneType = milestoneType;
        this.qualifiedCustomers = qualifiedCustomers;
        this.timesAwarded = timesAwarded;
        this.nextRewardAt = nextRewardAt;
        this.remaining = remaining;
    }

    // =========================
    // Safe helpers
    // =========================

    /**
     * Cuando el usuario NO está autenticado
     * o aún no tenemos email disponible.
     */
    public static MilestoneProgressDto unauthenticated() {
        return new MilestoneProgressDto(
                MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                0,
                0,
                10,
                10
        );
    }

    /**
     * Fallback seguro cuando ocurre un error interno.
     * IMPORTANTE:
     * - No rompe el dashboard
     * - No cambia el modelo de negocio
     * - Evita regresiones tipo 0/10 arbitrario
     */
    public static MilestoneProgressDto safeFallback() {
        return new MilestoneProgressDto(
                MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                0,
                0,
                10,
                10
        );
    }

    // =========================
    // Getters & Setters
    // =========================

    public String getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(String milestoneType) {
        this.milestoneType = milestoneType;
    }

    public int getQualifiedCustomers() {
        return qualifiedCustomers;
    }

    public void setQualifiedCustomers(int qualifiedCustomers) {
        this.qualifiedCustomers = qualifiedCustomers;
    }

    public int getTimesAwarded() {
        return timesAwarded;
    }

    public void setTimesAwarded(int timesAwarded) {
        this.timesAwarded = timesAwarded;
    }

    public int getNextRewardAt() {
        return nextRewardAt;
    }

    public void setNextRewardAt(int nextRewardAt) {
        this.nextRewardAt = nextRewardAt;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
