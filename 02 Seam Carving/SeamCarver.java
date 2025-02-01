import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.Arrays;

public class SeamCarver {
    private static final boolean VERTICAL = false;
    private static final boolean HORIZONTAL = true;
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

    private boolean status;
    private Picture currentPic;

    // energy cache
    double[] energies;

    // helper container to find seam
    double[] distTo;
    int[] edgeTo;

    private int toIndex(int x, int y) {
        return y * width() + x;
    }

    // maybe not need to transpose picture?
    // only transpose energies (distTo and edgeTo will be re-init during usage)
    private void transpose(boolean direction) {
        if (status == direction) return;

        Picture picture = new Picture(height(), width());

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                picture.setARGB(y, x, currentPic.getARGB(x, y));
            }
        }

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                int oldIndex = y * width() + x;
                int newIndex = x * height() + y;
                double temp = energies[oldIndex];
                energies[oldIndex] = energies[newIndex];
                energies[newIndex] = temp;
            }
        }

        status = direction;
        currentPic = picture;
    }

    private void validateSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("seam is null");
        }

        int height = height();
        int width = width();

        if (seam.length != height) {
            throw new IllegalArgumentException("seam.length out of bounds");
        }

        if (width <= 1) {
            throw new IllegalArgumentException("picture cannot be carved");
        }

        if (seam[0] < 0 || seam[0] >= width) throw new IllegalArgumentException("seam[0] out of bounds");

        for (int i = 1; i < seam.length; ++i) {
            if (seam[i] < 0 || seam[i] >= width) throw new IllegalArgumentException("seam[0] out of bounds");

            if (Math.abs(seam[i] - seam[i - 1]) > 1) {
                throw new IllegalArgumentException("seam out of adjacent pixels");
            }
        }
    }

    private void relaxAdj(int x, int y) {
        if (y >= height() - 1) return;
        int startX = Math.max(x - 1, 0);
        int endX = Math.min(x + 2, width());

        for (int adj = startX; adj < endX; ++adj) {
            // v -> w
            int v = toIndex(x, y);
            int w = toIndex(adj, y + 1);
            double weight = energy(adj, y + 1);

            if (distTo[v] + weight < distTo[w]) {
                distTo[w] = distTo[v] + weight;
                edgeTo[w] = v;
            }
        }
    }

    private void removeSeamEnergies(int[] seam) {
        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < width(); ++x) {
                if (x != seam[y]) continue;

                // up
                if (y > 0) {
                    energies[toIndex(x, y - 1)] = Double.POSITIVE_INFINITY;
                }

                // down
                if (y + 1 < height()) {
                    energies[toIndex(x, y + 1)] = Double.POSITIVE_INFINITY;
                }

                // left
                if (x > 0) {
                    energies[toIndex(x - 1, y)] = Double.POSITIVE_INFINITY;
                }

                // right
                if (y > 0) {
                    energies[toIndex(x + 1, y)] = Double.POSITIVE_INFINITY;
                }
            }
        }

        int step = 0;

        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < width(); ++x) {
                if (x == seam[y]) {
                    step++;
                    continue;
                }

                int index = toIndex(x, y);
                energies[index - step] = energies[index];
            }
        }
    }

    // suppose vertical seam
    private int[] findSeam() {
        int width = width();
        int height = height();

        Arrays.fill(distTo, 0, width, 0);
        Arrays.fill(distTo, width, distTo.length, Double.POSITIVE_INFINITY);
        Arrays.fill(edgeTo, -1);

        // topological order, which is constant in this problem
        // equivalent to dynamic programming
        for (int i = width - 1; i >= 0; --i) {
            for (int y = 0, x = i; y < height && x < width; ++y, ++x) {
                relaxAdj(x, y);
            }
        }

        for (int i = 1; i < height; ++i) {
            for (int y = i, x = 0; y < height && x < width; ++y, ++x) {
                relaxAdj(x, y);
            }
        }

        int minIndex = -1;
        double min = Double.POSITIVE_INFINITY;

        for (int index = toIndex(0, height - 1); index < distTo.length; ++index) {
            if (distTo[index] < min) {
                min = distTo[index];
                minIndex = index;
            }
        }

        int[] seam = new int[height];

        for (int y = height - 1; y >= 0; --y) {
            int x = minIndex % width;
            seam[y] = x;
            minIndex = edgeTo[minIndex];
        }

        return seam;
    }

    private void removeSeam(int[] seam) {
        Picture newPic = new Picture(width() - 1, height());

        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < seam[y]; ++x) {
                newPic.setARGB(x, y, currentPic.getARGB(x, y));
            }

            for (int x = seam[y] + 1; x < width(); ++x) {
                newPic.setARGB(x - 1, y, currentPic.getARGB(x, y));
            }
        }

        // re-init energies cache before replace picture
        // leaving changed energies Double.POSITIVE_INFINITE
        removeSeamEnergies(seam);
        currentPic = newPic;
    }

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }

        currentPic = new Picture(picture);
        status = VERTICAL;
        distTo = new double[width() * height()];
        edgeTo = new int[width() * height()];
        energies = new double[width() * height()];

        for (int x = 0; x < width(); ++x) {
            for (int y = 0; y < height(); ++y) {
                energy(x, y);
            }
        }
    }

    // current picture
    public Picture picture() {
        transpose(VERTICAL);
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
        if (x < 0 || x >= width()) {
            throw new IllegalArgumentException("x out of bounds, must be between 0 and " + (width() - 1));
        }

        if (y < 0 || y >= height()) {
            throw new IllegalArgumentException("y out of bounds, must be between 0 and " + (height() - 1));
        }

        int cacheIndex = toIndex(x, y);

        if (energies[cacheIndex] != Double.POSITIVE_INFINITY) {
            return energies[cacheIndex];
        }

        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) {
            energies[cacheIndex] = BORDER_ENERGY;
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
        energies[cacheIndex] = Math.sqrt(deltaX + deltaY);

        return energies[cacheIndex];
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transpose(HORIZONTAL);
        return findSeam();
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        transpose(VERTICAL);
        return findSeam();
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        validateSeam(seam);
        transpose(HORIZONTAL);
        removeSeam(seam);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validateSeam(seam);
        transpose(VERTICAL);
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

        Stopwatch stopwatch = new Stopwatch();

        for (int i = 0; i < picture.width() / 2; ++i) {
            seamCarver.removeVerticalSeam(seamCarver.findVerticalSeam());
        }

        StdOut.println("Elapsed time: " + stopwatch.elapsedTime());

        seamCarver.picture().show();
    }
}