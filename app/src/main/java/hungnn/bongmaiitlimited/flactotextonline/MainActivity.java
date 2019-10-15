package hungnn.bongmaiitlimited.flactotextonline;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    Uri backupUri=null;
    private final String CLOUD_API_KEY = "AIzaSyB56gDbL2NL_Kz9gIWymU6dZKSkFmyDONI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=findViewById(R.id.textView);
        imageView=findViewById(R.id.imageView);

        Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
        filePicker.setType("audio/flac");
        startActivityForResult(filePicker, 1);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, backupUri.toString(), Toast.LENGTH_SHORT).show();

                MediaPlayer player = new MediaPlayer();
                try {
                    player.setDataSource(MainActivity.this, backupUri);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                player.setOnCompletionListener(
                        new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.release();
                                Toast.makeText(MainActivity.this, "complete play audio", Toast.LENGTH_SHORT).show();

                            }
                        });


                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream stream = null;
                            stream = getContentResolver().openInputStream(backupUri);
                            byte[] audioData = IOUtils.toByteArray(stream);
                            stream.close();
                            String base64EncodedData =
                                    Base64.encodeBase64String(audioData);


                            Speech speechService = new Speech.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new AndroidJsonFactory(),
                                    null
                            ).setSpeechRequestInitializer(
                                    new SpeechRequestInitializer(CLOUD_API_KEY))
                                    .build();

                            RecognitionConfig recognitionConfig = new RecognitionConfig();
                            recognitionConfig.setLanguageCode("en-US");

                            RecognitionAudio recognitionAudio = new RecognitionAudio();
                            recognitionAudio.setContent(base64EncodedData);

                            // Create request
                            SyncRecognizeRequest request = new SyncRecognizeRequest();
                            request.setConfig(recognitionConfig);
                            request.setAudio(recognitionAudio);

// Generate response
                            SyncRecognizeResponse response = speechService.speech()
                                    .syncrecognize(request)
                                    .execute();

// Extract transcript
                            SpeechRecognitionResult result = response.getResults().get(0);
                            final String transcript = result.getAlternatives().get(0)
                                    .getTranscript();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.setText(backupUri.toString());
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // More code here
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            final Uri soundUri = data.getData();
            backupUri = soundUri;
        }
        else
            Toast.makeText(MainActivity.this, "FAIL", Toast.LENGTH_LONG).show();

    }
}
