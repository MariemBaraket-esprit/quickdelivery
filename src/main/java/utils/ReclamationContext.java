package utils;

import java.util.HashMap;
import java.util.Map;

public class ReclamationContext {
    private static final Map<String, String> contextMap = new HashMap<>();

    static {
        // Initialize with predefined contexts for common questions
        contextMap.put("submit",
                "Pour soumettre une réclamation, suivez ces étapes: 1) Accédez à l'onglet 'Support' dans le menu principal. " +
                        "2) Cliquez sur le bouton 'Nouvelle Réclamation'. 3) Sélectionnez le type de réclamation (Problème de livraison, " +
                        "Problème de paiement, etc.). 4) Décrivez votre problème en détail. 5) Définissez la priorité (Haute, Moyenne, Basse). " +
                        "6) Ajoutez une image si nécessaire. 7) Cliquez sur 'Soumettre'. Votre réclamation sera traitée par notre équipe " +
                        "dans les plus brefs délais selon sa priorité.");

        contextMap.put("status",
                "Pour vérifier le statut de vos réclamations: 1) Accédez à l'onglet 'Support' dans le menu principal. " +
                        "2) Toutes vos réclamations sont affichées avec leur statut actuel: RECEIVED (Réclamation reçue), " +
                        "PENDING_RESPONSE (En attente de réponse), IN_PROGRESS (En cours de traitement), RESOLVED (Problème résolu), " +
                        "REJECTED (Réclamation rejetée), CLOSED (Dossier fermé). Vous pouvez cliquer sur une réclamation pour voir " +
                        "les détails et les réponses.");

        contextMap.put("types",
                "QuickDelivery gère plusieurs types de réclamations: • Problème de livraison - Pour les retards, colis non reçus " +
                        "ou endommagés. • Problème de paiement - Pour les erreurs de facturation ou paiements non traités. " +
                        "• Bug d'application - Pour signaler des problèmes techniques avec l'application. • Service client - Pour les " +
                        "problèmes liés au service client. • Remboursement - Pour les demandes de remboursement. • Autre - Pour tout " +
                        "autre type de problème. Choisissez le type qui correspond le mieux à votre situation pour un traitement plus rapide.");

        contextMap.put("modify",
                "Pour modifier une réclamation existante: 1) Accédez à l'onglet 'Support'. 2) Trouvez votre réclamation dans la liste. " +
                        "3) Cliquez sur le bouton 'Modifier' sur la carte de réclamation. 4) Mettez à jour les informations nécessaires. " +
                        "5) Cliquez sur 'Enregistrer'. Notez que vous ne pouvez modifier que les réclamations qui n'ont pas encore été " +
                        "résolues ou fermées.");

        contextMap.put("delete",
                "Pour supprimer une réclamation: 1) Accédez à l'onglet 'Support'. 2) Trouvez votre réclamation dans la liste. " +
                        "3) Cliquez sur la réclamation pour voir les détails. 4) Cliquez sur le bouton 'Supprimer' en bas de la page. " +
                        "5) Confirmez la suppression. Attention: La suppression est définitive et ne peut pas être annulée.");

        contextMap.put("image",
                "Pour ajouter une image à votre réclamation: 1) Lors de la création ou modification d'une réclamation. " +
                        "2) Cliquez sur le bouton 'Ajouter une image'. 3) Sélectionnez une image depuis votre ordinateur. " +
                        "4) L'image sera prévisualisée avant soumission. Les formats acceptés sont: JPG, PNG et GIF. " +
                        "La taille maximale est de 5 MB.");

        contextMap.put("response",
                "Lorsqu'un agent répond à votre réclamation: 1) Vous verrez la réponse dans les détails de votre réclamation. " +
                        "2) Le statut de votre réclamation sera mis à jour. 3) Vous pouvez continuer la conversation en modifiant votre " +
                        "réclamation et en ajoutant plus d'informations. Notre équipe s'efforce de répondre à toutes les réclamations " +
                        "dans un délai de 48 heures ouvrables.");

        contextMap.put("priority",
                "Notre équipe traite les réclamations selon leur priorité: • Haute priorité: 24 heures. • Priorité moyenne: 48 heures. " +
                        "• Faible priorité: 72 heures. La priorité est déterminée en fonction du type de réclamation et de son impact. " +
                        "Si votre réclamation est urgente, veuillez le mentionner dans la description.");

        contextMap.put("default",
                "QuickDelivery vous permet de soumettre et suivre des réclamations concernant vos livraisons. " +
                        "Vous pouvez créer une nouvelle réclamation, vérifier le statut de vos réclamations existantes, " +
                        "et communiquer avec notre équipe de support. Pour toute question spécifique, n'hésitez pas à demander " +
                        "de l'aide concernant la soumission, le suivi, ou la modification de vos réclamations.");
    }

    public static String getContext(String query) {
        query = query.toLowerCase();

        if (query.contains("soumettre") || query.contains("créer") || query.contains("nouvelle") ||
                query.contains("comment") && query.contains("réclamation")) {
            return contextMap.get("submit");
        } else if (query.contains("statut") || query.contains("état") || query.contains("status") ||
                query.contains("suivre")) {
            return contextMap.get("status");
        } else if (query.contains("type") && query.contains("réclamation")) {
            return contextMap.get("types");
        } else if (query.contains("modifier") || query.contains("éditer") || query.contains("changer")) {
            return contextMap.get("modify");
        } else if (query.contains("supprimer") || query.contains("effacer") || query.contains("enlever")) {
            return contextMap.get("delete");
        } else if (query.contains("image") || query.contains("photo") || query.contains("fichier")) {
            return contextMap.get("image");
        } else if (query.contains("réponse") || query.contains("répondre") || query.contains("agent")) {
            return contextMap.get("response");
        } else if (query.contains("priorité") || query.contains("urgent") || query.contains("délai") ||
                query.contains("attente")) {
            return contextMap.get("priority");
        } else {
            return contextMap.get("default");
        }
    }
} 