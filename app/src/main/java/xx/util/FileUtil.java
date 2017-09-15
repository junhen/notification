package xx.util;

/**
 * Created by xiaoxin on 17-8-2.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Create By Anthony on 2016/1/15
 * Class Note:文件工具类
 * 包含内容：
 * 1 读取raw文件、file文件，drawable文件，asset文件，比如本地的json数据，本地文本等； 如：String result =FileUtil.getString(context,"raw://first.json")
 * 2 读取本地的property文件，并转化为hashMap类型的数据（{@link #simpleProperty2HashMap}）；
 * 3 将raw文件拷贝到指定目录（{@link #copyRawFile}）；
 * 4 基本文件读写操作（{@link #readFile}，{@link #writeFile}）；
 * 5 从文件的完整路径名（路径+文件名）中提取 路径（extractFilePath）；
 * 6 从文件的完整路径名（路径+文件名）中提取文件名(包含扩展名) 如：d:\path\file.ext --> file.ext（extractFileName）
 * 7 检查指定文件的路径是否存在（isPathExists）
 * 8 检查制定文件是否存在（isFileExists）
 * 9 创建目录（makeDir）
 * 10 移除字符串中的BOM前缀（removeBomHeaderIfExists）
 */
public class FileUtil {
    public static final String ASSETS_PREFIX = "file://android_assets/";
    public static final String ASSETS_PREFIX2 = "file://android_asset/";
    public static final String ASSETS_PREFIX3 = "assets://";
    public static final String ASSETS_PREFIX4 = "asset://";
    public static final String RAW_PREFIX = "file://android_raw/";
    public static final String RAW_PREFIX2 = "raw://";
    public static final String FILE_PREFIX = "file://";
    public static final String DRAWABLE_PREFIX = "drawable://";

    //获得输入流
    public static InputStream getStream(Context context, String url) throws IOException {
        String lowerUrl = url.toLowerCase();
        InputStream is;
        //asset
        if (lowerUrl.startsWith(ASSETS_PREFIX)) {
            String assetPath = url.substring(ASSETS_PREFIX.length());
            is = getAssetsStream(context, assetPath);
        } else if (lowerUrl.startsWith(ASSETS_PREFIX2)) {
            String assetPath = url.substring(ASSETS_PREFIX2.length());
            is = getAssetsStream(context, assetPath);
        } else if (lowerUrl.startsWith(ASSETS_PREFIX3)) {
            String assetPath = url.substring(ASSETS_PREFIX3.length());
            is = getAssetsStream(context, assetPath);
        } else if (lowerUrl.startsWith(ASSETS_PREFIX4)) {
            String assetPath = url.substring(ASSETS_PREFIX4.length());
            is = getAssetsStream(context, assetPath);
            //raw
        } else if (lowerUrl.startsWith(RAW_PREFIX)) {
            String rawName = url.substring(RAW_PREFIX.length());
            is = getRawStream(context, rawName);
        } else if (lowerUrl.startsWith(RAW_PREFIX2)) {
            String rawName = url.substring(RAW_PREFIX2.length());
            is = getRawStream(context, rawName);
            //file
        } else if (lowerUrl.startsWith(FILE_PREFIX)) {
            String filePath = url.substring(FILE_PREFIX.length());
            is = getFileStream(filePath);
            //drawable
        } else if (lowerUrl.startsWith(DRAWABLE_PREFIX)) {
            String drawableName = url.substring(DRAWABLE_PREFIX.length());
            is = getDrawableStream(context, drawableName);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported url: %s \n" +
                    "Supported: \n%sxxx\n%sxxx\n%sxxx", url, ASSETS_PREFIX, RAW_PREFIX, FILE_PREFIX));
        }
        return is;
    }

    private static InputStream getAssetsStream(Context context, String path) throws IOException {
        return context.getAssets().open(path);
    }

    private static InputStream getFileStream(String path) throws IOException {
        return new FileInputStream(path);
    }

    private static InputStream getRawStream(Context context, String rawName) throws IOException {
        int id = context.getResources().getIdentifier(rawName, "raw", context.getPackageName());
        if (id != 0) {
            try {
                return context.getResources().openRawResource(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new IOException(String.format("raw of id: %s from %s not found", id, rawName));
    }

    private static InputStream getDrawableStream(Context context, String rawName) throws IOException {
        int id = context.getResources().getIdentifier(rawName, "drawable", context.getPackageName());
        if (id != 0) {
            BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(id);
            Bitmap bitmap = drawable.getBitmap();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
            return new ByteArrayInputStream(os.toByteArray());
        }

        throw new IOException(String.format("bitmap of id: %s from %s not found", id, rawName));
    }

    public static String getString(Context context, String url) throws IOException {
        return getString(context, url, "UTF-8");
    }

    public static String getString(Context context, String url, String encoding) throws IOException {
        String result = readStreamString(getStream(context, url), encoding);
        if (result.startsWith("\ufeff")) {
            result = result.substring(1);
        }

        return result;
    }

    public static String readStreamString(InputStream is, String encoding) throws IOException {
        return new String(readStream(is), encoding);
    }

    public static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024 * 10];
        int readlen;
        while ((readlen = is.read(buf)) >= 0) {
            baos.write(buf, 0, readlen);
        }
        baos.close();

        return baos.toByteArray();
    }

    public static Bitmap getDrawableBitmap(Context context, String rawName) {
        int id = context.getResources().getIdentifier(rawName, "drawable", context.getPackageName());
        if (id != 0) {
            BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(id);
            if (drawable != null) {
                return drawable.getBitmap();
            }
        }

        return null;
    }

    /**
     * 读取Property文件
     */
    public static HashMap<String, String> simpleProperty2HashMap(Context context, String path) {
        try {
            InputStream is = getStream(context, path);
            return simpleProperty2HashMap(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HashMap<String, String>();
    }

    private static HashMap<String, String> simpleProperty2HashMap(InputStream in) throws IOException {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        Properties properties = new Properties();
        properties.load(in);
        in.close();
        Set keyValue = properties.keySet();
        for (Iterator it = keyValue.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            hashMap.put(key, (String) properties.get(key));
        }
        return hashMap;
    }

    /**
     * 将raw文件拷贝到指定目录
     */
    public static void copyRawFile(Context ctx, String rawFileName, String toPath) {
        String[] names = rawFileName.split("\\.");
        String toFile = toPath + "/" + names[0] + "." + names[1];
        File file = new File(toFile);
        if (file.exists()) {
            return;
        }
        try {
            InputStream is = getStream(ctx, "raw://" + names[0]);
            OutputStream os = new FileOutputStream(toFile);
            int byteCount = 0;
            byte[] bytes = new byte[1024];
            
            while ((byteCount = is.read(bytes)) != -1) {
                os.write(bytes, 0, byteCount);
            }
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void getAllFile(File dir, List<String> list) {
        File[] file = dir.listFiles();
        list.clear();
        directionList.clear();
        fileList.clear();
        for (int i = 0; i < file.length; i++) {
            if(file[i].isDirectory())
                directionList.add(file[i].getAbsolutePath());
            else if (file[i].isFile())
                fileList.add(file[i].getAbsolutePath());
        }
        if (!directionList.isEmpty()) {
            Collections.sort(directionList);
            list.addAll(directionList);
        }
        if (!fileList.isEmpty()) {
            Collections.sort(fileList);
            list.addAll(fileList);
        }
    }

    static private List directionList = new ArrayList();
    static private List fileList = new ArrayList();

    /**
     * 基本文件操作
     */
    public static String FILE_READING_ENCODING = "UTF-8";
    public static String FILE_WRITING_ENCODING = "UTF-8";

    public static String readFile(String fileName, String fileEncoding) throws Exception {
        StringBuffer buffContent = null;
        String sLine;

        FileInputStream fis = null;
        BufferedReader buffReader = null;
        if (fileEncoding == null || "".equals(fileEncoding)) {
            fileEncoding = FILE_READING_ENCODING;
        }

        try {
            fis = new FileInputStream(fileName);
            buffReader = new BufferedReader(new InputStreamReader(fis,
                    fileEncoding));
            boolean zFirstLine = "UTF-8".equalsIgnoreCase(fileEncoding);
            while ((sLine = buffReader.readLine()) != null) {
                if (buffContent == null) {
                    buffContent = new StringBuffer();
                } else {
                    buffContent.append("\n");
                }
                if (zFirstLine) {
                    sLine = removeBomHeaderIfExists(sLine);
                    zFirstLine = false;
                }
                buffContent.append(sLine);
            }// end while
            return (buffContent == null ? "" : buffContent.toString());
        } catch (FileNotFoundException ex) {
            throw new Exception("要读取的文件没有找到!", ex);
        } catch (IOException ex) {
            throw new Exception("读取文件时错误!", ex);
        } finally {
            // 增加异常时资源的释放
            try {
                if (buffReader != null)
                    buffReader.close();
                if (fis != null)
                    fis.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static File writeFile(InputStream is, String path, boolean isOverride) throws Exception {
        String sPath = extractFilePath(path);
        if (!isPathExists(sPath)) {
            makeDir(sPath, true);
        }

        if (!isOverride && isFileExists(path)) {
            if (path.contains(".")) {
                String suffix = path.substring(path.lastIndexOf("."));
                String pre = path.substring(0, path.lastIndexOf("."));
                path = pre + "_" + DateTimeUtil.getNowTime() + suffix;
            } else {
                path = path + "_" + DateTimeUtil.getNowTime();
            }
        }

        FileOutputStream os = null;
        File file = null;

        try {
            file = new File(path);
            os = new FileOutputStream(file);
            int byteCount = 0;
            byte[] bytes = new byte[1024];

            while ((byteCount = is.read(bytes)) != -1) {
                os.write(bytes, 0, byteCount);
            }
            os.flush();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("写文件错误", e);
        } finally {
            try {
                if (os != null)
                    os.close();
                if (is != null)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static File writeFile(String path, String content, String encoding, boolean isOverride) throws Exception {
        if (TextUtils.isEmpty(encoding)) {
            encoding = FILE_WRITING_ENCODING;
        }
        InputStream is = new ByteArrayInputStream(content.getBytes(encoding));
        return writeFile(is, path, isOverride);
    }

    /**
     * 从文件的完整路径名（路径+文件名）中提取 路径（包括：Drive+Directroy )
     *
     * @param filePathName
     * @return
     */
    public static String extractFilePath(String filePathName) {
        int nPos = filePathName.lastIndexOf('/');
        if (nPos < 0) {
            nPos = filePathName.lastIndexOf('\\');
        }

        return (nPos >= 0 ? filePathName.substring(0, nPos + 1) : "");
    }

    /**
     * 从文件的完整路径名（路径+文件名）中提取文件名(包含扩展名) <br>
     * 如：d:\path\file.ext --> file.ext
     *
     * @param filePathName
     * @return
     */
    public static String extractFileName(String filePathName) {
        return extractFileName(filePathName, File.separator);
    }

    /**
     * 从文件的完整路径名（路径+文件名）中提取文件名(包含扩展名)
     * 如：d:\path\file.ext --> file.ext
     *
     * @param filePathName  全文件路径名
     * @param fileSeparator 文件分隔符
     * @return
     */
    public static String extractFileName(String filePathName, String fileSeparator) {
        int nPos = -1;
        if (fileSeparator == null) {
            nPos = filePathName.lastIndexOf(File.separatorChar);
            if (nPos < 0) {
                nPos = filePathName.lastIndexOf(File.separatorChar == '/' ? '\\' : '/');
            }
        } else {
            nPos = filePathName.lastIndexOf(fileSeparator);
        }

        if (nPos < 0) {
            return filePathName;
        }

        return filePathName.substring(nPos + 1);
    }

    /**
     * 检查指定文件的路径是否存在
     *
     * @param _sPathFileName 文件名称(含路径）
     * @return 若存在，则返回true；否则，返回false
     */
    public static boolean isPathExists(String _sPathFileName) {
        String sPath = extractFilePath(_sPathFileName);
        return isFileExists(sPath);
    }

    public static boolean isFileExists(String _sPathFileName) {
        File file = new File(_sPathFileName);
        return file.exists();
    }

    /**
     * 创建目录
     *
     * @param dir             目录名称
     * @param needCreateParentDir 如果父目录不存在，是否创建父目录
     * @return
     */
    public static boolean makeDir(String dir, boolean needCreateParentDir) {
        boolean zResult = false;
        File file = new File(dir);
        if (needCreateParentDir)
            zResult = file.mkdirs(); // 如果父目录不存在，则创建所有必需的父目录
        else
            zResult = file.mkdir(); // 如果父目录不存在，不做处理
        if (!zResult)
            zResult = file.exists();
        return zResult;
    }

    /**
     * 移除字符串中的BOM前缀
     *
     * @param line 需要处理的字符串
     * @return 移除BOM后的字符串.
     */
    private static String removeBomHeaderIfExists(String line) {
        if (line == null) {
            return null;
        }
        if (line.length() > 0) {
            char ch = line.charAt(0);
            // 使用while是因为用一些工具看到过某些文件前几个字节都是0xfffe.
            // 0xfeff,0xfffe是字节序的不同处理.JVM中,一般是0xfeff
            while (ch == 0xfeff || ch == 0xfffe) {
                line = line.substring(1);
                if (line.length() == 0) {
                    break;
                }
                ch = line.charAt(0);
            }
        }
        return line;
    }

}
