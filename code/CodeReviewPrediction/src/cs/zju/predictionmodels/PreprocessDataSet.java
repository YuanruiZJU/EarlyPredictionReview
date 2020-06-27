package cs.zju.predictionmodels;

import weka.core.Attribute;
import weka.core.Instances;

public class PreprocessDataSet {
	
	public static void delete_attribute(Instances DataSet, String attr_name) throws Exception{
		int i = 0;
		for (i = 0; i < DataSet.numAttributes(); i ++){
			//System.out.println(DataSet.attribute(i).name());
			if (DataSet.attribute(i).name().equals(attr_name)){
			//	System.out.println(attr_name);
				DataSet.deleteAttributeAt(i);
				i --; 
				break;
			}
			
		}
	}
	
	public static void normalize(Instances DataSet1, Instances DataSet2) throws Exception{
		for (int i = 0; i < DataSet1.numAttributes(); i++)
		{
			Attribute attr = DataSet1.attribute(i);
			if (attr.isNumeric()){
				double min_value = DataSet1.instance(0).value(attr);
				double max_value = min_value;
				for(int j = 0; j < DataSet1.numInstances(); j++){
					if (DataSet1.instance(j).value(attr) > max_value){
						max_value = DataSet1.instance(j).value(attr);
					}
					if (DataSet1.instance(j).value(attr) < min_value){
						min_value = DataSet1.instance(j).value(attr);
					}
				}
				
				for(int j = 0; j < DataSet2.numInstances(); j++){
					if (DataSet2.instance(j).value(attr) > max_value){
						max_value = DataSet2.instance(j).value(attr);
					}
					if (DataSet2.instance(j).value(attr) < min_value){
						min_value = DataSet2.instance(j).value(attr);
					}
				}
				
				for(int j = 0; j < DataSet1.numInstances(); j++){
					double value = DataSet1.instance(j).value(attr);
					value = (value - min_value) / (max_value - min_value);
					DataSet1.instance(j).setValue(attr, value);
				}
				
				for (int j = 0; j < DataSet2.numInstances(); j++){
					double value = DataSet2.instance(j).value(attr);
					value = (value - min_value) / (max_value - min_value);
					DataSet2.instance(j).setValue(attr, value);
				}		
			}
		}
	}
}
