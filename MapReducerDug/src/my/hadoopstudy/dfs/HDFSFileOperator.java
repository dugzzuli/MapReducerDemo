package my.hadoopstudy.dfs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

public class HDFSFileOperator {

	long count = 0;

	public byte[] getBytesFromURI(Configuration conf, String uri) throws IOException {

		byte[] data = null;
		InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FileSystem fs = FileSystem.get(URI.create(uri), conf);

		try {

			in = fs.open(new Path(uri));
			IOUtils.copyBytes(in, out, 4096, false);
			data = out.toByteArray();
		} finally {

			out.close();
			IOUtils.closeStream(in);
		}

		return data;
	}

	public void printFileOnHDFS(Configuration conf, String uri) throws IOException {

		FileSystem fs = FileSystem.get(URI.create(uri), conf);

		if (fs.exists(new Path(uri)) == false)
			return;

		InputStream in = null;

		try {

			in = fs.open(new Path(uri));
			IOUtils.copyBytes(in, System.out, 4096, false);

		} finally {

			IOUtils.closeStream(in);
		}

	}

	public String seekFileOnHDFS(Configuration conf, String uri, long startPos) throws IOException {

		FSDataInputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FileSystem fs = FileSystem.get(URI.create(uri), conf);
		fs.setVerifyChecksum(false);

		if (fs.exists(new Path(uri)) == false)
			return "";

		try {
			in = fs.open(new Path(uri));
			in.seek(startPos);

			IOUtils.copyBytes(in, out, 4096, false);

			return out.toString();

		} finally {

			IOUtils.closeStream(in);
		}

	}

	public void writeToHDFS(Configuration conf, String uri, String text) throws IOException {

		FileSystem fs = FileSystem.get(conf);
		OutputStream out;
		if (fs.exists(new Path(uri))) {

			out = fs.append(new Path(uri));
		} else {

			out = fs.create(new Path(uri), false, 4096);
		}
		out.write(text.getBytes());
		out.close();
	}

	public void copyToHDFS(Configuration conf, String localPath, String HDFSPath, boolean showProgress)
			throws IOException {

		FileSystem fs = FileSystem.get(URI.create(HDFSPath), conf);
		InputStream in = new BufferedInputStream(new FileInputStream(localPath));
		OutputStream out = null;

		if (showProgress) {
			out = fs.create(new Path(HDFSPath), true, 4096, new Progressable() {
				public void progress() {
					System.out.print(".");
				}
			});
		} else {
			out = fs.create(new Path(HDFSPath), true, 4096);
		}

		IOUtils.copyBytes(in, out, 4096, true);
	}

	public void deleteFileOnHDFS(Configuration conf, String uri) throws IOException {

		FileSystem fs = FileSystem.get(URI.create(uri), conf);
		if (fs.exists(new Path(uri)) == false)
			return;
		fs.delete(new Path(uri), false);
	}
}
