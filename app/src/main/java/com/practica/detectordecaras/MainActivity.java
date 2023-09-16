package com.practica.detectordecaras;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.practica.detectordecaras.ml.FlowerModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Text>, OnFailureListener, ImageReader.OnImageAvailableListener {
    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;
    public Bitmap mSelectedImage;
    public ImageView mImageView;
    public TextView txtResults;
    public ImageLabeler imageLabeler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResults = findViewById(R.id.txtResults);
        mImageView = findViewById(R.id.image_view);
    }
    public void abrirGaleria (View view){
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }
    public void abrirCamera (View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {
                if (requestCode == REQUEST_CAMERA)
                    mSelectedImage = (Bitmap) data.getExtras().get("data");
                else
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                mImageView.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void PersonalizedModel(View v) {
        try {
            FlowerModel model = FlowerModel.newInstance(getApplicationContext());
            TensorImage image = TensorImage.fromBitmap(mSelectedImage);

            FlowerModel.Outputs outputs = model.process(image);

            List<Category> probability = outputs.getProbabilityAsCategoryList();
            Collections.sort(probability, new CategoryComparator());

            String res="";
            for (int i = 0; i < probability.size(); i++) {
                res = res + probability.get(i).getLabel() +  " " +  probability.get(i).getScore()*100 + " % \n";
            }

            txtResults.setText(res);
            model.close();
        } catch (IOException e) {
            txtResults.setText("Error al procesar Modelo");
        }
    }
    class CategoryComparator implements java.util.Comparator<Category> {
        @Override
        public int compare(Category a, Category b) {
            return (int)(b.getScore()*100) - (int)(a.getScore()*100);
        }






    }
    @Override
    public void onFailure(@NonNull Exception e) {

    }

    @Override
    public void onSuccess(Text text) {
        txtResults.setText("Imagen Identificada con éxito");

    }

    @Override
    public void onImageAvailable(ImageReader reader) {

    }

}