package com.example.imageclassify

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.imageclassify.ml.MobilenetV110224Quant
import com.example.imageclassify.ml.MobilenetV110224Quant.newInstance
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.net.URI

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var bitmap: Bitmap
    lateinit var imgView: ImageView
    var link:Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val filename = "labels.txt"
        val inputString = application.assets.open(filename).bufferedReader().use { it.readText() }
        var animalsnamelist = inputString.split("\n")


        var tv: TextView = findViewById(R.id.textView)

        imgView = findViewById(R.id.imageView)


        var select: Button = findViewById(R.id.button)
        select.setOnClickListener {

            opeinImageChooser()
//            Toast.makeText(this, link.toString(), Toast.LENGTH_SHORT).show()
//            var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "Image/*"

//            startActivityForResult(intent, 200)
        }


        var predict: Button = findViewById(R.id.button2)
        predict.setOnClickListener {

            var resized: Bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            val model =MobilenetV110224Quant.newInstance(this)


            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)

            var tbuffer = TensorImage.fromBitmap(resized)
            var byteBuffer = tbuffer.buffer

            inputFeature0.loadBuffer(byteBuffer)


            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            var max = getMax(outputFeature0.floatArray)
            tv.setText(animalsnamelist[max])
            model.close()
        }


    }
//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//            super.onActivityResult(requestCode, resultCode, data)
//            imgView.setImageURI(data?.data)
//
//            var uri:Uri?= data?.data
//
//            bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
//}




    fun getMax(arr:FloatArray):Int
    {
        var max=0
        var ans=0.0f
        for(i in 0..1000)
        {
            if(arr[i]>ans)
            {
                max=i;
                ans=arr[max]
            }

        }
        return max
    }


    private fun opeinImageChooser() {

        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> {
                    link = data?.data
                    bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,link)
                    imgView.setImageURI(link)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_IMAGE = 101
    }

    }
