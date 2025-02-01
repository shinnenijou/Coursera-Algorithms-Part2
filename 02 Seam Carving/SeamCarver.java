import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.Arrays;

public class SeamCarver {
    private VirtualPicture pic;

    private void validateSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("seam is null");
        }

        if (seam.length != pic.height()) {
            throw new IllegalArgumentException("seam.length out of bounds");
        }

        if (pic.width() <= 1) {
            throw new IllegalArgumentException("picture cannot be carved");
        }

        if (seam[0] < 0 || seam[0] >= pic.width()) throw new IllegalArgumentException("seam[0] out of bounds");

        for (int i = 1; i < seam.length; ++i) {
            if (seam[i] < 0 || seam[i] >= pic.width()) throw new IllegalArgumentException("seam[0] out of bounds");

            if (Math.abs(seam[i] - seam[i - 1]) > 1) {
                throw new IllegalArgumentException("seam out of adjacent pixels");
            }
        }
    }

    // virtual sight
    private void relaxAdj(double[] distTo, int[] edgeTo, int x, int y) {
        if (y >= pic.height() - 1) return;
        int virtualWidth = pic.width();
        int startX = Math.max(x - 1, 0);
        int endX = Math.min(x + 2, pic.width());

        for (int adj = startX; adj < endX; ++adj) {
            // v -> w
            int v = y * virtualWidth + x;
            int w = (y + 1) * virtualWidth + adj;
            double weight = pic.getEnergy(adj, y + 1);

            if (distTo[v] + weight < distTo[w]) {
                distTo[w] = distTo[v] + weight;
                edgeTo[w] = v;
            }
        }
    }

    // virtual sight
    private int[] findSeam() {
        int virtualWidth = pic.width();
        int virtualHeight = pic.height();

        double[] distTo = new double[virtualHeight * virtualWidth];
        Arrays.fill(distTo, 0, virtualWidth, 0);
        Arrays.fill(distTo, virtualWidth, distTo.length, Double.POSITIVE_INFINITY);

        int[] edgeTo = new int[virtualWidth * virtualHeight];
        Arrays.fill(edgeTo, 0, edgeTo.length, -1);

        // topological order, which is constant in this problem
        // equivalent to dynamic programming
        for (int i = virtualWidth - 1; i >= 0; --i) {
            for (int y = 0, x = i; y < virtualHeight && x < virtualWidth; ++y, ++x) {
                relaxAdj(distTo, edgeTo, x, y);
            }
        }

        for (int i = 1; i < virtualHeight; ++i) {
            for (int y = i, x = 0; y < virtualHeight && x < virtualWidth; ++y, ++x) {
                relaxAdj(distTo, edgeTo, x, y);
            }
        }

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for (int index = (virtualHeight - 1) * virtualWidth; index < distTo.length; ++index) {
            if (distTo[index] < min) {
                min = distTo[index];
                minIndex = index;
            }
        }

        int[] seam = new int[virtualHeight];

        for (int y = virtualHeight - 1; y >= 0; --y) {
            int x = minIndex % virtualWidth;
            seam[y] = x;
            minIndex = edgeTo[minIndex];
        }

        return seam;
    }

    private void removeSeam(int[] seam) {
        for (int y = 0; y < seam.length; ++y) {
            pic.removePixel(seam[y], y);
        }
        pic.carve();
    }

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }

        pic = new VirtualPicture(picture);
    }

    // current picture
    public Picture picture() {
        return pic.picture();
    }

    // width of current picture
    public int width() {
        return pic.status() == VirtualPicture.ORIGIN ? pic.width() : pic.height();
    }

    // height of current picture
    public int height() {
        return pic.status() == VirtualPicture.ORIGIN ? pic.height() : pic.width();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width()) {
            throw new IllegalArgumentException("x out of bounds, must be between 0 and " + (width() - 1));
        }

        if (y < 0 || y >= height()) {
            throw new IllegalArgumentException("y out of bounds, must be between 0 and " + (height() - 1));
        }

        int virtualCol = pic.status() == VirtualPicture.ORIGIN ? x : y;
        int virtualRow = pic.status() == VirtualPicture.ORIGIN ? y : x;
        return pic.getEnergy(virtualCol, virtualRow);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        pic.transpose(VirtualPicture.TRANSPOSED);
        return findSeam();
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        pic.transpose(VirtualPicture.ORIGIN);
        return findSeam();
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        // must validate before transpose since given seam throw exception
        pic.transpose(VirtualPicture.TRANSPOSED);
        validateSeam(seam);
        removeSeam(seam);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        // must validate before transpose since given seam throw exception
        pic.transpose(VirtualPicture.ORIGIN);
        validateSeam(seam);
        removeSeam(seam);
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
        StdOut.println("------");
        StdOut.println("Width: " + seamCarver.width());
        StdOut.println("Height: " + seamCarver.height());

        int verticalCount = picture.width() / 2;
        int horizontalCount = picture.height() / 2;

        StdOut.println("Do vertical(decrease width): " + verticalCount);
        StdOut.println("Do horizontal(decrease height): " + horizontalCount);

        Stopwatch stopwatch = new Stopwatch();

        int i = 0; // vertical seam
        int j = 0; // horizontal seam
        while (i < verticalCount && j < horizontalCount) {
            if (StdRandom.bernoulli((double) (verticalCount - i) / (verticalCount + horizontalCount - i - j))) {
                seamCarver.removeVerticalSeam(seamCarver.findVerticalSeam());
                i++;
            } else {
                seamCarver.removeHorizontalSeam(seamCarver.findHorizontalSeam());
                j++;
            }

            assert seamCarver.width() + i == picture.width();
            assert seamCarver.height() + j == picture.height();
        }

        while (i < verticalCount) {
            seamCarver.removeVerticalSeam(seamCarver.findVerticalSeam());
            i++;
        }

        while (j < horizontalCount) {
            seamCarver.removeHorizontalSeam(seamCarver.findHorizontalSeam());
            j++;
        }

        StdOut.println("Elapsed time: " + stopwatch.elapsedTime());
        StdOut.println("Width: " + seamCarver.width());
        StdOut.println("Height: " + seamCarver.height());

        seamCarver.picture().show();
    }
}