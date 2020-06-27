package cs.zju.cross.feature;

import java.io.BufferedWriter;
import java.io.FileWriter;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TransformToTF {
	private String my_dir;
	private int folds_num;
	
	public TransformToTF(String arff_dir, String project, int folds_num){
		my_dir = arff_dir + project + "/";
		this.folds_num = folds_num;
	}

	public void generate_TF_arff_file() throws Exception {
		for (int i = 0; i < folds_num; i ++){
			DataSource source = new DataSource(my_dir + i + "/raw_msg_train.arff");
			Instances dataRaw = source.getDataSet();
			
			DataSource source2 = new DataSource(my_dir + i + "/raw_msg_test.arff");
			Instances dataRaw2 = source2.getDataSet();
	
			StringToWordVector filter = new StringToWordVector();
			filter.setWordsToKeep(100000);
			filter.setInputFormat(dataRaw);
			filter.setTFTransform(true);
			filter.setIDFTransform(true);
	
			filter.setAttributeIndices("2");
			filter.setLowerCaseTokens(true);
	
			filter.setMinTermFreq(10);
			filter.setOutputWordCounts(true);
			 
			WordTokenizer a = new WordTokenizer();
			String opt= " \1\r\t\n\b.,;:\'\"()?!-><#$\\%&*+/@^_=[]{}|`~0123456789";
			a.setDelimiters(opt);
	
			filter.setTokenizer(a);
			SnowballStemmer stemmer = new SnowballStemmer();
			stemmer.setStemmer("porter");
			filter.setStemmer(stemmer);
			
			Instances data = Filter.useFilter(dataRaw, filter);
			Instances data2 = Filter.useFilter(dataRaw2, filter);
						
		
			BufferedWriter out = new BufferedWriter(new FileWriter(my_dir + i + "/train_TF.arff"));	
			out.write(data.toString());
			out.close();
			
			BufferedWriter out2 = new BufferedWriter(new FileWriter(my_dir + i + "/test_TF.arff"));	
			out2.write(data2.toString());
			out2.close();
		}
	}
}
