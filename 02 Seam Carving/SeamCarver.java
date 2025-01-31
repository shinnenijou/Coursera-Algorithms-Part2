import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private static final boolean VERTICAL = true;
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

    private final Picture originPic;
    private Picture currentPic;

    private int dimension(boolean direction) {
        return direction == VERTICAL ? height() : width();
    }

    private void validateCoordinate(boolean direction, int x) {
        if (x < 0 || x >= dimension(direction)) {
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

    private boolean onBorder(boolean direction, int x) {
        return x == 0 || x == dimension(direction) - 1;
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
        validateCoordinate(HORIZONTAL, x);
        validateCoordinate(VERTICAL, y);

        if (onBorder(HORIZONTAL, x) || onBorder(VERTICAL, y)) {
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

        return Math.sqrt((double) deltaX + deltaY);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        assert false;
        return null;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        assert false;
        return null;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        validateSeam(HORIZONTAL, seam);
        assert false;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        validateSeam(VERTICAL, seam);
        assert false;
    }

    // unit testing (optional)
    public static void main(String[] args) {

    }
}