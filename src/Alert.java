import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 *  Alert.java — Modèle de données d'une alerte
 * ============================================================
 *  Cette classe représente une alerte détectée.
 *  Elle contient toutes les informations sur l'incident
 *  et est capable de se convertir elle-même en JSON.
 *
 *  EXEMPLE DE JSON GÉNÉRÉ :
 *  {
 *    "serveur": "Server01",
 *    "ressource": "CPU",
 *    "valeur": 92.5,
 *    "seuil": 80.0,
 *    "message": "CPU trop élevé : 92.5% (seuil : 80%)",
 *    "timestamp": "2024-09-25T15:30:00"
 *  }
 * ============================================================
 */
public class Alert {

    // ─── Champs de l'alerte ───────────────────────────────────────────────────

    private final String serverName;    // Nom du serveur (ex : "Server01")
    private final String resourceType;  // Type : "CPU", "RAM" ou "DISK"
    private final double value;         // Valeur mesurée (ex : 92.5)
    private final double threshold;     // Seuil dépassé (ex : 80.0)
    private final String message;       // Message lisible (ex : "CPU trop élevé...")
    private final String timestamp;     // Heure de détection au format ISO

    // Formateur de date au format ISO 8601 (standard international)
    private static final DateTimeFormatter ISO_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Constructeur : crée une alerte avec toutes ses informations.
     * L'horodatage est généré automatiquement au moment de la création.
     *
     * @param serverName   Nom du serveur
     * @param resourceType "CPU", "RAM" ou "DISK"
     * @param value        Valeur mesurée en pourcentage
     * @param threshold    Seuil dépassé en pourcentage
     * @param message      Message descriptif de l'alerte
     */
    public Alert(String serverName, String resourceType,
                 double value, double threshold, String message) {
        this.serverName   = serverName;
        this.resourceType = resourceType;
        this.value        = value;
        this.threshold    = threshold;
        this.message      = message;
        // L'heure est capturée au moment exact de la création de l'alerte
        this.timestamp    = LocalDateTime.now().format(ISO_FORMATTER);
    }

    // ─── Getters (accesseurs) ─────────────────────────────────────────────────

    public String getServerName()   { return serverName;   }
    public String getResourceType() { return resourceType; }
    public double getValue()        { return value;        }
    public double getThreshold()    { return threshold;    }
    public String getMessage()      { return message;      }
    public String getTimestamp()    { return timestamp;    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CONVERSION EN JSON
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convertit l'alerte en chaîne JSON.
     *
     * Le JSON est construit manuellement avec des concaténations de chaînes.
     * Cela permet d'éviter toute bibliothèque externe (comme Gson ou Jackson).
     *
     * @return La représentation JSON de cette alerte (String)
     */
    public String toJson() {
        return "{"
            + "\"serveur\": \""   + serverName                        + "\", "
            + "\"ressource\": \"" + resourceType                      + "\", "
            + "\"valeur\": "      + String.format("%.1f", value)      + ", "
            + "\"seuil\": "       + String.format("%.1f", threshold)  + ", "
            + "\"message\": \""   + escapeJson(message)               + "\", "
            + "\"timestamp\": \"" + timestamp                         + "\""
            + "}";
    }

    /**
     * Échappe les caractères spéciaux dans une chaîne pour qu'elle soit
     * valide à l'intérieur d'une valeur JSON.
     *
     * Sans cet échappement, un guillemet dans le message casserait le JSON.
     * Exemple : "message: "CPU > 80%"" deviendrait invalide.
     *
     * @param text La chaîne à sécuriser
     * @return La chaîne avec les caractères spéciaux échappés
     */
    private String escapeJson(String text) {
        return text
            .replace("\\", "\\\\") // Backslash  → \\
            .replace("\"", "\\\"") // Guillemet  → \"
            .replace("\n", "\\n")  // Saut ligne → \n
            .replace("\r", "\\r"); // Retour chariot → \r
    }

    /**
     * Représentation textuelle de l'alerte pour le débogage.
     */
    @Override
    public String toString() {
        return "Alert{"
            + "resource=" + resourceType
            + ", value="  + value
            + ", msg='"   + message + "'"
            + "}";
    }
}
