package web.core;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.stream.Stream;


public class FrontProjectLoader {
    private   final Logger logger = LoggerFactory.getLogger(getClass());

    public static FrontProjectLoader jndcStaticProject;

    private final int DEEP_LIMIT = 15;

    private String rootPath;

    private Map<String, InnerFileDescription> fileDescriptionMap=new HashMap<>() ;


    private volatile  boolean reloadInterrupt=false;


    public static FrontProjectLoader loadProject(String path) {
        FrontProjectLoader frontProjectLoader = new FrontProjectLoader(path);
        frontProjectLoader.reloadProject();
        return frontProjectLoader;

    }


    public void reloadProject(){
        logger.info("load front project: "+rootPath);
        destroyOldVersion();
        recordFile(new File(rootPath), 0);
        reloadInterrupt=false;
    }

    private void destroyOldVersion(){
        reloadInterrupt=true;
        Map<String, InnerFileDescription> fileDescriptionMap = this.fileDescriptionMap;
        fileDescriptionMap.forEach((k,v)->{
            v.release();
        });
        this.fileDescriptionMap=new HashMap<>();
    }

   public InnerFileDescription findFile(String path){
        if (reloadInterrupt){
            return null;
        }
       return fileDescriptionMap.get(path);
   }

    public FrontProjectLoader(String rootPath) {
        this.rootPath = rootPath;
    }



    private void recordFile(File file, Integer recursionDepth) {
        if (recursionDepth > DEEP_LIMIT) {
            throw new RuntimeException("over limit deep");
        }

        if (!file.exists()) {
            throw new RuntimeException("file not be found");
        }

        if (file.isDirectory()) {

            int nDeep = recursionDepth++;
            File[] files = file.listFiles();
            Stream.of(files).forEach(x -> {
                recordFile(x, nDeep);
            });
        } else {
            InnerFileDescription innerFileDescription = new InnerFileDescription(file, rootPath);
            fileDescriptionMap.put(innerFileDescription.getShortPath(),innerFileDescription);
        }
    }

    public class InnerFileDescription {


        private final int LAZY_LOAD_SIZE = 5 * 1024 * 1024;//A single file is less than 5mb loaded into the memory

        private final int NOT_RECOMMEND_CACHE_SIZE = 100 * 1024 * 1024;//not recommend cache this size file in memory

        private File file;

        private String fileType;

        private String rootPath;

        private String shortPath;

        private String fullPath;

        private boolean lazyLoad;

        private boolean cacheEnable;

        private byte[] fileBytes;

        public InnerFileDescription(File file, String rootPath) {
            this.file = file;
            this.rootPath = rootPath;
            this.fullPath = file.getAbsolutePath();
            this.shortPath = this.fullPath.substring(rootPath.length()-1);
            this.fileType=file.getName().substring(file.getName().lastIndexOf(".")+1);
            loadFile();
        }


        public void release(){
            this.file=null;
            this.fileBytes=null;
        }

        private void loadFile() {
            long length = file.length();
            if (length >= LAZY_LOAD_SIZE) {
                lazyLoad = true;
                if (length > NOT_RECOMMEND_CACHE_SIZE) {
                    cacheEnable = false;
                } else {
                    cacheEnable = true;
                }
            } else {
                try {
                    fileBytes = FileUtils.readFileToByteArray(file);
                } catch (IOException e) {
                    throw new RuntimeException("load file error:" + e);
                }
                lazyLoad = false;
            }

        }

        /**
         *
         *
         * @return
         */
        public byte[] getData() {
            if (fileBytes != null) {
                return fileBytes;
            }
            try {
                byte[] bytes = FileUtils.readFileToByteArray(file);
                if (cacheEnable) {
                    fileBytes = bytes;
                }
                return bytes;
            } catch (IOException e) {
                throw new RuntimeException("load file error:" + e);
            }
        }

        public String getFileType() {
            return fileType;
        }

        public String getShortPath() {
            return shortPath;
        }
    }
}
