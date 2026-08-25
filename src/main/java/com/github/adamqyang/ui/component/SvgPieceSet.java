package com.github.adamqyang.ui.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Loads chess piece SVG files from classpath resources and caches their
 * parsed vector data, so repeated renders of the same piece type (e.g. all
 * 8 pawns) don't re-parse the same file over and over. A single JavaFX Node
 * can't be attached to more than one parent, so what's cached is the raw
 * path data, not a constructed Node - createNode() builds a fresh, freshly
 * scaled Node from that cached data on every call.
 * <p>
 * Expects one file per piece/color under /pieces/ on the classpath, named
 * with the common wK/wQ/wR/wB/wN/wP (white) and bK/bQ/bR/bB/bN/bP (black)
 * convention - e.g. /pieces/wK.svg for the white king. This matches how
 * most existing Cburnett SVG bundles (e.g. lichess/chessground's) are
 * already named.
 * <p>
 * Only &lt;path&gt; elements are parsed - not &lt;circle&gt;, &lt;rect&gt;,
 * gradients, or transforms - since Cburnett's outline-style pieces are
 * built almost entirely from paths. If a particular file uses other SVG
 * primitives, those parts simply won't render.
 */
final class SvgPieceSet {

    private static final Map<String, PieceVectorData> CACHE = new HashMap<>();

    private SvgPieceSet() {
    }

    /**
     * Builds a fresh Node for the given resource key (e.g. "wK"), scaled to
     * fit within targetSize x targetSize. Returns null if the resource file
     * isn't found, so callers can fall back to something else instead of
     * crashing - useful before real asset files have been added.
     */
    static Group createNode(String resourceKey, double targetSize) {
        PieceVectorData data = CACHE.computeIfAbsent(resourceKey, SvgPieceSet::load);
        if (data == null) {
            return null;
        }

        Group group = new Group();
        for (PathData path : data.paths()) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(path.d());
            svgPath.setFill(colorOrDefault(path.fill(), Color.BLACK));
            if (path.stroke() != null && !"none".equalsIgnoreCase(path.stroke())) {
                svgPath.setStroke(colorOrDefault(path.stroke(), Color.BLACK));
                double strokeWidth = parseDouble(path.strokeWidth());
                svgPath.setStrokeWidth(strokeWidth > 0 ? strokeWidth : 1.0);
            }
            group.getChildren().add(svgPath);
        }

        // Node's scaleX/scaleY pivot around the node's own bounds CENTER by
        // default - unlike adding a Scale to getTransforms(), which pivots
        // around (0,0) and shrinks everything toward that corner instead of
        // staying centered within whatever container (e.g. a StackPane) is
        // trying to center this node.
        double scale = targetSize / Math.max(data.width(), data.height());
        group.setScaleX(scale);
        group.setScaleY(scale);
        return group;
    }

    private static PieceVectorData load(String resourceKey) {
        String resourcePath = "/pieces/" + resourceKey + ".svg";
        try (InputStream in = SvgPieceSet.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null; // asset not added yet
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            Element svgRoot = doc.getDocumentElement();

            double[] size = readSize(svgRoot);

            List<PathData> paths = new ArrayList<>();
            NodeList pathNodes = doc.getElementsByTagName("path");
            for (int i = 0; i < pathNodes.getLength(); i++) {
                Element pathEl = (Element) pathNodes.item(i);
                String d = pathEl.getAttribute("d");
                if (d.isBlank()) {
                    continue;
                }
                paths.add(new PathData(
                        d, attrOrNull(pathEl, "fill"), attrOrNull(pathEl, "stroke"),
                        attrOrNull(pathEl, "stroke-width")));
            }
            return new PieceVectorData(paths, size[0], size[1]);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.err.println("Failed to load piece SVG " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private static double[] readSize(Element svgRoot) {
        double width = parseDouble(svgRoot.getAttribute("width"));
        double height = parseDouble(svgRoot.getAttribute("height"));
        if (width <= 0 || height <= 0) {
            String[] viewBox = svgRoot.getAttribute("viewBox").trim().split("\\s+");
            if (viewBox.length == 4) {
                width = parseDouble(viewBox[2]);
                height = parseDouble(viewBox[3]);
            }
        }
        return (width > 0 && height > 0) ? new double[]{width, height} : new double[]{45, 45};
    }

    private static String attrOrNull(Element el, String name) {
        String value = el.getAttribute(name);
        return (value == null || value.isBlank()) ? null : value;
    }

    private static double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }
        try {
            return Double.parseDouble(raw.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Color colorOrDefault(String css, Color fallback) {
        if (css == null) {
            return fallback;
        }
        if ("none".equalsIgnoreCase(css)) {
            return Color.TRANSPARENT;
        }
        try {
            return Color.web(css);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private record PieceVectorData(List<PathData> paths, double width, double height) {
    }

    private record PathData(String d, String fill, String stroke, String strokeWidth) {
    }
}