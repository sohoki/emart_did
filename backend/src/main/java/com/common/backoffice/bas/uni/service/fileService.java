package com.common.backoffice.bas.uni.service;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class fileService {

	public String fileSize(File f){
		  return f.exists() == true ?  Long.toString(f.length()) : "0"; 			
	}
	
	public Map getImageSize (File _fileNm){
		Map<String, String> map = new HashMap<String, String>();
		
		Metadata metadata = null;
		try {
			metadata = ImageMetadataReader.readMetadata(_fileNm);
		} catch (ImageProcessingException e) {
			// TODO Auto-generated catch block
			log.error("ImageProcessingException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("IOException");
		}
		if (metadata == null) return map;
		for (com.drew.metadata.Directory directory : metadata.getDirectories()) {
			for (com.drew.metadata.Tag tag : directory.getTags()) {
				if (tag.getTagName().equals("Image Width") ){
					map.put("fileWidth", tag.getDescription());
					
				}
				if (tag.getTagName().equals("Image Height") ){
					map.put("fileHeight", tag.getDescription());
				}
				if (tag.getTagName().equals("File Size") ){
					map.put("fileSize", tag.getDescription().replace(" bytes", ""));
				}
			}
		}
		return map;   
	}
	
	public  String uploadFileNm(List<MultipartFile> mpf, String filePath){
		
		String fileNm = "";
		String ext = "";
		
		try {
			for (MultipartFile mFile : mpf) {
				
				String originalFilename = mFile.getOriginalFilename(); //파일명	   
				if (!originalFilename.equals("")){
					/*24.10.05 보안취약점 위험한 형식파일 업로드 조치*/
					if(originalFilename.endsWith(".gif") || originalFilename.endsWith(".jpg") || originalFilename.endsWith(".jpeg") || originalFilename.endsWith(".png") || originalFilename.endsWith(".xls") || originalFilename.endsWith(".xlsx")
							|| originalFilename.endsWith(".mp4") || originalFilename.endsWith(".mp3") || originalFilename.endsWith(".avi") || originalFilename.endsWith(".mpeg")) {
		
						String fileFullPath = filePath + "/" + originalFilename;
						File file_s =  new File(fileFullPath);	
						int dot = originalFilename.lastIndexOf(".");
						if (dot != -1) {                              //확장자가 없을때	     	    
							ext = originalFilename.substring(dot+1);
						} else {                                     //확장자가 있을때	     	    
							ext = "";
						}		     	    
						//fileNm = UUID.randomUUID().toString().replace("-", "") +"."+ ext;	     	  
						file_s = rename(file_s, originalFilename, filePath);
						mFile.transferTo(file_s);
						fileNm = file_s.getName();	
	            	}
				}
			}
		} catch (IllegalStateException e) {
			log.error("uploadFileNm IllegalStateException" );
		} catch (IOException e) {
			log.error("uploadFileNm IOException");
		} catch (Exception ex){			
			log.error("uploadFileNm Exception");
		}
		return fileNm;
	}
	public File rename(File f, String fileNm, String filedir) {             //File f는 원본 파일
			 
		
		String name = f.getName();
		String body = null;
		String ext = null;
		
		
		int dot = name.lastIndexOf(".");
		
		if (dot != -1) { //확장자 있을떄 
			body  = fileNm.substring(0, fileNm.lastIndexOf(".")) ;
			ext = fileNm.substring(fileNm.lastIndexOf("."), fileNm.length());
		} else {  //확장자가 없을떄      
			body = fileNm;
			ext = "";
		}	
		int count = 0;
		//중복된 파일이 있을때
		//파일이름뒤에 a숫자.확장자 이렇게 들어가게 되는데 숫자는 9999까지 된다.
		
		if ( fileExites(body +ext, filedir) && count == 0   ) {
			  String newName = body + ext;
			  f = new File(f.getParent(), newName);
			  log.debug("파일이 없을때  filenm:"+ newName);
		}else {	    
			while (!createNewFile(f) && count < 9999) {  
				count++;
				String newName = body+"[" + count +"]" + ext;
				log.debug("파일이 있을때  filenm:"+ newName);
				f = new File(f.getParent(), newName);
			}
		}
		return f;
	}
	private boolean createNewFile(File f) { 
		try {
			return f.createNewFile();                        //존재하는 파일이 아니면
		}catch (IOException ignored) {
			return false;
		}
	}	
	private boolean fileExites( String fileNm, String path){
		try{
			pathExist(path);
			
			File f = new File (path+"/"+fileNm);
			if (f.exists()){
				return false;
			}else {
				return true;
			}
		}catch (NullPointerException e){
			log.debug("fileExites Error:" + e.toString());
			return false;
		}catch (Exception e){
			log.debug("fileExites Error:" + e.toString());
			return false;
		}
			
	}
	public boolean pathExist(String path){
		try{
			File dir = new File(path);
			if(!dir.isDirectory())
				dir.mkdir(); //폴더 생성합니다.
			return true; 
		}catch (NullPointerException e){
			log.debug("pathExist Error:" + e.toString());
			return false;
		}catch(Exception e){
			log.error("pathExist Error:" + e.toString());
			return false;
		}
	}
	//파일 삭제 
	public  boolean deleteFile (String fileNm ) {
		try{        	
			File delFile = new File ( fileNm);
			delFile.delete();
			return true;    		
		}catch(NullPointerException e){
			log.debug("file Delete error{0}", e.toString());
			return false;
		} catch(Exception e){
			log.debug("file Delete error{0}", e.toString());
			return false;
		}
	}
	public  boolean deleteFile (String fileNm, String path ) {
		try{
			pathExist(path);
			//path 먼저 확인 
			File delFile = new File ( fileNm);
			delFile.delete();
			return true;    		
		}catch(NullPointerException e){
			log.debug("file Delete error{0}", e.toString());
			return false;
		} catch(Exception e){
			log.debug("file Delete error{0}", e.toString());
			return false;
		}    	
	}
}
