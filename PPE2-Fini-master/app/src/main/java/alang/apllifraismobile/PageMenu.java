package alang.apllifraismobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.TwoStatePreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class PageMenu extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    //Champ ou l'utilisateur rentre son login et son Mot de passe
    EditText Txt_Username;
    EditText Txt_Password;

    //Bouton Connexion
    Button Login;

    //Boolean true si la connexion est reussite
    boolean success;

    // ID de l'utilisateur en cas de connexion réussite
    public static int ID;

    // Creation de l'objet JSON Parser
    JSONParser jParser = new JSONParser();

    // url du controller Symfony
    private static String url_all_products = "http://94.23.221.178/Applifrais/web/app.php/api/connect";

    // Non des variable retourner par le JSON
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID = "message";


    //Fonction executé a la creation de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Appelle de l'activité et modification de son titre
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_menu);
        setTitle("Connexion");

        //Declaration Button Login :
        Login = (Button) findViewById(R.id.Btn_Login);
        //Fin Declaration Login----------------------------------

        //Declaration editText UserName :
        Txt_Username = (EditText) findViewById(R.id.Txt_Username);
        Txt_Username.setTextColor(Color.rgb(90,90,90));
        Txt_Username.setText("UserName");
        //Fin Declaration UserName -----------------------------------

        //Declaration editText Password :
        Txt_Password = (EditText) findViewById(R.id.Txt_Password);
        Txt_Password.setTextColor(Color.rgb(90,90,90));
        Txt_Password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        Txt_Password.setText("Password");
        //Fin Declaration Password -----------------------------------

        //Detection FOCUS Txt_Field Password :
        Txt_Password.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v,boolean hasFocus){
                if (hasFocus){
                    //Suppression de ce qui est écrit a l'intérieure pour que l'utilisateur puisse écrire
                    if (Txt_Password.getText().toString().equals("Password")){
                        Txt_Password.setText("");
                        Txt_Password.setTextColor(Color.BLACK);
                        Txt_Password.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }else{
                    //Si rien n'a été ecrit on renote "Password"
                    if (Txt_Password.getText().toString().trim().length() == 0){
                        Txt_Password.setTextColor(Color.rgb(90,90,90));
                        Txt_Password.setInputType(InputType.TYPE_CLASS_TEXT);
                        Txt_Password.setText("Password");
                    }
                }
            }
        });
        //---------------Fin Detection Focus Password------------------------------

        //Detection FOCUS Txt_Field USername :
        Txt_Username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    //Suppression de ce qui est écrit a l'intérieure pour que l'utilisateur puisse écrire
                    if (Txt_Username.getText().toString().equals("UserName")){
                        Txt_Username.setTextColor(Color.BLACK);
                        Txt_Username.setText("");
                    }
                }else{
                    //Si rien n'a été écrit on renote "UserName"
                    if (Txt_Username.getText().toString().trim().length() == 0){
                        Txt_Username.setTextColor(Color.rgb(90,90,90));
                        Txt_Username.setText("UserName");
                    }
                }
            }
        } );
        //---------------Fin Detection Focus UserName------------------------------

        //Détection du clic sur le bouton "Login"
        Login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //On lance la fonction connect qui s'exécute en arrière-plan
                new  Connect().execute(Txt_Username.getText().toString().trim(),Txt_Password.getText().toString().trim());

            }
        });


    }

    /**
     * Fonction qui s'exécute en arrière-plan
     * */
    class Connect extends AsyncTask<String, String, String> {

        //Variable qui contient le message retourner par le contoller
        String message;

        //Fonction qui démarre au début pour montrer à l'utilisateur ou en est la connexion
        @Override
        protected void onPreExecute() {
            //Ouverture d'une fenêtre de dialogue
            super.onPreExecute();
            pDialog = new ProgressDialog(PageMenu.this);
            pDialog.setMessage("Connexion en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Connexion via l'url
         */
        protected String doInBackground(String... args) {
            //Récupération des variables passé en paramètre (le nom de l'utilisateur et le mot de passe)
            String username = args[0];
            String password = args[1];

            //on stock ces valeurs dans une Hashmap
            HashMap<String, String> params = new HashMap<>();
            params.put("Username", username);
            params.put("password", password);

            //On envoie a JSONParser l'url, la méthode avec laquelle on veut communiquer (GET ou POST) et la HashMap qui contient les
            //Paramètres
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

            //On récupère les informations retourner par le controller. La valeur succès (true si connection reussie)
            // et la valeur de message
            try {
                // Récupération de success
                success = json.getBoolean(TAG_SUCCESS);
                if  (success == true ){
                    //Success est a true on récupére donc l'ID de l'utilisateur
                    ID = json.getInt(TAG_ID);
                }else{
                    //Succes est à false on récupère donc le message d'erreur
                    message = json.getString(TAG_ID);
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
                    //Si la connexion est réussite on passe à l'activité suivante
                    if (success == true){
                        //Toast.makeText(getApplicationContext(),"id = " + ID, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PageMenu.this,ListeFiche.class);
                        startActivity(intent);
                    }else{
                        //Si non on affiche l'erreur dans un Toast
                        Toast.makeText(getApplicationContext(), "Erreur: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}
