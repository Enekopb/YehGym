package com.example.yehgym;

import android.Manifest;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
public class configuracionFragment extends Fragment {

    private static final String PREFERENCIAS_IDIOMA = "config_idioma";
    private static final String PREFERENCIAS_TEMA = "config_tema";
    private static final String IDIOMA_PREF_KEY = "idioma";
    private static final String TEMA_PREF_KEY = "tema";
    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String user;

    private Button btnCamara;
    private Bitmap imgBitmap;
    private final String URLVerificacion = "http://146.148.62.83:81/verificacionImagen.php";
    private final String URLSubir = "http://146.148.62.83:81/subirImagen.php";
    private ImageView imgView;
    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
                    Bundle bundle = result.getData().getExtras();
                    imgBitmap = (Bitmap) bundle.get("data");
                    Log.d("bitmap", getStringImagen(imgBitmap));
                    imgView.setImageBitmap(imgBitmap);
                    subirFotoAlServidor(imgBitmap);
                } else {
                    Log.d("TakenPicture", "No photo taken");
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.configuracionfragment, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFERENCIAS_TEMA, MODE_PRIVATE);
        String temaGuardado = sharedPreferences.getString(TEMA_PREF_KEY, "DEFAULT");

        // Aplicar el tema correspondiente
        if (temaGuardado.equals("DEFAULT")) {
            requireActivity().setTheme(R.style.AppThemeLight);
            view.setBackgroundColor(Color.WHITE);
        } else {
            requireActivity().setTheme(R.style.AppThemeDark);
            view.setBackgroundColor(Color.BLACK);
        }

        sharedPreferences = requireActivity().getSharedPreferences("config_idioma", MODE_PRIVATE);
        String idiomaGuardado = sharedPreferences.getString("idioma", "values");
        Locale nuevoLocale = new Locale(idiomaGuardado);
        Locale.setDefault(nuevoLocale);
        Configuration configuration = requireActivity().getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevoLocale);
        requireActivity().getResources().updateConfiguration(configuration, requireActivity().getBaseContext().getResources().getDisplayMetrics());

        // Cargar la foto
        user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Log.d("usuario firebase", user);
        new VerificarImagenTask().execute(URLVerificacion, user, "es");

        imgView = view.findViewById(R.id.imageView);
        Button buttonCamera = view.findViewById(R.id.btnCamara);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button1 = view.findViewById(R.id.english);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button2 = view.findViewById(R.id.castellano);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button3 = view.findViewById(R.id.claro);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button4 = view.findViewById(R.id.Oscuro);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button5 = view.findViewById(R.id.logout);

        // Listener para el botón en inglés
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarIdioma("en");
            }
        });

        // Listener para el botón en castellano
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarIdioma("values");
            }
        });

        // Listener para el botón en castellano
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { updateTema("DEFAULT", container);
            }
        });

        // Listener para el botón en castellano
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTema("DARK", container);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salir();
            }
        });
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    // Si los permisos ya están concedidos, abrir la cámara
                    abrirCamara();
                }
            }
        });

        return view;
    }

    private void cambiarIdioma(String languageCode) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFERENCIAS_IDIOMA, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IDIOMA_PREF_KEY, languageCode);
        editor.apply();

        Locale nuevoLocale = new Locale(languageCode);
        Locale.setDefault(nuevoLocale);
        Configuration configuration = requireActivity().getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevoLocale);
        requireActivity().getResources().updateConfiguration(configuration, requireActivity().getBaseContext().getResources().getDisplayMetrics());
        requireActivity().recreate();
    }

    public void updateTema(String key, View view) {
        Log.d("Eneko", key);
        SharedPreferences sp = requireActivity().getSharedPreferences(PREFERENCIAS_TEMA, MODE_PRIVATE);
        SharedPreferences.Editor objEditor = sp.edit();
        objEditor.putString(TEMA_PREF_KEY, key);
        objEditor.apply();
        Log.d("Tema", sp.getString("tema", "DEFAULT"));
        if (key.equals("DEFAULT")) {
            // Se guarda al hacer el siguiente onCreate de la nueva actividad, no se aplica seguido.
            requireActivity().setTheme(R.style.AppThemeLight);
            view.setBackgroundColor(Color.WHITE);
        } else {
            requireActivity().setTheme(R.style.AppThemeDark);
            view.setBackgroundColor(Color.BLACK);
        }
        requireActivity().recreate();
    }

    private void salir(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(requireActivity(), Login.class));
        requireActivity().finish();
    }

    // Método para abrir la cámara
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            // Verificar si los permisos fueron concedidos
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si los permisos de la cámara fueron concedidos, abrir la cámara
                abrirCamara();
            } else {
                // Si los permisos de la cámara fueron denegados, mostrar un mensaje al usuario
                Toast.makeText(requireContext(), "Permiso de la cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getStringImagen(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void subirFotoAlServidor(Bitmap bitmap) {
        // Convertir la imagen a Base64
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (esFormatoPNG(bitmap)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }
        byte[] fototransformada = stream.toByteArray();
        String fotoen64 = Base64.encodeToString(fototransformada, Base64.DEFAULT);
        // Crear una solicitud HTTP POST para enviar la imagen al servidor
        new EnviarImagenTask().execute(URLSubir, user, fotoen64, "es");
    }

    // Método para verificar si la imagen es formato PNG
    private boolean esFormatoPNG(Bitmap bitmap) {
        // Convertir el bitmap a bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Verificar la firma del archivo para determinar el formato
        // La firma de un archivo PNG comienza con los bytes: 89 50 4E 47 0D 0A 1A 0A
        if (byteArray.length >= 8 &&
                byteArray[0] == (byte) 0x89 &&
                byteArray[1] == (byte) 0x50 &&
                byteArray[2] == (byte) 0x4E &&
                byteArray[3] == (byte) 0x47 &&
                byteArray[4] == (byte) 0x0D &&
                byteArray[5] == (byte) 0x0A &&
                byteArray[6] == (byte) 0x1A &&
                byteArray[7] == (byte) 0x0A) {
            return true; // Es un archivo PNG
        } else {
            return false; // No es un archivo PNG
        }
    }

    // Clase AsyncTask para enviar la imagen al servidor
    private class EnviarImagenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlServidor = params[0];
            String user = params[1];
            String fotoPath = params[2];
            String languageCode = params [3];
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlServidor);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("imagen", fotoPath)
                        .appendQueryParameter("usuario", user)
                        .appendQueryParameter("lang", languageCode);
                String parametrosURL = builder.build().getEncodedQuery();

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parametrosURL.getBytes());
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                inputStream.close();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return getString(R.string.img_err);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private class VerificarImagenTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            String urlServidor = params[0];
            String usuario = params[1];
            String languageCode = params[2];
            Bitmap bitmap = null;
            try {
                // Crear la URL de la conexión
                URL url = new URL(urlServidor);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // Configurar la conexión
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                // Crear los parámetros para enviar al servidor
                String parametros = "usuario=" + usuario + "&lang=" + languageCode;
                // Escribir los datos en el flujo de salida de la conexión
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parametros.getBytes());
                outputStream.flush();
                outputStream.close();
                // Leer la respuesta del servidor
                int responseCode = connection.getResponseCode();
                Log.d("VerificarImagenTask", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                } else {
                    Log.e("VerificarImagenTask", "Error en la respuesta del servidor: " + responseCode);
                }

                // Cerrar la conexión
                connection.disconnect();
            } catch (IOException e) {
                Log.e("VerificarImagenTask", "Error al realizar la conexión: " + e.getMessage());
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imgView.setImageBitmap(bitmap);
            } else {
                //Toast.makeText(Configuracion.this, getString(R.string.dwl_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

}