package net.conczin.immersive_paintings.client.gui;

import net.conczin.immersive_paintings.Config;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.widget.*;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.c2s.*;
import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.resources.FrameLoader;
import net.conczin.immersive_paintings.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ImmersivePaintingScreen extends Screen {
    private static final int SCREENSHOTS_PER_PAGE = 5;

    final int minResolution;
    final int maxResolution;
    final boolean showOtherPlayersPaintings;
    final int uploadPermissionLevel;

    public final ImmersivePaintingEntity entity;

    private String filteredString = "";
    private int filteredResolution = 0;
    private int filteredWidth = 0;
    private int filteredHeight = 0;
    private final List<ResourceLocation> filteredPaintings = new ArrayList<>();

    private int selectionPage;
    private Page page;

    private Button pageWidget;

    private final Map<ResourceLocation, PaintingWidget> paintingWidgets = new HashMap<>();
    private BufferedImage currentImage;
    private static int currentImagePixelZoomCache = -1;
    private String currentImageName;
    private PixelatorSettings settings;
    private BufferedImage pixelatedImage;

    private List<File> screenshots = List.of();
    private int screenshotPage;

    private ResourceLocation deletePainting;
    private Component error;
    private boolean shouldReProcess;
    private static volatile boolean shouldUpload;

    private final static ExecutorService service = Executors.newFixedThreadPool(1);

    public ImmersivePaintingScreen(int entityId, int minResolution, int maxResolution, boolean showOtherPlayersPaintings, int uploadPermissionLevel) {
        super(Component.translatable("item.immersive_paintings.painting"));

        this.minResolution = minResolution;
        this.maxResolution = maxResolution;
        this.showOtherPlayersPaintings = showOtherPlayersPaintings || Config.getInstance().showOtherPlayersPaintings; // Prefer server, fall back to client if they mismatch
        this.uploadPermissionLevel = uploadPermissionLevel;

        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(entityId) instanceof ImmersivePaintingEntity painting) {
            entity = painting;
        } else {
            entity = null;
        }

        if (entity == null) {
            onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        if (page == null) {
            setPage(Page.DATAPACKS);
        } else {
            refreshPage();
        }

        //reload screenshots
        File file = new File(Minecraft.getInstance().gameDirectory, "screenshots");
        File[] files = file.listFiles(v -> v.getName().endsWith(".png"));
        if (files != null) {
            screenshots = Arrays.stream(files).toList();
        }
    }

    private void clearSearch() {
        filteredString = "";
        filteredResolution = 0;
        filteredWidth = 0;
        filteredHeight = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        switch (page) {
            case NEW -> {
                graphics.fill(width / 2 - 115, height / 2 - 68, width / 2 + 115, height / 2 - 41, 0x50000000);
                List<Component> wrap = wrap(Component.translatable("immersive_paintings.drop"), 220);
                int y = height / 2 - 40 - wrap.size() * 12;
                for (Component text : wrap) {
                    graphics.drawCenteredString(font, text, width / 2, y, 0xFFFFFFFF);
                    y += 12;
                }
            }
            case CREATE -> {
                if (shouldReProcess && currentImage != null) {
                    service.submit(() -> {
                        pixelatedImage = pixelateImage(currentImage, settings);
                        shouldUpload = true;
                    });
                    shouldReProcess = false;
                }

                if (shouldUpload && pixelatedImage != null) {
                    Minecraft.getInstance().getTextureManager().register(Main.locate("temp_pixelated"), new DynamicTexture(ImageManipulations.bufferedToNative(pixelatedImage)));
                }

                int maxWidth = 190;
                int maxHeight = 135;
                int tw = settings.resolution * settings.width;
                int th = settings.resolution * settings.height;
                float size = Math.min((float) maxWidth / tw, (float) maxHeight / th);
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.translate(width / 2.0f - tw * size / 2.0f, height / 2.0f - th * size / 2.0f, 0.0f);
                poseStack.scale(size, size, 1.0f);
                graphics.blit(Main.locate("temp_pixelated"), 0, 0, 0, 0, tw, th, tw, th);
                poseStack.popPose();

                if (error != null) {
                    graphics.drawCenteredString(font, error, width / 2, height / 2, 0xFFFF0000);
                }
            }
            case DELETE -> {
                graphics.fill(width / 2 - 160, height / 2 - 50, width / 2 + 160, height / 2 + 50, 0x88000000);
                List<Component> wrap = wrap(Component.translatable("immersive_paintings.confirm_deletion"), 300);
                int y = height / 2 - 35;
                for (Component t : wrap) {
                    graphics.drawCenteredString(font, t, width / 2, y, 0XFFFFFF);
                    y += 15;
                }
            }
            case ADMIN_DELETE -> {
                graphics.fill(width / 2 - 160, height / 2 - 50, width / 2 + 160, height / 2 + 50, 0x88000000);
                List<Component> wrap = wrap(Component.translatable("immersive_paintings.confirm_admin_deletion"), 300);
                int y = height / 2 - 35;
                for (Component t : wrap) {
                    graphics.drawCenteredString(font, t, width / 2, y, 0XFFFFFF);
                    y += 15;
                }
            }
            case LOADING -> {
                Component text = Component.translatable("immersive_paintings.upload", (int) Math.ceil(LazyNetworkManager.getRemainingTime()));
                graphics.drawCenteredString(font, text, width / 2, height / 2, 0xFFFFFFFF);
            }
        }
    }

    private List<ResourceLocation> getMaterialsList(ResourceLocation frame) {
        return FrameLoader.frames.values().stream()
        .filter(v -> v.frame().equals(frame))
        .map(FrameLoader.Frame::material)
        .distinct()
        .sorted(ResourceLocation::compareTo)
        .toList();
    }

    private void rebuild() {
        clearWidgets();

        // filters
        if (page != Page.CREATE) {
            List<Page> b = new LinkedList<>();
            b.add(Page.YOURS);
            b.add(Page.DATAPACKS);
            if (showOtherPlayersPaintings || isOp()) {
                b.add(Page.PLAYERS);
            }
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.hasPermissions(uploadPermissionLevel)) {
                b.add(Page.NEW);
            }
            if (!entity.isGraffiti()) {
                b.add(Page.FRAME);
            }

            int x = width / 2 - 200;
            int w = 400 / b.size();
            for (Page page : b) {
                Button btn = addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.page." + page.name().toLowerCase(Locale.ROOT)), sender -> setPage(page))
                    .bounds(x, height / 2 - 90 - 22, w, 20)
                    .build()
                );
                btn.active = page != this.page;
                x += w;
            }
        }

        switch (page) {
            case NEW -> {
                //URL
                EditBox editBox = addRenderableWidget(new EditBox(font, width / 2 - 90, height / 2 - 38, 180, 16,
                        Component.literal("URL")));
                editBox.setMaxLength(1024);

                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.load"), sender -> loadImage(editBox.getValue()))
                    .bounds(width / 2 - 50, height / 2 - 15, 100, 20)
                    .build()
                );

                //screenshots
                rebuildScreenshots();

                //screenshot page
                addRenderableWidget(Button.builder(
                    Component.literal("<<"), sender -> setScreenshotPage(screenshotPage - 1))
                    .bounds(width / 2 - 65, height / 2 + 70, 30, 20)
                    .build()
                );

                pageWidget = addRenderableWidget(Button.builder(
                    Component.literal(""), sender -> {})
                    .bounds(width / 2 - 65 + 30, height / 2 + 70, 70, 20)
                    .build()
                );

                addRenderableWidget(Button.builder(
                    Component.literal(">>"), sender -> setScreenshotPage(screenshotPage + 1))
                    .bounds(width / 2 - 65 + 100, height / 2 + 70, 30, 20)
                    .build()
                );
                setScreenshotPage(screenshotPage);
            }
            case CREATE -> {
                // Name
                EditBox editBox = addRenderableWidget(new EditBox(font, width / 2 - 90, height / 2 - 100, 180, 20,
                        Component.translatable("immersive_paintings.name")));
                editBox.setMaxLength(256);
                editBox.setValue(currentImageName);
                editBox.setResponder(s -> currentImageName = s);

                int y = height / 2 - 60;

                // Width
                addRenderableWidget(new IntegerSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.width", settings.width, 1, 16, v -> {
                    settings.width = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Height
                addRenderableWidget(new IntegerSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.height", settings.height, 1, 16, v -> {
                    settings.height = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Resolution
                int x = width / 2 - 200;

                Button resolutionWidget = addRenderableWidget(Button
                        .builder(Component.literal(String.valueOf(settings.resolution)), sender -> {})
                        .pos(x + 25, y)
                        .size(50, 20)
                        .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.resolution")))
                        .build()
                );

                addRenderableWidget(Button
                        .builder(Component.literal("<"), sender -> {
                            settings.resolution = Math.max(minResolution, settings.resolution / 2);
                            if (settings.pixelArt) {
                                adaptToPixelArt();
                                refreshPage();
                            }
                            shouldReProcess = true;
                            resolutionWidget.setMessage(Component.literal(String.valueOf(settings.resolution)));
                        })
                        .pos(x, y)
                        .size(25, 20)
                        .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.resolution")))
                        .build()
                );

                addRenderableWidget(Button
                        .builder(Component.literal(">"), sender -> {
                            settings.resolution = Math.min(maxResolution, settings.resolution * 2);
                            if (settings.pixelArt) {
                                adaptToPixelArt();
                                refreshPage();
                            }
                            shouldReProcess = true;
                            resolutionWidget.setMessage(Component.literal(String.valueOf(settings.resolution)));
                        })
                        .pos(x + 75, y)
                        .size(25, 20)
                        .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.resolution")))
                        .build()
                );

                y += 22;
                y += 10;

                // Color reduction
                addRenderableWidget(new IntegerSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.colors", settings.colors, 2, 25, v -> {
                    settings.colors = v;
                    shouldReProcess = true;
                })).active = !settings.pixelArt;
                y += 22;

                // Dither
                addRenderableWidget(new PercentageSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.dither", settings.dither, v -> {
                    settings.dither = v;
                    shouldReProcess = true;
                })).active = !settings.pixelArt;

                // PixelArt
                y = height / 2 - 50;
                addRenderableWidget(Checkbox
                        .builder(Component.translatable("immersive_paintings.pixelart"), font)
                        .pos(width / 2 + 100, y)
                        .selected(settings.pixelArt)
                        .tooltip(Tooltip.create(Component.translatable("immersive_paintings.pixelart.tooltip")))
                        .onValueChange((w, v) -> {
                            settings.pixelArt = v;
                            adaptToPixelArt();
                            refreshPage();
                            shouldReProcess = true;
                        })
                        .build()
                );
                y += 22;

                // Hide
                addRenderableWidget(Checkbox
                        .builder(Component.translatable("immersive_paintings.hide"), font)
                        .pos(width / 2 + 100, y)
                        .selected(settings.hidden)
                        .tooltip(Tooltip.create(Component.translatable("immersive_paintings.visibility")))
                        .onValueChange((w, v) -> settings.hidden = !settings.hidden)
                        .build()
                );
                y += 22;

                // NSFW
                addRenderableWidget(Checkbox
                        .builder(Component.translatable("immersive_paintings.nsfw"), font)
                        .pos(width / 2 + 100, y)
                        .selected(settings.nsfw)
                        .tooltip(Tooltip.create(Component.translatable("immersive_paintings.nsfw.tooltip")))
                        .onValueChange((w, v) -> settings.nsfw = !settings.nsfw)
                        .build()
                );
                y += 22;

                // Offset X
                addRenderableWidget(new PercentageSliderWidget(width / 2 + 100, y, 100, 20, "immersive_paintings.x_offset", settings.offsetX, v -> {
                    settings.offsetX = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Offset Y
                addRenderableWidget(new PercentageSliderWidget(width / 2 + 100, y, 100, 20, "immersive_paintings.y_offset", settings.offsetY, v -> {
                    settings.offsetY = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Offset
                addRenderableWidget(new PercentageSliderWidget(width / 2 + 100, y, 100, 20, "immersive_paintings.zoom", settings.zoom, entity.isGraffiti() ? 0.5 : 1.0, entity.isGraffiti() ? 1.5 : 3.0, v -> {
                    settings.zoom = v;
                    shouldReProcess = true;
                })).active = !settings.pixelArt;

                // Cancel
                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.cancel"), v -> setPage(Page.NEW))
                    .bounds(width / 2 - 85, height / 2 + 75, 80, 20)
                    .build()
                );

                // Save
                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.save"), v -> {
                        ImageManipulations.processByteArrayInChunks(ImageManipulations.encode(pixelatedImage), (ints, split, splits) -> LazyNetworkManager.sendToServer(new ImageUploadPayload(currentImageName, ints, split, splits)));

                        EnumSet<Painting.Flag> flags = settings.getFlags();
                        if (entity.isGraffiti())
                            flags.add(Painting.Flag.GRAFFITI);

                        // Using LazyNetworkManager here guarantees the register request won't arrive before the image is uploaded
                        LazyNetworkManager.sendToServer(new PaintingRegisterPayload(
                            settings.width,
                            settings.height,
                            settings.resolution,
                            currentImageName,
                            flags
                        ));

                        setPage(Page.LOADING);
                    })
                    .bounds(width / 2 + 5, height / 2 + 75, 80, 20)
                    .build()
                );
            }
            case YOURS, DATAPACKS, PLAYERS -> {
                rebuildPaintings();

                // page
                addRenderableWidget(Button.builder(
                    Component.literal("<<"), sender -> setSelectionPage(selectionPage - 1))
                    .bounds(width / 2 - 35 - 30, height / 2 + 80, 30, 20)
                    .build()
                );

                pageWidget = addRenderableWidget(Button.builder(
                    Component.literal(""), sender -> {})
                    .bounds(width / 2 - 35, height / 2 + 80, 70, 20)
                    .build()
                );

                addRenderableWidget(Button.builder(
                    Component.literal(">>"), sender -> setSelectionPage(selectionPage + 1))
                    .bounds(width / 2 + 35, height / 2 + 80, 30, 20)
                    .build()
                );

                setSelectionPage(selectionPage);

                //search
                EditBox editBox = addRenderableWidget(new EditBox(font, width / 2 - 65, height / 2 - 88, 130, 16, Component.translatable("immersive_paintings.search")));
                editBox.setMaxLength(64);
                editBox.setSuggestion("search");
                editBox.setResponder(s -> {
                    filteredString = s;
                    updateSearch();
                    editBox.setSuggestion(null);
                });

                int x = width / 2 - 200 + 12;

                Button widget = addRenderableWidget(Button
                    .builder(Component.literal(String.valueOf(filteredResolution)), sender -> {})
                    .pos(x + 50 + 8, height / 2 - 90)
                    .size(25, 20)
                    .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.filter_resolution")))
                    .build()
                );

                Button allWidget = addRenderableWidget(Button
                    .builder(Component.translatable("immersive_paintings.filter.all"), sender -> {
                        filteredResolution = 0;
                        updateSearch();
                        widget.setMessage(Component.literal(String.valueOf(filteredResolution)));
                        sender.active = false;
                    })
                    .pos(x, height / 2 - 90)
                    .size(25, 20)
                    .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.filter_resolution")))
                    .build()
                );

                addRenderableWidget(Button
                    .builder(Component.literal("<"), sender -> {
                        filteredResolution = filteredResolution == 0 ? 32 : Math.max(minResolution, filteredResolution / 2);
                        updateSearch();
                        widget.setMessage(Component.literal(String.valueOf(filteredResolution)));
                        allWidget.active = true;
                    })
                    .pos(x + 25 + 8, height / 2 - 90)
                    .size(25, 20)
                    .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.filter_resolution")))
                    .build()
                );

                addRenderableWidget(Button
                    .builder(Component.literal(">"), sender -> {
                        filteredResolution = filteredResolution == 0 ? 32 : Math.min(maxResolution, filteredResolution * 2);
                        updateSearch();
                        widget.setMessage(Component.literal(String.valueOf(filteredResolution)));
                        allWidget.active = true;
                    })
                    .pos(x + 75 + 8, height / 2 - 90)
                    .size(25, 20)
                    .tooltip(Tooltip.create(Component.translatable("immersive_paintings.tooltip.filter_resolution")))
                    .build()
                );

                //width
                EditBox widthWidget = addRenderableWidget(new EditBox(font, width / 2 + 80, height / 2 - 88, 40, 16, Component.translatable("immersive_paintings.filter_width")));
                widthWidget.setMaxLength(2);
                widthWidget.setSuggestion("width");
                widthWidget.setResponder(s -> {
                    try {
                        filteredWidth = Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        filteredWidth = 0;
                    }
                    updateSearch();
                    widthWidget.setSuggestion(null);
                });

                //height
                EditBox heightWidget = addRenderableWidget(new EditBox(font, width / 2 + 80 + 40, height / 2 - 88, 40, 16, Component.translatable("immersive_paintings.filter_height")));
                heightWidget.setMaxLength(2);
                heightWidget.setSuggestion("height");
                heightWidget.setResponder(s -> {
                    try {
                        filteredHeight = Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        filteredHeight = 0;
                    }
                    updateSearch();
                    heightWidget.setSuggestion(null);
                });
            }
            case FRAME -> {
                //frame
                int y = height / 2 - 80;
                List<ResourceLocation> frames = FrameLoader.frames.values().stream().map(FrameLoader.Frame::frame).distinct().sorted(ResourceLocation::compareTo).toList();
                for (ResourceLocation frame : frames) {
                    Button widget = addRenderableWidget(Button.builder(
                        Component.translatable("immersive_paintings.frame." + identifierToTranslation(frame)), v -> {
                            ResourceLocation material = getMaterialsList(frame).getFirst();

                            // TODO
                            // This is needed so that when the GUI updates it has the right frame and material set
                            // I don't like having to set it here, there should be a better way
                            entity.setFrame(frame);
                            entity.setMaterial(material);
                            NetworkHandler.Client.sendToServer(new PaintingEditPayload(entity.getId(), entity.getMotive(), frame, material));
                            setPage(Page.FRAME);
                        })
                        .bounds(width / 2 - 200, y, 100, 20)
                        .build()
                    );
                    widget.active = !frame.equals(entity.getFrame());
                    y += 25;
                }

                //material
                int py = 0;
                int px = 0;
                List<ResourceLocation> materials = getMaterialsList(entity.getFrame());
                List<Button> materialList = new LinkedList<>();
                for (ResourceLocation material : materials) {
                    Button widget = addRenderableWidget(new TexturedButtonWidget(
                        width / 2 - 80 + px * 65, height / 2 - 80 + py * 20, 64, 16,
                        ResourceLocation.fromNamespaceAndPath(material.getNamespace(), material.getPath().replace("/block/", "/gui/")),
                        64, 32,
                        Component.literal(""),
                        v -> {
                            entity.setMaterial(material);
                            NetworkHandler.Client.sendToServer(new PaintingEditPayload(entity.getId(), entity.getMotive(), entity.getFrame(), material));
                            materialList.forEach(b -> b.active = true);
                            v.active = false;
                        }
                    ));
                    Tooltip paintingTooltip = Tooltip.create(Component.translatable("immersive_paintings.material." + identifierToTranslation(material)));
                    widget.setTooltip(paintingTooltip);
                    
                    widget.active = !material.equals(entity.getMaterial());
                    materialList.add(widget);

                    px++;
                    if (px > 3) {
                        px = 0;
                        py++;
                    }
                }

                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.done"), v -> onClose())
                    .bounds(width / 2 - 50, height / 2 + 70, 100, 20)
                    .build()
                );
            }
            case DELETE -> {
                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.cancel"), v -> setPage(Page.YOURS))
                    .bounds(width / 2 - 100 - 5, height / 2 + 20, 100, 20)
                    .build()
                );

                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.delete"), v -> {
                        NetworkHandler.Client.sendToServer(new PaintingDeletePayload(deletePainting, false));
                        setPage(Page.YOURS);
                    })
                    .bounds(width / 2 + 5, height / 2 + 20, 100, 20)
                    .build()
                );
            }
            case ADMIN_DELETE -> {
                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.cancel"), v -> setPage(Page.PLAYERS))
                    .bounds(width / 2 - 115, height / 2 + 10, 70, 20)
                    .build()
                );

                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.delete"), v -> {
                        NetworkHandler.Client.sendToServer(new PaintingDeletePayload(deletePainting, false));
                        setPage(Page.PLAYERS);
                    })
                    .bounds(width / 2 - 40, height / 2 + 10, 70, 20)
                    .build()
                );

                addRenderableWidget(Button.builder(
                    Component.translatable("immersive_paintings.delete_all"), v -> {
                        NetworkHandler.Client.sendToServer(new PaintingDeletePayload(deletePainting, true));
                        setPage(Page.PLAYERS);
                    })
                    .bounds(width / 2 + 35, height / 2 + 10, 70, 20)
                    .build()
                );
            }
        }
    }

    public void updateWidget(ResourceLocation identifier) {
        if (paintingWidgets.containsKey(identifier)) {
            ClientPaintingManager.getPainting(identifier).ifPresent(p -> paintingWidgets.get(identifier).update(ClientPaintingManager.getImageIdentifier(identifier, Painting.Size.THUMBNAIL), p.width(), p.height()));
        }
    }

    public static String identifierToTranslation(ResourceLocation location) {
        String s = location.getPath();
        String lastSplit = s.substring(s.lastIndexOf("/") + 1);

        int i = lastSplit.lastIndexOf(".");
        return i <  0 ? lastSplit : lastSplit.substring(0, i);
    }

    public static List<Component> wrap(Component text, int maxWidth) {
        return Minecraft.getInstance().font.getSplitter().splitLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
            MutableComponent compiled = Component.literal("");
            line.visit((s, t) -> {
                compiled.append(Component.literal(t).setStyle(s));
                return Optional.empty();
            }, text.getStyle());
            return compiled;
        }).collect(Collectors.toList());
    }

    private static Component consolidate(List<Component> textList) {
        if (textList == null)
            return null;

        Component base = Component.empty();
        MutableComponent lastTextNode = base.copy();

        if (textList.isEmpty())
            return base;

        for (int i = 0; i < textList.size() - 1; i++) {
            Component text = textList.get(i);
            lastTextNode = lastTextNode.append(text).append("\n");
        }

        Component finalElement = textList.getLast();
        return lastTextNode.append(finalElement);
    }

    private void rebuildPaintings() {
        paintingWidgets.forEach((id, widget) -> removeWidget(widget));
        paintingWidgets.clear();

        // paintings
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                int i = y * 8 + x + selectionPage * 24;
                if (i >= 0 && i < filteredPaintings.size()) {
                    ResourceLocation identifier = filteredPaintings.get(i);

                    //tooltip
                    List<Component> tooltip = new LinkedList<>();

                    ClientPaintingManager.getPainting(identifier).ifPresent(p -> {
                        tooltip.add(Component.literal(p.name()));
                        tooltip.add(Component.translatable("immersive_paintings.by_author", p.author()).withStyle(ChatFormatting.ITALIC));
                        tooltip.add(Component.translatable("immersive_paintings.resolution", p.width(), p.height(), p.resolution()).withStyle(ChatFormatting.ITALIC));

                        if (page == Page.YOURS && p.has(Painting.Flag.HIDDEN)) {
                            tooltip.add(Component.translatable("immersive_paintings.hidden").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                        }

                        if (page == Page.YOURS && p.has(Painting.Flag.NSFW)) {
                            tooltip.add(Component.translatable("immersive_paintings.nsfw").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                        }

                        if (page == Page.YOURS || page == Page.PLAYERS && isOp()) {
                            tooltip.add(Component.translatable("immersive_paintings.right_click_to_delete").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                        }
                    });


                    PaintingWidget paintingWidget = addRenderableWidget(new PaintingWidget(
                        (int) (width / 2 + (x - 3.5) * 48) - 24, height / 2 - 66 + y * 48, 46, 46,
                        sender -> {
                            NetworkHandler.Client.sendToServer(new PaintingEditPayload(entity.getId(), identifier, entity.getFrame(), entity.getMaterial()));
                            if (entity.isGraffiti()) {
                                onClose();
                            } else {
                                setPage(Page.FRAME);
                            }
                        },
                        sender -> {
                            if (page == Page.YOURS) {
                                deletePainting = identifier;
                                setPage(Page.DELETE);
                            } else if (page == Page.PLAYERS && isOp()) {
                                deletePainting = identifier;
                                setPage(Page.ADMIN_DELETE);
                            }
                        }
                    ));

                    paintingWidget.setTooltip(Tooltip.create(consolidate(tooltip)));

                    paintingWidgets.put(identifier, paintingWidget);
                    updateWidget(identifier);
                } else {
                    break;
                }
            }
        }
    }

    private void rebuildScreenshots() {
        paintingWidgets.forEach((id, widget) -> removeWidget(widget));
        paintingWidgets.clear();

        // screenshots
        for (int x = 0; x < SCREENSHOTS_PER_PAGE; x++) {
            int i = x + screenshotPage * SCREENSHOTS_PER_PAGE;
            if (i >= 0 && i < screenshots.size()) {
                File file = screenshots.get(i);

                PaintingWidget paintingWidget = addRenderableWidget(new PaintingWidget(
                    (width / 2 + (x - SCREENSHOTS_PER_PAGE / 2) * 68) - 32, height / 2 + 15, 64, 48,
                    b -> {
                        currentImage = ((PaintingWidget) b).getImage();
                        if (currentImage != null) {
                            currentImagePixelZoomCache = -1;
                            currentImageName = file.getName();
                            settings = new PixelatorSettings(currentImage);
                            setPage(Page.CREATE);
                            pixelateImage();
                        }
                    },
                    b -> {}
                ));

                paintingWidget.setTooltip(Tooltip.create(Component.literal(file.getName())));

                ResourceLocation identifier = Main.locate("screenshot_" + x);
                paintingWidgets.put(identifier, paintingWidget);

                service.submit(() -> {
                    BufferedImage image = loadImage(file.getPath(), identifier);
                    if (image != null) {
                        paintingWidget.update(identifier, image);
                    }
                });
            } else {
                break;
            }
        }
    }

    public void setPage(Page page) {
        if (page != this.page) {
            clearSearch();
        }

        this.page = page;
        filteredResolution = (page == Page.DATAPACKS ? 32 : 0);

        rebuild();

        if (page == Page.DATAPACKS || page == Page.PLAYERS || page == Page.YOURS) {
            updateSearch();
        }
    }

    private void updateSearch() {
        filteredPaintings.clear();

        boolean showNSFW = Config.getInstance().showNSFWPaintings;
        UUID uuid = Minecraft.getInstance().player.getUUID();
        filteredPaintings.addAll(ClientPaintingManager.getPaintings().entrySet().stream()
                .filter(e -> {
                    Painting p = e.getValue();

                    return  (
                            (page == Page.YOURS && !p.is(Painting.Type.DATAPACK) && p.authorUUID().equals(uuid)) ||
                            (page == Page.PLAYERS && !p.is(Painting.Type.DATAPACK) && (!p.has(Painting.Flag.HIDDEN) || isOp()) && (!p.has(Painting.Flag.NSFW) || showNSFW)) ||
                            (page == Page.DATAPACKS && p.is(Painting.Type.DATAPACK))
                            ) &&
                            p.has(Painting.Flag.GRAFFITI) == entity.isGraffiti() &&
                            e.getKey().toString().contains(filteredString) &&
                            (filteredResolution == 0 || p.resolution() == filteredResolution) &&
                            (filteredWidth == 0 || p.width() == filteredWidth) &&
                            (filteredHeight == 0 || p.height() == filteredHeight);
                })
                .sorted(Comparator.comparing(p -> p.getValue().name()))
                .map(Map.Entry::getKey)
                .toList());

        setSelectionPage(selectionPage);
    }

    private boolean isOp() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasPermissions(4);
    }

    private void setSelectionPage(int p) {
        int maxPages = (int) Math.ceil(filteredPaintings.size() / 24.0);
        selectionPage = Math.min(maxPages - 1, Math.max(0, p));
        rebuildPaintings();
        pageWidget.setMessage(Component.literal((selectionPage + 1) + " / " + maxPages));
    }

    private void setScreenshotPage(int p) {
        int maxPages = (int)Math.ceil(screenshots.size() / 8.0);
        int oldPage = screenshotPage;
        screenshotPage = Math.min(maxPages - 1, Math.max(0, p));
        if (oldPage != screenshotPage) {
            rebuildScreenshots();
        }
        pageWidget.setMessage(Component.literal((screenshotPage + 1) + " / " + maxPages));
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        loadImage(paths.getFirst().toString());
    }

    private void loadImage(String path) {
        currentImage = loadImage(path, Main.locate("temp"));
        currentImagePixelZoomCache = -1;
        if (currentImage != null) {
            int dot = path.lastIndexOf('.');
            int sep = path.lastIndexOf(Path.of(path).getFileSystem().getSeparator());
            currentImageName = path.substring(sep + 1, dot).replaceFirst("[.][^.]+$", "");
            settings = new PixelatorSettings(currentImage);
            setPage(Page.CREATE);
            pixelateImage();
        }
    }

    private BufferedImage loadImage(String path, ResourceLocation identifier) {
        InputStream stream = null;
        try {
            stream = URI.create(path).toURL().openStream();
        } catch (Exception exception) {
            try {
                stream = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                Main.LOGGER.error("failed loading image {} from path {}", identifier, path, e);
            }
        }

        if (stream != null) {
            try {
                BufferedImage image = ImageIO.read(stream);
                if (image != null) {
                    preprocessImage(image);
                    Minecraft.getInstance().getTextureManager().register(identifier, new DynamicTexture(ImageManipulations.bufferedToNative(image)));
                    stream.close();
                    return image;
                }
            } catch (IOException e) {
                Main.LOGGER.error("failed decoding image {} from path {}", identifier, path, e);
            }
        }

        return null;
    }

    // Only a graffiti properly supports alpha
    private void preprocessImage(BufferedImage image) {
        clearError();
        if (!entity.isGraffiti()) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int color = image.getRGB(x, y);
                    int alpha = (color >> 24) & 255;
                    if (alpha != 255) {
                        if (error == null) {
                            setError(Component.translatable("immersive_paintings.graffiti_warning"));
                        }
                        //image.setRGB(x, y, color & ((0xFF << 24) | 0x00ffffff));
                        color = (255 << 24) | (color & 0x00ffffff);
                        image.setRGB(x, y, color);
                    }
                }
            }
        }
    }

    private static int getCurrentImagePixelZoomCache(BufferedImage currentImage) {
        if (currentImagePixelZoomCache < 0) {
            currentImagePixelZoomCache = ImageManipulations.scanForPixelArtMultiple(currentImage);
        }
        return currentImagePixelZoomCache;
    }

    private void adaptToPixelArt() {
        double zoom = getCurrentImagePixelZoomCache(currentImage);
        settings.width = Math.max(1, Math.min(16, (int) (currentImage.getWidth() / zoom / settings.resolution)));
        settings.height = Math.max(1, Math.min(16, (int) (currentImage.getHeight() / zoom / settings.resolution)));
    }

    private void pixelateImage() {
        pixelatedImage = pixelateImage(currentImage, settings);
        shouldUpload = true;
    }

    public static BufferedImage pixelateImage(BufferedImage currentImage, PixelatorSettings settings) {
        BufferedImage pixelatedImage = new BufferedImage(settings.resolution * settings.width, settings.resolution * settings.height,BufferedImage.TYPE_INT_ARGB);

        //zoom
        float zoom;
        if (settings.pixelArt) {
            zoom = getCurrentImagePixelZoomCache(currentImage);
        } else {
            float fx = (float) currentImage.getWidth() / pixelatedImage.getWidth();
            float fy = (float) currentImage.getHeight() / pixelatedImage.getHeight();
            zoom = (float)(Math.min(fx, fy) / settings.zoom);
        }

        //offset
        int ox = (int) ((currentImage.getWidth() - pixelatedImage.getWidth() * zoom) * settings.offsetX);
        int oy = (int) ((currentImage.getHeight() - pixelatedImage.getHeight() * zoom) * settings.offsetY);
        if (settings.pixelArt) {
            ox = ox / ((int) zoom) * ((int) zoom);
            oy = oy / ((int) zoom) * ((int) zoom);
        }

        //downscale
        ImageManipulations.resize(pixelatedImage, currentImage, zoom, ox, oy);

        //dither
        if (settings.dither > 0 && !settings.pixelArt) {
            if (settings.colors > 1) {
                ImageManipulations.dither(pixelatedImage, settings.dither / settings.colors);
            } else {
                ImageManipulations.dither(pixelatedImage, settings.dither / 16.0);
            }
        }

        //reduce colors
        if (settings.colors > 1 && !settings.pixelArt) {
            ImageManipulations.reduceColors(pixelatedImage, settings.colors);
        }

        return pixelatedImage;
    }

    public void refreshPage() {
        setPage(page);
    }

    public void clearError() {
        error = null;
    }

    public void setError(Component text) {
        error = text;
    }

    public enum Page {
        YOURS,
        DATAPACKS,
        PLAYERS,
        NEW,
        CREATE,
        FRAME,
        DELETE,
        ADMIN_DELETE,
        LOADING
    }

    public static final class PixelatorSettings {
        public double dither;
        public int colors;
        public int resolution;
        public int width;
        public int height;
        public double offsetX;
        public double offsetY;
        public double zoom;
        public boolean pixelArt;
        public boolean hidden = true;
        public boolean nsfw;

        public PixelatorSettings(double dither, int colors, int resolution, int width, int height, double offsetX, double offsetY, double zoom, boolean pixelArt) {
            this.dither = dither;
            this.colors = colors;
            this.resolution = resolution;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.zoom = zoom;
            this.pixelArt = pixelArt;
        }

        PixelatorSettings(BufferedImage currentImage) {
            this(0.25, 10, 32, 1, 1, 0.5, 0.5, 1, false);

            double target = currentImage.getWidth() / (double) currentImage.getHeight();
            double bestScore = 100;

            double d = Math.sqrt(currentImage.getWidth() * currentImage.getWidth() + currentImage.getHeight() * currentImage.getHeight());
            double dw = currentImage.getWidth() / d;
            double dh = currentImage.getHeight() / d;
            for (double diagonal = 3.0f; diagonal < 6.0; diagonal += target) {
                int pw = (int) Math.ceil(dw * diagonal);
                int ph = (int) Math.ceil(dh * diagonal);
                double e = Math.abs(pw / (double) ph - target) * Math.sqrt(5 + width + height);
                if (e < bestScore) {
                    width = Math.max(1, Math.min(16, pw));
                    height = Math.max(1, Math.min(16, ph));
                    bestScore = e;
                }
            }
        }

        public EnumSet<Painting.Flag> getFlags() {
            EnumSet<Painting.Flag> flags = EnumSet.noneOf(Painting.Flag.class);
            if (nsfw)
                flags.add(Painting.Flag.NSFW);

            if (hidden)
                flags.add(Painting.Flag.HIDDEN);

            return flags;
        }
    }
}
