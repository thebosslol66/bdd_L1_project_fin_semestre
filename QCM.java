import java.util.ArrayList;

public class QCM{
	static int statusConnection;
	
	
	public static String menuPrincipal(){
		String text = "Choisissez ce que vous voulez faire:\n";
		text += "1. Saisir une nouvelle question\n";
		text += "2. Creer un nouveau QCM\n";
		text += "3. Remplir ou modifier un qcm existant\n";
		text += "4. Tester un QCM\n";
		text += "5. Sortir\n";
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
			Ecran.afficherln("Desole mais votre question est trop longue:");
			texteQuestion = Clavier.saisirString();
			 texteQuestion = texteQuestion.replace("'","\\'");
		}
		Ecran.afficher("Saisissez le nombre de reponces possibles pour cette question (attention ce nombre ne pourrat pas etre change et au maximum valoir 6):");
		nbQuestion = Clavier.saisirInt();
		while (nbQuestion <= 0 || nbQuestion > 6) {
			Ecran.afficher("Recommencez, le nombre de reponce doit etre entre 1 et 6: ");
			nbQuestion = Clavier.saisirInt();
		}
		String[] reponces = new String[nbQuestion];
		for (int i=0; i < nbQuestion; i++){
			Ecran.afficherln("Ecrivez la reponce numero ", i+1,"(je rapelle que la reponce doit faire moins de 200 carateres)");
			reponces[i] = Clavier.saisirString();
			reponces[i] = reponces[i].replace("'","\\'");
			while (reponces[i].length() > 200){
				Ecran.afficherln("Desole mais votre reponce est trop longue:");
				reponces[i] = Clavier.saisirString();
				reponces[i] = reponces[i].replace("'","\\' ");
			}
		}
		/*
		int selection = 0;
		while (selection != texteQuestion){
			Ecran.afficherln("Voici comment se presentras votre question:");
			Ecran.afficherln("0. ", texteQuestion);
			for (inr i=0; i < nbQuestion; i++){
				Ecran.afficherln(i+1, ". ", texteQuestion);
			}
			Ecran.afficherln(texteQuestion, ". Valider");
			Ecran.afficherln("Selectionner le texte a modifier avec le nombre qui se trouve a cote (entrez le nombre):");
			selection = Clavier.saisirInt();
			if (selection == 0){
				Ecran.afficherln("Reecrivez votre question:");
			}
			if (selection>0 && selection <texteQuestion){
			}
		}
		*/
		Ecran.afficherln("Voici comment se presentras votre question:");
		Ecran.afficherln(texteQuestion);
		for (int i=0; i < nbQuestion; i++){
			Ecran.afficherln(i+1, ". ", reponces[i]);
		}
		int selection = 0;
		Ecran.afficher("Selectionnez la bonne reponce (avec le nombre a cote de la reponce: ");
		selection = Clavier.saisirInt();
		while (selection <1 || selection > nbQuestion){
			Ecran.afficher("Le nombre doit se trouver a cote d une de vos reponce ");
			selection = Clavier.saisirInt();
		}
			
		String requestQuestion = "INSERT INTO `question`(`quTexte`, `quBonneReponse`) VALUES ('" + texteQuestion +"','" + selection + "')";
		int idQuestion = BD.executerUpdate(statusConnection, requestQuestion);
		
		if (idQuestion>=0){
			String requestReponce = "INSERT INTO `reponse`(`reQuestion`, `reOrdre`, `reTexte`) VALUES ";
			for (int i=0; i < nbQuestion-1; i++){
				requestReponce +="('" + idQuestion + "','" + (i+1) + "','" + reponces[i] + "'),";
			}
			requestReponce +="('" + idQuestion + "','" + nbQuestion + "','" + reponces[nbQuestion-1] + "')";
			if (BD.executerUpdate(statusConnection, requestReponce) < 0){
				Ecran.afficherln("Erreur lors de l'ecriture des reponces");
			}
			else{
				Ecran.afficherln("La question a ete enregistre");
			}
		}
		else{
			Ecran.afficherln("Erreur lors de l'ecriture de la question");
		}
	}
	
	public static void procedureNouveauQCM(){
		int res = BD.executerSelect(statusConnection, "SELECT `qcmTitre` FROM `qcm`");
		ArrayList<String> titrePris = new ArrayList<String>();
		Ecran.afficherln("Voici tous les QCM deja enregistres: ");
		while (BD.suivant(res)) {
			Ecran.afficherln(BD.attributString(res, "qcmTitre"));
			titrePris.add(BD.attributString(res, "qcmTitre"));
		}
		if (titrePris.size() == 0) {
			Ecran.afficherln("Il n'y a aucun QCM actuellement");
		}
		Ecran.afficherln("Ecrivez le titre du nouveau QCM pour ajouter le QCM ou laissez le champ blanc pour revenir a la page principale");
		String nouveauTitre = Clavier.saisirString();
		nouveauTitre.replace("'","\\'");
		while((titrePris.contains(nouveauTitre) && nouveauTitre.length() > 0) || nouveauTitre.length() > 200){
			if (nouveauTitre.length() > 200){
				Ecran.afficherln("Ce nom est trop long (maximum 100 carateres)(vous pouvez laissez le champ vide pour quitter).");
			}
			else {
				Ecran.afficherln("Ce nom existe deja (vous pouvez laissez le champ vide pour quitter).");
			}
			nouveauTitre = Clavier.saisirString();
			nouveauTitre.replace("'","\\'");
		}
		
		if (nouveauTitre.length() > 0){
			BD.executerUpdate(statusConnection, "INSERT INTO `qcm` (`qcmTitre`) VALUES ('" + nouveauTitre + "')");
			Ecran.afficherln("Votre QCM a ete enregistre avec succes. Pour ajouter des question a ce qcm rendez vous dans l'option Pemplir ou modifier un qcm existant");			
		}
	}
	
	public static void procedureRemplissageModificationQCM(){
		Ecran.afficherln("Choisissez le QCM a modifier");
		int res = BD.executerSelect(statusConnection, "SELECT * FROM `qcm`");
		ArrayList<Integer> possibleID = new ArrayList<Integer>();
		while (BD.suivant(res)) {
			Ecran.afficherln(BD.attributInt(res, "qcmID"), ". ", BD.attributString(res, "qcmTitre"));
			possibleID.add(BD.attributInt(res, "qcmID"));
		}
		
		Ecran.afficherln("Pour selectionner une question entrez l'id de la question (pour quitter ecrivez -1):");
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
				Ecran.afficherln("2. Supprimer une Question au QCM");
				Ecran.afficherln("3. Quitter les modifications");
				
				Ecran.afficher("Entrez votre choix (le numero): ");
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
					
					Ecran.afficher("Selectionnez le numero de la question a ajouter au QCM (ecrivez -1 pour revenir a l'ecan precedent): ");
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
					Ecran.afficher("Selectionnez le numero de la question a supprimer (ecrivez -1 pour revenir a l'ecan precedent): ");
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
		
		Ecran.afficherln("Pour selectionner un QCM entrez l'id du QCM (pour quitter ecrivez -1):");
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
				questionList.add(new Quest(BD.attributInt(res, "quID"), BD.attributString(res, "quTexte"),BD.attributInt(res, "quBonneReponse")));
			}
			res = BD.executerSelect(statusConnection, "SELECT * FROM `reponse` WHERE `reQuestion` IN (SELECT `quID` FROM `question` WHERE `quID` IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = '" + selectionQCM + "')) ORDER BY `reQuestion`,`reOrdre`");
			while (BD.suivant(res)) {
				reponseList.add(new Rep(BD.attributInt(res, "reQuestion"), BD.attributInt(res, "reOrdre"),BD.attributString(res, "reTexte")));
			}
			
			int score = 0;
			int numQuest = 0;
			
			Quest questionActuelle;
			int idLastReponse =1;
			
			while (questionList.size() > 0) {
				numQuest+=1;
				Ecran.afficherln("QCM : ", titreQCM);
				Ecran.afficherln("Question ", numQuest, "/", numQuest+questionList.size()-1,": ");
				questionActuelle = questionList.get((int)Math.random()*questionList.size());
				Ecran.afficherln(questionActuelle.quTexte);
				for (int i =0; i < reponseList.size(); i++){
					if (questionActuelle.quID == reponseList.get(i).reQuestion){
						Ecran.afficherln(reponseList.get(i).reOrdre, ". ", reponseList.get(i).reTexte);
						 idLastReponse = reponseList.get(i).reOrdre;
					}
				}
				Ecran.afficher("Choisissez votre reponse avec l'id de la reponse: ");
				int selectionReponse= Clavier.saisirInt();
				while(selectionReponse<1 && selectionReponse >= idLastReponse){
					Ecran.afficherln("Saisissez une reponse valide: ");
					selectionReponse = Clavier.saisirInt();
				}
				if (selectionReponse == questionActuelle.quBonneReponse){
					score += 1;
				}
				questionList.remove(questionActuelle);
			}
			if (numQuest == 0){
				Ecran.afficherln("Desole mais il n'y a pas de question dans ca qcm");
			}
			else {
				Ecran.afficherln("Votre score est de ", score,"/", numQuest, " soit ", (score*20/(float)numQuest), "/20");
			}
			
		}
		//Questions "SELECT * FROM `question` WHERE `quID` IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = 1)";
		//Reponses "SELECT * FROM `reponse` WHERE `reQuestion` IN (SELECT `quID` FROM `question` WHERE `quID` IN (SELECT `cqQuestion` FROM `compo_qcm` WHERE `cqQcm` = )) ORDER BY `reQuestion`,`reOrdre`";
	}
	
	
	public static class Quest{
		public int quID;
		public int quBonneReponse;
		public String quTexte;
		
		public Quest(int id, String texte, int bonneReponse){
			quID = id;
			quTexte = texte;
			quBonneReponse = bonneReponse;
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
	
	public static void main(String [] args){
		int choix=0;
		
		statusConnection = BD.ouvrirConnexion("www.db4free.net", "bdl1ufrst", "bdl1ufrst", "bdl1ufrstpass");
		
		while (choix != 5){
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
					procedureRepondreQCM();
				}break;
					
			}
		}
	}
	
}