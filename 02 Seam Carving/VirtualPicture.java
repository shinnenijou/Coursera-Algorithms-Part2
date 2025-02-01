import edu.princeton.cs.algs4.Picture;

import java.util.Arrays;

public class VirtualPicture {
    public static final boolean ORIGIN = false;
    public static final boolean TRANSPOSED = true;
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

    private final Picture picture;
    private final double[][] energies;
    private boolean status;
    private int width;
    private int height;

    public VirtualPicture(Picture picture) {
        this.picture = new Picture(picture);
        this.status = ORIGIN;
        this.width = picture.width();
        this.height = picture.height();
        this.energies = new double[picture.width()][picture.height()];

        for (int i = 0; i < picture.width(); i++) {
            Arrays.fill(energies[i], Double.POSITIVE_INFINITY);
        }
    }

    public boolean status() {
        return status;
    }

    // internal using width (considering transpose)
    public int width() {
        return status == ORIGIN ? width : height;
    }

    // internal using height (considering transpose)
    public int height() {
        return status == ORIGIN ? height : width;
    }

    // remove pixel by virtual coordinate(considering transpose)
    public void removePixel(int virtualCol, int virtualRow) {
        int realCol = status == ORIGIN ? virtualCol : virtualRow;
        int realRow = status == ORIGIN ? virtualRow : virtualCol;

        if (realCol > 0) {
            energies[realCol - 1][realRow] = Double.POSITIVE_INFINITY;
        }

        if (realCol + 1 < width) {
            energies[realCol + 1][realRow] = Double.POSITIVE_INFINITY;
        }

        if (realRow > 0) {
            energies[realCol][realRow - 1] = Double.POSITIVE_INFINITY;

        }

        if (realRow + 1 < height) {
            energies[realCol][realRow + 1] = Double.POSITIVE_INFINITY;

        }

        if (status == ORIGIN) {
            for (int x = realCol; x < width - 1; ++x) {
                picture.setARGB(x, realRow, picture.getARGB(x + 1, realRow));
                energies[x][realRow] = energies[x + 1][realRow];
            }
        } else {
            for (int y = realRow; y < height - 1; ++y) {
                picture.setARGB(realCol, y, picture.getARGB(realCol, y + 1));
                energies[realCol][y] = energies[realCol][y + 1];
            }
        }
    }

    public void transpose(boolean direction) {
        status = direction;
    }

    public void carve() {
        if (status == ORIGIN) {
            width--;
        } else {
            height--;
        }
    }

    // return real picture
    public Picture picture() {
        Picture pic = new Picture(width, height);

        for (int col = 0; col < pic.width(); col++) {
            for (int row = 0; row < pic.height(); row++) {
                pic.setARGB(col, row, picture.getARGB(col, row));
            }
        }

        return pic;
    }

    public double getEnergy(int virtualCol, int virtualRow) {
        int realCol = status == ORIGIN ? virtualCol : virtualRow;
        int realRow = status == ORIGIN ? virtualRow : virtualCol;

        if (energies[realCol][realRow] != Double.POSITIVE_INFINITY) {
            return energies[realCol][realRow];
        }

        if (realCol == 0 || realCol == width - 1 || realRow == 0 || realRow == height - 1) {
            energies[realCol][realRow] = BORDER_ENERGY;
            return energies[realCol][realRow];
        }

        int argb1 = picture.getARGB(realCol + 1, realRow);
        int argb2 = picture.getARGB(realCol - 1, realRow);

        int rx = extractR(argb1) - extractR(argb2);
        int gx = extractG(argb1) - extractG(argb2);
        int bx = extractB(argb1) - extractB(argb2);

        double deltaX = rx * rx + gx * gx + bx * bx;

        argb1 = picture.getARGB(realCol, realRow + 1);
        argb2 = picture.getARGB(realCol, realRow - 1);

        int ry = extractR(argb1) - extractR(argb2);
        int gy = extractG(argb1) - extractG(argb2);
        int by = extractB(argb1) - extractB(argb2);

        double deltaY = ry * ry + gy * gy + by * by;
        energies[realCol][realRow] = Math.sqrt(deltaX + deltaY);

        return energies[realCol][realRow];
    }
}
