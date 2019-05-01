package alang.apllifraismobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class FicheDetaille extends AppCompatActivity {

    // Non des variable retourner par le JSON
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MontantValide = "montant_v";
    private static final String TAG_DateFiche = "Date_Fiche";
    private static final String TAG_EtatFiche = "etat";
    private static final String TAG_LigneHF = "LigneHor";
    private static final String TAG_LigneF = "LigneForfais";

    private static final String TAG_date = "date";
    private static final String TAG_type = "type";
    private static final String TAG_montant = "montant";
    private static final String TAG_quantite = "quantite";
    private static final String TAG_valide = "valide";
    private static final String TAG_libelle = "libelle";



    // Progress Dialog
    private ProgressDialog pDialog;

    // Creation de l'objet JSON Parser
    JSONParser jParser = new JSONParser();

    //Boolean true si les données on bien été récupéré
    boolean success;

    // url du controller Symfony
    private static String url_all_products = "http://94.23.221.178/Applifrais/web/app.php/api/info";

    //Liste qui contient les Ligne Forfait et Hors Forfait de la fiche frais
    ArrayList<HashMap<String,String>> productLesLigneHF = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String,String>> productLesLigneF = new ArrayList<HashMap<String,String>>();

    //Fonction executé a la creation de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Appelle de l'activité et modification de son titre
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiche_detaille);
        setTitle("Détaille fiche frais");

        // Recuperation de la l'id de la fiche envoyé par l'ancienne activité
        Intent i = getIntent();
        String idfiche = i.getStringExtra("id_fiche");

        //On lance la fonction connect qui s'exécute en arrière-plan et on lui passe l'id de la fiche en paramètre
        new Connect().execute(""+idfiche);



    }

    /**
     * Fonction qui s'exécute en arrière-plan
     * */
    class Connect extends AsyncTask<String, String, String> {

        //Variable qui contient le message retourner par le contoller
        String message;

        //Fonction qui démarre au début pour montrer à l'utilisateur ou est la réception des informations de la fiche
        @Override
        protected void onPreExecute() {
            //Ouverture d'une fenêtre de dialogue
            super.onPreExecute();
            pDialog = new ProgressDialog(FicheDetaille.this);
            pDialog.setMessage("Chargement des fiches en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Connexion via l'url
         */
        protected String doInBackground(String... args) {

            //Récupération des variables passé en paramètre (l'id de la fiche)
            String id_fiche = args[0];

            //on stock cette valeur dans une Hashmap
            HashMap<String, String> params = new HashMap<>();
            params.put("ID", id_fiche);

            //On envoie a JSONParser l'url, la méthode avec laquelle on veut communiquer (GET ou POST) et la HashMap qui contient les
            //Paramètres
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);


            //On récupère les informations retourner par le controller. La valeur succès (true la récupération des informations de la fiche est
            // réussie) et la valeur de message
            try {


                success = json.getBoolean(TAG_SUCCESS);
                //Si la variable success vaut sur true
                if (success == true){
                    //On récupère les tableaux des lignes hors forfaits et des lignes forfaits
                    JSONArray LigneF = json.getJSONArray(TAG_LigneF);
                    JSONArray LigneHF = json.getJSONArray(TAG_LigneHF);

                    //Variable qui contienne la Date de la fiche et le Montant total de remboursement
                    final String DateFiche = json.getString(TAG_DateFiche);
                    final String TotalFiche = json.getString(TAG_MontantValide);

                    //Variable qui contient le montant totales des frais hors forfaits et des frais forfaits valide
                     double MontantF = 0;
                     double MontantHF = 0;

                    //On affiche s’il existe au moins une Ligne forfait le tableau des lignes forfaits
                    if (LigneF.length() > 0) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_date, "Date");
                        map.put(TAG_type, "Type");
                        map.put(TAG_quantite, "Quantité");
                        map.put(TAG_montant, "Montant");
                        map.put(TAG_valide, "Valide");
                        productLesLigneF.add(map);
                    }
                    //On affiche s’il existe au moins une Ligne hors forfait le tableau des lignes hors forfaits
                    if (LigneHF.length() > 0){
                        HashMap<String,String> map = new HashMap<String, String>();

                        map.put(TAG_date,"Date");
                        map.put(TAG_libelle,"Libelle");
                        map.put(TAG_montant,"Montant");
                        map.put(TAG_valide,"Valide");
                        productLesLigneHF.add(map);
                    }

                    //On récupère toute les ligne information des ligne Forfaits et on les stocks dans une HashMap que l'on stock dans une liste
                    for (int i = 0; i < LigneF.length();i++){
                        JSONObject uneFiche = LigneF.getJSONObject(i);

                        String Date = uneFiche.getString(TAG_date);
                        String type = uneFiche.getString(TAG_type);
                        String quantite = uneFiche.getString(TAG_quantite);
                        double montant = uneFiche.getDouble(TAG_montant);
                        boolean valide = uneFiche.getBoolean(TAG_valide);



                        HashMap<String,String> map = new HashMap<String, String>();

                    map.put(TAG_date,Date);
                    map.put(TAG_type,type);
                    map.put(TAG_quantite,quantite);
                    map.put(TAG_montant,""+montant+" €");
                    if (valide){
                        map.put(TAG_valide,"oui");
                        MontantF += montant;
                    }else{
                        map.put(TAG_valide,"non");
                    }


                    productLesLigneF.add(map);
                }
                    //On récupère toute les ligne information des ligne hors Forfaits et on les stocks dans une HashMap que l'on stock dans une liste
                    for (int i = 0; i < LigneHF.length();i++){
                        JSONObject uneFiche = LigneHF.getJSONObject(i);

                        String Date = uneFiche.getString(TAG_date);
                        String libelle = uneFiche.getString(TAG_libelle);
                        double montant = uneFiche.getDouble(TAG_montant);
                        boolean valide = uneFiche.getBoolean(TAG_valide);



                        HashMap<String,String> map = new HashMap<String, String>();

                        map.put(TAG_date,Date);
                        map.put(TAG_libelle,libelle);
                        map.put(TAG_montant,""+montant+" €");
                        if (valide){
                            map.put(TAG_valide,"oui");
                            MontantHF += montant;
                        }else{
                            map.put(TAG_valide,"non");
                        }


                        productLesLigneHF.add(map);
                    }

                    //On récupère le montant totale des frais forfaits et des frais hors forfaits
                    final double MontantFfinal = MontantF;
                    final double MontantHFfinal = MontantHF;


                    // On exécute un thread pour pouvoir modifier le TextField qui contient la valeur totale des frais forfaits
                    final TextView TotalFraisForfait = (TextView) findViewById(R.id.txt_total_frais_forfait);
                    TotalFraisForfait.post(new Runnable() {
                        public void run() {
                            TotalFraisForfait.setText(MontantFfinal+" €");
                        }
                    });

                    //On exécute un thread pour pouvoir modifier le TextField qui contient la valeur totale des frais hors forfaits
                    final TextView TotalFraisHorForfait = (TextView) findViewById(R.id.txt_total_frais_hor_forfait);
                    TotalFraisHorForfait.post(new Runnable() {
                        public void run() {
                            TotalFraisHorForfait.setText(MontantHFfinal+" €");
                        }
                    });


                    //On exécute un thread pour pouvoir modifier le TextField qui contient la valeur totale du montant remboursé
                    final TextView txt_TotalFiche = (TextView) findViewById(R.id.txt_total_frais);
                    txt_TotalFiche.post(new Runnable() {
                        public void run() {
                            txt_TotalFiche.setText(TotalFiche+" €");
                        }
                    });

                    //On exécute un thread pour pouvoir modifier le TextField qui contient la date de la fiche
                    final TextView TitreFiche = (TextView) findViewById(R.id.txt_title);
                    TitreFiche.post(new Runnable() {
                        public void run() {
                            TitreFiche.setText("Fiche Frais du : "+DateFiche);
                        }
                    });



                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * S'exécute après la fonction doInBackground
         * **/
        protected void onPostExecute(String file_url) {
            // Ferme la fenêtre de dialogue
            pDialog.dismiss();

            // S'exécute dans un thread en arrière-plan
            runOnUiThread(new Runnable() {
                public void run() {

                    //Ajoute à la ListView "list_Forfait" les données des lignes forfaits
                    ListAdapter adapter = new SimpleAdapter(
                            FicheDetaille.this, productLesLigneF,
                            R.layout.frais_forfais_type, new String[] { TAG_date,
                            TAG_type,TAG_quantite,TAG_montant,TAG_valide},
                            new int[] { R.id.txt_fdate, R.id.txt_type,R.id.txt_quantite,R.id.txt_montant,R.id.txt_valide });
                    // Mise à jour de ListView
                    ListView lv = (ListView)findViewById(R.id.List_Forfait);
                    lv.setAdapter(adapter);

                    //Ajoute à la ListView "List_Hor_Forfait" les données des lignes hors forfaits
                    ListAdapter adapter2 = new SimpleAdapter(
                            FicheDetaille.this, productLesLigneHF,
                            R.layout.frais_hor_forfais_type, new String[] { TAG_date,
                            TAG_libelle,TAG_montant,TAG_valide},
                            new int[] { R.id.txt_hdate, R.id.txt_libelle,R.id.txt_montant,R.id.txt_valide });
                    // Mise à jour de ListView
                    ListView lv2 = (ListView)findViewById(R.id.List_Hor_Forfait);
                    lv2.setAdapter(adapter2);



                }
            });
        }
    }
}
