package cs.zju.predictionmodels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.IB1;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.Instance;
import weka.core.Attribute;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.classifiers.Classifier;

import weka.classifiers.functions.Logistic;

import weka.attributeSelection.*;


import cs.zju.utils.DataSetInfo;
import cs.zju.utils.ResultToCSV;


public class PredictionModels {
	private int folds_num;
	private String my_dir;
	
	
	public PredictionModels(String arff_dir, String project, int folds_num){
		this.folds_num = folds_num;
		this.my_dir = arff_dir + project + "/";
	}
	
	public void run_random_guess(double recall) throws Exception {
		RandomGuess rg = new RandomGuess();
		rg.set_project_set(my_dir, folds_num, recall);
		rg.Evaluate();
	}
	
	public double compute_cost_effective(Classifier cla, Instances TestSet) throws Exception{
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		double test_positive = DataSetInfo.get_pos_number(TestSet);
		int instance_num_toCheck = (int)(TestSet.numInstances() * 0.2);
		if (test_positive > instance_num_toCheck)
			test_positive = instance_num_toCheck;
		
		int num_test = TestSet.numInstances();
		for (int i = 0; i < num_test; i++){
			map.put(i, cla.distributionForInstance(TestSet.instance(i))[1]);
		}
		List<Entry<Integer, Double>> list = new ArrayList<Entry<Integer, Double>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {

			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				if (o2.getValue() > o1.getValue()){
					return 1;
				}
				else if (o2.getValue() < o1.getValue()){
					return -1;
				}
				return 0;
			}
			
		});
		
		double correct = 0;
		for (int i = 0; i < instance_num_toCheck; i++){
			if (TestSet.instance(list.get(i).getKey()).classValue() == 1){
				correct += 1.0;
			}
		}
		
		return correct / test_positive;
	}
	
	public void run_classifiers(String classifier_model) throws Exception{
		double avg_fmessure = 0.0;
		double avg_recall = 0.0;
		double avg_precision = 0.0;
		double avg_auc = 0.0;
		double avg_effective = 0.0;
		double avg_acc = 0.0;
		String csv_path = my_dir + "resample_"+ classifier_model + "_eval.csv";
		String[] headers = {"precision", "recall", "fmeasure1", "roc", "cost_effective","accuracy", "false positive rate"};
		ResultToCSV csv_writer = new ResultToCSV(headers, csv_path);
		
		
		for (int i = 0; i < folds_num; i++){
			Instances TrainSet = new DataSource(my_dir + i + "/train.arff").getDataSet();
			Instances TestSet = new DataSource(my_dir + i + "/test.arff").getDataSet();
			TrainSet.setClassIndex(0);
			TestSet.setClassIndex(0);
			
			String[] to_delete_attributes = {"extern_num"};
			for (int j = 0; j < to_delete_attributes.length; j++){
				PreprocessDataSet.delete_attribute(TrainSet, to_delete_attributes[j]);
				PreprocessDataSet.delete_attribute(TestSet, to_delete_attributes[j]);
			}
			
			System.out.println(TrainSet.numAttributes());
			
			
			if (classifier_model != "BayesNet")
				PreprocessDataSet.normalize(TrainSet, TestSet);
			
			
			Classifier cla1 = null;
			
			if (classifier_model.equals("NaiveBayes"))
				cla1 = new NaiveBayes();
			else if (classifier_model.equals("SMO"))
				cla1 = new SMO();
			else if (classifier_model.equals("RandomForest"))
				cla1 = new RandomForest();
			else if (classifier_model.equals("BayesNet"))
				cla1 = new BayesNet();
			else if (classifier_model.equals("KNN")){
				cla1 = new IBk();
			}
			else if (classifier_model.equals("J48"))
				cla1 = new J48();
			
			cla1.buildClassifier(TrainSet);
			double cost_effective = compute_cost_effective(cla1, TestSet);
			avg_effective += cost_effective;
						
			Evaluation eval = new Evaluation(TrainSet);
			eval.evaluateModel(cla1, TestSet);
		
			avg_fmessure += eval.fMeasure(1);
			avg_recall += eval.recall(1);
			avg_precision += eval.precision(1);
			avg_auc += eval.areaUnderROC(1);
			avg_acc += (1 - eval.errorRate());
			
			String train_info = "positive: " + DataSetInfo.get_pos_number(TrainSet);
			train_info += " negative: " + DataSetInfo.get_neg_number(TrainSet);
			String test_info = "positive: " + DataSetInfo.get_pos_number(TestSet);
			test_info += " negative: " + DataSetInfo.get_neg_number(TestSet);
			
			System.out.println("The " + i + "th dataset:");
			System.out.println("TrainSet: " + train_info);
			System.out.println("TestSet: " + test_info);
			System.out.println("precision: " + eval.precision(1));
			System.out.println("recall: " + eval.recall(1));
			System.out.println("fmessure: " + eval.fMeasure(1));
			System.out.println("roc: " + eval.areaUnderROC(1));
			System.out.println("正确率：" + (1-eval.errorRate()));
			System.out.print("false positive rate:" + eval.falsePositiveRate(1));
			System.out.println();
			
			String[] contents = {""+eval.precision(1),
					""+eval.recall(1), ""+eval.fMeasure(1),
					""+eval.areaUnderROC(1), ""+cost_effective, ""+(1-eval.errorRate()),
					""+eval.falsePositiveRate(1)};
			csv_writer.write_contents(contents);
			
		}
		avg_fmessure /= folds_num;
		avg_precision /= folds_num;
		avg_recall /= folds_num;
		avg_auc /= folds_num;
		avg_effective /= folds_num;
		avg_acc /= folds_num;
		System.out.println("avg_fmeassure: " + avg_fmessure);
		System.out.println("avg_recall: "+ avg_recall);
		System.out.println("avg_precision: " + avg_precision);
		System.out.println();
		String[] contents = {""+avg_precision,
				""+avg_recall, ""+avg_fmessure, ""+avg_auc,""+avg_effective,""+avg_acc, ""};
		csv_writer.write_contents(contents);
		csv_writer.close();
	}
	
	public void run_cross_classifiers(String train_set_name, String test_set_name) throws Exception{
		
			String root_dir = "/Users/yuanruifan/Desktop/dataset/arff_files/";
			Instances TrainSet = new DataSource(root_dir + train_set_name + ".arff").getDataSet();
			Instances TestSet = new DataSource(root_dir + test_set_name + ".arff").getDataSet();
			TrainSet.setClassIndex(0);
			TestSet.setClassIndex(0);
			
			System.out.println("TrainSet: " + train_set_name + " TestSet: " + test_set_name);
			Classifier cla1 = null;
			cla1 = new BayesNet();
			
			cla1.buildClassifier(TrainSet);
						
			Evaluation eval = new Evaluation(TrainSet);
			eval.evaluateModel(cla1, TestSet);
			
			String train_info = "positive: " + DataSetInfo.get_pos_number(TrainSet);
			train_info += " negative: " + DataSetInfo.get_neg_number(TrainSet);
			String test_info = "positive: " + DataSetInfo.get_pos_number(TestSet);
			test_info += " negative: " + DataSetInfo.get_neg_number(TestSet);
			
			System.out.println("TrainSet: " + train_info);
			System.out.println("TestSet: " + test_info);
			System.out.println("precision: " + eval.precision(1));
			System.out.println("recall: " + eval.recall(1));
			System.out.println("fmessure: " + eval.fMeasure(1));
			System.out.println("roc: " + eval.areaUnderROC(1));
			System.out.println("正确率：" + (1-eval.errorRate()));
			System.out.print("false positive rate:" + eval.falsePositiveRate(1));
			System.out.println('\n');
	}
	
	public void run_text_classifier(String classifier_model) throws Exception{
		double avg_fmessure = 0.0;
		double avg_recall = 0.0;
		double avg_precision = 0.0;
		double avg_auc = 0.0;
		double avg_effective = 0.0;
		String csv_path = my_dir + "text_"+ classifier_model + "_eval.csv";
		String[] headers = {"precision", "recall", "fmeasure1", "roc", "cost_effective","accuracy", "false positive rate"};
		ResultToCSV csv_writer = new ResultToCSV(headers, csv_path);
		
		for (int i = 0; i < folds_num; i++){
			Instances TrainSet = new DataSource(my_dir + i + "/train_TF.arff").getDataSet();
			Instances TestSet = new DataSource(my_dir + i + "/test_TF.arff").getDataSet();
			TrainSet.setClassIndex(0);
			TestSet.setClassIndex(0);
			
			
			PreprocessDataSet.normalize(TrainSet, TestSet);
			FilteredClassifier cla1 = new FilteredClassifier();
			
			if (classifier_model.equals("NaiveBayes"))
				cla1.setClassifier(new NaiveBayes());
			else if (classifier_model.equals("SMO"))
				cla1.setClassifier(new SMO());
			else if (classifier_model.equals("RandomForest"))
				cla1.setClassifier(new RandomForest());
			
			cla1.buildClassifier(TrainSet);
			double cost_effective = compute_cost_effective(cla1, TestSet);
			avg_effective += cost_effective;
			
			Evaluation eval = new Evaluation(TrainSet);
			eval.evaluateModel(cla1, TestSet);
		
			avg_fmessure += eval.fMeasure(1);
			avg_recall += eval.recall(1);
			avg_precision += eval.precision(1);
			avg_auc += eval.areaUnderROC(1);
			
			String train_info = "positive: " + DataSetInfo.get_pos_number(TrainSet);
			train_info += " negative: " + DataSetInfo.get_neg_number(TrainSet);
			String test_info = "positive: " + DataSetInfo.get_pos_number(TestSet);
			test_info += " negative: " + DataSetInfo.get_neg_number(TestSet);
			
			System.out.println("The " + i + "th dataset:");
			System.out.println("TrainSet: " + train_info);
			System.out.println("TestSet: " + test_info);
			System.out.println("precision: " + eval.precision(1));
			System.out.println("recall: " + eval.recall(1));
			System.out.println("fmessure: " + eval.fMeasure(1));
			System.out.println("roc: " + eval.areaUnderROC(1));
			System.out.println("正确率：" + (1-eval.errorRate()));
			System.out.print("false positive rate:" + eval.falsePositiveRate(1));
			System.out.println();
			
			String[] contents = {""+eval.precision(1),
					""+eval.recall(1), ""+eval.fMeasure(1),
					""+eval.areaUnderROC(1),""+cost_effective, ""+(1-eval.errorRate()),
					""+eval.falsePositiveRate(1)};
		
			csv_writer.write_contents(contents);
		}
		avg_fmessure /= folds_num;
		avg_precision /= folds_num;
		avg_recall /= folds_num;
		avg_auc /= folds_num;
		avg_effective /= folds_num;
		System.out.println("avg_fmeassure: " + avg_fmessure);
		System.out.println("avg_recall: "+ avg_recall);
		System.out.println("avg_precision: " + avg_precision);
		System.out.println();
		String[] contents = {""+avg_precision,
				""+avg_recall, ""+avg_fmessure, ""+avg_auc,""+avg_effective,"",""};
		csv_writer.write_contents(contents);
		csv_writer.close();
	}
	
	public void run_under_sample(String classifier_model) throws Exception{
		Random r=new Random();
		double avg_fmessure = 0.0;
		double avg_precision = 0.0;
		double avg_recall = 0.0;
		double avg_roc = 0.0;
		String csv_path = my_dir + "undersample_"+ classifier_model + "_eval.csv";
		String[] headers = {"precision", "recall", "fmeasure1", "roc", "accuracy", "false positive rate"};
		ResultToCSV csv_writer = new ResultToCSV(headers, csv_path);
		for (int i = 0; i < folds_num; i++){
			Instances TrainSet = new DataSource( my_dir+ i + "/train.arff").getDataSet();
			Instances TestSet = new DataSource(my_dir + i + "/test.arff").getDataSet();
			TrainSet.setClassIndex(0);
			TestSet.setClassIndex(0);
			
			
			int positive = DataSetInfo.get_pos_number(TrainSet);
			int negative = DataSetInfo.get_neg_number(TrainSet);
			int num_to_delete = negative - positive;
			for (int j = 0; j < num_to_delete; ){
				int random = r.nextInt();
				int index = random % (TrainSet.numInstances());
				if (index <  0){
					index += TrainSet.numInstances();
				}
				if (TrainSet.instance(index).classValue() == 0){
					TrainSet.delete(index);
					j ++;
				}
			}
			
			PreprocessDataSet.normalize(TrainSet, TestSet);
			
			FilteredClassifier cla1 = new FilteredClassifier();
			
			if (classifier_model.equals("SMO"))
				cla1.setClassifier(new SMO());
			else if (classifier_model.equals("NaiveBayes"))
				cla1.setClassifier(new NaiveBayes());
			else if (classifier_model.equals("RandomForest"))
				cla1.setClassifier(new RandomForest());
			
			cla1.buildClassifier(TrainSet);
			
			
			
			Evaluation eval = new Evaluation(TrainSet);
			eval.evaluateModel(cla1, TestSet);
			avg_fmessure += eval.fMeasure(1);
			avg_recall += eval.recall(1);
			avg_precision += eval.precision(1);
			avg_roc += eval.areaUnderROC(1);
			String train_info = "positive: " + DataSetInfo.get_pos_number(TrainSet);
			train_info += " negative: " + DataSetInfo.get_neg_number(TrainSet);
			String test_info = "positive: " + DataSetInfo.get_pos_number(TestSet);
			test_info += " negative: " + DataSetInfo.get_neg_number(TestSet);
			
			System.out.println("The " + i + "th dataset:");
			System.out.println("TrainSet: " + train_info);
			System.out.println("TestSet: " + test_info);
			System.out.println("precision: " + eval.precision(1));
			System.out.println("recall: " + eval.recall(1));
			System.out.println("fmessure: " + eval.fMeasure(1));
			System.out.println("roc: " + eval.areaUnderROC(1));
			System.out.println("正确率：" + (1-eval.errorRate()));
			System.out.print("false positive rate:" + eval.falsePositiveRate(1));
			System.out.println();
			
			String[] contents = {""+eval.precision(1),
					""+eval.recall(1), ""+eval.fMeasure(1),
					""+eval.areaUnderROC(1), ""+(1-eval.errorRate()),
					""+eval.falsePositiveRate(1)};
			csv_writer.write_contents(contents);
		}
		
		avg_fmessure /= folds_num;
		avg_precision /= folds_num;
		avg_recall /=  folds_num;
		avg_roc /= folds_num;
		System.out.println("avg_fmeassure: " + avg_fmessure);
		System.out.println("avg_precision: " + avg_precision);
		System.out.println("avg_recall: " + avg_recall);
		System.out.println();
		String[] contents = {""+avg_precision,
				""+avg_recall, ""+avg_fmessure, ""+avg_roc,"",""};
		csv_writer.write_contents(contents);
		csv_writer.close();
	}
	
	public void run_threshold_leanrning_classifier(String classifier_model) throws Exception{
		double avg_fmessure = 0.0;
		double avg_recall = 0.0;
		double avg_precision = 0.0;
		double avg_roc = 0.0;
		double avg_effective = 0.0;
		String csv_path = my_dir + "threshold_learning_" + classifier_model + "_eval.csv";
		String[] headers = {"precision", "recall", "fmeasure1", "roc","cost_effective", "accuracy", "false positive rate"};
		ResultToCSV csv_writer = new ResultToCSV(headers, csv_path);
		for (int i = 0; i < folds_num; i++){
			Instances TrainSet = new DataSource(my_dir + i + "/train.arff").getDataSet();
			Instances TestSet = new DataSource(my_dir +	i + "/test.arff").getDataSet();
			TrainSet.setClassIndex(0);
			TestSet.setClassIndex(0);
			
			
			PreprocessDataSet.normalize(TrainSet, TestSet);
			System.out.println(TrainSet.numAttributes());
			ThresholdLearning cla1 = new ThresholdLearning();
			if (classifier_model == "RandomForest")
				cla1.setCla(new RandomForest());
			else if (classifier_model == "NaiveBayes")
				cla1.setCla(new NaiveBayes());
			else if (classifier_model == "KNN")
				cla1.setCla(new IBk());
			else if (classifier_model == "SMO")
				cla1.setCla(new SMO());
			cla1.buildClassifier(TrainSet);
			
			double cost_effective = compute_cost_effective(cla1, TestSet);
			avg_effective += cost_effective;
			
			Evaluation eval = new Evaluation(TrainSet);
			eval.evaluateModel(cla1, TestSet);
		
			avg_fmessure += eval.fMeasure(1);
			avg_recall += eval.recall(1);
			avg_precision += eval.precision(1);
			avg_roc += eval.areaUnderROC(1);
			
			String train_info = "positive: " + DataSetInfo.get_pos_number(TrainSet);
			train_info += " negative: " + DataSetInfo.get_neg_number(TrainSet);
			String test_info = "positive: " + DataSetInfo.get_pos_number(TestSet);
			test_info += " negative: " + DataSetInfo.get_neg_number(TestSet);
			
			System.out.println("The " + i + "th dataset:");
			System.out.println("TrainSet: " + train_info);
			System.out.println("TestSet: " + test_info);
			System.out.println("precision: " + eval.precision(1));
			System.out.println("recall: " + eval.recall(1));
			System.out.println("fmessure: " + eval.fMeasure(1));
			System.out.println("roc: " + eval.areaUnderROC(1));
			System.out.println("正确率：" + (1-eval.errorRate()));
			System.out.print("false positive rate:" + eval.falsePositiveRate(1));
			System.out.println();
			
			String[] contents = {""+eval.precision(1),
					""+eval.recall(1), ""+eval.fMeasure(1),
					""+eval.areaUnderROC(1), ""+cost_effective,""+(1-eval.errorRate()),
					""+eval.falsePositiveRate(1)};
			csv_writer.write_contents(contents);
		}
		avg_fmessure /= folds_num;
		avg_precision /= folds_num;
		avg_recall /= folds_num;
		avg_roc /= folds_num;
		avg_effective /= folds_num;
		System.out.println("avg_fmeassure: " + avg_fmessure);
		System.out.println("avg_recall: "+ avg_recall);
		System.out.println("avg_precision: " + avg_precision);
		System.out.println();
		
		String[] contents = {""+avg_precision,
				""+avg_recall, ""+avg_fmessure, ""+avg_roc,""+avg_effective,"",""};
		csv_writer.write_contents(contents);
		csv_writer.close();
	}
}
