package org.cuair.ground.util;

public class MatrixUtil {
  public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
    int r1 = firstMatrix.length;
    int c1 = secondMatrix.length;
    int c2 = secondMatrix[0].length;

    double[][] product = new double[r1][c2];
    for (int i = 0; i < r1; i++) {
      for (int j = 0; j < c2; j++) {
        for (int k = 0; k < c1; k++) {
          product[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
        }
      }
    }

    return product;
  }
  
  // only works for square matrices
  public static double[][] transpose(double[][] A) {
    for (int i = 0; i < A.length; i++)
      for (int j = i + 1; j < A[i].length; j++) {
        double temp = A[i][j];
        A[i][j] = A[j][i];
        A[j][i] = temp;
      }

    return A;
  }

  public static double[] scaleMultiplyVector(double[] vec, double scale) {
    for (int i = 0; i < vec.length; i++) {
      vec[i] = scale*vec[i];
    }
    return vec;
  }

  public static double[][] vecFromArray(double[] arr) {
    double[][] vec = new double[arr.length][1];
    for (int i = 0; i < arr.length; i++) {
      vec[i][0] = arr[i];
    }
    return vec;
  }

  public static double[] arrFromVec(double[][] vec) {
    double[] arr = new double[vec.length];
    for (int i = 0; i < vec.length; i++) {
      arr[i] = vec[i][0];
    }
    return arr;
  }
}
