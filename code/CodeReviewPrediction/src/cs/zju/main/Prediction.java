package cs.zju.main;

import cs.zju.predictionmodels.PredictionModels;
import cs.zju.cross.feature.TransformToTF;

public class Prediction {
private static String arff_dir = "/Users/yuanruifan/Desktop/dataset/arff_file_my_paper/";
	
	
	private static int folds_num = 10;
	
	public static void run_classifiers(String project_name) throws Exception{
		PredictionModels pms = new PredictionModels(arff_dir, project_name, folds_num);
		
//		pms.run_cross_classifiers("eclipse", "libreoffice");
//		pms.run_cross_classifiers("eclipse", "openstack");
		
//		pms.run_cross_classifiers("libreoffice", "eclipse");
//		pms.run_cross_classifiers("libreoffice", "openstack");
		
//		pms.run_cross_classifiers("openstack", "eclipse");
//		pms.run_cross_classifiers("openstack", "libreoffice");
		
//		pms.run_classifiers("J48");
		pms.run_classifiers("BayesNet");
//		pms.run_classifiers("SMO");
//		pms.run_classifiers("NaiveBayes");
//		pms.run_classifiers("RandomForest");
//		pms.run_classifiers("KNN");
		
//		pms.run_under_sample("RandomForest");
		
//		pms.run_threshold_leanrning_classifier("RandomForest");

//		pms.run_random_guess(1.0);
//		pms.run_random_guess(0.5);
		
//		TransformToTF tf = new TransformToTF(arff_dir, project_name, folds_num);
//		tf.generate_TF_arff_file();
//		pms.run_text_classifier("NaiveBayes");
	}
	
	public static void main(String[] args) throws Exception {
		arff_dir = "/Users/yuanruifan/Desktop/dataset/arff_file_my_paper/"; 
		run_classifiers("libreoffice");
		run_classifiers("eclipse");
		run_classifiers("openstack");
		
//		arff_dir = "/Users/yuanruifan/Desktop/dataset/arff_file_author_exp/";
//		run_classifiers("libreoffice");
//		run_classifiers("eclipse");
//		run_classifiers("openstack");
		
//		arff_dir = "/Users/yuanruifan/Desktop/dataset/arff_file_author_exp/";
//		run_classifiers("libreoffice");
//		run_classifiers("eclipse");
//		run_classifiers("openstack");
		
//		arff_dir = "/Users/yuanruifan/Desktop/dataset/arff_file_social/";
//		run_classifiers("libreoffice");
//		run_classifiers("eclipse");
//		run_classifiers("openstack");
		
//		arff_dir = "/Users/yuanruifan/Desktop/dataset/arff_file_msg/";
//		run_classifiers("libreoffice");
//		run_classifiers("eclipse");
//		run_classifiers("openstack");
	}
}
