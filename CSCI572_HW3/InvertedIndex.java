import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.hadoop.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class InvertedIndex {
	
	public static class WordCountMapper extends Mapper<LongWritable, Text, Text, Text> {
   
		private final static Text docId = new Text();
		private Text word = new Text();
	  
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		    String line = value.toString();
		    String arr[] = line.split("\t", 2);
		    docId.set(arr[0]);
			StringTokenizer itr = new StringTokenizer(arr[1].toLowerCase().replaceAll("[^a-z]", " "));
			
		    while (itr.hasMoreTokens()) {
		      word.set(itr.nextToken());
		      context.write(word, docId);
		    }
		}
	}
	
	
	public static class WordCountReducer extends Reducer<Text, Text, Text, Text> {
		private Text result = new Text();
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
			
			for (Text val : values) {
				if(occurrences.containsKey(val.toString())) {
					occurrences.put(val.toString(), occurrences.get(val.toString())+1);
				}
				else {
					occurrences.put(val.toString(), 1);
				}
			}
			
			String count = "";
			
			for(String entry: occurrences.keySet()) {
				count += entry + ":" + occurrences.get(entry) + " ";
			}
			
			result.set(count);
			context.write(key, result);
		}
	}
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	    
	    if (args.length != 2) {
	      System.err.println("Usage: Word Count <inputpath> <outputpath>");
	      System.exit(-1);
	    }
	    
	    Job job = new Job();
	    job.setJarByClass(InvertedIndex.class);
	    job.setJobName("Word Count");
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    job.setMapperClass(WordCountMapper.class);
	    job.setReducerClass(WordCountReducer.class);
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
	    job.waitForCompletion(true);

	}
	
}
