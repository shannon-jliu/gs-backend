package org.cuair.ground.util;

public class MatrixUtil {
  public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
    double[][] result = new double[firstMatrix.length][secondMatrix[0].length];

    for (int row = 0; row < result.length; row++) {
      for (int col = 0; col < result[row].length; col++) {
        result[row][col] = multiplyMatricesCell(firstMatrix, secondMatrix, row, col);
      }
    }

    return result;
  }

  static double multiplyMatricesCell(double[][] firstMatrix, double[][] secondMatrix, int row, int col) {
    double cell = 0;
    for (int i = 0; i < secondMatrix.length; i++) {
      cell += firstMatrix[row][i] * secondMatrix[i][col];
    }
    return cell;
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
