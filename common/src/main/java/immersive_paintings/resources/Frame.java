package immersive_paintings.resources;

import net.minecraft.util.Identifier;

import java.util.Objects;

public final class Frame {
    private final Identifier frame;
    private final boolean diagonals;
    private final Identifier material;

    Frame(Identifier frame, boolean diagonals, Identifier material) {
        this.frame = frame;
        this.diagonals = diagonals;
        this.material = material;
    }

    public Identifier frame() {
        return frame;
    }

    public boolean diagonals() {
        return diagonals;
    }

    public Identifier material() {
        return material;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Frame that = (Frame)obj;
        return Objects.equals(this.frame, that.frame) &&
                this.diagonals == that.diagonals &&
                Objects.equals(this.material, that.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frame, diagonals, material);
    }

    @Override
    public String toString() {
        return "Frame[" +
                "frame=" + frame + ", " +
                "diagonals=" + diagonals + ", " +
                "material=" + material + ']';
    }

}
