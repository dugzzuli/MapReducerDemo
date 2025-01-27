package my.hadoopstudy.dfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PaiXu {

	public static class PaiXuMap extends Mapper<Object, Text, IntWritable, IntWritable> {
		private static IntWritable data = new IntWritable();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			String line = value.toString();
			System.out.println("key=" + key);
			data.set(Integer.parseInt(line));
			context.write(data, new IntWritable(1));
		}

	}

	public static class PaiXuReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
		private static IntWritable linenum = new IntWritable(1);

		@Override
		protected void reduce(IntWritable key, Iterable<IntWritable> values,
				Reducer<IntWritable, IntWritable, IntWritable, IntWritable>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			for (IntWritable val : values) {

				context.write(linenum, key);

				linenum = new IntWritable(linenum.get() + 1);

			}
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		String[] otherArgs = new String[] { "hdfs://localhost:9000/paixu/*", "hdfs://localhost:9000/paixuoutput/" };

		Configuration conf = new Configuration();
		Job job = new Job(conf, "Data Sort");
		job.setJarByClass(PaiXu.class);
		job.setMapperClass(PaiXuMap.class);
		job.setReducerClass(PaiXuReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);
		// 设置输入和输出目录

		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
		

	}
}
