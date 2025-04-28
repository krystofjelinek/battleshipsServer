package cz.vse.server;

public enum ShipShape {
    L_SHAPE(new int[][]{
            {1, 0},
            {1, 0},
            {1, 1}
    }),
    T_SHAPE(new int[][]{
            {1, 1, 1},
            {0, 1, 0}
    });

    private final int[][] shape;

    ShipShape(int[][] shape) {
        this.shape = shape;
    }

    public int[][] getShape() {
        return shape;
    }
}
