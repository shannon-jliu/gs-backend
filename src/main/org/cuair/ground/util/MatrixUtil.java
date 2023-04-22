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

  public static double[][] transpose(double[][] A) {
    for (int i = 0; i < A.length; i++)
      for (int j = i + 1; j < A[i].length; j++) {
        double temp = A[i][j];
        A[i][j] = A[j][i];
        A[j][i] = temp;
      }

    return A;
  }
}
