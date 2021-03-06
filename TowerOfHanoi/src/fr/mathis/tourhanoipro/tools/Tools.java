package fr.mathis.tourhanoipro.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.res.Resources;
import android.view.View;

public class Tools {

	public static int convertDpToPixel(float dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

	public static final String generateMd5(String s) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			s = s + s;
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static int getViewDiagonal(View v) {
		int d = 0;

		d = (int) Math.sqrt(v.getHeight() * v.getHeight() + v.getWidth() * v.getWidth());

		return d;
	}
}
