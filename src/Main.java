/**
 * ============================================================
 *  Main.java — Point d'entrée du programme
 * ============================================================
 *  Ce fichier configure les paramètres principaux du moniteur
 *  et démarre la surveillance automatique.
 *
 *  CONFIGURATION RAPIDE :
 *  1. Modifiez N8N_WEBHOOK_URL avec l'URL fournie par n8n
 *  2. Modifiez INTERVAL_SECONDS si vous voulez vérifier plus
 *     ou moins souvent (ex : 30 pour toutes les 30 secondes)
 * ============================================================
 */
public class Main {

    // ─────────────────────────────────────────────────────────────────────────
    //  URL DU WEBHOOK N8N
    //  Copiez l'URL depuis votre Workflow n8n (nœud "Webhook")
    //  Exemple : http://localhost:5678/webhook/abc123
    // ─────────────────────────────────────────────────────────────────────────
private static final String N8N_WEBHOOK_URL = "http://localhost:5678/webhook/server-alert";

    // ─────────────────────────────────────────────────────────────────────────
    //  INTERVALLE DE SURVEILLANCE
    //  Durée en secondes entre chaque cycle de collecte des métriques.
    //  10 secondes = bonne valeur pour les tests
    // ─────────────────────────────────────────────────────────────────────────
    private static final int INTERVAL_SECONDS = 5;

    public static void main(String[] args) {
        // Affichage du bandeau de démarrage
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     Moniteur de Ressources Serveur — Java        ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("  Webhook n8n  : " + N8N_WEBHOOK_URL);
        System.out.println("  Intervalle   : " + INTERVAL_SECONDS + " secondes");
        System.out.println("  Seuils       : CPU=" + ResourceAnalyzer.CPU_THRESHOLD + "%"
                         + "  RAM=" + ResourceAnalyzer.RAM_THRESHOLD + "%"
                         + "  Disque=" + ResourceAnalyzer.DISK_THRESHOLD + "%");
        System.out.println("──────────────────────────────────────────────────\n");

        // Création du moniteur et démarrage de la boucle infinie
        ServerMonitor monitor = new ServerMonitor(N8N_WEBHOOK_URL, INTERVAL_SECONDS);
        monitor.start();
    }
}
