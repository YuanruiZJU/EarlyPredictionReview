package cs.zju.predictionmodels;


import weka.core.Instances;
import cs.zju.utils.ResultToCSV;
import weka.core.Instance;
import weka.core.converters.ConverterUtils.DataSource;


public class RandomGuess {
	
	private String my_dir;
	private int folds_num;
	private double recall;
	
	public void set_project_set(String my_dir, int folds_num, double recall){
		this.my_dir = my_dir;
		this.folds_num = folds_num;
		this.recall = recall;
	}
	
	public void Evaluate() throws Exception{
		double avg_fmeassure = 0.0;
		double avg_precision = 0.0;
		double avg_recall = 0.0;
		
		String csv_path = my_dir + "constant_classifier.csv";
		if (recall == 0.5)
			csv_path = my_dir + "random_guess.csv";
		String[] headers = {"precision", "recall", "fmeasure1"};
		ResultToCSV csv_writer = new ResultToCSV(headers, csv_path);
		for(int i = 0; i < folds_num; i++){
			String dir = my_dir + i + "/";
			String train_file_path = dir + "train.arff";
			String test_file_path = dir + "test.arff";
			Instances TrainSet = new DataSource(train_file_path).getDataSet();	
			Instances TestSet = new DataSource(test_file_path).getDataSet();
			TrainSet.setClassIndex(0);
			TestSet.setClassIndex(0);
			
			double positive = 0;
			double negative = 0;
			
			for(int j = 0; j < TestSet.numInstances(); j ++){
				Instance cur = TestSet.instance(j);
				if (cur.classValue() ==  1){
					positive ++;
				}else{
					negative ++;
				}
			}
			double positive_precision = positive / (positive + negative);
			double positive_f1 = 2 * recall * positive_precision /( positive_precision + recall);
			
			avg_fmeassure += positive_f1;
			avg_precision += positive_precision;
			avg_recall += recall;			
			System.out.println("The" + i + "th dataset:");
			System.out.println("total:" + (positive + negative));
			System.out.println("positive: " + positive);
			System.out.println("positive precision: " + positive_precision);
			System.out.println("recall: " + recall);
			System.out.println("positive f1: " + positive_f1);
			System.out.println();
			
			String[] contents = {""+positive_precision,
					""+recall, ""+positive_f1,
					""+0.5,"-", ""+0.5,
					"-"};
		
			csv_writer.write_contents(contents);
		}
		avg_fmeassure /= folds_num;
		avg_precision /= folds_num;
		avg_recall /= folds_num;
		
		String[] contents = {""+avg_precision,
				""+avg_recall, ""+avg_fmeassure};
		csv_writer.write_contents(contents);
		csv_writer.close();
		System.out.println("avg_fmessure: " + avg_fmeassure);
		System.out.println("avg_precision: " + avg_precision);
		System.out.println("avg_recall: " + avg_recall);
	}
}
