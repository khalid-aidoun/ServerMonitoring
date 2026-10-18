import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 *  AlertManager.java — Gestion de la déduplication des alertes
 * ============================================================
 *  Cette classe évite qu'une même alerte soit envoyée plusieurs
 *  fois en boucle pour le même incident.
 *
 *  LOGIQUE COMPLÈTE :
 *  ┌──────────────────────────────────────────────────────────┐
 *  │ Cycle 1 : CPU = 92%  → alerte envoyée (première fois)  │
 *  │ Cycle 2 : CPU = 88%  → alerte bloquée (déjà envoyée)   │
 *  │ Cycle 3 : CPU = 75%  → retour normal, état réinitialisé │
 *  │ Cycle 4 : CPU = 95%  → alerte envoyée (nouvel incident) │
 *  └──────────────────────────────────────────────────────────┘
 * ============================================================
 */
public class AlertManager {

    /**
     * Map mémorisant l'état de chaque ressource.
     *
     * Clé   : nom de la ressource ("CPU", "RAM", "DISK")
     * Valeur: true  → alerte déjà envoyée (incident en cours, on bloque)
     *         false → aucune alerte active (on peut envoyer)
     */
    private final Map<String, Boolean> alertSent = new HashMap<>();

    /**
     * Constructeur : initialise toutes les ressources à l'état "normal".
     * Au démarrage, aucune alerte n'est considérée comme active.
     */
    public AlertManager() {
        alertSent.put("CPU",  false); // Pas d'alerte CPU active au départ
        alertSent.put("RAM",  false); // Pas d'alerte RAM active au départ
        alertSent.put("DISK", false); // Pas d'alerte Disque active au départ
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MÉTHODE 1 : Doit-on envoyer l'alerte ?
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne true si l'alerte doit être envoyée.
     *
     * Une alerte doit être envoyée uniquement si :
     * - Le seuil est dépassé (vérifié dans ResourceAnalyzer)
     *   ET
     * - L'alerte n'a PAS encore été envoyée pour cet incident
     *
     * @param resourceType "CPU", "RAM" ou "DISK"
     * @return true → envoyer l'alerte | false → bloquer l'envoi
     */
    public boolean shouldSend(String resourceType) {
        // getOrDefault gère le cas où la clé n'existerait pas (sécurité)
        boolean alreadySent = alertSent.getOrDefault(resourceType, false);
        return !alreadySent; // On envoie si l'alerte N'est PAS déjà active
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MÉTHODE 2 : Marquer l'alerte comme envoyée
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Marque une alerte comme "déjà envoyée" pour bloquer les renvois.
     * Appelée juste après un envoi réussi vers n8n.
     *
     * @param resourceType "CPU", "RAM" ou "DISK"
     */
    public void markAsSent(String resourceType) {
        alertSent.put(resourceType, true);
        // Après cet appel, shouldSend() retournera false pour cette ressource
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MÉTHODE 3 : Réinitialiser si le problème est résolu
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Réinitialise l'état d'une alerte si la ressource est repassée
     * sous son seuil normal.
     *
     * Après cette réinitialisation :
     * - Si le problème réapparaît, une nouvelle alerte sera envoyée
     *
     * @param resourceType "CPU", "RAM" ou "DISK"
     * @param isResolved   true si la valeur est repassée SOUS le seuil
     */
    public void resetIfResolved(String resourceType, boolean isResolved) {
        boolean wasSent = alertSent.getOrDefault(resourceType, false);

        // On réinitialise seulement si :
        // 1. Le problème est résolu (isResolved = true)
        // 2. Une alerte avait bien été envoyée (wasSent = true)
        //    (évite un message console inutile si tout était normal)
        if (isResolved && wasSent) {
            alertSent.put(resourceType, false);
            System.out.println("  [OK] " + resourceType
                + " revenu à la normale — prêt pour une nouvelle alerte si nécessaire.");
        }
    }
}
