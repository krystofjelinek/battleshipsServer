package cz.vse.server;

public enum ShipShape {
    SIX_SHAPE(new int[][]{
            {1, 1, 1},
            {1, 1, 1}
    }),

    TWO_SHAPE(new int[][]{
        {1, 1}
    }),

    BLOCK_SHAPE(new int[][]{
        {1, 1},
        {1, 1},
    }),

    FOUR_SHAPE(new int[][]{
        {1, 1, 1, 1}
    });

    private final int[][] shape;

    ShipShape(int[][] shape) {
        this.shape = shape;
    }

    public int[][] getShape() {
        return shape;
    }
}
