package be.android.pernumerator.data;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemImageHandler {

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        //TODO handle exception regarding closing streams
        stream.close();
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    // resize bitmap (otherwise crash while getting it with cursor, which is limited to 1 Mb
    public static Bitmap resizeBitmap(Bitmap bitmap, int desiredWidth) {
        float aspectRatio = (float)bitmap.getWidth()/(float)bitmap.getHeight();
        int desiredHeight = Math.round(desiredWidth / aspectRatio);
        return Bitmap.createScaledBitmap(bitmap, desiredWidth,desiredHeight, false);
    }
}
