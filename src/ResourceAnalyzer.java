import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  ResourceAnalyzer.java — Analyse des seuils
 * ============================================================
 *  Cette classe compare les métriques collectées aux seuils
 *  définis et retourne la liste des alertes à traiter.
 *
 *  ╔══════════════════════════════════════════════════════╗
 *  ║   MODE TEST (prototype sur PC personnel)             ║
 *  ║   CPU_THRESHOLD  = 20.0  → alerte si CPU  > 20%    ║
 *  ║   RAM_THRESHOLD  = 50.0  → alerte si RAM  > 50%    ║
 *  ║   DISK_THRESHOLD = 40.0  → alerte si Disk > 40%    ║
 *  ║   (Remettez 80/80/90 pour la production)            ║
 *  ╚══════════════════════════════════════════════════════╝
 * ============================================================
 */
public class ResourceAnalyzer {

    // ─────────────────────────────────────────────────────────────────────────
    //  SEUILS D'ALERTE (en pourcentage — faciles à modifier)
    // ─────────────────────────────────────────────────────────────────────────

    // ══════════════════════════════════════════════════════════════
    //  MODE TEST — seuils bas pour un prototype sur PC personnel
    //  Votre PC dépasse facilement ces valeurs → parfait pour tester
    //  ──────────────────────────────────────────────────────────────
    //  Pour la PRODUCTION (vrai serveur), remplacez par :
    //  CPU_THRESHOLD = 80.0 | RAM_THRESHOLD = 80.0 | DISK_THRESHOLD = 90.0
    // ══════════════════════════════════════════════════════════════

    /** Seuil CPU : alerte si CPU > 20% (votre PC dépasse souvent 20% au repos) */
    public static final double CPU_THRESHOLD  = 10.0;

    /** Seuil RAM : alerte si RAM > 50% (PC 8 Go → souvent 4+ Go utilisés) */
    public static final double RAM_THRESHOLD  = 10.0;

    /** Seuil disque : alerte si Disque > 40% (la plupart des PC dépassent 40%) */
    public static final double DISK_THRESHOLD = 10.0;

    // ─────────────────────────────────────────────────────────────────────────
    //  NOM DU SERVEUR
    // ─────────────────────────────────────────────────────────────────────────

    /** Nom du serveur qui apparaîtra dans chaque alerte et dans le JSON */
    private static final String SERVER_NAME = "Server01";

    /**
     * Analyse les trois métriques et retourne la liste des alertes détectées.
     *
     * Si aucun seuil n'est dépassé, la liste retournée est vide.
     * Si plusieurs seuils sont dépassés en même temps, plusieurs alertes sont retournées.
     *
     * @param cpu  Pourcentage d'utilisation CPU  (ex: 85.3)
     * @param ram  Pourcentage d'utilisation RAM  (ex: 72.1)
     * @param disk Pourcentage d'utilisation Disk (ex: 91.0)
     * @return Liste des alertes à traiter (peut être vide)
     */
    public List<Alert> analyze(double cpu, double ram, double disk) {
        List<Alert> alerts = new ArrayList<>();

        // ── Vérification CPU ──────────────────────────────────────────────────
        if (cpu > CPU_THRESHOLD) {
            alerts.add(new Alert(
                SERVER_NAME,
                "CPU",    // Type de ressource (utilisé comme clé dans AlertManager)
                cpu,
                CPU_THRESHOLD,
                String.format("CPU trop élevé : %.1f%% (seuil : %.0f%%)", cpu, CPU_THRESHOLD)
            ));
        }

        // ── Vérification RAM ──────────────────────────────────────────────────
        if (ram > RAM_THRESHOLD) {
            alerts.add(new Alert(
                SERVER_NAME,
                "RAM",
                ram,
                RAM_THRESHOLD,
                String.format("RAM trop élevée : %.1f%% (seuil : %.0f%%)", ram, RAM_THRESHOLD)
            ));
        }

        // ── Vérification Disque ───────────────────────────────────────────────
        if (disk > DISK_THRESHOLD) {
            alerts.add(new Alert(
                SERVER_NAME,
                "DISK",
                disk,
                DISK_THRESHOLD,
                String.format("Espace disque critique : %.1f%% (seuil : %.0f%%)", disk, DISK_THRESHOLD)
            ));
        }

        return alerts;
    }
}
