package com.example.yehgym;

import static android.content.Context.MODE_PRIVATE;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

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


            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button1 = view.findViewById(R.id.english);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button2 = view.findViewById(R.id.castellano);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button3 = view.findViewById(R.id.claro);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button4 = view.findViewById(R.id.Oscuro);

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
    }
