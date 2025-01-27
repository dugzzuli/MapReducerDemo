package my.hadoopstudy.dfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TempCalc {

	public static class TempCalcMap extends Mapper<LongWritable, Text, Text, IntWritable> {
		private static Text year = new Text();
		public static IntWritable temp = new IntWritable();

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			String line = value.toString();
			if (line != (null)) {
				if (!line.isEmpty() || !line.equals("")) {
					String tem = line.substring(line.length() - 3, line.length() - 1);
					String yeartemp = line.substring(0, 4);
					year.set(yeartemp);
					temp.set(Integer.valueOf(tem));
					context.write(year, temp);
					System.out.println(yeartemp + "=======" + temp);
				}
			}
		}

	}

	public static class TempCalcReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		int MIN_VALUE = Integer.MIN_VALUE;

		@Override
		protected void reduce(Text key, Iterable<IntWritable> valyes,
				Reducer<Text, IntWritable, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			for (IntWritable v : valyes) {
				MIN_VALUE = Math.max(v.get(), MIN_VALUE);
			}
			System.out.println("MIN_VALUE=" + MIN_VALUE);
			context.write(key, new IntWritable(MIN_VALUE));

		}

	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		String dst = "hdfs://localhost:9000/tem/tem";
		String dstOut = "hdfs://localhost:9000/temoutput/";
		Configuration hadoopConfig = new Configuration();
		Job job = new Job(hadoopConfig);
		FileInputFormat.addInputPath(job, new Path(dst));

		FileOutputFormat.setOutputPath(job, new Path(dstOut));
		job.setMapperClass(TempCalcMap.class);
		job.setReducerClass(TempCalcReducer.class);
		// 设置最后输出结果的Key和Value的类型

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(IntWritable.class);

		// 执行job，直到完成

		job.waitForCompletion(true);

		System.out.println("Finished");

	}

}
