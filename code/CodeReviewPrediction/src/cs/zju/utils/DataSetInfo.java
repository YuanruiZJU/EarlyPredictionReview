package cs.zju.utils;

import weka.core.Instances;

public class DataSetInfo {
	public static int get_pos_number(Instances DataSet) throws Exception{
		int num_data = DataSet.numInstances();
		int positive=0;
		for(int i = 0; i < num_data; i++){
			if(DataSet.instance(i).classValue() == 1){
				positive ++;
			}
		}
		return positive;
	}
	
	public static int get_neg_number(Instances DataSet) throws Exception{
		int num_data = DataSet.numInstances();
		int negative=0;
		for(int i = 0; i < num_data; i++){
			if(DataSet.instance(i).classValue() == 0){
				negative ++;
			}
		}
		return negative;
	}
}
