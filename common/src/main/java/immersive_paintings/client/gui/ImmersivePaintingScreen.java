package immersive_paintings.client.gui;

import immersive_paintings.Main;
import immersive_paintings.client.ClientUtils;
import immersive_paintings.client.gui.widget.*;
import immersive_paintings.cobalt.network.NetworkHandler;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.network.c2s.PaintingDeleteRequest;
import immersive_paintings.network.c2s.PaintingModifyRequest;
import immersive_paintings.network.c2s.RegisterPaintingRequest;
import immersive_paintings.network.c2s.UploadPaintingRequest;
import immersive_paintings.resources.*;
import immersive_paintings.util.FlowingText;
import immersive_paintings.util.ImageManipulations;
import immersive_paintings.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static immersive_paintings.util.ImageManipulations.scanForPixelArtMultiple;
import static immersive_paintings.util.Utils.identifierToTranslation;

public class ImmersivePaintingScreen extends Screen {
    private static final int SCREENSHOTS_PER_PAGE = 5;

    final int entityId;
    final int minResolution;
    final int maxResolution;
    final boolean showOtherPlayersPaintings;
    final int uploadPermissionLevel;

    public final ImmersivePaintingEntity entity;

    private String filteredString = "";
    private int filteredResolution = 0;
    private int filteredWidth = 0;
    private int filteredHeight = 0;
    private final List<Identifier> filteredPaintings = new ArrayList<>();

    private int selectionPage;
    private Page page;

    private ButtonWidget pageWidget;

    private final List<PaintingWidget> paintingWidgetList = new LinkedList<>();
    private ByteImage currentImage;
    private static int currentImagePixelZoomCache = -1;
    private String currentImageName;
    private PixelatorSettings settings;
    private ByteImage pixelatedImage;

    private List<File> screenshots = List.of();
    private int screenshotPage;

    private Identifier deletePainting;
    private Text error;
    private boolean shouldReProcess;
    private static volatile boolean shouldUpload;

    final ExecutorService service = Executors.newFixedThreadPool(1);

    public ImmersivePaintingScreen(int entityId, int minResolution, int maxResolution, boolean showOtherPlayersPaintings, int uploadPermissionLevel) {
        super(Text.translatable("item.immersive_paintings.painting"));

        this.entityId = entityId;
        this.minResolution = minResolution;
        this.maxResolution = maxResolution;
        this.showOtherPlayersPaintings = showOtherPlayersPaintings;
        this.uploadPermissionLevel = uploadPermissionLevel;

        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.getEntityById(entityId) instanceof ImmersivePaintingEntity painting) {
            entity = painting;
        } else {
            entity = null;
        }

        if (entity == null) {
            close();
        }
    }

    @Override
    public boolean shouldPause() {
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
        File file = new File(MinecraftClient.getInstance().runDirectory, "screenshots");
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        switch (page) {
            case NEW -> {
                context.fill(width / 2 - 115, height / 2 - 68, width / 2 + 115, height / 2 - 41, 0x50000000);
                List<Text> wrap = FlowingText.wrap(Text.translatable("immersive_paintings.drop"), 220);
                int y = height / 2 - 40 - wrap.size() * 12;
                for (Text text : wrap) {
                    context.drawCenteredTextWithShadow(textRenderer, text, width / 2, y, 0xFFFFFFFF);
                    y += 12;
                }
            }
            case CREATE -> {
                if (shouldReProcess && currentImage != null) {
                    Runnable task = () -> {
                        pixelatedImage = pixelateImage(currentImage, settings);
                        shouldUpload = true;
                    };
                    service.submit(task);
                    shouldReProcess = false;
                }

                if (shouldUpload && pixelatedImage != null) {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(Main.locate("temp_pixelated"), new NativeImageBackedTexture(ClientUtils.byteImageToNativeImage(pixelatedImage)));
                }

                int maxWidth = 190;
                int maxHeight = 135;
                int tw = settings.resolution * settings.width;
                int th = settings.resolution * settings.height;
                float size = Math.min((float) maxWidth / tw, (float) maxHeight / th);
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(width / 2.0f - tw * size / 2.0f, height / 2.0f - th * size / 2.0f, 0.0f);
                matrices.scale(size, size, 1.0f);
                context.drawTexture(Main.locate("temp_pixelated"), 0, 0, 0, 0, tw, th, tw, th);
                matrices.pop();

                if (error != null) {
                    context.drawCenteredTextWithShadow(textRenderer, error, width / 2, height / 2, 0xFFFF0000);
                }
            }
            case DELETE -> {
                context.fill(width / 2 - 160, height / 2 - 50, width / 2 + 160, height / 2 + 50, 0x88000000);
                List<Text> wrap = FlowingText.wrap(Text.translatable("immersive_paintings.confirm_deletion"), 300);
                int y = height / 2 - 35;
                for (Text t : wrap) {
                    context.drawCenteredTextWithShadow(textRenderer, t, width / 2, y, 0XFFFFFF);
                    y += 15;
                }
            }
            case ADMIN_DELETE -> {
                context.fill(width / 2 - 160, height / 2 - 50, width / 2 + 160, height / 2 + 50, 0x88000000);
                List<Text> wrap = FlowingText.wrap(Text.translatable("immersive_paintings.confirm_admin_deletion"), 300);
                int y = height / 2 - 35;
                for (Text t : wrap) {
                    context.drawCenteredTextWithShadow(textRenderer, t, width / 2, y, 0XFFFFFF);
                    y += 15;
                }
            }
            case LOADING -> {
                Text text = Text.translatable("immersive_paintings.upload", (int) Math.ceil(LazyNetworkManager.getRemainingTime()));
                context.drawCenteredTextWithShadow(textRenderer, text, width / 2, height / 2, 0xFFFFFFFF);
            }
        }
    }

    private List<Identifier> getMaterialsList() {
        return FrameLoader.frames.values().stream()
                .filter(v -> v.frame().equals(entity.getFrame()))
                .map(Frame::material)
                .distinct()
                .sorted(Identifier::compareTo)
                .toList();
    }


    private void rebuild() {
        clearChildren();

        // filters
        if (page != Page.CREATE) {
            List<Page> b = new LinkedList<>();
            b.add(Page.YOURS);
            b.add(Page.DATAPACKS);
            if (showOtherPlayersPaintings || isOp()) {
                b.add(Page.PLAYERS);
            }
            if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().player.hasPermissionLevel(uploadPermissionLevel)) {
                b.add(Page.NEW);
            }
            if (!entity.isGraffiti()) {
                b.add(Page.FRAME);
            }

            int x = width / 2 - 200;
            int w = 400 / b.size();
            for (Page page : b) {
                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.page." + page.name().toLowerCase(Locale.ROOT)), sender -> setPage(page))
                        .position(x, height / 2 - 90 - 22)
                        .size(w, 20)
                        .build()
                ).active = page != this.page;

                x += w;
            }
        }

        switch (page) {
            case NEW -> {
                //URL
                TextFieldWidget textFieldWidget = addDrawableChild(new TextFieldWidget(this.textRenderer, width / 2 - 90, height / 2 - 38, 180, 16,
                        Text.literal("URL")));
                textFieldWidget.setMaxLength(1024);

                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.load"), sender -> loadImage(textFieldWidget.getText()))
                        .position(width / 2 - 50, height / 2 - 15)
                        .size(100, 20)
                        .build()
                );


                //screenshots
                rebuildScreenshots();

                //screenshot page
                addDrawableChild(ButtonWidget
                        .builder(Text.literal("<<"), sender -> setScreenshotPage(screenshotPage - 1))
                        .position(width / 2 - 65, height / 2 + 70)
                        .size(30, 20)
                        .build()
                );

                this.pageWidget = addDrawableChild(ButtonWidget
                        .builder(Text.literal(""), sender -> {})
                        .position(width / 2 - 65 + 30, height / 2 + 70)
                        .size(70, 20)
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.literal(">>"), sender -> setScreenshotPage(screenshotPage + 1))
                        .position(width / 2 - 65 + 100, height / 2 + 70)
                        .size(30, 20)
                        .build()
                );

                setScreenshotPage(screenshotPage);
            }

            case CREATE -> {
                // Name
                TextFieldWidget textFieldWidget = addDrawableChild(new TextFieldWidget(this.textRenderer, width / 2 - 90, height / 2 - 100, 180, 20,
                        Text.translatable("immersive_paintings.name")));
                textFieldWidget.setMaxLength(256);
                textFieldWidget.setText(currentImageName);
                textFieldWidget.setChangedListener(s -> currentImageName = s);

                int y = height / 2 - 60;

                // Width
                addDrawableChild(new IntegerSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.width", settings.width, 1, 16, v -> {
                    settings.width = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Height
                addDrawableChild(new IntegerSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.height", settings.height, 1, 16, v -> {
                    settings.height = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Resolution
                int x = width / 2 - 200;

                ButtonWidget resolutionWidget = addDrawableChild(ButtonWidget
                        .builder(Text.literal(String.valueOf(settings.resolution)), sender -> {})
                        .position(x + 25, y)
                        .size(50, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.resolution")))
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.literal("<"), sender -> {
                            settings.resolution = Math.max(minResolution, settings.resolution / 2);
                            if (settings.pixelArt) {
                                adaptToPixelArt();
                                refreshPage();
                            }
                            shouldReProcess = true;
                            resolutionWidget.setMessage(Text.literal(String.valueOf(settings.resolution)));
                        })
                        .position(x, y)
                        .size(25, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.resolution")))
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.literal(">"), sender -> {
                            settings.resolution = Math.min(maxResolution, settings.resolution * 2);
                            if (settings.pixelArt) {
                                adaptToPixelArt();
                                refreshPage();
                            }
                            shouldReProcess = true;
                            resolutionWidget.setMessage(Text.literal(String.valueOf(settings.resolution)));
                        })
                        .position(x + 75, y)
                        .size(25, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.resolution")))
                        .build()
                );
                y += 22;
                y += 10;

                // Color reduction
                addDrawableChild(new IntegerSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.colors", settings.colors, 1, 25, v -> {
                    settings.colors = v;
                    shouldReProcess = true;
                })).active = !settings.pixelArt;
                y += 22;

                // Dither
                addDrawableChild(new PercentageSliderWidget(width / 2 - 200, y, 100, 20, "immersive_paintings.dither", settings.dither, v -> {
                    settings.dither = v;
                    shouldReProcess = true;
                })).active = !settings.pixelArt;

                // PixelArt
                y = height / 2 - 50;
                addDrawableChild(CheckboxWidget
                        .builder(Text.translatable("immersive_paintings.pixelart"), this.textRenderer)
                        .pos(width / 2 + 100, y)
                        .checked(settings.pixelArt)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.pixelart.tooltip")))
                        .callback((w, v) -> {
                            settings.pixelArt = v;
                            adaptToPixelArt();
                            refreshPage();
                            shouldReProcess = true;
                        })
                        .build()
                );
                y += 22;

                // Hide
                addDrawableChild(CheckboxWidget
                        .builder(Text.translatable("immersive_paintings.hide"), this.textRenderer)
                        .pos(width / 2 + 100, y)
                        .checked(settings.hidden)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.visibility")))
                        .callback((w, v) -> settings.hidden = !settings.hidden)
                        .build()
                );
                y += 22;

                // Offset X
                addDrawableChild(new PercentageSliderWidget(width / 2 + 100, y, 100, 20, "immersive_paintings.x_offset", settings.offsetX, v -> {
                    settings.offsetX = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Offset Y
                addDrawableChild(new PercentageSliderWidget(width / 2 + 100, y, 100, 20, "immersive_paintings.y_offset", settings.offsetY, v -> {
                    settings.offsetY = v;
                    shouldReProcess = true;
                }));
                y += 22;

                // Offset
                addDrawableChild(new PercentageSliderWidget(width / 2 + 100, y, 100, 20, "immersive_paintings.zoom", settings.zoom, entity.isGraffiti() ? 0.5 : 1.0, entity.isGraffiti() ? 1.5 : 3.0, v -> {
                    settings.zoom = v;
                    shouldReProcess = true;
                })).active = !settings.pixelArt;

                // Cancel
                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.cancel"), sender -> setPage(Page.NEW))
                        .position(width / 2 - 85, height / 2 + 75)
                        .size(80, 20)
                        .build()
                );

                // Save
                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.save"), sender -> {
                            Utils.processByteArrayInChunks(pixelatedImage.encode(), (ints, split, splits) ->
                                    LazyNetworkManager.sendToServer(new UploadPaintingRequest(ints, split, splits))
                            );

                            LazyNetworkManager.sendToServer(new RegisterPaintingRequest(currentImageName, new Painting(
                                    pixelatedImage,
                                    settings.width,
                                    settings.height,
                                    settings.resolution,
                                    settings.hidden,
                                    entity.isGraffiti()
                            )));

                            setPage(Page.LOADING);
                        })
                        .position(width / 2 + 5, height / 2 + 75)
                        .size(80, 20)
                        .build()
                );
            }

            case YOURS, DATAPACKS, PLAYERS -> {
                rebuildPaintings();

                // page
                addDrawableChild(ButtonWidget
                        .builder(Text.literal("<<"), sender -> setSelectionPage(selectionPage - 1))
                        .position(width / 2 - 35 - 30, height / 2 + 80)
                        .size(30, 20)
                        .build()
                );
                this.pageWidget = addDrawableChild(ButtonWidget
                        .builder(Text.literal(""), sender -> {})
                        .position(width / 2 - 35, height / 2 + 80)
                        .size(70, 20)
                        .build()
                );
                addDrawableChild(ButtonWidget
                        .builder(Text.literal(">>"), sender -> setSelectionPage(selectionPage + 1))
                        .position(width / 2 + 35, height / 2 + 80)
                        .size(30, 20)
                        .build()
                );
                setSelectionPage(selectionPage);

                //search
                TextFieldWidget textFieldWidget = addDrawableChild(new TextFieldWidget(this.textRenderer, width / 2 - 65, height / 2 - 88, 130, 16,
                        Text.translatable("immersive_paintings.search")));
                textFieldWidget.setMaxLength(64);
                textFieldWidget.setSuggestion("search");
                textFieldWidget.setChangedListener(s -> {
                    filteredString = s;
                    updateSearch();
                    textFieldWidget.setSuggestion(null);
                });

                int x = width / 2 - 200 + 12;

                ButtonWidget widget = addDrawableChild(ButtonWidget
                        .builder(Text.literal(String.valueOf(filteredResolution)), sender -> {})
                        .position(x + 50 + 8, height / 2 - 90)
                        .size(25, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.filter_resolution")))
                        .build()
                );

                ButtonWidget allWidget = addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.filter.all"), sender -> {
                            filteredResolution = 0;
                            updateSearch();
                            widget.setMessage(Text.literal(String.valueOf(filteredResolution)));
                            sender.active = false;
                        })
                        .position(x, height / 2 - 90)
                        .size(25, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.filter_resolution")))
                        .build()
                );


                addDrawableChild(ButtonWidget
                        .builder(Text.literal("<"), sender -> {
                            filteredResolution = filteredResolution == 0 ? 32 : Math.max(minResolution, filteredResolution / 2);
                            updateSearch();
                            widget.setMessage(Text.literal(String.valueOf(filteredResolution)));
                            allWidget.active = true;
                        })
                        .position(x + 25 + 8, height / 2 - 90)
                        .size(25, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.filter_resolution")))
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.literal(">"), sender -> {
                            filteredResolution = filteredResolution == 0 ? 32 : Math.min(maxResolution, filteredResolution * 2);
                            updateSearch();
                            widget.setMessage(Text.literal(String.valueOf(filteredResolution)));
                            allWidget.active = true;
                        })
                        .position(x + 75 + 8, height / 2 - 90)
                        .size(25, 20)
                        .tooltip(Tooltip.of(Text.translatable("immersive_paintings.tooltip.filter_resolution")))
                        .build()
                );


                //width
                TextFieldWidget widthWidget = addDrawableChild(new TextFieldWidget(this.textRenderer, width / 2 + 80, height / 2 - 88, 40, 16,
                        Text.translatable("immersive_paintings.filter_width")));
                widthWidget.setMaxLength(2);
                widthWidget.setSuggestion("width");
                widthWidget.setChangedListener(s -> {
                    try {
                        filteredWidth = Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        filteredWidth = 0;
                    }
                    updateSearch();
                    widthWidget.setSuggestion(null);
                });

                //height
                TextFieldWidget heightWidget = addDrawableChild(new TextFieldWidget(this.textRenderer, width / 2 + 80 + 40, height / 2 - 88, 40, 16,
                        Text.translatable("immersive_paintings.filter_height")));
                heightWidget.setMaxLength(2);
                heightWidget.setSuggestion("height");
                heightWidget.setChangedListener(s -> {
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
                List<Identifier> frames = FrameLoader.frames.values()
                        .stream()
                        .map(Frame::frame)
                        .distinct()
                        .sorted(Identifier::compareTo)
                        .toList();

                for (Identifier frame : frames) {
                    ButtonWidget widget = addDrawableChild(ButtonWidget
                            .builder(Text.translatable("immersive_paintings.frame." + identifierToTranslation(frame)), sender -> {
                                entity.setFrame(frame);
                                entity.setMaterial(getMaterialsList().get(0));
                                NetworkHandler.sendToServer(new PaintingModifyRequest(entity));
                                setPage(Page.FRAME);
                            })
                            .position(width / 2 - 200, y)
                            .size(100, 20)
                            .build()
                    );

                    widget.active = !frame.equals(entity.getFrame());
                    y += 25;
                }

                //material
                int py = 0;
                int px = 0;

                List<Identifier> materials = getMaterialsList();
                List<ButtonWidget> materialList = new LinkedList<>();

                for (Identifier material : materials) {
                    ButtonWidget widget = addDrawableChild(new TexturedButtonWidget(
                            width / 2 - 80 + px * 65,
                            height / 2 - 80 + py * 20,
                            64,
                            16,
                            new Identifier(material.getNamespace(), material.getPath().replace("/block/", "/gui/")),
                            0,
                            0,
                            64,
                            32,
                            Text.literal(""),
                            v -> {
                                entity.setMaterial(material);
                                NetworkHandler.sendToServer(new PaintingModifyRequest(entity));
                                materialList.forEach(b -> b.active = true);
                                v.active = false;
                            }
                    ));

                    Tooltip paintingTooltip = Tooltip.of(Text.translatable("immersive_paintings.material." + identifierToTranslation(material)));
                    widget.setTooltip(paintingTooltip);

                    widget.active = !material.equals(entity.getMaterial());
                    materialList.add(widget);

                    px++;
                    if (px > 3) {
                        px = 0;
                        py++;
                    }
                }

                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.done"), sender -> close())
                        .position(width / 2 - 50, height / 2 + 70)
                        .size(100, 20)
                        .build()
                );
            }

            case DELETE -> {
                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.cancel"), sender -> setPage(Page.YOURS))
                        .position(width / 2 - 100 - 5, height / 2 + 20)
                        .size(100, 20)
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.delete"), sender -> {
                            NetworkHandler.sendToServer(new PaintingDeleteRequest(deletePainting));
                            setPage(Page.YOURS);
                        })
                        .position(width / 2 + 5, height / 2 + 20)
                        .size(100, 20)
                        .build()
                );
            }

            case ADMIN_DELETE -> {

                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.cancel"), v -> setPage(Page.PLAYERS))
                        .position(width / 2 - 115, height / 2 + 10)
                        .size(70, 20)
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.delete"), v -> {
                            NetworkHandler.sendToServer(new PaintingDeleteRequest(deletePainting));
                            setPage(Page.PLAYERS);
                        })
                        .position(width / 2 - 40, height / 2 + 10)
                        .size(70, 20)
                        .build()
                );

                addDrawableChild(ButtonWidget
                        .builder(Text.translatable("immersive_paintings.delete_all"), v -> {
                            String author = ClientPaintingManager.getPainting(deletePainting).author;
                            ClientPaintingManager.getPaintings().entrySet().stream()
                                    .filter(p -> Objects.equals(p.getValue().author, author) && !p.getValue().datapack)
                                    .map(Map.Entry::getKey)
                                    .forEach(p -> NetworkHandler.sendToServer(new PaintingDeleteRequest(p)));
                            setPage(Page.PLAYERS);
                        })
                        .position(width / 2 + 35, height / 2 + 10)
                        .size(70, 20)
                        .build()
                );
            }
        }
    }

    private void rebuildPaintings() {
        for (PaintingWidget w : paintingWidgetList) {
            remove(w);
        }
        paintingWidgetList.clear();

        // paintings
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                int i = y * 8 + x + selectionPage * 24;
                if (i >= 0 && i < filteredPaintings.size()) {
                    Identifier identifier = filteredPaintings.get(i);
                    Painting painting = ClientPaintingManager.getPainting(identifier);

                    //tooltip
                    List<Text> tooltip = new LinkedList<>();
                    tooltip.add(Text.literal(painting.name));
                    tooltip.add(Text.translatable("immersive_paintings.by_author", painting.author).formatted(Formatting.ITALIC));
                    tooltip.add(Text.translatable("immersive_paintings.resolution", painting.width, painting.height, painting.resolution).formatted(Formatting.ITALIC));

                    if (page == Page.YOURS && painting.hidden) {
                        tooltip.add(Text.translatable("immersive_paintings.hidden").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
                    }

                    if (page == Page.YOURS || page == Page.PLAYERS && isOp()) {
                        tooltip.add(Text.translatable("immersive_paintings.right_click_to_delete").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
                    }

                    PaintingWidget paintingWidget = addDrawableChild(new PaintingWidget(
                            ClientPaintingManager.getPaintingTexture(identifier, Painting.Type.THUMBNAIL),
                            (int) (width / 2 + (x - 3.5) * 48) - 24, height / 2 - 66 + y * 48,
                            46, 46,

                            // Left-Click
                            sender -> {
                                entity.setMotive(identifier);
                                NetworkHandler.sendToServer(new PaintingModifyRequest(entity));
                                if (entity.isGraffiti()) {
                                    close();
                                } else {
                                    setPage(Page.FRAME);
                                }
                            },

                            // Right-Click
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

                    Tooltip paintingTooltip = Tooltip.of(FlowingText.consolidate(tooltip));
                    paintingWidget.setTooltip(paintingTooltip);

                    this.paintingWidgetList.add(paintingWidget);

                } else {
                    break;
                }
            }
        }
    }

    private void rebuildScreenshots() {
        for (PaintingWidget w : paintingWidgetList) {
            remove(w);
        }
        paintingWidgetList.clear();

        // screenshots
        for (int x = 0; x < SCREENSHOTS_PER_PAGE; x++) {
            int i = x + screenshotPage * SCREENSHOTS_PER_PAGE;

            if (i >= 0 && i < screenshots.size()) {

                File file = screenshots.get(i);
                Painting painting = new Painting(null, 16, 16, 16, false, entity.isGraffiti());

                PaintingWidget paintingWidget = addDrawableChild(new PaintingWidget(
                        painting.thumbnail,
                        (width / 2 + (x - SCREENSHOTS_PER_PAGE / 2) * 68) - 32, height / 2 + 15,
                        64, 48,

                        // On Left-Click
                        b -> {
                            currentImage = ((PaintingWidget) b).thumbnail.image;
                            if (currentImage != null) {
                                currentImagePixelZoomCache = -1;
                                currentImageName = file.getName();
                                settings = new PixelatorSettings(currentImage);
                                setPage(Page.CREATE);
                                pixelateImage();
                            }
                        },

                        // On Right-Click
                        b -> { }
                ));

                Tooltip paintingTooltip = Tooltip.of(Text.literal(file.getName()));
                paintingWidget.setTooltip(paintingTooltip);

                this.paintingWidgetList.add(paintingWidget);

                Identifier identifier = Main.locate("screenshot_" + x);
                Runnable task = () -> {
                    ByteImage image = loadImage(file.getPath(), identifier);
                    if (image != null) {
                        painting.width = image.getWidth();
                        painting.height = image.getHeight();
                        painting.thumbnail.image = image;
                        painting.thumbnail.textureIdentifier = identifier;
                    }
                };

                service.submit(task);

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

        if (page == Page.DATAPACKS) {
            filteredResolution = 32;
        } else {
            filteredResolution = 0;
        }

        rebuild();

        if (page == Page.DATAPACKS || page == Page.PLAYERS || page == Page.YOURS) {
            updateSearch();
        }
    }

    private void updateSearch() {
        filteredPaintings.clear();

        String playerName = getPlayerName();
        filteredPaintings.addAll(ClientPaintingManager.getPaintings().entrySet().stream()
                .filter(v -> v.getValue().graffiti == entity.isGraffiti())
                .filter(v -> page != Page.YOURS || Objects.equals(v.getValue().author, playerName) && !v.getValue().datapack)
                .filter(v -> page != Page.PLAYERS || !v.getValue().datapack && !v.getValue().hidden)
                .filter(v -> page != Page.DATAPACKS || v.getValue().datapack)
                .filter(v -> v.getKey().toString().contains(filteredString))
                .filter(v -> filteredResolution == 0 || v.getValue().resolution == filteredResolution)
                .filter(v -> filteredWidth == 0 || v.getValue().width == filteredWidth)
                .filter(v -> filteredHeight == 0 || v.getValue().height == filteredHeight)
                .map(Map.Entry::getKey)
                .toList());

        setSelectionPage(selectionPage);
    }

    private String getPlayerName() {
        return MinecraftClient.getInstance().player == null ? "" : MinecraftClient.getInstance().player.getGameProfile().getName();
    }

    private boolean isOp() {
        return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.hasPermissionLevel(4);
    }

    private void setSelectionPage(int p) {
        selectionPage = Math.min(getMaxPages() - 1, Math.max(0, p));
        rebuildPaintings();
        pageWidget.setMessage(Text.literal((selectionPage + 1) + " / " + getMaxPages()));
    }

    private int getMaxPages() {
        return (int) Math.ceil(filteredPaintings.size() / 24.0);
    }

    private void setScreenshotPage(int p) {
        int oldPage = screenshotPage;
        screenshotPage = Math.min(getScreenshotMaxPages() - 1, Math.max(0, p));
        if (oldPage != screenshotPage) {
            rebuildScreenshots();
        }
        pageWidget.setMessage(Text.literal((screenshotPage + 1) + " / " + getScreenshotMaxPages()));
    }

    private int getScreenshotMaxPages() {
        return (int) Math.ceil(screenshots.size() / 8.0);
    }

    @Override
    public void filesDragged(List<Path> paths) {
        Path path = paths.get(0);
        loadImage(path.toString());
    }

    private void loadImage(String path) {
        currentImage = loadImage(path, Main.locate("temp"));
        currentImagePixelZoomCache = -1;
        if (currentImage != null) {
            currentImageName = FilenameUtils.getBaseName(path).replaceFirst("[.][^.]+$", "");
            settings = new PixelatorSettings(currentImage);
            setPage(Page.CREATE);
            pixelateImage();
        }
    }

    private ByteImage loadImage(String path, Identifier identifier) {
        InputStream stream = null;
        try {
            stream = new URL(path).openStream();
        } catch (Exception exception) {
            try {
                stream = new FileInputStream(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (stream != null) {
            try {
                ByteImage nativeImage = ByteImage.read(stream);
                preprocessImage(nativeImage);
                MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(ClientUtils.byteImageToNativeImage(nativeImage)));
                stream.close();
                return nativeImage;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Only a graffiti properly supports alpha
    private void preprocessImage(ByteImage image) {
        clearError();
        if (!entity.isGraffiti()) {
            byte[] bytes = image.getBytes();
            for (int i = 3; i < bytes.length; i += 4) {
                if (bytes[i] != ((byte) 255)) {
                    if (error == null) {
                        setError(Text.translatable("immersive_paintings.graffiti_warning"));
                    }
                    bytes[i] = ((byte) 255);
                }
            }
        }
    }

    private static int getCurrentImagePixelZoomCache(ByteImage currentImage) {
        if (currentImagePixelZoomCache < 0) {
            currentImagePixelZoomCache = scanForPixelArtMultiple(currentImage);
        }
        return currentImagePixelZoomCache;
    }

    private void pixelateImage() {
        pixelatedImage = pixelateImage(currentImage, settings);
        shouldUpload = true;
    }

    private void adaptToPixelArt() {
        double zoom = getCurrentImagePixelZoomCache(currentImage);
        settings.width = Math.max(1, Math.min(16, (int) (currentImage.getWidth() / zoom / settings.resolution)));
        settings.height = Math.max(1, Math.min(16, (int) (currentImage.getHeight() / zoom / settings.resolution)));
    }

    public static ByteImage pixelateImage(ByteImage currentImage, PixelatorSettings settings) {
        ByteImage pixelatedImage = new ByteImage(settings.resolution * settings.width, settings.resolution * settings.height);

        //zoom
        double zoom;
        if (settings.pixelArt) {
            zoom = getCurrentImagePixelZoomCache(currentImage);
        } else {
            float fx = (float) currentImage.getWidth() / pixelatedImage.getWidth();
            float fy = (float) currentImage.getHeight() / pixelatedImage.getHeight();
            zoom = Math.min(fx, fy) / settings.zoom;
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
        this.error = null;
    }

    public void setError(Text text) {
        this.error = text;
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
        public boolean hidden;

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

        PixelatorSettings(ByteImage currentImage) {
            this(0.25, 10, 32, 1, 1, 0.5, 0.5, 1, false);

            double target = currentImage.getWidth() / (double) currentImage.getHeight();
            double bestScore = 100;

            double d = Math.sqrt(currentImage.getWidth() * currentImage.getWidth() + currentImage.getHeight() * currentImage.getHeight());
            double dw = currentImage.getWidth() / d;
            double dh = currentImage.getHeight() / d;
            for (float diagonal = 3.0f; diagonal < 6.0; diagonal += target) {
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
    }
}
