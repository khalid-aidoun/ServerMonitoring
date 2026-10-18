import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================
 *  ServerMonitor.java — Boucle principale de surveillance
 * ============================================================
 *  Cette classe orchestre tout le système :
 *  1. Elle tourne en boucle infinie (toutes les N secondes)
 *  2. Elle déclenche la collecte des métriques
 *  3. Elle déclenche l'analyse des seuils
 *  4. Elle gère l'envoi des alertes (avec déduplication)
 *  5. Elle détecte le retour à la normale
 * ============================================================
 */
public class ServerMonitor {

    // Paramètres de configuration
    private final String webhookUrl;
    private final int intervalSeconds;

    // Instances des classes spécialisées
    private final ResourceCollector collector;    // Lit les métriques
    private final ResourceAnalyzer  analyzer;     // Analyse les seuils
    private final AlertManager      alertManager; // Évite les alertes répétées
    private final N8nClient         n8nClient;    // Envoie les alertes à n8n

    // Formateur de date pour l'affichage dans la console
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructeur : initialise tous les composants du moniteur.
     *
     * @param webhookUrl      URL du Webhook n8n
     * @param intervalSeconds Durée de la pause entre chaque cycle (secondes)
     */
    public ServerMonitor(String webhookUrl, int intervalSeconds) {
        this.webhookUrl      = webhookUrl;
        this.intervalSeconds = intervalSeconds;

        // Initialisation des composants
        this.collector    = new ResourceCollector();
        this.analyzer     = new ResourceAnalyzer();
        this.alertManager = new AlertManager();
        this.n8nClient    = new N8nClient(webhookUrl);
    }

    /**
     * Démarre la surveillance en boucle infinie.
     * Le programme tourne jusqu'à ce qu'il soit arrêté manuellement (Ctrl+C).
     */
    public void start() {
        System.out.println("Surveillance démarrée. Appuyez sur Ctrl+C pour arrêter.\n");

        while (true) {
            try {
                // Exécuter un cycle complet de surveillance
                runCycle();

                // Attendre avant le prochain cycle
                Thread.sleep(intervalSeconds * 1000L);

            } catch (InterruptedException e) {
                System.err.println("Surveillance interrompue : " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Exécute UN cycle complet de surveillance :
     *   Collecte → Affichage → Analyse → Alertes → Réinitialisation
     */
    private void runCycle() {
        // Affichage de l'heure du cycle
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println("─── " + timestamp + " ───────────────────────────────────");

        // ── ÉTAPE 1 : Collecte des métriques ──────────────────────────────────
        double cpu  = collector.getCpuUsage();
        double ram  = collector.getRamUsage();
        double disk = collector.getDiskUsage();

        // ── ÉTAPE 2 : Affichage des valeurs dans la console ───────────────────
        // Le symbole ▲ indique que le seuil est dépassé
        String cpuStatus  = cpu  > ResourceAnalyzer.CPU_THRESHOLD  ? " ▲ SEUIL DÉPASSÉ" : "";
        String ramStatus  = ram  > ResourceAnalyzer.RAM_THRESHOLD  ? " ▲ SEUIL DÉPASSÉ" : "";
        String diskStatus = disk > ResourceAnalyzer.DISK_THRESHOLD ? " ▲ SEUIL DÉPASSÉ" : "";

        System.out.printf("  CPU   : %5.1f%%  (seuil : %.0f%%)%s%n",
            cpu,  ResourceAnalyzer.CPU_THRESHOLD,  cpuStatus);
        System.out.printf("  RAM   : %5.1f%%  (seuil : %.0f%%)%s%n",
            ram,  ResourceAnalyzer.RAM_THRESHOLD,  ramStatus);
        System.out.printf("  Disque: %5.1f%%  (seuil : %.0f%%)%s%n",
            disk, ResourceAnalyzer.DISK_THRESHOLD, diskStatus);

        // ── ÉTAPE 3 : Analyse des dépassements de seuil ───────────────────────
        // L'analyseur retourne une liste des alertes à traiter
        List<Alert> alerts = analyzer.analyze(cpu, ram, disk);

        // ── ÉTAPE 4 : Traitement de chaque alerte détectée ────────────────────
        if (alerts.isEmpty()) {
            System.out.println("  Situation normale — aucune alerte.");
        }

        for (Alert alert : alerts) {
            String resource = alert.getResourceType();

            // Vérification de la déduplication
            if (alertManager.shouldSend(resource)) {
                // Première occurrence de cet incident → on envoie l'alerte
                System.out.println("  [ALERTE] " + alert.getMessage());
                System.out.println("  Envoi de l'alerte à n8n...");

                boolean success = n8nClient.sendAlert(alert);

                if (success) {
                    // Marquer comme envoyée pour bloquer les renvois
                    alertManager.markAsSent(resource);
                    System.out.println("  [OK] Alerte envoyée avec succès à n8n.");
                } else {
                    // L'envoi a échoué → on ne marque PAS comme envoyée
                    // (on réessaiera au prochain cycle)
                    System.out.println("  [ERREUR] Impossible d'envoyer l'alerte à n8n.");
                }

            } else {
                // Alerte déjà active → on ne renvoie pas
                System.out.println("  [PAUSE] Alerte " + resource
                    + " déjà active — pas de renvoi.");
            }
        }

        // ── ÉTAPE 5 : Réinitialisation si retour à la normale ─────────────────
        // Si la ressource est repassée sous le seuil, on autorise une future alerte
        alertManager.resetIfResolved("CPU",  cpu  < ResourceAnalyzer.CPU_THRESHOLD);
        alertManager.resetIfResolved("RAM",  ram  < ResourceAnalyzer.RAM_THRESHOLD);
        alertManager.resetIfResolved("DISK", disk < ResourceAnalyzer.DISK_THRESHOLD);

        System.out.println(); // Ligne vide pour la lisibilité
    }
}
