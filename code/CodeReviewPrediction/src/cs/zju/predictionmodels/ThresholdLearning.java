package cs.zju.predictionmodels;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.WeightedInstancesHandler;

import cs.zju.utils.DataSetInfo;;

public class ThresholdLearning extends Classifier implements OptionHandler,
		UpdateableClassifier, WeightedInstancesHandler,
		TechnicalInformationHandler, AdditionalMeasureProducer, Serializable {
	double threshold;
	Classifier cla;
	@Override
	public Enumeration enumerateMeasures() {
		return null;
	}
	
	public Classifier getCla() {
		return cla;
	}

	public void setCla(Classifier cla) {
		this.cla = cla;
	}

	@Override
	public double getMeasure(String measureName) {
		return 0;
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		return null;
	}

	@Override
	public void updateClassifier(Instance instance) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		// TODO Auto-generated method stub
		double[] scores = cla.distributionForInstance(instance);
		
		
		
		double sum = threshold+scores[1];
		
		if(sum==0)
		{
			scores[0]=1;
			scores[1]=0;
		}
		
		else{
		scores[0]=threshold/sum;
		scores[1]=scores[1]/sum;}
		
		
		
		return scores;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

//		int num_test = data.numInstances() / 5;
//		int num_train = data.numInstances() - num_test;
//		Instances testSub = new Instances(data);
//		Instances trainSub = new Instances(data);
//		for (int i = 0; i < num_test; i++){
//			trainSub.delete(trainSub.numInstances()-1);
//		}
//		
//		for (int i = 0; i < num_train; i++){
//			testSub.delete(0);
//		}
		Instances testSub = data.testCV(5, 0);
		Instances trainSub = data.trainCV(5, 0);
		
		ThresholdComputing(trainSub, testSub);

		cla.buildClassifier(data);

	}

	private void ThresholdComputing(Instances trainSub, Instances testSub)
			throws Exception {

		cla.buildClassifier(trainSub);
		int numtest = testSub.numInstances();

		double[] trainScores = new double[numtest];

		double[] labelSpace = new double[numtest];
		for (int i = 0; i < numtest; i++) {
			Instance cur = testSub.instance(i);

			labelSpace[i] = cur.classValue();

			double[] dis = cla.distributionForInstance(cur);
			trainScores[i] = dis[1];

			//System.out.println("scoress "+trainScores[i]);

		}
		double bestmcc = 0;
		for (double i = 0; i <= 100; i++) {
			double tmpThreshold = (double) i * 0.01;

			double tp = 0;
			double tn = 0;
			double fp = 0;
			double fn = 0;

			for (int j = 0; j < numtest; j++) {
				double predicted = 0;
				if (trainScores[j] >= tmpThreshold)
					predicted = 1;

				if (labelSpace[j] == 1) {
					if (predicted == 1) {
						tp++;
					} else {
						fn++;
					}

				} else if (labelSpace[j] == 0) {
					if (predicted == 0) {
						tn++;
					} else {
						fp++;
					}
				}

			}

			double tmpFmeasure = 0;
			if ((2 * tp + fp + fn) > 0) {
				tmpFmeasure = 2 * tp / (2 * tp + fp + fn);
			} else {
				tmpFmeasure = 0;
			}
			
			
			if (tmpFmeasure >= bestmcc) {
				threshold = tmpThreshold;
				bestmcc = tmpFmeasure;
			}
			
		}
	}

}
