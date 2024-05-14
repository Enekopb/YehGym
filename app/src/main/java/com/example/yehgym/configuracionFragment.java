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
import android.graphics.Color;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;
import java.util.Locale;
public class configuracionFragment extends Fragment {

        private static final String PREFERENCIAS_IDIOMA = "config_idioma";
        private static final String PREFERENCIAS_TEMA = "config_tema";
        private static final String IDIOMA_PREF_KEY = "idioma";
        private static final String TEMA_PREF_KEY = "tema";
        private static final int REQUEST_CAMERA_PERMISSION = 10;
        private static final int REQUEST_IMAGE_CAPTURE = 1;

        private Button btnCamara;
        private Bitmap imgBitmap;
        private final String URL = "URLUnai";
        private ImageView imgView;
        private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
                    Bundle bundle = result.getData().getExtras();
                    imgBitmap = (Bitmap) bundle.get("data");
                    Log.d("bitmap", getStringImagen(imgBitmap));
                    imgView.setImageBitmap(imgBitmap);
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
            MenuInicio activity = (MenuInicio) requireActivity();

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
            // TODO: startActivity(new Intent(MainActivity.this, Login.class));
            Intent i = new Intent(requireActivity(), MenuInicio.class);
            startActivity(i);
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
}
