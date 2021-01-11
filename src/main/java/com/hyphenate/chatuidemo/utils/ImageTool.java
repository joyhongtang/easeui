package com.hyphenate.chatuidemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @desc:图片处理
 */
public class ImageTool {
    private final static int MAX_NUM_PIXELS = 320 * 490;
    private final static int MAX_NUM_PIXELS_DEATAIL = 1080 * 960;
    private final static int MIN_SIDE_LENGTH = 350;

    /**
     * 
     * @Description 生成图片的压缩图
     * @param filePath
     * @return
     */
    public static Bitmap createImageThumbnail(String filePath) {
        if (null == filePath || !new File(filePath).exists())
            return null;
        Bitmap bitmap = null;
        int degree = readPictureDegree(filePath);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, MAX_NUM_PIXELS);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath, opts);
        } catch (Exception e) {
            return null;
        }
        Bitmap newBitmap = rotaingImageView(degree, bitmap);
        return newBitmap;
    }
    
    /**
     * 
     * @Description 生成图片的压缩图
     * @param filePath
     * @return
     */
    public static Bitmap createImageDetailThumbnail(String filePath) {
        if (null == filePath || !new File(filePath).exists())
            return null;
        Bitmap bitmap = null;
        int degree = readPictureDegree(filePath);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, MAX_NUM_PIXELS_DEATAIL);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath, opts);
        } catch (Exception e) {
            return null;
        }
        Bitmap newBitmap = rotaingImageView(degree, bitmap);
        return newBitmap;
    }
    /**
     * 
     * @Description 生成图片的压缩图
     * @param filePath
     * @return
     */
    public static Bitmap createImageDetailThumbnail(String filePath, int w, int h) {
        if (null == filePath || !new File(filePath).exists())
            return null;
        Bitmap bitmap = null;
        int degree = readPictureDegree(filePath);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, h*w);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath, opts);
        } catch (Exception e) {
            return null;
        }
        Bitmap newBitmap = rotaingImageView(degree, bitmap);
        return newBitmap;
    }


    private static Bitmap rotaingImageView(int degree, Bitmap bitmap) {
        if (null == bitmap) {
            return null;
        }
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? MIN_SIDE_LENGTH : (int) Math
                .min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


    private static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        if (null == bitmap) {
            return null;
        }
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) width / w);
            float scaleHeight = ((float) height / h);
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix,
                    true);
            return newbmp;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获得图片的旋转角度
     * 
     * @param filePath
     * @return
     */
    private static int readPictureDegree(String filePath) {
        // 获得图片的角度
        int degreen = 0;
        ExifInterface ef;
        try {
            ef = new ExifInterface(filePath);
            int orientation = ef.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degreen = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degreen = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degreen = 270;
                    break;

                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degreen;
    }


    private static final String SD_PATH = "/sdcard/yunji/pic/";
    private static final String IN_PATH = "/yunji/pic/";

    /**
     * 随机生产文件名
     *
     * @return
     */
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }
    /**
     * 保存bitmap到本地
     *
     * @param context
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Context context, Bitmap mBitmap) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            savePath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath()
                    + IN_PATH;
        }
        try {
            filePic = new File(savePath + generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }
}
