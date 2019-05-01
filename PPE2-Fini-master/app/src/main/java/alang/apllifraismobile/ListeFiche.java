package alang.apllifraismobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ListeFiche extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creation de l'objet JSON Parser
    JSONParser jParser = new JSONParser();

    // url du controller Symfony
    private static String url_all_products = "http://94.23.221.178/Applifrais/web/app.php/api/listefiche";


    //Liste qui contient les fiches frais
    ArrayList<HashMap<String,String>> productLesFiche = new ArrayList<HashMap<String,String>>();

    //Boolean true si les données on bien été récupéré
    boolean success;

    //LisViex qui contient les informations des fiches frais (id, Date, Etat)
    private ListView lv;

    // Non des variable retourner par le JSON
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_LesFiches = "LesFiches";
    private static final String TAG_L_Date = "Date";
    private static final String TAG_L_Etat = "etat";
    private static final String TAG_L_ID = "ID";

    private static final  String TAG_A_Date = "txt_date";
    private static final String TAG_A_Etat = "txt_etat";
    private  static  final String TAG_A_ID = "txt_id";

    private  static  final  String TAG_MESSAGE = "message";

    //Fonction executé a la creation de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Appelle de l'activité et modification de son titre
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_fiche);
        setTitle("Fiche Frais");

        //On lance la fonction connect qui s'exécute en arrière-plan
        new  Connect().execute(""+PageMenu.ID);

        //On récupère la listView "List_Fiche" et on la stock dans lv
        lv = (ListView)findViewById(R.id.List_Fiche);

        //On ajoute un Listener a la fiche pour détecter les clics sur cette dernière
        lv.setOnItemClickListener(new  AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Lancement de l'activite DetailFicheFraisActivity en passant en paramètre l'id de la fiche
                Intent i = new Intent(ListeFiche.this, FicheDetaille.class);
                i.putExtra("id_fiche",productLesFiche.get(position).get(TAG_A_ID));
                startActivity(i);

            }
        });
    }

    /**
     * Fonction qui s'exécute en arrière-plan
     * */
    class Connect extends AsyncTask<String, String, String> {

        //Variable qui contient le message retourner par le contoller
        String message;


        //Fonction qui démarre au début pour montrer à l'utilisateur ou est la réception des fiches
        @Override
        protected void onPreExecute() {
            //Ouverture d'une fenêtre de dialogue
            super.onPreExecute();
            pDialog = new ProgressDialog(ListeFiche.this);
            pDialog.setMessage("Chargement des fiches en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Connexion via l'url
         */
        protected String doInBackground(String... args) {

            //Récupération des variables passé en paramètre (l'id de l'utilisateur)
            String ID = args[0];

            //on stock cette valeur dans une Hashmap
            HashMap<String, String> params = new HashMap<>();
            params.put("ID", ID);

            //On envoie a JSONParser l'url, la méthode avec laquelle on veut communiquer (GET ou POST) et la HashMap qui contient les
            //Paramètres
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);



            try {

                success = json.getBoolean(TAG_SUCCESS);
                //Si success vaut true
                if (success == true){

                    //On récuère la liste des fiches qui a été retourné
                    JSONArray LesFiche = json.getJSONArray(TAG_LesFiches);


                    //On récupère ligne par ligne toute les information des fiches que l'on stock dans une HasmpMap que l'on
                    //stock elle même dans une Liste
                    for (int i = 0; i < LesFiche.length();i++){
                        Log.d("test Boucle","bb");
                        JSONObject uneFiche = LesFiche.getJSONObject(i);

                        String Date = uneFiche.getString(TAG_L_Date);
                        String Etat = uneFiche.getString(TAG_L_Etat);
                        String id = uneFiche.getString(TAG_L_ID);

                        HashMap<String,String> map = new HashMap<String, String>();

                        map.put(TAG_A_ID,id);
                        map.put(TAG_A_Etat,Etat);
                        map.put(TAG_A_Date,Date);

                        Log.d("id ",id);
                        Log.d("Etat ",Etat);
                        Log.d("Date ",Date);
                        productLesFiche.add(map);
                    }
                }else{
                    //Si on récupère l'erreur et on la stock dans une HaspMap que l'on stock dans une Liste
                    String message = json.getString(TAG_MESSAGE);
                    HashMap<String,String> map = new HashMap<String, String>();

                    map.put(TAG_MESSAGE,message);
                    productLesFiche.add(map);
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
                    ListAdapter adapter = null;
                    if (success) {
                        //Si success est à true on Update la listView "lv" avec toute les données des fiche frais récupéré
                        adapter = new SimpleAdapter(
                                ListeFiche.this, productLesFiche,
                                R.layout.liste_type, new String[]{TAG_A_ID,
                                TAG_A_Etat, TAG_A_Date},
                                new int[]{R.id.txt_id, R.id.txt_etat, R.id.txt_date});
                    }else{
                        //Si success est a false on affiche dans la listView "lv" l'erreur qui a été retourné par le controller
                        adapter = new SimpleAdapter(
                                ListeFiche.this,productLesFiche,
                                R.layout.error_type, new String[]{TAG_MESSAGE},
                                new int[]{R.id.txt_error});
                        }

                    // Mise a jour de la listView
                    lv.setAdapter(adapter);




                }
            });
        }
    }
}



