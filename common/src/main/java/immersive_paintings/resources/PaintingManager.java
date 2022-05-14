package immersive_paintings.resources;

import java.util.List;

public class PaintingManager {
    private static List<Paintings.PaintingData> paintings;

    public static List<Paintings.PaintingData> fetchAllPaintings() {
        return Paintings.paintings.values().stream().toList();
    }

    public static void receivePaintingList(List<Paintings.PaintingData> paintings) {
        PaintingManager.paintings = paintings;
    }

    public static List<Paintings.PaintingData> getPaintings() {
        return paintings;
    }
}
