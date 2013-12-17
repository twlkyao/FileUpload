package com.twlkyao.fileupload;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<File> files; // The ArrayList to store the files
    private boolean isSDcard; // To indicate whether the file is the SDcard
    private LayoutInflater mInflater; // The LayoutInflater
      
    public FileListAdapter (Context context, ArrayList<File> files, boolean isSDcard) {
        this.context = context;
        this.files = files;
        this.isSDcard = isSDcard;
        mInflater = LayoutInflater.from(context);
    }
         
    @Override
    public int getCount () {
        return files.size();
    }

    @Override
    public Object getItem (int position) {
        return files.get(position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }
         
    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        
    	ViewHolder viewHolder;
        if(convertView == null) { // The view is not initialized
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.file_list_item, null); // Inflate from the layout file
            convertView.setTag(viewHolder);
            viewHolder.file_title = (TextView) convertView.findViewById(R.id.file_title); // Find the file_title textview
            viewHolder.file_icon = (ImageView) convertView.findViewById(R.id.file_icon); // Find the file_icon imageview
            viewHolder.file_path = (TextView) convertView.findViewById(R.id.file_path); // Find the file_path textview
        } else {
              viewHolder = (ViewHolder) convertView.getTag();
        }
        
        File file = (File) getItem(position);
        if(position == 0 && !isSDcard) { // Add the back to SDcard item
	        viewHolder.file_title.setText(R.string.back_to_sdcard);
	        viewHolder.file_icon.setImageResource(R.drawable.sdcard_icon);
	        viewHolder.file_path.setText(Environment.getExternalStorageDirectory().toString());
        }
        if(position == 1 && !isSDcard) { // Add the back to parent item
	        viewHolder.file_title.setText(R.string.back_to_parent);
	        viewHolder.file_icon.setImageResource(R.drawable.folder_up_icon);
	        viewHolder.file_path.setText(file.getPath());
        } else { // The current filepath is the SDcard or the position is neither 0 nor 1
              String fileName = file.getName();
              viewHolder.file_title.setText(fileName);
              viewHolder.file_path.setText(file.getPath());
              if(file.isDirectory()) { // The variable file is a directory
                  viewHolder.file_icon.setImageResource(R.drawable.folder_icon);
              } else { // The variable file is a file
            	  viewHolder.file_icon.setImageResource(R.drawable.file_icon);
              }
          }
     
     return convertView;
}
        
    private class ViewHolder {
    	private ImageView file_icon; // The icon of the file
		private TextView file_title; // The title of the file
		private TextView file_path; // The path of the file
//		private RatingBar ratingBar; // The secret level of the file
    }
}
