# Moniteur de Ressources Serveur — Java

Système de surveillance des ressources serveur (CPU, RAM, Disque) avec envoi d'alertes via Webhook n8n.

---

## Structure du projet

```
ServerMonitoring/
├── src/
│   ├── Main.java              ← Point d'entrée — configurez l'URL du Webhook ici
│   ├── ServerMonitor.java     ← Boucle principale de surveillance
│   ├── ResourceCollector.java ← Lecture CPU / RAM / Disque
│   ├── ResourceAnalyzer.java  ← Seuils d'alerte (modifiables ici)
│   ├── Alert.java             ← Modèle de données + conversion JSON
│   ├── AlertManager.java      ← Déduplication des alertes
│   └── N8nClient.java         ← Envoi HTTP POST vers n8n
└── README.md
```

---

## Configuration rapide

### 1. Modifier l'URL du Webhook n8n
Dans `src/Main.java`, ligne 18 :
```java
private static final String N8N_WEBHOOK_URL =
    "http://localhost:5678/webhook/votre-webhook-id";
//                                   ↑ Remplacez par votre ID
```

### 2. Modifier les seuils d'alerte
Dans `src/ResourceAnalyzer.java`, lignes 32-36 :
```java
public static final double CPU_THRESHOLD  = 80.0;  // %
public static final double RAM_THRESHOLD  = 80.0;  // %
public static final double DISK_THRESHOLD = 90.0;  // %
```

### 3. Modifier l'intervalle de surveillance
Dans `src/Main.java`, ligne 27 :
```java
private static final int INTERVAL_SECONDS = 10; // secondes
```

---

## Compilation et exécution

### Prérequis
- Java JDK 11 ou supérieur
- n8n installé et démarré (`n8n start`)

### Compiler (depuis le dossier ServerMonitoring/)

**Windows (PowerShell) :**
```powershell
cd src
javac -source 11 -target 11 *.java
```

### Exécuter

**Windows (PowerShell) :**
```powershell
# Depuis le dossier src/
java Main
```

**Avec le flag nécessaire pour com.sun.management (JDK 17+) :**
```powershell
java --add-opens java.management/com.sun.management=ALL-UNNAMED Main
```

---

## Exemple de sortie console

```
╔══════════════════════════════════════════════════╗
║     Moniteur de Ressources Serveur — Java        ║
╚══════════════════════════════════════════════════╝
  Webhook n8n  : http://localhost:5678/webhook/abc123
  Intervalle   : 10 secondes
  Seuils       : CPU=80.0%  RAM=80.0%  Disque=90.0%
──────────────────────────────────────────────────

─── 2024-09-25 15:30:00 ───────────────────────────────────
  CPU   :  92.5%  (seuil : 80%)  ▲ SEUIL DÉPASSÉ
  RAM   :  65.3%  (seuil : 80%)
  Disque:  45.1%  (seuil : 90%)
  [ALERTE] CPU trop élevé : 92.5% (seuil : 80%)
  Envoi de l'alerte à n8n...
  JSON : {"serveur": "Server01", "ressource": "CPU", ...}
  [OK] Alerte envoyée avec succès à n8n.

─── 2024-09-25 15:30:10 ───────────────────────────────────
  CPU   :  88.1%  (seuil : 80%)  ▲ SEUIL DÉPASSÉ
  RAM   :  65.3%  (seuil : 80%)
  Disque:  45.1%  (seuil : 90%)
  [PAUSE] Alerte CPU déjà active — pas de renvoi.

─── 2024-09-25 15:30:20 ───────────────────────────────────
  CPU   :  72.0%  (seuil : 80%)
  RAM   :  65.3%  (seuil : 80%)
  Disque:  45.1%  (seuil : 90%)
  Situation normale — aucune alerte.
  [OK] CPU revenu à la normale — prêt pour une nouvelle alerte si nécessaire.
```

---

## JSON envoyé à n8n

```json
{
  "serveur": "Server01",
  "ressource": "CPU",
  "valeur": 92.5,
  "seuil": 80.0,
  "message": "CPU trop élevé : 92.5% (seuil : 80%)",
  "timestamp": "2024-09-25T15:30:00"
}
```

---

## Technologies utilisées

| Technologie | Usage |
|-------------|-------|
| Java (standard) | Surveillance, analyse, envoi HTTP |
| `com.sun.management.OperatingSystemMXBean` | Lecture CPU et RAM |
| `java.io.File` | Lecture espace disque |
| `java.net.HttpURLConnection` | HTTP POST vers n8n |
| n8n | Réception Webhook → envoi Email |
