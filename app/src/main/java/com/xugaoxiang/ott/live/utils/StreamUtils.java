package com.xugaoxiang.ott.live.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

	public static String stream2String(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int len = 0;
		byte[] buffer = new byte[1024];
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}

		in.close();
		out.close();

		return out.toString();
	}
}
