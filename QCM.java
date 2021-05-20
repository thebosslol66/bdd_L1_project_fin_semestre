import java.util.ArrayList;

public class QCM{
	static int statusConnection;
	
	
	public static String menuPrincipal(){
		String text = "Choisissez ce que vous voulez faire:\n";
		text += "1. Saisir une nouvelle question\n";
		text += "2. Créer un nouveau QCM\n";
		text += "3. Remplir ou modifier un qcm existant\n";
		text += "4. Rechercher une question par mot clé\n";
		text += "5. Modifier une question ou une réponse\n";
		text += "6. Supprimer une question\n";
		text += "7. Tester un QCM\n";
		text += "8. Sortir\n";
		text += ">";
		return text;
	}
	
	public static void procedureAjoutQuestion(){
		String texteQuestion;
		int nbQuestion;
		int idBonneReponce;
		Ecran.afficherln("Saisissez votre question:");
		texteQuestion = Clavier.saisirString();
		texteQuestion = texteQuestion.replace("'","\\'");
		while(texteQuestion.length() >200){
			Ecran.afficherln("Désolé mais votre question est trop longue:");
			texteQuestion = Clavier.saisirString();
			 texteQuestion = texteQuestion.replace("'","\\'");
		}
		Ecran.afficher("Saisissez le nombre de réponses possibles pour cette question (attention ce nombre ne pourra pas être changé et au maximum valoir 6):");
		nbQuestion = Clavier.saisirInt();
		while (nbQuestion <= 0 || nbQuestion > 6) {
			Ecran.afficher("Recommencez, le nombre de réponse doit être entre 1 et 6: ");
			nbQuestion = Clavier.saisirInt();
		}
		String[] reponses = new String[nbQuestion];
		for (int i=0; i < nbQuestion; i++){
			Ecran.afficherln("Ecrivez la réponse numero ", i+1,"(je rappele que la réponse doit faire moins de 200 caratères)");
			reponses[i] = Clavier.saisirString();
			reponses[i] = reponses[i].replace("'","\\'");
			while (reponses[i].length() > 200){
				Ecran.afficherln("Désolé mais votre réponse est trop longue:");
				reponses[i] = Clavier.saisirString();
				reponses[i] = reponses[i].replace("'","\\' ");
			}
		}

		Ecran.afficherln("Voici comment se présentera votre question:");
		Ecran.afficherln(texteQuestion.replace("\\'","'"));
		for (int i=0; i < nbQuestion; i++){
			Ecran.afficherln(i+1, ". ", reponses[i].replace("\\'","'"));
		}
		int selection = 0;
		Ecran.afficher("Sélectionnez la bonne réponse (avec le nombre à côté de la réponse: ");
		selection = Clavier.saisirInt();
		while (selection <1 || selection > nbQuestion){
			Ecran.afficher("Le nombre doit se trouver à côté d'une de vos réponse ");
			selection = Clavier.saisirInt();
		}
			
		String requestQuestion = "INSERT INTO `question`(`quTexte`, `quBonnereponse`) VALUES ('" + texteQuestion +"','" + selection + "')";
		int idQuestion = BD.executerUpdate(statusConnection, requestQuestion);
		
		if (idQuestion>=0){
			String requestReponce = "INSERT INTO `reponse`(`reQuestion`, `reOrdre`, `reTexte`) VALUES ";
			for (int i=0; i < nbQuestion-1; i++){
				requestReponce +="('" + idQuestion + "','" + (i+1) + "','" + reponses[i] + "'),";
			}
			requestReponce +="('" + idQuestion + "','" + nbQuestion + "','" + reponses[nbQuestion-1] + "')";
			if (BD.executerUpdate(statusConnection, requestReponce) < 0){
				Ecran.afficherln("Erreur lors de l'écriture des réponses");
			}
			else{
				Ecran.afficherln("La question a été enregistrée");
			}
		}
		else{
			Ecran.afficherln("Erreur lors de l'écriture de la question");
		}
	}
	
	public static void procedureNouveauQCM(){
		int res = BD.executerSelect(statusConnection, "SELECT `qcmTitre` FROM `qcm`");
		ArrayList<String> titrePris = new ArrayList<String>();
		Ecran.afficherln("Voici tous les QCM déjà enregistrés: ");
		while (BD.suivant(res)) {
			Ecran.afficherln(BD.attributString(res, "qcmTitre"));
			titrePris.add(BD.attributString(res, "qcmTitre"));
		}
		if (titrePris.size() == 0) {
			Ecran.afficherln("Il n'y a aucun QCM actuellement");
		}
		Ecran.afficherln("Ecrivez le titre du nouveau QCM pour ajouter le QCM ou laissez le champ blanc pour revenir à la page principale");
		String nouveauTitre = Clavier.saisirString();
		nouveauTitre.replace("'","\\'");
		while((titrePris.contains(nouveauTitre) && nouveauTitre.length() > 0) || nouveauTitre.length() > 200){
			if (nouveauTitre.length() > 200){
				Ecran.afficherln("Ce nom est trop long (maximum 100 caratères)(vous pouvez laissez le champ vide pour quitter).");
			}
			else {
				Ecran.afficherln("Ce nom existe déjà (vous pouvez laissez le champ vide pour quitter).");
			}
			nouveauTitre = Clavier.saisirString();
			nouveauTitre.replace("'","\\'");
		}
		
		if (nouveauTitre.length() > 0){
			BD.executerUpdate(statusConnection, "INSERT INTO `qcm` (`qcmTitre`) VALUES ('" + nouveauTitre + "')");
			Ecran.afficherln("Votre QCM a été enregistré avec succès. Pour ajouter des question à ce qcm rendez vous dans l'option Remplir ou modifier un qcm existant");			
		}
	}
	
	public static void procedureRemplissageModificationQCM(){
		Ecran.afficherln("Choisissez le QCM à modifier");
		int res = BD.executerSelect(statusConnection, "SELECT * FROM `qcm`");
		ArrayList<Integer> possibleID = new ArrayList<Integer>();
		while (BD.suivant(res)) {
			Ecran.afficherln(BD.attributInt(res, "qcmID"), ". ", BD.attributString(res, "qcmTitre"));
			possibleID.add(BD.attributInt(res, "qcmID"));
		}
		
		Ecran.afficherln("Pour sélectionner une question entrez l'id de la question (pour quitter écrivez -1):");
		int selectionQuestion = Clavier.saisirInt();
		while(!possibleID.contains(selectionQuestion) && selectionQuestion >= 0){
			Ecran.afficherln("Saisissez un nombre existant");
			selectionQuestion = Clavier.saisirInt();
		
		}
		int selection;
		if (selectionQuestion >= 0){
			do{
				possibleID = new ArrayList<Integer>();
				if (selectionQuestion >= 0){
					Ecran.afficherln("Voici les questions du qcm:");
					res = BD.executerSelect(statusConnection, "SELECT `quID`, `quTexte` FROM `question` WHERE `quID` IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = '" + selectionQuestion + "')");
					while (BD.suivant(res)) {
						Ecran.afficherln(BD.attributInt(res, "quID"), ". ", BD.attributString(res, "quTexte"));
						possibleID.add(BD.attributInt(res, "quID"));
					}
				}
				if (possibleID.size() == 0) {
					Ecran.afficherln("Il n'y a aucune question dans ce QCM actuellement");
				}
				
				Ecran.afficherln("Que voulez vous faire: ");
				Ecran.afficherln("1. Ajouter une question au QCM");
				Ecran.afficherln("2. Supprimer une question au QCM");
				Ecran.afficherln("3. Quitter les modifications");
				
				Ecran.afficher("Entrez votre choix (le numéro): ");
				selection = Clavier.saisirInt();
				while(selection < 1 || selection > 3){
					Ecran.afficherln("Saisissez un nombre possible");
					selection = Clavier.saisirInt();
				}
				
				possibleID = new ArrayList<Integer>();
				
				if (selection == 1) {
					res = BD.executerSelect(statusConnection, "SELECT `quID`, `quTexte` FROM `question` WHERE `quID` NOT IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = '" + selectionQuestion + "')");
					
					while (BD.suivant(res)) {
						Ecran.afficherln(BD.attributInt(res, "quID"), ". ", BD.attributString(res, "quTexte"));
						possibleID.add(BD.attributInt(res, "quID"));
					}
					
					Ecran.afficher("Sélectionnez le numéro de la question à ajouter au QCM (écrivez -1 pour revenir à l'ecan précédent): ");
					int choix = Clavier.saisirInt();
					while(!possibleID.contains(choix) && choix >= 0){
						Ecran.afficherln("Saisissez un nombre possible");
						selection = Clavier.saisirInt();
					}
					
					if (choix >= 0) {
						BD.executerUpdate(statusConnection, "INSERT INTO `compo_qcm`(`cqQcm`, `cqQuestion`) VALUES ('" + selectionQuestion +"','" + choix + "')");
					}
				}
				
				else if (selection == 2) {
					BD.reinitialiser(res);
					while (BD.suivant(res)) {
						Ecran.afficherln(BD.attributInt(res, "quID"), ". ", BD.attributString(res, "quTexte"));
						possibleID.add(BD.attributInt(res, "quID"));
					}
					Ecran.afficher("Sélectionnez le numéro de la question a supprimer (écrivez -1 pour revenir à l'ecran précédent): ");
					int choix = Clavier.saisirInt();
					while(!possibleID.contains(choix) && choix >= 0){
						Ecran.afficherln("Saisissez un nombre possible");
						selection = Clavier.saisirInt();
					}
					
					if (choix >= 0) {
						BD.executerUpdate(statusConnection, "DELETE FROM `compo_qcm` WHERE `cqQcm`='" + selectionQuestion + "' AND `cqQuestion`='" + choix +"'");
					}
				}
			}while(selection != 3);
		}
		
	}
	
	public static void procedureRepondreQCM(){
		Ecran.afficherln("Choisissez le QCM que vous voulez faire");
		int res = BD.executerSelect(statusConnection, "SELECT * FROM `qcm`");
		ArrayList<Integer> possibleID = new ArrayList<Integer>();
		while (BD.suivant(res)) {
			Ecran.afficherln(BD.attributInt(res, "qcmID"), ". ", BD.attributString(res, "qcmTitre"));
			possibleID.add(BD.attributInt(res, "qcmID"));
		}
		
		Ecran.afficherln("Pour sélectionner un QCM entrez l'id du QCM (pour quitter écrivez -1):");
		int selectionQCM = Clavier.saisirInt();
		while(!possibleID.contains(selectionQCM) && selectionQCM >= 0){
			Ecran.afficherln("Saisissez un nombre existant");
			selectionQCM = Clavier.saisirInt();
		}
		if (selectionQCM >= 0){
			BD.reinitialiser(res);
			String titreQCM = "";
			while (BD.suivant(res) && titreQCM == ""){
				if (BD.attributInt(res, "qcmID") == selectionQCM){
					titreQCM  = BD.attributString(res, "qcmTitre");
				}
			}
			
			ArrayList<Quest> questionList = new ArrayList<Quest>();
			ArrayList<Rep> reponseList = new ArrayList<Rep>();
			res = BD.executerSelect(statusConnection, "SELECT * FROM `question` WHERE `quID` IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = '" + selectionQCM + "')");
			while (BD.suivant(res)) {
				questionList.add(new Quest(BD.attributInt(res, "quID"), BD.attributString(res, "quTexte"),BD.attributInt(res, "quBonnereponse")));
			}
			res = BD.executerSelect(statusConnection, "SELECT * FROM `reponse` WHERE `reQuestion` IN (SELECT `quID` FROM `question` WHERE `quID` IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = '" + selectionQCM + "')) ORDER BY `reQuestion`,`reOrdre`");
			while (BD.suivant(res)) {
				reponseList.add(new Rep(BD.attributInt(res, "reQuestion"), BD.attributInt(res, "reOrdre"),BD.attributString(res, "reTexte")));
			}
			
			int score = 0;
			int numQuest = 0;
			
			Quest questionActuelle;
			int idLastreponse =1;
			
			while (questionList.size() > 0) {
				numQuest+=1;
				Ecran.afficherln("QCM : ", titreQCM);
				Ecran.afficherln("Question ", numQuest, "/", numQuest+questionList.size()-1,": ");
				questionActuelle = questionList.get((int)Math.random()*questionList.size());
				Ecran.afficherln(questionActuelle.quTexte);
				for (int i =0; i < reponseList.size(); i++){
					if (questionActuelle.quID == reponseList.get(i).reQuestion){
						Ecran.afficherln(reponseList.get(i).reOrdre, ". ", reponseList.get(i).reTexte);
						 idLastreponse = reponseList.get(i).reOrdre;
					}
				}
				Ecran.afficher("Choisissez votre réponse avec l'id de la réponse: ");
				int selectionreponse= Clavier.saisirInt();
				while(selectionreponse<1 && selectionreponse >= idLastreponse){
					Ecran.afficherln("Saisissez une réponse valide: ");
					selectionreponse = Clavier.saisirInt();
				}
				if (selectionreponse == questionActuelle.quBonnereponse){
					score += 1;
				}
				questionList.remove(questionActuelle);
			}
			if (numQuest == 0){
				Ecran.afficherln("Désolé mais il n'y a pas de question dans ce qcm");
			}
			else {
				Ecran.afficherln("Votre score est de ", score,"/", numQuest, " soit ", (score*20/(float)numQuest), "/20");
			}
			
		}
		}
	public static void procedureSupprimeQuestion(){
		int idQuestion;
		boolean idValide = false;
		ArrayList<Quest> questionList = new ArrayList<Quest>();
		String requestQuestionInitial = "SELECT * FROM question ORDER BY quID ASC";
		int res = BD.executerSelect(statusConnection, requestQuestionInitial);
		while (BD.suivant(res)) {
			questionList.add(new Quest(BD.attributInt(res, "quID"), BD.attributString(res, "quTexte"),BD.attributInt(res, "quBonnereponse")));
		}
		Ecran.afficherln("Choisissez la question que vous voulez supprimer : ");
		for (int i = 0; i<questionList.size(); i++) {
			Ecran.afficherln(questionList.get(i).quID," : ", questionList.get(i).quTexte);
		}
		idQuestion = Clavier.saisirInt();
		int i1 = 0;
		while (i1<questionList.size() && !idValide) {
			if (idQuestion == questionList.get(i1).quID) {
				idValide = true;
			}
			i1++;
		}
		while (!idValide) {
			Ecran.afficherln("Cette question n'existe pas !");
			Ecran.afficherln("Choisissez la question que vous voulez supprimer : ");
			for (int i = 0; i<questionList.size(); i++) {
				Ecran.afficherln(questionList.get(i).quID," : ", questionList.get(i).quTexte);
			}
			idQuestion = Clavier.saisirInt();
			int i2 = 0;
			while (i2<questionList.size() && !idValide) {
				if (idQuestion == questionList.get(i2).quID) {
					idValide = true;
				}
				i2++;
			}
		}
		BD.fermerResultat(res);
		String reqestIsQcm= "SELECT DISTINCT cqQcm FROM compo_qcm WHERE cqQuestion = "+idQuestion;
		res = BD.executerSelect(statusConnection, reqestIsQcm);
		Ecran.afficherln(res);
		if(res !=0){
			Ecran.afficherln("Attention, cette question est présente dans un ou plusieurs QCM,");
			Ecran.afficherln("La suppression ne peut pas être effectuée !");
		}else{
			String reqestSuppression = "DELETE FROM question WHERE quID = "+idQuestion;
			Ecran.afficherln(reqestSuppression);
			int result = BD.executerUpdate(statusConnection, reqestSuppression);	
			Ecran.afficherln(result);
		}

	}
	public static void procedureModifierQuestion(){
		int idQuestion;
		int resultQuestion;
		int wantreponse = 0;
		String newQuestion;
		int newreponse;
		String requestnewreponse;
		ArrayList<Quest> questionList = new ArrayList<Quest>();
		String requestQuestionInitial = "SELECT * FROM question ORDER BY quID ASC";
		String requestModification = "UPDATE question SET quTexte = ";
		int res = BD.executerSelect(statusConnection, requestQuestionInitial);
		while (BD.suivant(res)) {
			questionList.add(new Quest(BD.attributInt(res, "quID"), BD.attributString(res, "quTexte"),BD.attributInt(res, "quBonnereponse")));
		}
		Ecran.afficherln("Choisissez la question que vous voulez modifier : ");
		for (int i = 0; i<questionList.size(); i++) {
			Ecran.afficherln(questionList.get(i).quID," : ", questionList.get(i).quTexte);
		}
		idQuestion = Clavier.saisirInt();
		Ecran.afficherln("Modification de la question N°",idQuestion,".");
		Ecran.afficherln("Entrez le nouveau texte de la question : ");
		newQuestion = Clavier.saisirString();
		requestModification = requestModification + "\"" + newQuestion.replace("'","\\'") + "\"" + " WHERE quID = " + idQuestion;
		resultQuestion = BD.executerUpdate(statusConnection, requestModification);
		Ecran.afficherln("Voulez vous modifier les réponses de cette question ?");
		Ecran.afficherln("0 : Oui");
		Ecran.afficherln("1 : Non");
		wantreponse = Clavier.saisirInt();
		while(wantreponse !=0 && wantreponse !=1){
			Ecran.afficherln("Veuillez choisir 0 ou 1 !");
			Ecran.afficherln("Voulez vous modifier les réponses de cette question ?");
			Ecran.afficherln("0 : Oui");
			Ecran.afficherln("1 : Non");
			wantreponse = Clavier.saisirInt();
		}
		if (wantreponse == 1) {
			procedureModifierreponse(idQuestion);
		}
		Ecran.afficherln("Maintenant choisissez la nouvelle bonne réponse si elle a changé (-1 si elle n'a pas changé");
		newreponse = Clavier.saisirInt();
		if (newreponse != -1) {
			requestnewreponse = "UPDATE question SET quBonnereponse = "+newreponse+" WHERE quID = "+idQuestion;

		}




	}
	public static void procedureModifierreponse(int idQuestion){
		Ecran.afficherln("Voici les réponses de cette Question\n");
		int sortir = 0;
		int nbreponse = 0;
		while (sortir !=3 ){
			ArrayList<Rep> reponseList = new ArrayList<Rep>();
			String requestModificationreponse = "SELECT * FROM reponse WHERE reQuestion = " + idQuestion + " ORDER BY reOrdre";
			int res2 = BD.executerSelect(statusConnection, requestModificationreponse);
			while(BD.suivant(res2)){
				reponseList.add(new Rep(BD.attributInt(res2, "reQuestion"), BD.attributInt(res2, "reOrdre"),BD.attributString(res2, "reTexte")));
			}
			nbreponse = reponseList.size();
			int choix = 0;
			for (int i = 0; i<reponseList.size(); i++) {
				Ecran.afficherln(reponseList.get(i).reOrdre," : ", reponseList.get(i).reTexte);

			}
			Ecran.afficherln("Voulez-vous : ");
			Ecran.afficherln("1 : Modifier le texte d'une réponse");
			Ecran.afficherln("2 : Ajouter une réponse");
			Ecran.afficherln("3 : Sortir de la modification des réponses.");
			choix = Clavier.saisirInt();
			switch (choix) {
				case 1:{
					int reponseId;
					String text;
					String request;
					Ecran.afficherln("Entrez le numéro de la réponse à modifier : ");
					reponseId = Clavier.saisirInt();
					Ecran.afficherln("Entrez le nouveau texte de la réponse");
					text = Clavier.saisirString();
					request = "UPDATE reponse SET reTexte = " + "\"" + text + "\" " + "WHERE reQuestion = " + idQuestion  + " AND reOrdre = " + reponseId;
					BD.executerUpdate(statusConnection, request);
				}break;
				case 2:{
					nbreponse +=1;
					String text;
					int nb;
					String request;
					Ecran.afficherln("Entrez le numéro de la nouvelle réponse");
					nb = Clavier.saisirInt();
					Ecran.afficherln("Entrez le texte de la nouvelle réponse : ");
					text = Clavier.saisirString();
					Ecran.afficherln("Cette réponse sera la réponse N°",nbreponse,".");
					request = "INSERT INTO reponse (reQuestion, reOrdre, reTexte) VALUES ("+idQuestion+", "+nb+", "+"\'"+text.replace("'","\\'")+"\')";
					BD.executerUpdate(statusConnection, request);
				}break;
				case 3:{
					sortir = 3;
				}break;
				default:
					Ecran.afficherln("Valeur incorrecte");
			}		
			
		}
	}

	



	
	
	public static class Quest{
		public int quID;
		public int quBonnereponse;
		public String quTexte;
		
		public Quest(int id, String texte, int bonnereponse){
			quID = id;
			quTexte = texte;
			quBonnereponse = bonnereponse;
		}
	}
	
	public static class Rep {
		public int reQuestion;
		public int reOrdre;
		public String reTexte;
		
		public Rep(int question, int ordre, String texte){
			reQuestion = question;
			reOrdre = ordre;
			reTexte = texte;
		}
	}
	
	
	
	public static void procedureRechercheQCM(){
		String motRecherche;
		ArrayList<Integer> possibleID;

		Ecran.afficherln("Entrez votre texte à rechercher dans les questions(écrivez 'exit' pour sortir)");
		motRecherche = Clavier.saisirString();
		
		if (motRecherche != "exit"){
			Ecran.afficherln("Voici la liste de toutes les questions contenant votre mot clé, vous pouvez retenir le numéro de la question pour la modifier ou la supprimer dans les autres menus.");
			int res = BD.executerSelect(statusConnection, "SELECT `quID`, `quTexte` FROM `question` WHERE `quTexte` LIKE '%" + motRecherche + "%'");
			possibleID = new ArrayList<Integer>();
			while (BD.suivant(res)) {
				Ecran.afficherln(BD.attributInt(res, "quID"), ". ", BD.attributString(res, "quTexte"));
				possibleID.add(BD.attributInt(res, "quID"));
			}
			
			
			
		}
		
	}
	
	public static void main(String [] args){
		int choix=0;
		
		statusConnection = BD.ouvrirConnexion("www.db4free.net", "bdl1ufrst", "bdl1ufrst", "bdl1ufrstpass");
		
		while (choix != 8){
			Ecran.afficher(menuPrincipal());
			choix = Clavier.saisirInt();
			switch(choix){
				case 1:{
					procedureAjoutQuestion();
				}break;
					
				case 2:{
					procedureNouveauQCM();
				}break;
					
				case 3:{
					procedureRemplissageModificationQCM();
				}break;
					
				case 4:{
					procedureRechercheQCM();
				}break;
				
				case 5:{
					procedureModifierQuestion();
				}break;

				case 6:{
					procedureSupprimeQuestion();
				}break;
				case 7:{
					procedureRepondreQCM();
				}break;
					
				
					
				
					
			}
		}
		
		BD.fermerConnexion(statusConnection);
	}
	
}
