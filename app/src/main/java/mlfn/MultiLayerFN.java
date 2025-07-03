package mlfn;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;
import java.io.*;

/**
 * Created by Dennis on 2017-11-15.
 * A Multilayer Feedforward Network that currently only uses sigmoidActivationFunction
 * and errorFunction (out-target)^2 -> deriv 1/2 (target-out)
 * though the 1/2 is removed as you can just double the learningrate to get same effect
 */

public class MultiLayerFN {
    private List<Layer> layers;
    private double learningRate = 0.02;
    private double momentum = 0.9;
    //for debugging
    private int info = 0;

    public MultiLayerFN(int inputs, int hiddens, int outputs) {
        layers = new ArrayList<>();
        Layer inputLayer = new Layer(inputs);
        inputLayer.name = "inputLayer";
        layers.add(inputLayer);
        Layer hiddenLayer = new Layer(hiddens, inputLayer);
        hiddenLayer.name = "Hidden";
        layers.add(hiddenLayer);
        Layer outputLayer = new Layer(outputs, hiddenLayer);
        outputLayer.name = "out";
        layers.add(outputLayer);
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double[] predict(double[] pattern) {
        //if (info>0)
        //System.out.println("\nPREDICT"+Arrays.toString(pattern));
        //1 copy pattern to first layer
        Layer firstLayer = layers.get(0);
        for (int x = 0; x < pattern.length; x++) {
            firstLayer.outputs[x] = pattern[x];
        }
        //if (info>0)
        //  System.out.println("\nFirstLayer"+Arrays.toString(firstLayer.outputs));
        //2 We only use one hidden layer
        double sum = 0;
        for (int layer = 1; layer < layers.size(); layer++) {
            forwardPass(layers.get(layer));
        }
      
      /*if (info>0){
    System.out.println("Predict first layer out"+Arrays.toString(firstLayer.outputs) + " "+firstLayer.name);
	System.out.println("hidden layer out"+Arrays.toString(layers.get(1).outputs));
	System.out.println("last layer out"+Arrays.toString(layers.get(2).outputs));
	}*/
      
      /*
      //3 We currently only use softmax classification for last layer
      double layerSum = 0;
      Layer lastLayer = layers.get(layers.size() - 1);
      for (int out = 0; out < lastLayer.neurons; out++)
      layerSum += lastLayer.outputs[out];
      for (int out = 0; out < lastLayer.neurons; out++)
      lastLayer.outputs[out] /= layerSum;*/
        return layers.get(layers.size() - 1).outputs;
    }

    //Forward pass from previousLayer to layer to layer
    private void forwardPass(Layer targetLayer) {
        double[] inputs = targetLayer.previousLayer.outputs;
        //if (info>0){
        //  System.out.println("forwardPass From "+targetLayer.previousLayer.name+" To"+targetLayer.name+" :inputs"+Arrays.toString( targetLayer.previousLayer.outputs));
        //}
        //calculate output for all layer neurons (except bias)
        for (int out = 0; out < targetLayer.neurons; out++) {
            double sum = 0;
            for (int input = 0; input < inputs.length; input++) {
                //if(info>1)
                //System.out.println(inputs[input]+","+targetLayer.weights[out][input]);
                sum += inputs[input] * targetLayer.weights[out][input];
            }
            sum += targetLayer.bias[out];
            targetLayer.outputs[out] = sigmoid(sum);
            //  if (info>1){
            //  System.out.println("sum"+sum+"out"+out+" :-> "+targetLayer.outputs[out]);
            //}
        }
    }

    // To calc Slope for a weight we need to multiply
    // 1. Slope of the loss function w.r.t value at the node we feed into
    // 2. The value of the node that feeds into our weight
    // 3. Slope of the activation function w.r.t value we feed into (the activations reached in the 'nextLayer')

    public double learn(double[] inputs, double[] ideals) {
        double[] predicted = predict(inputs);
        Layer lastLayer = layers.get(layers.size() - 1);
        //1. calculate the error for output neurons

        //System.out.println("netout"+Arrays.toString(actuals)+" Target "+ideals[0]);
        for (int neuron = 0; neuron < ideals.length; neuron++) {
            lastLayer.error[neuron] = (ideals[neuron] - predicted[neuron]) * sigmoidDeriv(predicted[neuron]);
        }

        Layer hiddenLayer = layers.get(1);
        //Calculate the errors for the hidden layer neurons
        backpropagateErrors(lastLayer);
        updateWeights(lastLayer);
        updateWeights(hiddenLayer);
        double squaredError = 0;
        for (int ideal = 0; ideal < ideals.length; ideal++) {
            double diff = ideals[ideal] - predicted[ideal];
            squaredError += diff * diff;
        }
        return squaredError / ideals.length;
    }

    private void backpropagateErrors(Layer layer) {
        Layer prevLayer = layer.previousLayer;
        for (int prevNeuron = 0; prevNeuron < prevLayer.neurons; prevNeuron++) {
            double error = 0;
            for (int neuron = 0; neuron < layer.neurons; neuron++) {
                error += layer.weights[neuron][prevNeuron] * layer.error[neuron];
            }
            prevLayer.error[prevNeuron] = error * sigmoidDeriv(prevLayer.outputs[prevNeuron]);
        }
    }

    //Update the weights between between layer and the previous layer
    private void updateWeights(Layer layer) {
        //if(info>0)
        //  System.out.println(layer.name+"Layer update Wsum"+sumWeights(layer));
        Layer prevLayer = layer.previousLayer;
        for (int nextNeuron = 0; nextNeuron < layer.neurons; nextNeuron++) {
            double error = layer.error[nextNeuron];
            for (int prevNeuron = 0; prevNeuron < prevLayer.neurons; prevNeuron++) {
                double weight = layer.weights[nextNeuron][prevNeuron];
                //double delta = (1 - momentum) * learningRate * weight * error + momentum * nextLayer.delta[nextNeuron][prevNeuron];
                double weightChange = learningRate * error * prevLayer.outputs[prevNeuron];
                //System.out.println("hi"+prevNeuron+"next"+nextNeuron+"error"+nextLayer.error[nextNeuron]);
                //System.out.println("weightChange"+weightChange);
                layer.weights[nextNeuron][prevNeuron] += weightChange;
                //System.out.println("w"+layer.weights[nextNeuron][prevNeuron]+"delta"+weightChange);
                //nextLayer.delta[nextNeuron][prevNeuron]=delta;
            }
            layer.bias[nextNeuron] += learningRate * layer.error[nextNeuron];
        }
        //if(info>0)
        //  System.out.println(layer.name+"After layer update Wsum"+sumWeights(layer));
    }

    public double sumWeights(Layer layer) {
        double sum = 0;
        int prevLayerSize = layer.previousLayer.neurons;
        for (int neuron = 0; neuron < layer.neurons; neuron++)
            for (int prevNeuron = 0; prevNeuron < prevLayerSize; prevNeuron++) {
                sum += Math.abs(layer.weights[neuron][prevNeuron]);
            }
        return sum;
    }

    private static double sigmoid(double value) {
        return 1 / (1 + Math.pow(Math.E, -value));
    }

    //sigmoidDeriv(x) = sigmoid(x) * (1-sigmoid(x))
    private static double sigmoidDeriv(double value) {
        return (value - Math.pow(value, 2));
    }

    public void printWeights() {
        for (int layerNr = 1; layerNr < 3; layerNr++) {
            System.out.println("layer" + layerNr);
            Layer layer = layers.get(layerNr);
            for (int out = 0; out < layer.neurons; out++) {
                System.out.println("neuron" + out);
                double sum = 0;
                for (int input = 0; input < layer.previousLayer.neurons; input++) {
                    System.out.println("W" + layer.weights[out][input]);
                }
                System.out.println("Bias" + layer.bias[out]);
            }
        }
    }

    public double[][] getWeights(int layer) {
        return layers.get(layer).weights;
    }

    public void save(String filename) throws IOException {
        File file = new File(filename);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.writeInt(layers.get(0).neurons);
        Layer hiddenLayer = layers.get(1);
        out.writeInt(hiddenLayer.neurons);
        Layer outLayer = layers.get(2);
        out.writeInt(outLayer.neurons);
        writeLayer(hiddenLayer, out);
        writeLayer(outLayer, out);
        out.close();
    }

    public static MultiLayerFN load(String filename) throws IOException {
        File file = new File(filename);
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        return load(in);
    /*int inputs= in.readInt();
    int hiddens= in.readInt();
    int outputs= in.readInt();
    MultiLayerFN mlfn = new MultiLayerFN(inputs,hiddens,outputs);
    readLayer(mlfn.layers.get(1),in);
    readLayer(mlfn.layers.get(2),in);
    in.close();
    return mlfn;*/
    }

    public static MultiLayerFN load(DataInputStream in) throws IOException {
        int inputs = in.readInt();
        int hiddens = in.readInt();
        int outputs = in.readInt();
        MultiLayerFN mlfn = new MultiLayerFN(inputs, hiddens, outputs);
        readLayer(mlfn.layers.get(1), in);
        readLayer(mlfn.layers.get(2), in);
        in.close();
        return mlfn;
    }

    private static void writeLayer(Layer layer, DataOutputStream out) throws IOException {
        int prevLayerNeurons = layer.previousLayer.neurons;
        for (int neuron = 0; neuron < layer.neurons; neuron++) {
            for (int prevLayerNeuron = 0; prevLayerNeuron < prevLayerNeurons; prevLayerNeuron++)
                out.writeDouble(layer.weights[neuron][prevLayerNeuron]);
            out.writeDouble(layer.bias[neuron]);
        }
    }

    private static void readLayer(Layer layer, DataInputStream in) throws IOException {
        int prevLayerNeurons = layer.previousLayer.neurons;
        for (int neuron = 0; neuron < layer.neurons; neuron++) {
            for (int prevLayerNeuron = 0; prevLayerNeuron < prevLayerNeurons; prevLayerNeuron++)
                layer.weights[neuron][prevLayerNeuron] = in.readDouble();
            layer.bias[neuron] = in.readDouble();
        }
    }
}
