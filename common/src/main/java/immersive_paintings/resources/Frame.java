package immersive_paintings.resources;

import net.minecraft.util.Identifier;

public record Frame(Identifier frame, boolean diagonals, Identifier material) {
}
