package wei.hu.lib.dllib;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class BaseDownloader implements Downloader {

	/** {@value} */
	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
	/** {@value} */
	public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds

	/** {@value} */
	protected static final int BUFFER_SIZE = 32 * 1024; // 32 Kb
	/** {@value} */
	protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

	protected static final int MAX_REDIRECT_COUNT = 5;

	private static final String ERROR_UNSUPPORTED_SCHEME = "UIL doesn't support scheme(protocol) by default [%s]. "
			+ "You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";

	protected final Context context;
	protected final int connectTimeout;
	protected final int readTimeout;

	public BaseDownloader(Context context) {
		this.context = context.getApplicationContext();
		this.connectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT;
		this.readTimeout = DEFAULT_HTTP_READ_TIMEOUT;
	}

	public BaseDownloader(Context context, int connectTimeout, int readTimeout) {
		this.context = context.getApplicationContext();
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	@Override
	public InputStream getStream(String imageUri, Object extra) throws IOException {
		switch (Scheme.ofUri(imageUri)) {
		case HTTP:
		case HTTPS:
			return getStreamFromNetwork(imageUri, extra);
		case FILE:
			return getStreamFromFile(imageUri, extra);
		case CONTENT:
			return getStreamFromContent(imageUri, extra);
		case ASSETS:
			return getStreamFromAssets(imageUri, extra);
		case DRAWABLE:
			return getStreamFromDrawable(imageUri, extra);
		case UNKNOWN:
		default:
			return getStreamFromOtherSource(imageUri, extra);
		}
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in the network).
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 * @throws IOException
	 *             if some I/O error occurs during network request or if no InputStream could be created for URL.
	 */
	protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
		HttpURLConnection conn = createConnection(imageUri);

		int redirectCount = 0;
		while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
			conn = createConnection(conn.getHeaderField("Location"));
			redirectCount++;
		}

		return new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
	}

	/**
	 * Create {@linkplain HttpURLConnection HTTP connection} for incoming URL
	 * 
	 * @param url
	 *            URL to connect to
	 * @return {@linkplain HttpURLConnection Connection} for incoming URL. Connection isn't established so it still
	 *         configurable.
	 * @throws IOException
	 *             if some I/O error occurs during network request or if no InputStream could be created for URL.
	 */
	protected HttpURLConnection createConnection(String url) throws IOException {
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located on the local file system or SD card).
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 * @throws IOException
	 *             if some I/O error occurs reading from file system
	 */
	protected InputStream getStreamFromFile(String imageUri, Object extra) throws IOException {
		String filePath = Scheme.FILE.crop(imageUri);
		return new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is accessed using {@link ContentResolver}).
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 * @throws FileNotFoundException
	 *             if the provided URI could not be opened
	 */
	protected InputStream getStreamFromContent(String imageUri, Object extra) throws FileNotFoundException {
		ContentResolver res = context.getContentResolver();
		Uri uri = Uri.parse(imageUri);
		return res.openInputStream(uri);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in assets of application).
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 * @throws IOException
	 *             if some I/O error occurs file reading
	 */
	protected InputStream getStreamFromAssets(String imageUri, Object extra) throws IOException {
		String filePath = Scheme.ASSETS.crop(imageUri);
		return context.getAssets().open(filePath);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in drawable resources of application).
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 */
	protected InputStream getStreamFromDrawable(String imageUri, Object extra) {
		String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
		int drawableId = Integer.parseInt(drawableIdString);
		BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(drawableId);
		Bitmap bitmap = drawable.getBitmap();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, os);
		return new ByteArrayInputStream(os.toByteArray());
	}

	/**
	 * Retrieves {@link InputStream} of image by URI from other source with unsupported scheme. Should be overriden by
	 * successors to implement image downloading from special sources.<br />
	 * This method is called only if image URI has unsupported scheme. Throws {@link UnsupportedOperationException} by
	 * default.
	 * 
	 * @param imageUri
	 *            Image URI
	 * @param extra
	 *            Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *            DisplayImageOptions.extraForDownloader(Object)}; can be null
	 * @return {@link InputStream} of image
	 * @throws IOException
	 *             if some I/O error occurs
	 * @throws UnsupportedOperationException
	 *             if image URI has unsupported scheme(protocol)
	 */
	protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
		throw new UnsupportedOperationException(String.format(ERROR_UNSUPPORTED_SCHEME, imageUri));
	}
}
