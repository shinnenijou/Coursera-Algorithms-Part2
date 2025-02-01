import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.util.*;


public class SeamCarver {
//    private class Direction{}
//    private class Y_COLUMN extends Direction{}
//    private class X_COLUMN extends Direction{}

    private static final boolean Y_COLUMN = true;
    private static final boolean X_COLUMN = false;
    private static final double BORDER_ENERGY = 1000;

    private static int extractR(int argb) {
        return (argb >> 16) & 0xFF;
    }

    private static int extractG(int argb) {
        return (argb >> 8) & 0xFF;
    }

    private static int extractB(int argb) {
        return argb & 0xFF;
    }

    private final Picture originPic;
    private Picture currentPic;

    private int dimension(boolean direction) {
        return direction == Y_COLUMN ? height() : width();
    }

    private void validateCoordinate(boolean direction, int index) {
        if (index < 0 || index >= dimension(direction)) {
            throw new IllegalArgumentException();
        }
    }

    private void validateSeam(boolean direction, int[] seam) {
        if (seam.length != dimension(direction)) {
            throw new IllegalArgumentException();
        }

        if (dimension(!direction) <= 1) {
            throw new IllegalArgumentException();
        }

        validateCoordinate(!direction, 0);

        for (int i = 1; i < seam.length; ++i) {
            validateCoordinate(!direction, i);

            if (Math.abs(seam[i] - seam[i - 1]) > 1) {
                throw new IllegalArgumentException();
            }
        }
    }

    private boolean onBorder(boolean direction, int index) {
        return index == 0 || index == dimension(direction) - 1;
    }

    private double energy(boolean direction, int pos, int level) {
        int x = direction == Y_COLUMN ? level : pos;
        int y = direction == Y_COLUMN ? pos : level;

        validateCoordinate(X_COLUMN, x);
        validateCoordinate(Y_COLUMN, y);

        if (onBorder(X_COLUMN, x) || onBorder(Y_COLUMN, y)) {
            return BORDER_ENERGY;
        }

        int argb1 = currentPic.getARGB(x + 1, y);
        int argb2 = currentPic.getARGB(x - 1, y);

        int rx = extractR(argb1) - extractR(argb2);
        int gx = extractG(argb1) - extractG(argb2);
        int bx = extractB(argb1) - extractB(argb2);

        double deltaX = rx * rx + gx * gx + bx * bx;

        argb1 = currentPic.getARGB(x, y + 1);
        argb2 = currentPic.getARGB(x, y - 1);

        int ry = extractR(argb1) - extractR(argb2);
        int gy = extractG(argb1) - extractG(argb2);
        int by = extractB(argb1) - extractB(argb2);

        double deltaY = ry * ry + gy * gy + by * by;

        return Math.sqrt(deltaX + deltaY);
    }

    private void relaxAdj(boolean direction, double[] distTo, int[] edgeTo, int pos, int posDimension, int level, int levelDimension) {
        for (int adj = pos - 1; adj <= pos + 1; ++adj) {
            if (adj < 0 || adj >= posDimension || level + 1 >= levelDimension) continue;

            // v -> w
            int v = level * width() + pos;
            int w = (level + 1) * width() + adj;
            double weight = energy(direction, adj, level + 1);

            if (distTo[v] + weight < distTo[w]) {
                distTo[w] = distTo[v] + weight;
                edgeTo[w] = v;
            }
        }
    }

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }

        originPic = picture;
        currentPic = picture;
    }

    // current picture
    public Picture picture() {
        return currentPic;
    }

    // width of current picture
    public int width() {
        return currentPic.width();
    }

    // height of current picture
    public int height() {
        return currentPic.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        return energy(X_COLUMN, x, y);
    }

    private int[] findSeam(boolean direction) {
        int levelDimension = dimension(direction);
        int posDimension = dimension(!direction);

        double[] distTo = new double[width() * height()];
        Arrays.fill(distTo, 0, posDimension, 0);
        Arrays.fill(distTo, posDimension, distTo.length, Double.POSITIVE_INFINITY);

        int[] edgeTo = new int[width() * height()];
        Arrays.fill(edgeTo, -1);

        for (int i = posDimension - 1; i >= 0; --i) {
            for (int level = 0, pos = i; level < levelDimension && pos < posDimension; ++level, ++pos) {
                relaxAdj(direction, distTo, edgeTo, pos, posDimension, level, levelDimension);
            }
        }

        for (int i = 1; i < levelDimension; ++i) {
            for (int level = i, pos = 0; level < levelDimension && pos < posDimension; ++level, ++pos) {
                relaxAdj(direction, distTo, edgeTo, pos, posDimension, level, levelDimension);
            }
        }

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for (int pos = 0; pos < posDimension; ++pos) {
            int current = (levelDimension - 1) * posDimension + pos;

            if (distTo[current] < min) {
                min = distTo[current];
                minIndex = current;
            }
        }

        int[] seam = new int[levelDimension];

        for (int level = levelDimension - 1; level >= 0; --level) {
            int pos = minIndex % posDimension;
            seam[level] = pos;
            minIndex = edgeTo[minIndex];
        }

        return seam;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return findSeam(Y_COLUMN);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeam(X_COLUMN);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        validateSeam(X_COLUMN, seam);

        Picture newPic = new Picture(width(), height() - 1);

        for (int x = 0; x < width(); ++x) {
            for (int y = 0; y < seam[x]; ++y) {
                newPic.setARGB(x, y, currentPic.getARGB(x, y));
            }

            for (int y = seam[x] + 1; y < height(); ++y) {
                newPic.setARGB(x, y - 1, currentPic.getARGB(x, y));
            }
        }

        currentPic = newPic;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        validateSeam(Y_COLUMN, seam);

        Picture newPic = new Picture(width() - 1, height());

        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < seam[y]; ++x) {
                newPic.setARGB(x, y, currentPic.getARGB(x, y));
            }

            for (int x = seam[y] + 1; x < width(); ++x) {
                newPic.setARGB(x - 1, y, currentPic.getARGB(x, y));
            }
        }

        currentPic = newPic;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Width: ").append(width()).append("\n");
        s.append("Height: ").append(height()).append("\n");

        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < width(); ++x) {
                s.append(String.format("%8.2f ", energy(x, y)));
            }
            s.append("\n");
        }

        return s.toString();
    }

    // unit testing (optional)
    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);

        SeamCarver seamCarver = new SeamCarver(picture);
        StdOut.println(seamCarver);

        for (int x : seamCarver.findVerticalSeam()) {
            StdOut.print(x + " ");
        }

        StdOut.println();

        for (int y : seamCarver.findHorizontalSeam()) {
            StdOut.print(y + " ");
        }

        StdOut.println();
    }
}