package my.hadoopstudy.dfs;

import java.io.IOException;

import java.util.Iterator;

import java.util.StringTokenizer;

 

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.GenericOptionsParser;

public class Score {
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			
			String line = value.toString();
			StringTokenizer token = new StringTokenizer(line, "\n");
			while (token.hasMoreElements()) {
				// 每行按空格划分

				StringTokenizer tokenizerLine = new StringTokenizer(token.nextToken());

				String strName = tokenizerLine.nextToken();// 学生姓名部分

				String strScore = tokenizerLine.nextToken();// 成绩部分

				Text name = new Text(strName);

				int scoreInt = Integer.parseInt(strScore);

				// 输出姓名和成绩

				context.write(name, new IntWritable(scoreInt));

			}
		}

	}

	public static class Reducer extends org.apache.hadoop.mapreduce.Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				org.apache.hadoop.mapreduce.Reducer<Text, IntWritable, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			int sum = 0;

			int count = 0;
			Iterator<IntWritable> iterator = values.iterator();

			while (iterator.hasNext()) {

				sum += iterator.next().get();// 计算总分

				count++;// 统计总的科目数

			}
			int average = (int) sum / count;// 计算平均成绩

			context.write(key, new IntWritable(average));

		}

	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();

		// 这句话很关键

		String[] ioArgs = new String[] { "score_in", "score_out" };

		String[] otherArgs =new String[] {"hdfs://localhost:9000/score/*","hdfs://localhost:9000/scoreoutput/"};

		if (otherArgs.length != 2) {

			System.err.println("Usage: Score Average <in> <out>");

			System.exit(2);

		}

		Job job = new Job(conf, "Score Average");

		job.setJarByClass(Score.class);

		// 设置Map、Combine和Reduce处理类

		job.setMapperClass(Map.class);

		job.setCombinerClass(Reducer.class);

		job.setReducerClass(Reducer.class);

		// 设置输出类型

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(IntWritable.class);

		// 将输入的数据集分割成小数据块splites，提供一个RecordReder的实现

		job.setInputFormatClass(TextInputFormat.class);

		// 提供一个RecordWriter的实现，负责数据输出

		job.setOutputFormatClass(TextOutputFormat.class);

		// 设置输入和输出目录

		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
