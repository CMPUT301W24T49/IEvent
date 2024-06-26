package com.example.qr.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qr.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * An ArrayAdapter for displaying images in a list.
 * Manages a list of Image objects and populates a ListView with views representing each image.
 */
public class ImageArrayAdapter extends ArrayAdapter<Image> {

    private ArrayList<Image> images;
    private Context context;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs an ImageArrayAdapter.
     *
     * @param context The current context, used to inflate the layout file.
     * @param image An ArrayList of Image objects to be displayed.
     */
    public ImageArrayAdapter(Context context, ArrayList<Image> image) {
        super(context, 0, image);
        this.context = context;
        this.images = image;
    }

    /**
     * Provides a view for an AdapterView.
     *
     * @param position The position in the list of data that should be displayed in the list item view.
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_profile_list_view_info, parent, false);
        }

        Image image = images.get(position);
        TextView imageID = view.findViewById(R.id.image_list_info);
        TextView imagePic = view.findViewById(R.id.image_picture);

        imageID.setText(image.getId());
        imagePic.setText(image.getUrl());


        return view;
    }
}
