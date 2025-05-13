package models;

import java.util.*;
import java.util.regex.Pattern;

public class ChatbotKnowledgeBase {
    // Organize knowledge by categories for better maintenance
    private static final Map<String, Map<String, String>> categorizedKnowledge = new HashMap<>();

    // Fallback responses when no match is found
    private static final String[] FALLBACK_RESPONSES = {
            "Je n'ai pas compris votre question. Pourriez-vous la reformuler ou préciser votre demande ?",
            "Désolé, je n'ai pas d'information sur ce sujet. Puis-je vous aider avec autre chose ?",
            "Pour cette question spécifique, je vous recommande de contacter notre service client au 0800-123-456.",
            "Je ne peux pas répondre à cette question pour le moment. Voulez-vous que je vous mette en relation avec un conseiller ?"
    };

    static {
        // Initialize categories
        categorizedKnowledge.put("tarifs", new HashMap<>());
        categorizedKnowledge.put("delais", new HashMap<>());
        categorizedKnowledge.put("suivi", new HashMap<>());
        categorizedKnowledge.put("reclamations", new HashMap<>());
        categorizedKnowledge.put("points_relais", new HashMap<>());
        categorizedKnowledge.put("emballage", new HashMap<>());
        categorizedKnowledge.put("international", new HashMap<>());
        categorizedKnowledge.put("paiement", new HashMap<>());
        categorizedKnowledge.put("contact", new HashMap<>());
        categorizedKnowledge.put("modifications", new HashMap<>());
        categorizedKnowledge.put("compte", new HashMap<>());
        categorizedKnowledge.put("services_speciaux", new HashMap<>());
        categorizedKnowledge.put("restrictions", new HashMap<>());
        categorizedKnowledge.put("entreprises", new HashMap<>());
        categorizedKnowledge.put("application", new HashMap<>());
        categorizedKnowledge.put("environnement", new HashMap<>());
        categorizedKnowledge.put("covid", new HashMap<>());
        categorizedKnowledge.put("fetes", new HashMap<>());
        categorizedKnowledge.put("assurance", new HashMap<>());
        categorizedKnowledge.put("general", new HashMap<>());

        // TARIFS
        addKnowledge("tarifs", "tarif|prix|coût|combien coûte|frais de livraison|prix d'envoi|combien pour envoyer|tarification|grille tarifaire|estimation prix",
                "Nos tarifs varient selon le type de service : Standard à partir de 5€, Express à partir de 15€. Pour un devis précis, veuillez indiquer le poids, la taille et la destination du colis.");

        addKnowledge("tarifs", "tarif national|prix france|livraison france|tarif métropole|prix hexagone|tarif dom tom|prix outre-mer",
                "Pour les livraisons en France métropolitaine : Standard (2-3 jours) à partir de 5€, Express (24h) à partir de 15€. Pour les DOM-TOM, comptez à partir de 20€ avec des délais de 5-7 jours ouvrés.");

        addKnowledge("tarifs", "tarif international|prix étranger|livraison internationale|tarif europe|prix mondial|tarif hors france",
                "Nos tarifs internationaux débutent à 15€ pour l'Europe (3-5 jours) et 25€ pour le reste du monde (5-10 jours). Des frais de douane peuvent s'appliquer selon la destination.");

        addKnowledge("tarifs", "réduction|promotion|code promo|remise|offre spéciale|tarif préférentiel|prix réduit|rabais",
                "Nous proposons régulièrement des codes promotionnels. Consultez notre newsletter ou l'application pour les dernières offres. Les clients réguliers bénéficient automatiquement de notre programme de fidélité avec jusqu'à 20% de réduction.");

        addKnowledge("tarifs", "comparaison prix|moins cher que|différence tarif|meilleur prix|prix compétitif|tarif avantageux",
                "Nos tarifs sont parmi les plus compétitifs du marché avec un excellent rapport qualité-prix. Nous garantissons la transparence des prix sans frais cachés.");

        addKnowledge("tarifs", "calcul tarif|calculer prix|estimation coût|simulateur tarif|devis livraison|évaluation prix",
                "Utilisez notre calculateur de tarifs dans l'application ou sur notre site web. Indiquez simplement le poids, les dimensions et l'adresse de livraison pour obtenir un devis instantané.");

        addKnowledge("tarifs", "supplément|frais additionnels|coûts supplémentaires|frais cachés|surcoût|majoration prix",
                "Des suppléments peuvent s'appliquer pour : livraison en zone difficile d'accès (+5€), colis volumineux (+10€), livraison le samedi (+5€) ou assurance premium (+2% de la valeur déclarée).");

        addKnowledge("tarifs", "tarif entreprise|prix professionnel|coût business|tarification commerciale|prix volume|remise quantité",
                "Les entreprises bénéficient de tarifs dégressifs selon le volume d'envois. Contactez notre service commercial au 0800-789-123 pour une offre personnalisée adaptée à vos besoins.");

        addKnowledge("tarifs", "tarif express|prix urgent|coût livraison rapide|tarif 24h|prix jour même|express premium",
                "Notre service Express garantit une livraison en 24h à partir de 15€. Pour les livraisons ultra-urgentes, notre option Premium assure une livraison en moins de 12h à partir de 25€ (selon zone géographique).");

        addKnowledge("tarifs", "paiement échelonné|payer en plusieurs fois|facilité paiement|étalement paiement|mensualités",
                "Pour les envois de valeur importante, nous proposons le paiement en 3 fois sans frais à partir de 100€ d'achat. Cette option est disponible lors du paiement par carte bancaire.");

        // DELAIS
        addKnowledge("delais", "délai|temps de livraison|combien de temps|quand arrive|date d'arrivée|livraison express|livraison standard|durée transport",
                "La livraison Standard prend 2-3 jours ouvrables. La livraison Express est assurée en 24h. Les délais exacts sont confirmés lors de la validation de votre commande.");

        addKnowledge("delais", "délai international|temps livraison étranger|durée expédition internationale|jours ouvrés international",
                "Les délais internationaux varient selon la destination : Europe (3-5 jours ouvrés), Amérique du Nord (5-7 jours), Asie (6-10 jours), reste du monde (7-14 jours). Ces délais n'incluent pas le dédouanement éventuel.");

        addKnowledge("delais", "retard|délai dépassé|livraison en retard|colis pas arrivé|attente trop longue|délai non respecté",
                "En cas de retard, consultez d'abord le suivi de votre colis. Si le délai annoncé est dépassé de plus de 24h, contactez notre service client qui pourra vous informer et, le cas échéant, vous proposer une compensation.");

        addKnowledge("delais", "livraison jour même|livraison aujourd'hui|express même jour|livraison immédiate|livraison rapide",
                "Notre service de livraison le jour même est disponible pour les commandes passées avant 10h dans certaines zones urbaines. Vérifiez l'éligibilité de votre code postal dans l'application.");

        addKnowledge("delais", "livraison weekend|livraison samedi|livraison dimanche|hors jours ouvrés|jour férié",
                "La livraison le samedi est possible moyennant un supplément de 5€. La livraison le dimanche et les jours fériés n'est actuellement pas disponible sauf pour les clients Premium dans certaines grandes villes.");

        addKnowledge("delais", "heure livraison|créneau horaire|plage horaire|livraison précise|heure exacte|moment livraison",
                "Pour les livraisons Standard, nous ne pouvons garantir d'heure précise. Le service Express Premium permet de choisir un créneau de 2h moyennant un supplément de 5€.");

        addKnowledge("delais", "délai préparation|temps traitement|délai prise en charge|temps avant expédition",
                "Nos colis sont généralement pris en charge dans l'heure suivant la commande pour les envois Express, et dans les 24h pour les envois Standard. Ce délai s'ajoute au temps de transport.");

        addKnowledge("delais", "jours ouvrés|jours ouvrables|jour ouvré|weekend|jour férié|calcul délai",
                "Nos délais sont exprimés en jours ouvrés (du lundi au vendredi, hors jours fériés). Une commande passée le vendredi en Standard sera généralement livrée le mardi ou mercredi suivant.");

        addKnowledge("delais", "garantie délai|engagement livraison|promesse délai|délai garanti|remboursement retard",
                "Notre service Express est garanti : en cas de retard, nous vous remboursons intégralement les frais de livraison. Cette garantie ne s'applique pas en cas de force majeure (intempéries, grèves, etc.).");

        addKnowledge("delais", "délai période fêtes|temps noël|retard fêtes|livraison fin d'année|délai black friday",
                "Pendant les périodes de forte activité (Black Friday, Noël), prévoyez 1-2 jours supplémentaires pour les livraisons Standard. Nous recommandons l'option Express pour garantir vos délais.");

        // SUIVI
        addKnowledge("suivi", "suivi|où est mon colis|tracking|statut colis|colis perdu|colis retardé|colis bloqué|colis non livré|localiser envoi",
                "Vous pouvez suivre votre colis en temps réel via la section 'Suivi' de l'application ou en utilisant votre numéro de suivi. Si votre colis est retardé ou perdu, contactez notre service client.");

        addKnowledge("suivi", "numéro suivi|code tracking|référence suivi|où trouver numéro|code de suivi perdu",
                "Votre numéro de suivi vous est envoyé par email et SMS dès la prise en charge de votre colis. Vous pouvez également le retrouver dans votre espace client sous 'Mes Commandes'.");

        addKnowledge("suivi", "étapes suivi|statuts colis|différents états|signification statut|que veut dire|état livraison",
                "Les principaux statuts sont : 'Commande enregistrée', 'Colis pris en charge', 'En transit', 'En cours de livraison', 'Livré', 'Incident'. Chaque mise à jour est accompagnée d'informations détaillées.");

        addKnowledge("suivi", "notification|alerte suivi|sms suivi|email tracking|prévenir livraison|avertir arrivée",
                "Activez les notifications dans l'application pour être informé en temps réel de l'avancement de votre livraison. Vous recevrez également un SMS le jour de la livraison avec un créneau horaire estimé.");

        addKnowledge("suivi", "colis perdu|perte colis|envoi disparu|package perdu|livraison disparue",
                "Si votre colis n'a pas été livré 48h après la date prévue, signalez-le immédiatement via l'application ou au service client. Une enquête sera ouverte et vous serez remboursé si le colis n'est pas retrouvé sous 7 jours.");

        addKnowledge("suivi", "colis bloqué|envoi immobilisé|livraison en attente|statut inchangé|tracking figé",
                "Un colis 'bloqué' peut être en attente de dédouanement, en transit entre deux centres ou en attente de livraison. Si le statut reste inchangé plus de 48h, contactez notre service client pour vérification.");

        addKnowledge("suivi", "preuve livraison|confirmation réception|signature livraison|photo livraison|preuve dépôt",
                "Une preuve de livraison (signature électronique ou photo) est disponible dans votre espace client 1h après la livraison. Cette preuve est conservée 1 an et peut être téléchargée si nécessaire.");

        addKnowledge("suivi", "géolocalisation|position exacte|carte suivi|localisation livreur|position temps réel",
                "Notre service Express Premium permet de suivre votre livreur en temps réel sur une carte le jour de la livraison. Cette fonctionnalité est accessible 30 minutes avant l'heure estimée de livraison.");

        addKnowledge("suivi", "historique livraisons|anciennes livraisons|commandes passées|archives colis|suivi ancien",
                "L'historique complet de vos livraisons est disponible dans votre espace client pendant 3 ans. Vous pouvez y retrouver tous les détails, preuves de livraison et factures associées.");

        addKnowledge("suivi", "partager suivi|partage tracking|envoyer statut|informer destinataire|partage numéro",
                "Vous pouvez partager le suivi de votre colis avec le destinataire ou un tiers via l'application. Un lien de suivi anonymisé sera généré et pourra être envoyé par email, SMS ou messagerie.");

        // RECLAMATIONS
        addKnowledge("reclamations", "réclamation|faire une réclamation|problème colis|colis endommagé|colis manquant|ouvrir un litige|plainte|contestation",
                "Pour toute réclamation, veuillez utiliser la section 'Réclamations' de l'application. Fournissez votre numéro de suivi et une description du problème. Nous traitons les réclamations sous 48h ouvrées.");

        addKnowledge("reclamations", "colis endommagé|package abîmé|produit cassé|détérioré|contenu abîmé|dommage transport",
                "En cas de colis endommagé, prenez des photos avant et après ouverture et soumettez-les via l'application dans les 48h suivant la réception. Notre service client vous contactera sous 24h ouvrées.");

        addKnowledge("reclamations", "remboursement|compensation|dédommagement|indemnisation|réparation préjudice",
                "Selon la nature du problème, nous proposons : remboursement des frais de port, avoir sur prochaine expédition, ou indemnisation jusqu'à 500€ pour les envois standard (valeur déclarée pour les envois assurés).");

        addKnowledge("reclamations", "délai réclamation|temps pour réclamer|date limite plainte|prescription réclamation",
                "Les réclamations doivent être soumises dans les 3 jours ouvrés suivant la livraison pour les dommages visibles, et dans les 10 jours pour les dommages non apparents. Au-delà, nous ne pourrons malheureusement pas les traiter.");

        addKnowledge("reclamations", "procédure réclamation|étapes litige|comment réclamer|processus plainte|démarche réclamation",
                "1) Connectez-vous à votre compte 2) Accédez à 'Mes Commandes' 3) Sélectionnez l'envoi concerné 4) Cliquez sur 'Signaler un problème' 5) Complétez le formulaire avec photos si nécessaire 6) Soumettez votre réclamation.");

        addKnowledge("reclamations", "suivi réclamation|état litige|avancement plainte|traitement réclamation|résolution problème",
                "Vous pouvez suivre l'état de votre réclamation dans la section 'Mes Réclamations' de votre espace client. Chaque mise à jour génère une notification par email.");

        addKnowledge("reclamations", "contact réclamation|joindre service litige|téléphone réclamations|email plaintes",
                "Notre service réclamations est joignable au 0800-456-789 du lundi au vendredi de 9h à 18h, ou par email à reclamations@quickdelivery.com. Mentionnez toujours votre numéro de réclamation dans vos échanges.");

        addKnowledge("reclamations", "preuve réclamation|justificatif dommage|document litige|attestation problème",
                "Pour faciliter le traitement de votre réclamation, fournissez : photos du colis et du contenu endommagé, bon de livraison, facture d'achat du contenu si pertinent, et description précise du problème.");

        addKnowledge("reclamations", "réclamation refusée|litige rejeté|contestation refus|appel décision|recours réclamation",
                "Si votre réclamation a été refusée et que vous contestez cette décision, vous pouvez faire appel dans les 14 jours via le formulaire 'Contestation de décision' dans votre espace client.");

        addKnowledge("reclamations", "médiateur|conciliateur|résolution amiable|médiation consommation|recours externe",
                "En cas de désaccord persistant, vous pouvez saisir notre médiateur indépendant via mediation@quickdelivery.com. Ce service gratuit vous proposera une solution équitable dans un délai de 90 jours.");

        // POINTS RELAIS
        addKnowledge("points_relais", "point relais|pickup|où déposer|où récupérer|retrait colis|dépôt colis|consigne|relais colis",
                "Vous pouvez déposer ou retirer vos colis dans l'un de nos 10 000 points relais partenaires. Trouvez le plus proche dans la section 'Points Relais' de l'application.");

        addKnowledge("points_relais", "horaires point relais|heures ouverture relais|quand ouvert|fermeture point retrait",
                "Les horaires varient selon chaque point relais. Consultez les informations détaillées dans l'application ou sur notre site. La plupart sont ouverts du lundi au samedi de 9h à 19h, certains également le dimanche.");

        addKnowledge("points_relais", "trouver point relais|localiser relais|point relais proche|relais à proximité|carte points relais",
                "Utilisez la fonction 'Trouver un point relais' dans l'application ou sur notre site. Vous pouvez rechercher par adresse, code postal ou géolocalisation et filtrer selon vos besoins (accessibilité, horaires tardifs, etc.).");

        addKnowledge("points_relais", "délai conservation|temps stockage|durée garde|combien de jours|date limite retrait",
                "Vos colis sont conservés 14 jours calendaires en point relais. Vous recevrez un rappel 3 jours avant expiration. Au-delà, le colis sera retourné à l'expéditeur (des frais peuvent s'appliquer).");

        addKnowledge("points_relais", "documents retrait|pièce identité|justificatif retrait|procuration point relais",
                "Pour retirer un colis, présentez une pièce d'identité et le code de retrait reçu par SMS/email. Pour qu'un tiers retire votre colis, ajoutez son nom dans l'application via 'Autorisation de retrait'.");

        addKnowledge("points_relais", "colis volumineux|dimensions point relais|poids maximum relais|taille limite pickup",
                "Les points relais acceptent les colis jusqu'à 20kg et 100x60x60cm. Pour les envois plus volumineux, optez pour la livraison à domicile ou utilisez nos consignes XL disponibles dans certaines villes.");

        addKnowledge("points_relais", "point relais plein|relais saturé|capacité maximale|refus dépôt|point relais complet",
                "En période de forte activité, certains points relais peuvent atteindre leur capacité maximale. L'application vous indique en temps réel les points relais disponibles à proximité.");

        addKnowledge("points_relais", "devenir point relais|partenariat relais|comment être relais|conditions point relais",
                "Pour devenir point relais partenaire, rendez-vous sur partenaires.quickdelivery.com ou contactez notre service commercial au 0800-789-123. Nous recherchons particulièrement des partenaires dans les zones rurales.");

        addKnowledge("points_relais", "consignes automatiques|casiers colis|locker|boîte retrait|consigne 24/7",
                "Nos consignes automatiques sont accessibles 24/7 dans plus de 500 emplacements (gares, centres commerciaux, stations-service). Le retrait se fait via un code QR envoyé sur votre smartphone.");

        addKnowledge("points_relais", "problème point relais|relais fermé|point relais absent|difficulté retrait|litige relais",
                "Si vous rencontrez un problème avec un point relais (fermeture imprévue, colis introuvable), signalez-le immédiatement via l'application ou contactez notre service client qui vous proposera une solution alternative.");

        // EMBALLAGE
        addKnowledge("emballage", "emballage|comment emballer|taille colis|poids maximum|dimensions|restrictions colis|conditionnement",
                "Assurez-vous que votre colis est bien emballé et respecte les dimensions maximales : 120x80x80 cm et 30 kg. Consultez nos conseils d'emballage dans l'application.");

        addKnowledge("emballage", "matériel emballage|carton|papier bulle|scotch|enveloppe|fourniture emballage",
                "Nous vendons du matériel d'emballage dans nos agences et points relais partenaires : cartons de différentes tailles (à partir de 2€), papier bulle (3€ le mètre), scotch renforcé (4€), enveloppes sécurisées (à partir de 1€).");

        addKnowledge("emballage", "conseils emballage|comment bien emballer|protection colis|emballage sécurisé|astuces emballage",
                "Pour un emballage optimal : 1) Utilisez un carton rigide adapté à la taille du contenu 2) Protégez avec du papier bulle ou des chips de calage 3) Évitez les espaces vides 4) Fermez avec du scotch renforcé en H 5) Étiquetez clairement.");

        addKnowledge("emballage", "étiquette|étiquetage|adresse colis|label|où coller étiquette|impression étiquette",
                "L'étiquette d'expédition doit être collée à plat sur la face la plus grande du colis, sans pli ni chevauchement. Vous pouvez l'imprimer depuis l'application ou la faire générer en point relais.");

        addKnowledge("emballage", "emballage fragile|protéger objet fragile|envoi délicat|colis sensible|protection verre",
                "Pour les objets fragiles : 1) Emballez individuellement chaque objet avec 5cm de protection tout autour 2) Utilisez un double emballage 3) Indiquez 'FRAGILE' sur toutes les faces 4) Optez pour l'assurance complémentaire.");

        addKnowledge("emballage", "emballage écologique|carton recyclé|emballage durable|éco-responsable|emballage vert",
                "Nous proposons une gamme d'emballages éco-responsables : cartons 100% recyclés, papier bulle biodégradable et scotch en papier kraft. Ces options sont identifiées par notre logo 'Green Delivery'.");

        addKnowledge("emballage", "réutiliser emballage|seconde vie carton|recyclage emballage|carton usagé|emballage occasion",
                "Vous pouvez réutiliser un carton d'occasion si celui-ci est en bon état, propre et solide. Assurez-vous de retirer ou masquer toutes les anciennes étiquettes et codes-barres pour éviter les confusions.");

        addKnowledge("emballage", "emballage spécifique|colis atypique|forme particulière|objet non standard|emballage sur mesure",
                "Pour les objets de forme atypique, nous recommandons nos emballages spéciaux disponibles en agence : tubes pour plans et posters, boîtes triangulaires, caisses renforcées pour objets lourds, etc.");

        addKnowledge("emballage", "emballer vélo|emballer meuble|emballer tableau|colis volumineux|objet grand format",
                "Pour les objets volumineux, nous proposons un service d'emballage professionnel en agence sur rendez-vous. Nous pouvons également vous fournir des kits d'emballage spécifiques (vélo, TV, œuvre d'art).");

        addKnowledge("emballage", "erreur emballage|colis refusé|emballage non conforme|problème conditionnement",
                "Un emballage non conforme peut entraîner le refus de votre colis ou des frais supplémentaires. Les motifs courants de refus sont : emballage insuffisant, colis fuyard, dimensions excessives, étiquetage illisible.");

        // INTERNATIONAL
        addKnowledge("international", "international|expédition internationale|douane|frais douaniers|documents|envoyer à l'étranger|livraison internationale",
                "Nous proposons des livraisons internationales vers plus de 220 pays. Des documents douaniers peuvent être nécessaires selon la destination. Les frais de douane sont à la charge du destinataire.");

        addKnowledge("international", "documents douane|déclaration douanière|formulaire CN23|facture commerciale|documents export",
                "Pour les envois hors UE, vous devez compléter une déclaration en douane (générée automatiquement lors de votre commande). Pour les envois commerciaux, joignez également une facture commerciale en 3 exemplaires.");

        addKnowledge("international", "frais douane|taxes importation|droits de douane|TVA internationale|frais dédouanement",
                "Les frais de douane comprennent : droits de douane, TVA du pays destinataire et frais de dédouanement. Ils sont calculés selon la valeur déclarée, la nature et l'origine des produits, et sont à la charge du destinataire.");

        addKnowledge("international", "délai international|temps livraison étranger|durée expédition internationale|jours ouvrés international",
                "Nos délais internationaux indicatifs : Europe (3-5 jours), Amérique du Nord (5-7 jours), Asie (6-10 jours), reste du monde (7-14 jours). Le dédouanement peut ajouter 1-5 jours selon les pays.");

        addKnowledge("international", "pays desservis|destinations internationales|couverture mondiale|pays livraison|où livrez-vous",
                "Nous livrons dans plus de 220 pays et territoires. Certaines restrictions s'appliquent pour quelques destinations (zones de conflit, sanctions internationales). Vérifiez l'éligibilité de votre destination dans l'application.");

        addKnowledge("international", "restrictions internationales|produits interdits|limitations export|embargo|sanctions",
                "Certains produits sont interdits à l'exportation ou soumis à restrictions : denrées périssables, animaux, armes, contrefaçons, matières dangereuses, etc. Consultez la liste complète sur notre site.");

        addKnowledge("international", "suivi international|tracking mondial|statut colis étranger|localiser envoi international",
                "Le suivi international est disponible pour toutes nos destinations, avec une mise à jour à chaque étape clé (départ, transit, dédouanement, livraison). La précision peut varier selon les pays partenaires.");

        addKnowledge("international", "retour international|renvoi étranger|retourner colis international|rapatriement colis",
                "Les retours internationaux sont possibles mais soumis aux mêmes formalités douanières que l'envoi initial. Des frais supplémentaires s'appliquent. Contactez notre service client pour organiser un retour international.");

        addKnowledge("international", "assurance internationale|garantie envoi étranger|protection colis international",
                "Nous recommandons vivement notre assurance Ad Valorem pour les envois internationaux (2% de la valeur déclarée). Elle couvre la perte, le vol et les dommages jusqu'à 5000€, avec une procédure de remboursement simplifiée.");

        addKnowledge("international", "Brexit|envoi Royaume-Uni|livraison UK|douane anglaise|post-Brexit",
                "Suite au Brexit, les envois vers le Royaume-Uni nécessitent une déclaration en douane et peuvent être soumis à des droits et taxes. Prévoyez 1-2 jours supplémentaires pour le dédouanement.");

        // PAIEMENT
        addKnowledge("paiement", "facture|paiement|moyen de paiement|carte bancaire|remboursement|problème de paiement|payer|règlement",
                "Nous acceptons les paiements par carte bancaire, PayPal et virement. Pour toute question sur la facturation ou un remboursement, contactez notre service client.");

        addKnowledge("paiement", "moyens paiement|méthodes paiement|comment payer|options paiement|modes règlement",
                "Nous acceptons : cartes bancaires (Visa, Mastercard, Amex), PayPal, Apple Pay, Google Pay, virements bancaires et prélèvements SEPA pour les abonnements. Les espèces sont acceptées uniquement en agence.");

        addKnowledge("paiement", "facture|reçu|justificatif paiement|preuve achat|attestation paiement",
                "Votre facture est automatiquement envoyée par email après paiement et reste disponible dans votre espace client pendant 10 ans. Pour les professionnels, nos factures sont conformes aux exigences fiscales.");

        addKnowledge("paiement", "remboursement|annulation paiement|rétractation|annuler commande|récupérer argent",
                "Les remboursements sont traités sous 5 jours ouvrés et crédités sur votre moyen de paiement initial. En cas d'annulation avant prise en charge, le remboursement est intégral. Après prise en charge, des frais peuvent s'appliquer.");

        addKnowledge("paiement", "paiement refusé|échec transaction|erreur paiement|problème carte|paiement échoué",
                "En cas de paiement refusé, vérifiez le solde de votre compte, les limites de votre carte et la validité de vos informations. Vous pouvez réessayer avec un autre moyen de paiement ou contacter votre banque.");

        addKnowledge("paiement", "paiement sécurisé|sécurité transaction|protection données bancaires|cryptage paiement",
                "Toutes nos transactions sont sécurisées par cryptage SSL 256 bits et authentification 3D Secure. Nous ne stockons jamais vos données bancaires complètes et sommes conformes aux normes PCI-DSS.");

        addKnowledge("paiement", "contre-remboursement|paiement à la livraison|COD|cash on delivery|payer à réception",
                "Le service de contre-remboursement est disponible pour les livraisons nationales jusqu'à 1000€. Des frais supplémentaires de 5€ ou 2% du montant (au plus élevé des deux) s'appliquent. Le paiement peut se faire par carte ou espèces.");

        addKnowledge("paiement", "devise|monnaie étrangère|paiement en euros|conversion devise|taux change",
                "Tous nos tarifs sont en euros. Pour les paiements dans d'autres devises, la conversion est effectuée par votre banque ou PayPal selon leurs taux en vigueur. Nous n'appliquons pas de frais supplémentaires.");

        addKnowledge("paiement", "facturation entreprise|compte professionnel|paiement différé|facturation mensuelle",
                "Les clients professionnels peuvent bénéficier d'une facturation mensuelle avec paiement différé à 30 jours. Ce service nécessite l'ouverture d'un compte Pro et une vérification de solvabilité.");

        addKnowledge("paiement", "TVA|taxe|récupération TVA|taux TVA|TVA déductible|mention TVA",
                "Nos services sont soumis à la TVA au taux de 20%. Pour les professionnels, la TVA est mentionnée clairement sur nos factures et peut être récupérée selon les règles fiscales en vigueur.");

        // CONTACT
        addKnowledge("contact", "contact|service client|numéro|téléphone|aide|support|joindre|assistance|email|chat",
                "Notre service client est disponible 24/7 au 0800-123-456, par chat dans l'application ou par email à support@quickdelivery.com.");

        addKnowledge("contact", "horaires service client|heures ouverture support|disponibilité assistance|quand joindre",
                "Notre service client est disponible par téléphone du lundi au vendredi de 8h à 20h, le samedi de 9h à 18h. Le chat en ligne et l'assistance par email sont disponibles 24/7, avec une réponse garantie sous 4h.");

        addKnowledge("contact", "réclamation|service réclamations|plainte|litige|problème livraison",
                "Pour les réclamations, contactez notre service dédié au 0800-456-789 ou via reclamations@quickdelivery.com. Mentionnez toujours votre numéro de suivi pour un traitement plus rapide.");

        addKnowledge("contact", "commercial|service commercial|devis entreprise|offre professionnelle|contrat cadre",
                "Notre équipe commerciale dédiée aux professionnels est joignable au 0800-789-123 du lundi au vendredi de 9h à 18h, ou par email à commercial@quickdelivery.com.");

        addKnowledge("contact", "agences|bureaux|points de vente|où vous trouver|localisation agence",
                "Nous disposons de 150 agences en France. Trouvez la plus proche via notre application ou site web. Nos agences sont généralement ouvertes du lundi au vendredi de 9h à 18h et le samedi de 9h à 12h.");

        addKnowledge("contact", "réseaux sociaux|Facebook|Twitter|Instagram|LinkedIn|social media",
                "Suivez-nous sur Facebook, Twitter et Instagram (@QuickDeliveryFR) pour nos actualités et promotions. Pour une assistance, privilégiez nos canaux officiels (téléphone, email, chat) pour un traitement prioritaire.");

        addKnowledge("contact", "urgence|assistance immédiate|problème urgent|contact prioritaire|aide rapide",
                "Pour les situations urgentes, utilisez l'option 'Urgence' dans notre application ou appelez notre ligne prioritaire au 0800-999-456 (service facturé 0,50€/min). Ce service est réservé aux véritables urgences.");

        addKnowledge("contact", "callback|rappel|être rappelé|demande rappel|faire rappeler",
                "Si nos lignes sont occupées, vous pouvez demander à être rappelé via notre application ou site web. Indiquez votre numéro et un créneau horaire, et nous vous rappellerons dans l'heure.");

        addKnowledge("contact", "chat bot|assistant virtuel|aide automatique|robot conversation|IA support",
                "Notre assistant virtuel est disponible 24/7 dans l'application et sur le site. Il peut répondre à vos questions courantes et vous orienter vers un conseiller humain si nécessaire.");

        addKnowledge("contact", "langue|support multilingue|assistance langue étrangère|contact international",
                "Notre service client international est disponible en 8 langues (français, anglais, allemand, espagnol, italien, portugais, arabe, chinois) au +33 1 23 45 67 89.");

        // MODIFICATIONS
        addKnowledge("modifications", "annuler|modifier|changer adresse|changer livraison|changer date|changer destinataire|correction",
                "Pour modifier ou annuler une livraison, rendez-vous dans la section 'Mes Commandes' ou contactez le service client avant l'expédition.");

        addKnowledge("modifications", "modifier adresse|changer destination|corriger adresse|erreur adresse|mauvaise adresse",
                "Vous pouvez modifier l'adresse de livraison jusqu'à la prise en charge du colis via votre espace client. Après prise en charge, contactez rapidement le service client (des frais peuvent s'appliquer).");

        addKnowledge("modifications", "annuler commande|annulation envoi|annuler expédition|stopper livraison|annuler colis",
                "L'annulation est possible gratuitement avant la prise en charge du colis. Après prise en charge mais avant expédition, des frais de 5€ s'appliquent. Une fois en transit, l'annulation n'est plus possible.");

        addKnowledge("modifications", "changer date livraison|reporter livraison|avancer livraison|modifier créneau|changer jour",
                "La modification de la date de livraison est possible jusqu'à la veille de la livraison prévue, via votre espace client ou auprès du service client. Ce service est gratuit pour la première modification.");

        addKnowledge("modifications", "changer point relais|modifier point retrait|autre relais|relais différent",
                "Vous pouvez changer de point relais jusqu'à la veille de la mise à disposition prévue, via votre espace client. Choisissez simplement un nouveau point relais parmi ceux disponibles.");

        addKnowledge("modifications", "modifier destinataire|changer réceptionnaire|autre personne|nouveau destinataire",
                "La modification du destinataire est possible avant l'expédition. Pour des raisons de sécurité, cette opération nécessite une vérification d'identité et doit être effectuée auprès du service client.");

        addKnowledge("modifications", "seconde présentation|nouvelle tentative|relivraison|deuxième passage|livraison manquée",
                "En cas d'absence lors de la livraison, une seconde présentation est automatiquement programmée le jour ouvré suivant. Vous pouvez modifier cette date via votre espace client ou l'avis de passage.");

        addKnowledge("modifications", "retour expéditeur|renvoyer colis|retourner à l'envoyeur|colis non réclamé",
                "Un colis non livré après deux tentatives ou non réclamé en point relais est retourné à l'expéditeur après 14 jours. Des frais de retour équivalents aux frais d'envoi initiaux peuvent s'appliquer.");

        addKnowledge("modifications", "modifier service|changer option|passer en express|downgrade service",
                "La modification du niveau de service (Standard vers Express ou inversement) est possible uniquement avant la prise en charge du colis. La différence de tarif sera remboursée ou facturée selon le cas.");

        addKnowledge("modifications", "instructions livraison|consignes livreur|indications spéciales|directives livraison",
                "Vous pouvez ajouter ou modifier des instructions de livraison (code d'accès, étage, personne à contacter...) jusqu'au jour de la livraison via votre espace client ou en contactant le service client.");

        // COMPTE
        addKnowledge("compte", "créer compte|inscription|s'inscrire|nouveau compte|enregistrement|créer profil",
                "Créez votre compte gratuitement en quelques clics sur notre application ou site web. Vous aurez besoin d'une adresse email valide et d'un numéro de téléphone pour la vérification.");

        addKnowledge("compte", "connexion|login|se connecter|accéder compte|identifiants|authentification",
                "Connectez-vous avec votre email et mot de passe. Si vous avez oublié votre mot de passe, utilisez la fonction 'Mot de passe oublié' pour recevoir un lien de réinitialisation par email.");

        addKnowledge("compte", "mot de passe oublié|réinitialiser mot de passe|reset password|nouveau mot de passe",
                "En cas d'oubli de mot de passe, cliquez sur 'Mot de passe oublié' sur la page de connexion. Un lien de réinitialisation valable 24h vous sera envoyé par email.");

        addKnowledge("compte", "modifier profil|changer informations|mettre à jour compte|éditer coordonnées",
                "Vous pouvez modifier vos informations personnelles, coordonnées et préférences à tout moment dans la section 'Mon Profil' de votre espace client.");

        addKnowledge("compte", "supprimer compte|fermer compte|désactiver profil|effacer données|droit à l'oubli",
                "Pour supprimer votre compte, rendez-vous dans 'Paramètres > Confidentialité > Supprimer mon compte'. La suppression sera effective sous 30 jours et toutes vos données personnelles seront effacées.");

        addKnowledge("compte", "compte professionnel|compte entreprise|profil business|compte pro|client professionnel",
                "Les professionnels bénéficient d'un espace dédié avec facturation mensuelle, tarifs préférentiels et outils de gestion avancés. Pour créer un compte Pro, rendez-vous sur pro.quickdelivery.com.");

        addKnowledge("compte", "carnet d'adresses|adresses enregistrées|gérer adresses|contacts sauvegardés",
                "Gérez votre carnet d'adresses dans la section 'Mes Adresses'. Vous pouvez enregistrer jusqu'à 100 adresses d'expédition et de livraison pour gagner du temps lors de vos prochains envois.");

        addKnowledge("compte", "préférences notification|alertes|paramètres communication|gérer emails|opt-out",
                "Personnalisez vos préférences de notification dans 'Paramètres > Notifications'. Vous pouvez choisir quelles alertes recevoir (email, SMS, push) et vous désabonner des communications marketing.");

        addKnowledge("compte", "historique commandes|anciennes expéditions|envois passés|archives livraisons",
                "Retrouvez l'historique complet de vos expéditions dans 'Mes Commandes'. Les données sont conservées pendant 3 ans et peuvent être exportées en PDF ou CSV.");

        addKnowledge("compte", "programme fidélité|points fidélité|statut client|avantages fidélité|récompenses",
                "Notre programme de fidélité vous permet de cumuler 1 point pour chaque euro dépensé. Ces points sont convertibles en réductions (100 points = 5€) ou avantages exclusifs (livraison gratuite, assurance offerte...).");

        // SERVICES SPECIAUX
        addKnowledge("services_speciaux", "livraison sur rendez-vous|créneau précis|heure exacte|plage horaire|livraison programmée",
                "Notre service de livraison sur rendez-vous vous permet de choisir un créneau de 2h, du lundi au samedi de 8h à 20h. Ce service est disponible moyennant un supplément de 5€.");

        addKnowledge("services_speciaux", "livraison le jour même|same day delivery|livraison immédiate|express urgent",
                "La livraison le jour même est disponible pour les commandes passées avant 10h dans un rayon de 30km autour des grandes villes. Tarif à partir de 25€, selon distance et urgence.");

        addKnowledge("services_speciaux", "livraison le dimanche|weekend|jour férié|livraison hors jours ouvrés",
                "La livraison le dimanche et jours fériés est disponible dans certaines grandes villes, pour les commandes passées au plus tard la veille avant 12h. Supplément de 10€ applicable.");

        addKnowledge("services_speciaux", "livraison soirée|livraison nocturne|créneau tardif|après 18h|livraison de nuit",
                "Notre service de livraison en soirée est disponible du lundi au vendredi entre 18h et 22h dans les zones urbaines, moyennant un supplément de 7€. Idéal pour les personnes qui travaillent en journée.");

        addKnowledge("services_speciaux", "installation|montage|mise en service|déballage|livraison avec installation",
                "Pour les objets volumineux ou techniques, nous proposons un service d'installation comprenant : livraison, déballage, montage simple et reprise des emballages. Tarif à partir de 30€ selon complexité.");

        addKnowledge("services_speciaux", "enlèvement à domicile|collecte à domicile|ramassage colis|pickup à la maison",
                "Notre service d'enlèvement à domicile permet de faire collecter vos colis chez vous. Réservez un créneau de 2h via l'application, tarif à partir de 5€ selon volume et urgence.");

        addKnowledge("services_speciaux", "emballage sur mesure|service emballage|conditionnement professionnel",
                "Notre service d'emballage professionnel est disponible en agence sur rendez-vous. Nos experts conditionnent vos objets fragiles ou volumineux avec des matériaux adaptés. Tarif selon nature et dimensions.");

        addKnowledge("services_speciaux", "assurance ad valorem|assurance complémentaire|protection valeur|garantie envoi",
                "L'assurance Ad Valorem couvre la valeur réelle de votre envoi en cas de perte, vol ou dommage, jusqu'à 50 000€. Tarif : 2% de la valeur déclarée, avec un minimum de 5€.");

        addKnowledge("services_speciaux", "stockage temporaire|garde colis|conservation envoi|entreposage colis",
                "Notre service de stockage temporaire permet de conserver vos colis dans nos entrepôts sécurisés jusqu'à 30 jours. Tarif : 2€/jour pour un colis standard, dégressif selon durée et volume.");

        addKnowledge("services_speciaux", "envoi anonyme|expédition discrète|colis neutre|envoi confidentiel",
                "Notre service d'envoi discret garantit que l'identité de l'expéditeur n'apparaît pas sur le colis. Seules les informations du destinataire et le numéro de suivi sont visibles. Supplément de 3€.");

        // RESTRICTIONS
        addKnowledge("restrictions", "objets interdits|produits prohibés|articles non autorisés|restrictions envoi|ce qu'on ne peut pas envoyer",
                "Certains articles sont interdits à l'expédition : matières dangereuses, armes, stupéfiants, contrefaçons, denrées périssables, animaux vivants, argent liquide. Consultez la liste complète sur notre site.");

        addKnowledge("restrictions", "matières dangereuses|produits dangereux|ADR|substances interdites|IATA restrictions",
                "Les matières dangereuses (inflammables, corrosives, explosives, toxiques) sont généralement interdites. Certaines peuvent être acceptées en quantité limitée avec un emballage spécial et une déclaration spécifique.");

        addKnowledge("restrictions", "liquides|envoi liquide|expédier bouteille|restrictions fluides|conditionnement liquides",
                "Les liquides sont acceptés uniquement avec un emballage étanche à triple protection : contenant hermétique, matériau absorbant, emballage extérieur rigide. Limite de 5L par colis en national, 1L à l'international.");

        addKnowledge("restrictions", "nourriture|denrées alimentaires|envoyer aliments|restrictions alimentaires",
                "Les denrées non périssables et correctement emballées sont acceptées en national. À l'international, de nombreuses restrictions s'appliquent selon les pays. Les aliments frais sont généralement interdits.");

        addKnowledge("restrictions", "médicaments|produits pharmaceutiques|envoyer médicaments|restrictions médicales",
                "L'envoi de médicaments est strictement réglementé. Seuls les médicaments en vente libre, dans leur emballage d'origine et en quantité raisonnable sont acceptés en national. À l'international, consultez-nous.");

        addKnowledge("restrictions", "objets de valeur|bijoux|or|argent|pierres précieuses|objets précieux",
                "Les objets de valeur (bijoux, métaux précieux, œuvres d'art) doivent être déclarés et assurés via notre option Ad Valorem. Certaines restrictions s'appliquent selon les pays de destination.");

        addKnowledge("restrictions", "dimensions maximales|taille limite|poids maximum|restrictions taille|colis trop grand",
                "Dimensions maximales acceptées : 120x80x80 cm et 30 kg pour les envois standard. Pour les objets plus volumineux ou lourds, notre service 'Hors Gabarit' est disponible sur devis.");

        addKnowledge("restrictions", "animaux|êtres vivants|expédier animal|restrictions animaux|envoi vivant",
                "L'expédition d'animaux vivants est strictement interdite par nos services standard. Pour les besoins spécifiques, consultez des transporteurs spécialisés dans le transport d'animaux.");

        addKnowledge("restrictions", "plantes|végétaux|graines|fleurs|restrictions végétales|envoi plantes",
                "L'expédition de plantes est soumise à de nombreuses restrictions, particulièrement à l'international (risques phytosanitaires). En national, les plantes non protégées, bien emballées et sans terre sont acceptées.");

        addKnowledge("restrictions", "contrefaçons|produits contrefaits|faux articles|copies illégales|contrebande",
                "L'expédition de contrefaçons est strictement interdite et illégale. Tout colis suspecté de contenir des articles contrefaits peut être ouvert, saisi et signalé aux autorités compétentes.");

        // ENTREPRISES
        addKnowledge("entreprises", "solutions entreprises|offre professionnelle|services business|B2B|compte pro",
                "Nos solutions entreprises incluent : tarifs préférentiels, facturation mensuelle, interface de gestion dédiée, API d'intégration, service client prioritaire et reporting personnalisé.");

        addKnowledge("entreprises", "compte professionnel|ouvrir compte pro|devenir client pro|conditions compte entreprise",
                "Pour ouvrir un compte professionnel, rendez-vous sur pro.quickdelivery.com ou contactez notre service commercial au 0800-789-123. Vous devrez fournir un extrait Kbis de moins de 3 mois et un RIB professionnel.");

        addKnowledge("entreprises", "API|intégration|connecter site e-commerce|plugin|module e-commerce",
                "Notre API REST permet d'intégrer nos services à votre site e-commerce ou ERP. Nous proposons également des plugins pour les principales plateformes (Shopify, Magento, WooCommerce, PrestaShop).");

        addKnowledge("entreprises", "étiquettes en masse|impression multiple|génération étiquettes|lots d'envois",
                "Notre outil professionnel permet de générer et imprimer des étiquettes en masse (jusqu'à 1000 par lot), d'importer des fichiers CSV de destinataires et de gérer vos envois par campagnes.");

        addKnowledge("entreprises", "enlèvement régulier|collecte programmée|ramassage quotidien|pickup récurrent",
                "Nous proposons des services d'enlèvement régulier (quotidien, hebdomadaire) à horaires fixes pour les entreprises. Tarifs dégressifs selon volume et fréquence, sur contrat annuel.");

        addKnowledge("entreprises", "reporting|statistiques|tableau de bord|analyse envois|suivi performance",
                "Notre interface professionnelle inclut un module de reporting avancé : statistiques d'envoi, taux de livraison, délais moyens, analyse des coûts, export de données et rapports personnalisables.");

        addKnowledge("entreprises", "facturation entreprise|relevé mensuel|facture récapitulative|échéancier paiement",
                "Les clients professionnels bénéficient d'une facturation mensuelle récapitulative avec paiement à 30 jours. Les factures détaillées sont disponibles en ligne et peuvent être intégrées à votre comptabilité.");

        addKnowledge("entreprises", "livraison en masse|envois volumineux|expédition grande quantité|gros volumes",
                "Pour les envois en masse (plus de 100 colis simultanés), nous proposons des solutions logistiques dédiées avec enlèvement par camion, tarifs dégressifs et suivi de campagne personnalisé.");

        addKnowledge("entreprises", "retours clients|gestion retours|reverse logistics|solution SAV|retours e-commerce",
                "Notre solution de gestion des retours permet à vos clients de générer des étiquettes de retour prépayées. Vous contrôlez les conditions (délai, motif) et recevez des notifications à chaque retour.");

        addKnowledge("entreprises", "stockage|entreposage|fulfillment|préparation commandes|logistique e-commerce",
                "Notre service Fulfillment offre une solution complète : stockage de vos produits, préparation de commandes, emballage personnalisé et expédition. Idéal pour les e-commerçants souhaitant externaliser leur logistique.");

        // APPLICATION
        addKnowledge("application", "application mobile|app|télécharger application|installer app|application smartphone",
                "Notre application mobile est disponible gratuitement sur iOS et Android. Elle permet de créer et suivre vos envois, gérer vos adresses, recevoir des notifications en temps réel et contacter le support.");

        addKnowledge("application", "fonctionnalités application|features app|options application|possibilités appli",
                "Principales fonctionnalités : création d'envois, suivi en temps réel, notifications push, scan de QR code, géolocalisation des points relais, paiement mobile, chat avec le service client, carnet d'adresses.");

        addKnowledge("application", "problème application|bug appli|application ne fonctionne pas|erreur app|plantage",
                "En cas de problème avec l'application, essayez de : 1) Fermer et rouvrir l'app 2) Vérifier votre connexion internet 3) Mettre à jour l'application 4) Redémarrer votre téléphone 5) Réinstaller l'application.");

        addKnowledge("application", "mettre à jour application|nouvelle version|update app|dernière version|mise à jour",
                "Pour bénéficier des dernières fonctionnalités et corrections, mettez régulièrement à jour l'application via l'App Store ou Google Play. Les mises à jour sont généralement publiées tous les mois.");

        addKnowledge("application", "compatibilité application|versions supportées|appareils compatibles|configuration requise",
                "Notre application est compatible avec iOS 12+ et Android 8.0+. Pour une expérience optimale, nous recommandons les versions iOS 15+ ou Android 10+. L'application nécessite environ 100 Mo d'espace.");

        addKnowledge("application", "notifications|alertes push|messages application|activer notifications|push",
                "Pour recevoir les notifications en temps réel, assurez-vous qu'elles sont activées dans les paramètres de votre téléphone. Vous pouvez personnaliser les types d'alertes dans l'application (Paramètres > Notifications).");

        addKnowledge("application", "scanner code|scan QR|lecture code-barres|scanner étiquette|numériser code",
                "La fonction scanner de l'application permet de lire les codes QR et codes-barres pour suivre rapidement un colis ou valider une remise. Accédez-y depuis l'écran d'accueil ou l'icône d'appareil photo.");

        addKnowledge("application", "mode hors ligne|fonctionnement sans internet|offline mode|sans connexion",
                "Certaines fonctionnalités de l'application sont disponibles hors ligne : consultation des derniers suivis chargés, accès aux étiquettes générées et à votre carnet d'adresses. La synchronisation se fera automatiquement à la reconnexion.");

        addKnowledge("application", "confidentialité application|données personnelles app|vie privée|RGPD application",
                "Notre application respecte strictement le RGPD. Vos données sont cryptées et utilisées uniquement pour fournir nos services. Consultez notre politique de confidentialité dans l'application (Paramètres > Confidentialité).");

        addKnowledge("application", "widget application|raccourci app|extension smartphone|widget suivi|écran d'accueil",
                "Notre widget pour écran d'accueil permet de suivre vos colis en cours sans ouvrir l'application. Disponible sur iOS et Android, il affiche le statut de vos 3 derniers envois et se met à jour toutes les heures.");

        // ENVIRONNEMENT
        addKnowledge("environnement", "politique environnementale|écologie|développement durable|impact écologique|green delivery",
                "Notre programme 'Green Delivery' vise la neutralité carbone d'ici 2025 : véhicules électriques, emballages recyclables, compensation carbone et optimisation des tournées pour réduire notre empreinte environnementale.");

        addKnowledge("environnement", "véhicules électriques|livraison électrique|transport écologique|zéro émission",
                "Notre flotte compte déjà 60% de véhicules électriques en zone urbaine. Nous visons 100% d'ici 2025. Ces véhicules sont identifiables par leur logo 'Green Delivery' et sont prioritaires dans certaines zones à faibles émissions.");

        addKnowledge("environnement", "compensation carbone|neutralité CO2|offset carbone|empreinte neutralisée",
                "Pour chaque envoi, vous pouvez opter pour la compensation carbone (+0,10€). Ces fonds financent des projets certifiés de reforestation et d'énergies renouvelables. En 2023, nous avons compensé l'équivalent de 25 000 tonnes de CO2.");

        addKnowledge("environnement", "optimisation tournées|routes efficientes|réduction trajets|planification écologique",
                "Notre algorithme d'optimisation des tournées réduit les distances parcourues de 15% en moyenne. Nous regroupons les livraisons par zones et adaptons nos horaires pour éviter les embouteillages, réduisant ainsi notre consommation d'énergie.");

        addKnowledge("environnement", "consignes réutilisables|emballages consignés|boîtes retournables|packaging circulaire",
                "Notre système d'emballages consignés permet de réutiliser les contenants jusqu'à 100 fois. Disponible pour les clients réguliers et les entreprises, il réduit significativement les déchets d'emballage.");

        addKnowledge("environnement", "livraison à vélo|vélo cargo|cyclo-logistique|coursier vélo|livraison douce",
                "Dans les centres-villes de plus de 20 villes françaises, nous proposons la livraison à vélo cargo, sans émission et plus rapide en zone congestionnée. Cette option est identifiable par le logo 'Bike Delivery'.");

        addKnowledge("environnement", "rapport développement durable|bilan carbone|RSE|responsabilité environnementale",
                "Notre rapport annuel de développement durable détaille nos actions et résultats en matière d'environnement. Il est disponible sur notre site web et inclut notre bilan carbone certifié par un organisme indépendant.");

        addKnowledge("environnement", "recyclage emballages|que faire des cartons|récupération packaging|collecte emballages",
                "Nos livreurs peuvent récupérer vos anciens emballages lors de la livraison (service gratuit sur demande). Nous assurons leur recyclage ou réutilisation via notre programme 'Second Life Packaging'.");

        addKnowledge("environnement", "livraison groupée|regroupement colis|réduction empreinte|consolidation envois",
                "Notre option 'Green Day' permet de regrouper vos livraisons un jour fixe par semaine, réduisant ainsi l'empreinte carbone de 30%. Cette option est proposée avec une remise de 10% sur les frais de port.");

        addKnowledge("environnement", "certifications environnementales|labels écologiques|normes vertes|ISO 14001",
                "Nous sommes certifiés ISO 14001 (management environnemental), labellisés 'Objectif CO2' par l'ADEME et signataires de la charte 'Fret 21' pour la réduction des émissions du transport de marchandises.");

        // COVID
        addKnowledge("covid", "mesures covid|protocole sanitaire|précautions coronavirus|livraison covid|sécurité sanitaire",
                "Nos protocoles COVID-19 incluent : livraison sans contact, désinfection régulière des véhicules et équipements, port du masque par nos livreurs si demandé, et respect des gestes barrières lors des interactions.");

        addKnowledge("covid", "livraison sans contact|contactless delivery|sans signature|dépose sécurisée|zéro contact",
                "La livraison sans contact reste disponible sur demande : le livreur dépose le colis à l'endroit indiqué, prend une photo comme preuve de livraison et vous envoie une notification, sans signature physique requise.");

        addKnowledge("covid", "délais covid|retards coronavirus|impact pandémie|perturbations sanitaires",
                "Nos délais de livraison ont retrouvé leur niveau normal. En cas de résurgence épidémique locale, des retards ponctuels peuvent survenir. Consultez notre page 'Info Service' pour les dernières mises à jour.");

        addKnowledge("covid", "désinfection colis|nettoyage package|hygiène livraison|stérilisation envoi",
                "Nos colis ne font pas l'objet d'une désinfection systématique, mais nos équipes respectent des protocoles d'hygiène stricts. Si vous le souhaitez, vous pouvez désinfecter votre colis à réception ou attendre quelques heures avant ouverture.");

        addKnowledge("covid", "vaccination livreurs|statut vaccinal|pass sanitaire|obligation vaccinale",
                "Conformément à la réglementation en vigueur, nous ne demandons plus de pass sanitaire ou vaccinal à nos équipes. Nos livreurs continuent d'appliquer les gestes barrières recommandés par les autorités sanitaires.");

        addKnowledge("covid", "zones à risque|régions confinées|restrictions locales|zones rouges covid",
                "Nous assurons les livraisons dans toutes les zones, même en cas de restrictions locales. Dans les zones à forte incidence, des protocoles renforcés peuvent être appliqués et de légers retards sont possibles.");

        addKnowledge("covid", "colis international covid|restrictions frontières|douanes coronavirus|livraison pays à risque",
                "Les livraisons internationales ont repris normalement vers la plupart des destinations. Certains pays maintiennent des contrôles sanitaires pouvant rallonger les délais de dédouanement. Consultez notre carte interactive des restrictions par pays.");

        addKnowledge("covid", "quarantaine colis|isolement package|délai décontamination|attente sanitaire",
                "Selon les études scientifiques, le virus survit très peu de temps sur les surfaces comme le carton. Il n'y a plus de protocole de quarantaine pour les colis, mais vous pouvez prendre vos propres précautions à réception.");

        addKnowledge("covid", "masque livreur|équipement protection|gants livraison|protection personnelle",
                "Nos livreurs disposent d'équipements de protection (masques, gel hydroalcoolique) qu'ils peuvent utiliser à leur discrétion ou à votre demande. N'hésitez pas à préciser vos préférences dans les instructions de livraison.");

        addKnowledge("covid", "signalement cas covid|livreur positif|contamination livraison|alerte sanitaire",
                "En cas de signalement d'un cas positif parmi notre personnel, nous appliquons un protocole strict : mise en quarantaine de l'employé, traçage des contacts, et information des clients potentiellement concernés, conformément aux recommandations sanitaires.");

        // FETES
        addKnowledge("fetes", "livraison noël|colis fêtes|délais fin d'année|expédition période noël|envoi cadeaux",
                "Pour les fêtes de fin d'année, prévoyez vos envois à l'avance : avant le 15 décembre pour les livraisons nationales standard, avant le 20 décembre pour l'Express. Des délais supplémentaires s'appliquent pour l'international.");

        addKnowledge("fetes", "horaires fêtes|ouverture période noël|fermeture jour férié|disponibilité fin d'année",
                "Pendant les fêtes, nos agences adaptent leurs horaires (généralement élargis jusqu'au 23 décembre, fermées les 25 décembre et 1er janvier). Les points relais peuvent également modifier leurs horaires. Vérifiez sur notre application.");

        addKnowledge("fetes", "emballage cadeau|papier cadeau|packaging festif|présentation cadeau|emballage fêtes",
                "Notre service d'emballage cadeau est disponible du 15 novembre au 31 décembre. Pour 3€ par colis, nous emballons votre envoi dans un papier cadeau au choix et pouvons inclure une carte personnalisée (+1€).");

        addKnowledge("fetes", "carte message|mot personnalisé|message cadeau|carte voeux|message fêtes",
                "Ajoutez une carte message personnalisée à votre envoi pour 1€. Rédigez votre texte (300 caractères max) lors de la commande, nous l'imprimerons sur une carte élégante glissée dans le colis.");

        addKnowledge("fetes", "retours après noël|renvoi cadeaux|retour post-fêtes|échange après noël",
                "Notre période de retours étendus s'applique du 15 novembre au 31 janvier : les colis peuvent être retournés gratuitement jusqu'à 45 jours après réception (au lieu des 14 jours habituels).");

        addKnowledge("fetes", "black friday|cyber monday|promotions novembre|offres spéciales shopping",
                "Pendant la période du Black Friday, nous renforçons nos équipes pour absorber le pic d'activité. Prévoyez néanmoins 1-2 jours supplémentaires pour les livraisons Standard. Des offres spéciales sont proposées sur notre application.");

        addKnowledge("fetes", "calendrier avent|livraison quotidienne|envoi journalier|service avent",
                "Notre service 'Calendrier de l'Avent' permet de programmer 24 petites livraisons quotidiennes du 1er au 24 décembre. Réservation avant le 15 novembre, tarif forfaitaire selon zone géographique.");

        addKnowledge("fetes", "père noël|livraison déguisée|surprise enfants|livreur noël|service festif",
                "Dans certaines villes, notre service 'Livraison Père Noël' est disponible du 20 au 24 décembre : un livreur déguisé en Père Noël livre vos cadeaux à l'heure convenue. Réservation obligatoire avant le 10 décembre, supplément de 15€.");

        addKnowledge("fetes", "voeux entreprise|cadeaux clients|envois corporate|colis collaborateurs|distribution professionnelle",
                "Notre service 'Corporate Gifts' gère vos envois de fin d'année à vos clients et collaborateurs : stockage de vos cadeaux, préparation personnalisée, expédition simultanée ou programmée, et reporting détaillé.");

        addKnowledge("fetes", "colis alimentaires|envoi chocolats|expédition produits festifs|livraison périssable fêtes",
                "Pour l'envoi de produits alimentaires festifs, nous proposons un service spécial avec emballage isotherme et livraison prioritaire. Disponible du 15 novembre au 31 décembre pour les chocolats, foie gras et autres délices de fêtes.");

        // ASSURANCE
        addKnowledge("assurance", "assurance colis|garantie envoi|protection livraison|couverture dommages|indemnisation perte",
                "Tous nos envois incluent une assurance de base (jusqu'à 100€). Pour une protection supérieure, notre assurance Ad Valorem couvre jusqu'à 50 000€ pour 2% de la valeur déclarée (minimum 5€).");

        addKnowledge("assurance", "valeur déclarée|déclarer valeur|montant assuré|valeur assurance|couverture valeur",
                "Déclarez la valeur réelle de votre envoi lors de la création de votre expédition. Cette déclaration est obligatoire pour l'assurance Ad Valorem et doit pouvoir être justifiée en cas de sinistre (facture, estimation).");

        addKnowledge("assurance", "procédure sinistre|déclaration dommage|signaler perte|démarche assurance|réclamation assurée",
                "En cas de sinistre, signalez-le dans les 3 jours ouvrés via votre espace client (section 'Mes Envois > Signaler un problème'). Joignez photos, description détaillée et justificatif de valeur. Nous traiterons votre dossier sous 5 jours ouvrés.");

        addKnowledge("assurance", "remboursement assurance|indemnisation sinistre|compensation dommage|dédommagement perte",
                "Le remboursement intervient sous 10 jours ouvrés après validation de votre dossier. Il couvre la valeur déclarée (dans la limite du préjudice réel) et les frais d'expédition. Le versement se fait sur votre moyen de paiement initial.");

        addKnowledge("assurance", "exclusions assurance|limitations garantie|non couvert|hors garantie|refus assurance",
                "Exclusions principales : emballage inadapté, dommages préexistants, retard simple, force majeure, objets interdits, absence de preuve de valeur, déclaration tardive, ou valeur déclarée manifestement excessive.");

        addKnowledge("assurance", "assurance internationale|garantie envoi étranger|couverture mondiale|protection douane",
                "Notre assurance internationale couvre les mêmes risques qu'en national, plus les litiges douaniers. Recommandée pour tous les envois hors UE, elle facilite les démarches en cas de blocage ou taxation imprévue.");

        addKnowledge("assurance", "franchise assurance|seuil indemnisation|minimum remboursable|déduction assurance",
                "Notre assurance Ad Valorem ne comporte aucune franchise. Tout dommage ou perte est indemnisé dès le premier euro, dans la limite de la valeur déclarée et sur présentation des justificatifs requis.");

        addKnowledge("assurance", "preuve valeur|justificatif prix|facture originale|attestation valeur|estimation objet",
                "Conservez toujours une preuve de la valeur de votre envoi : facture d'achat, relevé bancaire, estimation officielle pour les objets anciens ou faits main. Sans justificatif, l'indemnisation sera limitée à 100€.");

        addKnowledge("assurance", "assurance objets spéciaux|garantie œuvre art|couverture antiquité|protection objet rare",
                "Pour les objets spéciaux (œuvres d'art, antiquités, pièces de collection), notre assurance Premium offre une couverture sur-mesure avec expertise préalable et transport spécialisé. Contactez notre service dédié au 0800-789-456.");

        addKnowledge("assurance", "litige assurance|contestation indemnisation|désaccord remboursement|recours assurance",
                "En cas de désaccord sur l'indemnisation proposée, vous disposez d'un droit de recours dans les 30 jours. Soumettez votre contestation motivée et documentée via le formulaire 'Recours Assurance' dans votre espace client.");

        // GENERAL
        addKnowledge("general", "à propos|qui êtes-vous|votre entreprise|présentation société|historique entreprise",
                "QuickDelivery est un acteur majeur de la livraison de colis en France et à l'international depuis 2010. Avec plus de 5000 collaborateurs et 10 000 points relais, nous livrons plus de 250 millions de colis par an dans 220 pays.");

        addKnowledge("general", "recrutement|emploi|carrière|postuler|offres emploi|travailler chez vous",
                "Rejoignez nos équipes ! Nous recrutons régulièrement des livreurs, préparateurs, développeurs et commerciaux. Consultez nos offres sur carrieres.quickdelivery.com ou envoyez votre candidature à recrutement@quickdelivery.com.");

        addKnowledge("general", "actualités|nouveautés|dernières infos|news|blog entreprise",
                "Retrouvez nos actualités, innovations et conseils sur notre blog blog.quickdelivery.com et nos réseaux sociaux (@QuickDeliveryFR). Abonnez-vous à notre newsletter mensuelle pour ne rien manquer.");

        addKnowledge("general", "partenariats|collaboration|devenir partenaire|programme affiliation|partenaire commercial",
                "Nous développons différents types de partenariats : points relais, affiliés e-commerce, intégrateurs technologiques et partenaires logistiques. Contactez notre équipe partenariats à partenaires@quickdelivery.com.");

        addKnowledge("general", "presse|médias|contact presse|communiqués|relations médias",
                "Pour toute demande presse, contactez notre service de communication à presse@quickdelivery.com ou au 01-23-45-67-89. Notre espace presse (presse.quickdelivery.com) contient nos communiqués et ressources médias.");

        addKnowledge("general", "investisseurs|relations investisseurs|actionnaires|résultats financiers|rapport annuel",
                "QuickDelivery est une société cotée à la bourse de Paris (QDL). Les informations financières, rapports annuels et présentations aux investisseurs sont disponibles sur investisseurs.quickdelivery.com.");

        addKnowledge("general", "siège social|adresse entreprise|localisation siège|bureaux centraux",
                "Notre siège social est situé au 123 Avenue de la Livraison, 75008 Paris. Nos centres logistiques principaux sont à Roissy, Lyon, Marseille et Bordeaux. Nous disposons de 150 agences réparties sur tout le territoire.");

        addKnowledge("general", "concurrents|autres transporteurs|différence concurrence|comparaison services|avantages",
                "Nous nous distinguons de la concurrence par notre réseau dense de points relais, notre application mobile intuitive, nos options de livraison flexibles et notre engagement environnemental. Nous garantissons le meilleur rapport qualité-prix.");

        addKnowledge("general", "récompenses|prix|distinctions|trophées|reconnaissances",
                "QuickDelivery a reçu plusieurs distinctions : 'Meilleur Service Client' (2022), 'Entreprise Éco-responsable' (2023), et 'Innovation Logistique' pour notre système de consignes connectées (2021).");

        addKnowledge("general", "responsabilité sociale|RSE|engagement sociétal|actions solidaires|impact social",
                "Notre politique RSE s'articule autour de trois piliers : environnement (réduction CO2), social (insertion, diversité) et sociétal (livraisons solidaires, soutien aux commerces locaux). Découvrez nos actions sur rse.quickdelivery.com.");
    }

    private static void addKnowledge(String category, String keywords, String answer) {
        categorizedKnowledge.get(category).put(keywords, answer);
    }

    /**
     * Searches for an answer based on user input
     * @param userInput the user's question or query
     * @return the most relevant answer or null if no match is found
     */
    public static String searchAnswer(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return null;
        }

        String lowerInput = userInput.toLowerCase(Locale.ROOT);
        Map<String, Integer> categoryMatches = new HashMap<>();
        Map<String, Double> bestMatchByCategory = new HashMap<>();
        Map<String, String> bestAnswerByCategory = new HashMap<>();

        // Search through all categories
        for (String category : categorizedKnowledge.keySet()) {
            Map<String, String> categoryKnowledge = categorizedKnowledge.get(category);
            int matchCount = 0;
            double bestMatchScore = 0;
            String bestMatchAnswer = null;

            for (Map.Entry<String, String> entry : categoryKnowledge.entrySet()) {
                String[] keywords = entry.getKey().split("\\|");
                double matchScore = 0;

                for (String keyword : keywords) {
                    keyword = keyword.trim();
                    if (lowerInput.contains(keyword)) {
                        // Calculate match score based on keyword length and position
                        double keywordScore = (double) keyword.length() / lowerInput.length();
                        // Keywords at the beginning get higher score
                        if (lowerInput.indexOf(keyword) < lowerInput.length() / 3) {
                            keywordScore *= 1.5;
                        }
                        matchScore += keywordScore;
                        matchCount++;
                    }
                }

                if (matchScore > bestMatchScore) {
                    bestMatchScore = matchScore;
                    bestMatchAnswer = entry.getValue();
                }
            }

            if (matchCount > 0) {
                categoryMatches.put(category, matchCount);
                bestMatchByCategory.put(category, bestMatchScore);
                bestAnswerByCategory.put(category, bestMatchAnswer);
            }
        }

        // Find the category with the highest match score
        if (!bestMatchByCategory.isEmpty()) {
            String bestCategory = null;
            double highestScore = 0;

            for (Map.Entry<String, Double> entry : bestMatchByCategory.entrySet()) {
                if (entry.getValue() > highestScore) {
                    highestScore = entry.getValue();
                    bestCategory = entry.getKey();
                }
            }

            // Return the best answer if score is above threshold
            if (highestScore > 0.1) {
                return bestAnswerByCategory.get(bestCategory);
            }
        }

        // If no good match, return a random fallback response
        return FALLBACK_RESPONSES[new Random().nextInt(FALLBACK_RESPONSES.length)];
    }

    /**
     * Gets all the knowledge base entries for a specific category
     * @param category the category to retrieve
     * @return a map of keyword patterns to answers
     */
    public static Map<String, String> getCategoryKnowledge(String category) {
        return categorizedKnowledge.getOrDefault(category, new HashMap<>());
    }

    /**
     * Gets all categories in the knowledge base
     * @return a set of category names
     */
    public static Set<String> getAllCategories() {
        return categorizedKnowledge.keySet();
    }

    /**
     * Gets the total number of entries in the knowledge base
     * @return the total number of question/answer pairs
     */
    public static int getTotalEntriesCount() {
        int count = 0;
        for (Map<String, String> categoryMap : categorizedKnowledge.values()) {
            count += categoryMap.size();
        }
        return count;
    }
}
