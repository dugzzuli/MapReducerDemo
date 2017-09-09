package my.hadoopstudy.dfs;

import java.io.InputStream;
import java.net.URL;

import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
import org.apache.hadoop.io.IOUtils;

public class IOUtilsDemo {
	/***
	 * Hadoop IO的基础类，提供一组静态方法来控制HadoopIO。通过IOUtils类，可以使用java.net.URL类来访问HDFS，
	 * 同时也可以在标准输入流和输出流之间复制数据。需要注意的是，为了是java.net.URL能够识别HDFS的URL方案
	 * (hdfs://namenode:port/)需要在使用前设置URL的流处理工厂类为org.apache.hadoop.fs.
	 * FsUrlStreamHandlerFactory， 详见代码示例。
	 * 
	 **/
	static {
		// JVM只能调用一次setURLStreamHandlerFactory方法，所以在只能在静态方法中使用了，
		// 若工程中其他类库之前以调用了该方法，那将无法再使用该方法从 hadoop中得到数据
		URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
	}

	public static void main(String[] args) throws Exception {
		InputStream in = null;
		try {
			in = new URL(args[0]).openStream();
			IOUtils.copyBytes(in, System.out, 4091, false);
		} finally {
			IOUtils.closeStream(in);
		}
	}
}
