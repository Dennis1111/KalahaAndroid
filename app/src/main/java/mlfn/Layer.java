package mlfn;

/**
 * Created by Dennis on 2017-11-15.
 */

public class Layer {
    protected int neurons;
    protected double outputs[];
    protected double[][] weights;
    protected double[][] delta;
    protected double[] bias;
    //The errors for a layer as described by
    //http://neuralnetworksanddeeplearning.com/chap2.html BP1 and BP2
    
    protected double[] error;
    //protected double[] gradients;
    protected Layer previousLayer;
    private double minStartWeight=0.01;
    protected String name="";
  
    //Constructor for first layer
    public Layer(int neurons){
        this.neurons=neurons;
        //Include bias in the outputs
        outputs = new double[neurons];       
        this.error=null;
        //this.gradients=null;
        this.weights=null;
	this.bias=null;
        this.previousLayer=null;
    }

    //Constructor for first layer
    public Layer(int neurons,Layer previousLayer){
        this.neurons=neurons;
        //Include bias in the outputs
        outputs = new double[neurons];
        //Use the last output as bias always = 1
	weights = new double[neurons][previousLayer.outputs.length];
	delta = new double[neurons][previousLayer.outputs.length];
	bias = new double[neurons];
	for(int neuron=0;neuron<neurons;neuron++) {
	  for(int prevLayerNeuron=0;prevLayerNeuron<previousLayer.outputs.length;prevLayerNeuron++){
	    weights[neuron][prevLayerNeuron]=getRandomWeight();
	  }
	  bias[neuron]=getRandomWeight();
	}
	    
        //we need no delta for bias neurons as it has no connection to prev layer
        this.error = new double[neurons];
        //this.gradients=new double[neurons];
        this.previousLayer=previousLayer;
    }

  private double getRandomWeight(){
    double w=0;
    do
      w = (Math.random()-0.5)/5.0;
    //If a weight is to close to zero repeat
    //so that we can conclude small weights later is due to learning
    while (Math.abs(w)<minStartWeight);
    return w;
  }
}
