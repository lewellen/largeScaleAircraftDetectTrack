package org.lewellen.lsadt.detection;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.lewellen.lsadt.Resource;

public class ComplexMatrix implements Serializable  {
	private static final long serialVersionUID = 5791972157543162613L;

	private final Complex[][] A;
    private final int columns;
    private final int rows;
    
	public static ComplexMatrix fromImage(String pngFilePath) {
		BufferedImage bufferedImage;
		try {
			Resource resource = new Resource();
			bufferedImage = ImageIO.read(resource.getFile(pngFilePath));
		} catch (IOException e) {
			return null;
		}

		ComplexMatrix matrix = new ComplexMatrix(bufferedImage.getHeight(), bufferedImage.getWidth());
		
		for(int x = 0; x < bufferedImage.getWidth(); x++)
			for(int y = 0; y < bufferedImage.getHeight(); y++) {
				int rgb = bufferedImage.getRGB(x, y);
				int r = (rgb & 0xffffff) >> 16;
				int g = (rgb & 0xffff) >> 8;
				int b = rgb & 0xff;
				
				if(r == 0 && g == 0 && b == 0)
					continue;
				
				double f = Math.sqrt(r * r + g * g + b * b) / 255.0 / Math.sqrt(3.0);
				
				matrix.set(y, x, new Complex( f, 0 ));
			}
		
		return matrix;
	}
	
    public ComplexMatrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        this.A = new Complex[rows][columns];
    	for(int i = 0; i < getRows(); i++)
    		for(int j = 0; j < getColumns(); j++)
    			A[i][j] = Complex.Zero;
    }

    public Complex get(int i, int j) {
    	return A[i][j];
    }
    

    public int getColumns() {
    	return columns;
    }

    public int getRows() {
    	return rows;
    }

    public ComplexMatrix hadamardProduct(ComplexMatrix B) {
        if (getColumns() != B.getColumns() || getRows() != B.getRows())
            throw new IllegalArgumentException("Rows and columns of this matrix and B must be the same.");

        ComplexMatrix C = new ComplexMatrix(getRows(), getColumns());
        for(int i = 0; i < C.getRows(); i++)
        	for(int j = 0; j < C.getColumns(); j++)
        		C.set(i,j, get(i,j).multiply(B.get(i,j)));
        
        return C;
    }

    public ComplexMatrix multiply(ComplexMatrix B) {
        ComplexMatrix C = new ComplexMatrix(getColumns(), B.getRows());

        for (int aColBRow = 0; aColBRow < getRows(); aColBRow++) {
            for (int aRow = 0; aRow < getColumns(); aRow++)
                for (int bCol = 0; bCol < getRows(); bCol++)
                	C.set(aRow, bCol, 
            			C.get(aRow, bCol).add(
        					get(aRow, aColBRow).multiply(B.get(aColBRow, bCol))
    					)
        			);
                	
        }

        return C;
    }

    public ComplexMatrix resize(int rows, int columns) {
		ComplexMatrix B = new ComplexMatrix(rows, columns);
		
		for(int i = 0; i < Math.min(B.getRows(), getRows()); i++)
			for(int j = 0; j < Math.min(B.getColumns(), getColumns()); j++)
				B.set(i, j, get(i, j));
				
		return B;
    }
    
    public void set(int i, int j, Complex x) { 
    	A[i][j] = x;    	
    }
    
    public double[][] RealCentered() {
        double[][] A = new double[getRows()][getColumns()];
        
        for(int column = 0; column < getColumns(); column++) {
        	for(int row = 0; row < getRows(); row++) {
        		int actRow = (row + (getRows() / 2)) % getRows();
        		int actCol = (column + (getColumns() / 2)) % getColumns();
        		
        		A[actRow][actCol] = get(row,  column).Real;
        	}
        }
        
        return A;
    }
}