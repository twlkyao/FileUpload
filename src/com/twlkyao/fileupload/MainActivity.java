package com.twlkyao.fileupload;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// The path of the file to upload
//    private String srcPath = "";  
    private String Tag = "MainActivity";
    
    // The server url
    private String actionUrl = "http://10.0.2.2/cloud/storage/file_operation.php";  
    private ListView fileListView; // The listview to stotre the file information
    private FileListAdapter fileListAdapter; // The self defined Adapter
    
    private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
			case 1:
				Toast.makeText(getApplicationContext(),
						R.string.upload_file_succeeded, Toast.LENGTH_SHORT).show();
				break;
			case 0:
				Toast.makeText(getApplicationContext(),
						R.string.upload_file_failed, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
    	
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initData(Environment.getExternalStorageDirectory());
        setListeners();
    }
    
    public void findViews() {
    	fileListView = (ListView) this.findViewById(R.id.file_listview); // The listview to store file information
    }
    
    public void setListeners() {
    	
    	// Set the fileListView listener
    	fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view, 
    				int position, long id) {
    			// TODO Auto-generated method stub
    			File file = (File) fileListAdapter.getItem(position);
    			if(!file.canRead()) { // If the file can't read, alert
    				Log.d(Tag, "File can't read!");
    			} else if(file.isDirectory()) { // If the clicked item is a directory, get into it
    				initData(file);
    			} else { // If the clicked item is a file, get the file information, such as md5 or sha1
	    			startUploadFile(file.getPath(), actionUrl);
    			}
    		}
    	});
    }


	/**
	 * Update the fileListView
	 * @param folder The new folder path to display
	 */
	private void initData(File folder) {
		boolean isSDcard = folder.equals(Environment.getExternalStorageDirectory()); // Judge if the folder is the SDcard
		ArrayList<File> files = new ArrayList<File>();   
		if (!isSDcard) { // If the current folder is not the SDcard
			files.add(Environment.getExternalStorageDirectory()); // Back to the SDcard
			files.add(folder.getParentFile()); // Back to parent
		}
		File[] filterFiles = folder.listFiles(); // Get the file list under the specified folder
		if (null != filterFiles && filterFiles.length > 0) {
			for (File file : filterFiles) { // Add the files to the ArrayList
				files.add(file);
			}
		}
		fileListAdapter= new FileListAdapter(getApplicationContext(), files, isSDcard); // Update the fileListAdapter
		fileListView.setAdapter(fileListAdapter); // Update the fileListView's adapter
	}

	/**
	 * Create a thread to upload the file.
	 * @param filepath
	 * @param uploadUrl
	 */
	public void startUploadFile(final String filepath, final String uploadUrl) {
		
		final Message msg = Message.obtain(); // Get the Message object
		// Create a new thread to do the upload
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean flag = uploadFile(filepath, uploadUrl); // Call the upload file function
				if(flag) {
					msg.what = 1; // Upload file succeeded.
				} else {
					msg.what = 0; // Upload file failed.
				}
				handler.sendMessage(msg);
			}
		});
		
		thread.start(); // Start the thread
			
	}
	
	/**
	 * Upload the specified file to remote server.
	 * @param filepath The path of the local file.
	 * @param uploadUrl The server url.
	 * @return The upload status.
	 */
	public boolean uploadFile(String filepath, String uploadUrl) {
		boolean status = true;
		
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try
		{
			URL url = new URL(uploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

			// Set the size of the transfer stream, in case that the application
			// collapses due to small memory, this method is used when we don't
			// know the size of the content, we use HTTP request without cache
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K  
			
			// Set the input and output
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			
			// Set the HTTP method
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			// Get outputstream according to the url connection
			DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
			
			// Write the HTTP POST header
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
					+ filepath.substring(filepath.lastIndexOf("/") + 1)
                + "\"" + end);
            dos.writeBytes(end);
        
            FileInputStream fis = new FileInputStream(filepath);
            
            int bufferSize = 8 * 1024; // The size of the buffer, 8KB.
            byte[] buffer = new byte[bufferSize];
            int length = 0;

            while ((length = fis.read(buffer)) != -1) {
            	
            	// Write data to DataOutputStream
            	dos.write(buffer, 0, length);
            }
            
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            
            fis.close(); // Close the FileInputStream.
            dos.flush(); // Flush the data to DataOutputStream.
        
            // Get the content of the response
            InputStream is = httpURLConnection.getInputStream();
            
            InputStreamReader isr = new InputStreamReader(is, "utf-8");  
            BufferedReader br = new BufferedReader(isr, 8 * 1024);  
            String result = br.readLine();
        
            Log.d(Tag, result);
            
//            dos.close(); // Will respond I/O exception if closes.
          } catch (Exception e) {
        	  e.printStackTrace();
        	  status = true;
          }
		return status;
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
