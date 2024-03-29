import cv2
import numpy as np
import os

files = []
for flip, group in enumerate(
    (
        (
            "acacia",
            "birch",
            "crimson",
            "dark_oak",
            "hyphae",
            "jungle",
            "oak",
            "spruce",
        ),
        (
            "calcite",
            "coal",
            "copper",
            "crimson",
            "dark_oak",
            "diamond",
            "emerald",
            "end_stone",
            "glowstone",
            "iron",
            "lapis",
            "netherite",
            "netherrack",
            "prismarine",
            "red_sandstone",
            "sandstone",
            "stone",
            "sugar_cane",
        ),
    )
):
    for directory in [
        ("textures", "../common/src/main/resources"),
        ("faithful", "../faithfulAddon"),
    ]:
        for material in group:
            files.append(
                {
                    "frame": "simple",
                    "material": material,
                    "dir": directory[1],
                    "texture": f"{directory[0]}/{material}.png",
                    "flip": flip == 0,
                    "offsetY": 6,
                    "brightness": [0.6, 0.8, 1.0, 1.0, 0.8],
                    "mirroring": [(2, 0, 1), (3, 0, 1)],
                    "edges": [(1, 2), (4, 3)],
                    "contrast": 1.4,
                }
            ),

            files.append(
                {
                    "frame": "vintage",
                    "material": material,
                    "dir": directory[1],
                    "texture": f"{directory[0]}/{material}.png",
                    "flip": flip == 0,
                    "offsetY": 5,
                    "brightness": np.asarray([0.6, 0.7, 0.8, 1.0, 1.0, 0.8, 0.7, 0.6])
                    * 0.9,
                    "mirroring": [(3, 0, 1), (4, 0, 1), (6, 2, 2)],
                    "edges": [(2, 3), (5, 4), (6, 7)],
                    "contrast": 1.6,
                    "saturation": 1.15,
                }
            )


def main():
    for f in files:
        im = cv2.imread(f["texture"])
        res = im.shape[0]
        factor = res // 16

        alpha = np.ones((res, res)) * 255

        im = im[:res, :res, :]

        # flip 90°
        if f["flip"]:
            im = np.swapaxes(im, 0, 1)

        # vertical offset
        im = np.roll(im, -f["offsetY"], axis=0)

        # blend edges
        blend = 0.75
        for toY, fromY in f["edges"]:
            im[int(res - 1 - toY * factor)] = im[
                int(res - 1 - fromY * factor)
            ] * blend + im[int(res - 1 - toY * factor)] * (1 - blend)

        im = cv2.cvtColor(im, cv2.COLOR_BGR2HSV)

        # per line brightness
        for y in range(res):
            c = (
                0
                if y // factor >= len(f["brightness"])
                else f["brightness"][y // factor]
            )
            im[res - 1 - y, :, 2] = np.ceil(im[res - 1 - y, :, 2] * c).astype(np.uint8)
            if c == 0:
                alpha[res - 1 - y, :] = 0

        # saturation
        if "saturation" in f:
            im[:, :, 1] = np.maximum(
                0, np.minimum(255, np.ceil(im[:, :, 1] * f["saturation"]))
            ).astype(np.uint8)

        # contrast
        if "contrast" in f:
            im[res - 1 - len(f["brightness"]) :, :, 2] = np.maximum(
                0,
                np.minimum(
                    255,
                    np.ceil(
                        (
                            im[res - 1 - len(f["brightness"]) :, :, 2]
                            - im[res - 1 - len(f["brightness"]) :, :, 2].mean()
                        )
                        * f["contrast"]
                        + im[res - 1 - len(f["brightness"]) :, :, 2].mean()
                    ),
                ),
            ).astype(np.uint8)

        im = cv2.cvtColor(im, cv2.COLOR_HSV2BGR)

        im = np.concatenate((im, alpha.reshape((res, res, 1))), axis=2).reshape(
            (res, res, 4)
        )

        # duplicate
        im = np.concatenate((im, im, im, im), axis=1)

        # mirroring required parts
        for y, fx, tx in f["mirroring"]:
            for x in range(fx * factor, (tx + 1) * factor):
                for yy in range(y * factor, (y + 1) * factor):
                    im[res - 1 - yy, x, 3] = 0
                    im[res - 1 - yy, res - 1 - x + res * 2] = 0
                    im[res - 1 - yy, x + res * 3] = 0
                    im[res - 1 - yy, res - 1 - x + res * 3] = 0

        os.makedirs(
            f"{f['dir']}/assets/immersive_paintings/textures/block/frame/{f['frame']}",
            exist_ok=True,
        )

        cv2.imwrite(
            f"{f['dir']}/assets/immersive_paintings/textures/block/frame/{f['frame']}/{f['material']}.png",
            im,
        )

        # icon
        im = cv2.imread(f["texture"])

        im = im[:res, :res, :]

        # vertical offset
        im = np.roll(im, -4, axis=0)

        # flip 90°
        if f["flip"]:
            im = np.swapaxes(im, 0, 1)

        im = cv2.cvtColor(im, cv2.COLOR_BGR2HSV)

        # saturation
        if "saturation" in f:
            im[:, :, 1] = np.maximum(
                0, np.minimum(255, np.ceil(im[:, :, 1] * f["saturation"]))
            ).astype(np.uint8)

        # contrast
        if "contrast" in f:
            im[res - 1 - len(f["brightness"]) :, :, 2] = np.maximum(
                0,
                np.minimum(
                    255,
                    np.ceil(
                        (
                            im[res - 1 - len(f["brightness"]) :, :, 2]
                            - im[res - 1 - len(f["brightness"]) :, :, 2].mean()
                        )
                        * f["contrast"]
                        + im[res - 1 - len(f["brightness"]) :, :, 2].mean()
                    ),
                ),
            ).astype(np.uint8)

        im = cv2.cvtColor(im, cv2.COLOR_HSV2BGR)

        # duplicate
        im = np.concatenate((im, im), axis=1)

        im = cv2.resize(im, (res * 4, res * 2), interpolation=cv2.INTER_NEAREST_EXACT)

        height = res

        mask = np.ones((res * 2, res * 4))

        def gen(yy):
            mask[yy + 1, : res * 4 - 2] = 1.25
            mask[yy + height - 3, 1:] = 0.5
            mask[yy + height - 2, 1:] = 0.5

            mask[yy : height - 3, 1] = 1.25
            mask[yy + 2 :, res * 4 - 2] = 0.5

            if yy > 0:
                mask[yy:] = 2 - mask[yy:]
                mask[yy:] *= 0.4

            mask[yy:, 0] = 0.25
            mask[yy + 0, :] = 0.25
            mask[yy + height - 1, :] = 0.25
            mask[yy:, res * 4 - 1] = 0.25

        gen(0)
        gen(height)

        mask = np.stack((mask, mask, mask), axis=2)
        im = np.maximum(0, np.minimum(255, im * mask)).astype(np.uint8)

        os.makedirs(
            f"{f['dir']}/assets/immersive_paintings/textures/gui/frame/{f['frame']}",
            exist_ok=True,
        )

        cv2.imwrite(
            f"{f['dir']}/assets/immersive_paintings/textures/gui/frame/{f['frame']}/{f['material']}.png",
            im,
        )

        os.makedirs(
            f"{f['dir']}/assets/immersive_paintings/frames/{f['frame']}",
            exist_ok=True,
        )

        # files
        with open(
            f"{f['dir']}/assets/immersive_paintings/frames/{f['frame']}/{f['material']}.json",
            "w",
        ) as file:
            file.write(
                f"""{{
  "frame": "immersive_paintings:objects/frame/{f['frame']}",
  "material": "immersive_paintings:textures/block/frame/{f['frame']}/{f['material']}.png"
}}"""
            )


if __name__ == "__main__":
    main()
