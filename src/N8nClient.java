import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ============================================================
 *  N8nClient.java — Envoi des alertes vers le Webhook n8n
 * ============================================================
 *  Cette classe construit la requête HTTP POST et l'envoie
 *  au Webhook n8n. Elle utilise uniquement java.net (standard).
 *
 *  FONCTIONNEMENT :
 *  1. Convertit l'alerte en JSON (via Alert.toJson())
 *  2. Ouvre une connexion HTTP vers l'URL du Webhook
 *  3. Envoie le JSON dans le corps de la requête (body)
 *  4. Lit le code de réponse HTTP (200 = succès)
 *  5. Gère les erreurs réseau proprement
 *
 *  AUCUN FICHIER JSON N'EST CRÉÉ :
 *  Le JSON transite directement en mémoire (String) vers n8n.
 * ============================================================
 */
public class N8nClient {

    // URL du Webhook n8n (transmise depuis Main.java)
    private final String webhookUrl;

    // Temps maximum d'attente pour établir la connexion (en ms)
    private static final int CONNECT_TIMEOUT_MS = 5000; // 5 secondes

    // Temps maximum d'attente pour recevoir une réponse (en ms)
    private static final int READ_TIMEOUT_MS = 5000; // 5 secondes

    /**
     * Constructeur.
     *
     * @param webhookUrl URL du Webhook n8n (ex: http://localhost:5678/webhook/xxx)
     */
    public N8nClient(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MÉTHODE PRINCIPALE : Envoyer une alerte
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Envoie une alerte au Webhook n8n via HTTP POST.
     *
     * @param alert L'alerte à envoyer
     * @return true si l'envoi a réussi (HTTP 200 ou 201), false sinon
     */
    public boolean sendAlert(Alert alert) {
        // Étape 1 : Conversion de l'alerte en JSON
        String json = alert.toJson();

        // Affichage du JSON dans la console pour le débogage
        System.out.println("  JSON : " + json);

        try {
            // Étape 2 : Envoi HTTP POST et récupération du code de réponse
            int statusCode = postJson(json);

            // Étape 3 : Interprétation de la réponse
            if (statusCode == 200 || statusCode == 201) {
                // Succès : n8n a bien reçu l'alerte
                return true;
            } else {
                // n8n a répondu mais avec une erreur
                System.err.println("  n8n a répondu avec le code HTTP : " + statusCode);
                System.err.println("  Vérifiez que votre Webhook est bien activé dans n8n.");
                return false;
            }

        } catch (IOException e) {
            // Erreur réseau : n8n n'est pas démarré, URL incorrecte, etc.
            System.err.println("  Erreur réseau : impossible de contacter n8n.");
            System.err.println("  Cause : " + e.getMessage());
            System.err.println("  → Vérifiez que n8n est démarré sur localhost:5678");
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MÉTHODE INTERNE : HTTP POST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Effectue la requête HTTP POST vers le Webhook n8n.
     *
     * Détail de la requête :
     *   POST http://localhost:5678/webhook/votre-id
     *   Content-Type: application/json; charset=UTF-8
     *   Body: { "serveur": "Server01", "ressource": "CPU", ... }
     *
     * @param json Le JSON à envoyer dans le corps de la requête
     * @return Le code HTTP retourné par n8n (200, 404, 500...)
     * @throws IOException Si la connexion échoue (n8n non démarré, timeout...)
     */
    private int postJson(String json) throws IOException {
        // ── Étape A : Création de la connexion ────────────────────────────────
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // ── Étape B : Configuration de la requête ─────────────────────────────
        connection.setRequestMethod("POST");

        // Content-Type indique à n8n que le corps est du JSON
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // Accept indique qu'on peut recevoir du JSON en réponse
        connection.setRequestProperty("Accept", "application/json");

        // Timeouts pour éviter de bloquer le programme si n8n ne répond pas
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);

        // Indique qu'on va envoyer des données dans le corps (body)
        connection.setDoOutput(true);

        // ── Étape C : Écriture du JSON dans le corps de la requête ────────────
        // On convertit la String JSON en bytes UTF-8 pour l'envoi
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(jsonBytes);
            outputStream.flush(); // S'assure que tout est bien envoyé
        }

        // ── Étape D : Lecture du code de réponse HTTP ─────────────────────────
        // C'est seulement à ce moment que la connexion est réellement établie
        int statusCode = connection.getResponseCode();

        // ── Étape E : Lecture du corps de la réponse (pour le débogage) ───────
        // Optionnel mais utile pour comprendre les erreurs n8n
        if (statusCode != 200 && statusCode != 201) {
            try {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {

          String errorBody = readStream(errorStream);                    
              System.err.println("  Réponse n8n : " + errorBody);
                }
            } catch (Exception ignored) {
                // On ignore les erreurs de lecture de la réponse d'erreur
            }
        }

        // ── Étape F : Fermeture propre de la connexion ────────────────────────
        connection.disconnect();

        return statusCode;
    }
    /**
 * Lit tout le contenu d'un InputStream et le retourne en String.
 * Compatible Java 8 (readAllBytes n'existe qu'à partir de Java 9).
 */
private String readStream(InputStream stream) throws IOException {
    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
    byte[] data = new byte[1024];
    int nRead;
    while ((nRead = stream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
    }
    buffer.flush();
    return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
}
}
