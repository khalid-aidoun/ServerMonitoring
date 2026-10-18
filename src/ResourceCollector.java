import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

/**
 * ============================================================
 *  ResourceCollector.java — Collecte des métriques système
 * ============================================================
 *  Cette classe lit les ressources directement depuis l'OS
 *  via les APIs Java standard. Aucune bibliothèque externe.
 *
 *  APIS UTILISÉES :
 *  - CPU  : com.sun.management.OperatingSystemMXBean
 *            (fourni par le JDK, pas besoin de télécharger)
 *  - RAM  : com.sun.management.OperatingSystemMXBean
 *  - Disk : java.io.File (méthodes getTotalSpace, getFreeSpace)
 * ============================================================
 */
public class ResourceCollector {

    // Bean de gestion de l'OS fourni par la JVM
    // Permet de lire le CPU et la RAM via des méthodes simples
    private final OperatingSystemMXBean osBean;

    /**
     * Constructeur : récupère le bean système au démarrage.
     */
    public ResourceCollector() {
        // ManagementFactory donne accès aux informations de la JVM et de l'OS
        // Le cast vers OperatingSystemMXBean (com.sun) donne accès au CPU système
        this.osBean = (OperatingSystemMXBean)
            ManagementFactory.getOperatingSystemMXBean();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CPU
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le pourcentage d'utilisation CPU du système entier.
     *
     * Valeur de retour : 0.0 à 100.0
     * Exemple : 72.3 signifie que le CPU est utilisé à 72,3%
     *
     * Note : au tout premier appel, la JVM peut retourner -1 car elle n'a pas
     * encore eu le temps de mesurer. Dans ce cas, on retourne 0.
     */
    public double getCpuUsage() {
        // getCpuLoad() retourne une valeur entre 0.0 (0%) et 1.0 (100%)
       double load = osBean.getSystemCpuLoad();
        // Si la valeur n'est pas encore disponible, on retourne 0
        if (load < 0) {
            return 0.0;
        }

        // Conversion en pourcentage (multiplié par 100)
        return load * 100.0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RAM
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le pourcentage d'utilisation de la RAM du système.
     *
     * Calcul : (RAM totale - RAM libre) / RAM totale × 100
     *
     * Valeur de retour : 0.0 à 100.0
     * Exemple : 65.4 signifie que 65,4% de la RAM est utilisée
     */
    public double getRamUsage() {
        long totalRam = osBean.getTotalPhysicalMemorySize();
long freeRam  = osBean.getFreePhysicalMemorySize();
        long usedRam  = totalRam - freeRam;          // RAM utilisée en octets

        // Protection contre une division par zéro (cas théorique)
        if (totalRam == 0) {
            return 0.0;
        }

        // Calcul du pourcentage
        return ((double) usedRam / totalRam) * 100.0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DISQUE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le pourcentage d'utilisation du disque principal.
     *
     * Sur Windows : surveille le disque C:\
     * Sur Linux / macOS : surveille la partition racine /
     *
     * Calcul : (espace total - espace libre) / espace total × 100
     *
     * Valeur de retour : 0.0 à 100.0
     * Exemple : 45.0 signifie que 45% du disque est utilisé
     */
    public double getDiskUsage() {
        File root;

        // Détection automatique du système d'exploitation
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows : disque C:\
            root = new File("C:\\");
        } else {
            // Linux ou macOS : partition racine /
            root = new File("/");
        }

        long totalSpace = root.getTotalSpace(); // Espace total en octets
        long freeSpace  = root.getFreeSpace();  // Espace libre en octets
        long usedSpace  = totalSpace - freeSpace; // Espace utilisé en octets

        // Protection contre une division par zéro
        if (totalSpace == 0) {
            return 0.0;
        }

        // Calcul du pourcentage
        return ((double) usedSpace / totalSpace) * 100.0;
    }
}
