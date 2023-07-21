/**
 * MIT License
 *
 * Copyright (c) 2020, 2023 Mark Schmieder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is part of the FxDxfConverter Library
 *
 * You should have received a copy of the MIT License along with the 
 * FxDxfConverter Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxdxfconverter
 */
package com.mhschmieder.fxdxfconverter;

import com.mhschmieder.fxcharttoolkit.ChartContentGroup;
import com.mhschmieder.fxgraphicstoolkit.paint.ColorUtilities;
import com.mhschmieder.fxguitoolkit.GuiUtilities;
import com.mhschmieder.physicstoolkit.DistanceUnit;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;

/**
 * This is a Group container for Imported Graphics.
 */
public class ImportedGraphicsGroup extends ChartContentGroup {

    protected static final double IMPORTED_GRAPHICS_STROKE_WIDTH_RATIO = 0.75d;

    /**
     * Background opacity level is defined a constant, in case we switch to a
     * computed ratio and in case we provide programmatic support for changing
     * its value (this then gives us our defined default value).
     */
    private static final double   BACKGROUND_OPACITY_DEFAULT           = 0.75d;

    /**
     * Declare a flag to indicate whether imported geometry is active or not.
     */
    private boolean               _importedGraphicsActive;

    /**
     * Imported Graphics container that is neither draggable nor clickable.
     */
    private DxfShapeGroup         _importedGraphics;

    /**
     * Distance Unit for the Graphics Import content source.
     */
    private DistanceUnit          _importedGraphicsDistanceUnit;

    /**
     * Flag for whether to show the Imported Graphics or not.
     */
    private boolean               _showImportedGraphics;

    /**
     * Keep a local copy of the global Imported Graphics Opacity Percent
     * property, for data binding.
     */
    private DoubleProperty        importedGraphicsOpacityPercent;

    /**
     * This is the full constructor, when all parameters are known.
     */
    public ImportedGraphicsGroup() {
        // Always call the superclass constructor first!
        super();

        _importedGraphicsDistanceUnit = DistanceUnit.defaultValue();
        _importedGraphics = null;
        _importedGraphicsActive = false;

        _showImportedGraphics = true;

        importedGraphicsOpacityPercent = new SimpleDoubleProperty();

        try {
            initialize();
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Add new graphics node from Graphics Import, such as a DXF.
     *
     * @param importedGraphics
     *            The container for all of the Imported Graphics
     * @param importedGraphicsDistanceUnit
     *            The Distance Unit chosen for the Graphics Import content
     *            source
     * @param displayToVenueScaleFactor
     *            The display-to-venue scale factor
     * @param backColor
     *            The background color to use for determining the foreground
     *            color
     */
    public final void addImportedGraphics( final DxfShapeGroup importedGraphics,
                                           final DistanceUnit importedGraphicsDistanceUnit,
                                           final double displayToVenueScaleFactor,
                                           final Color backColor ) {
        // Cache the original Distance Unit associated with the Graphics Import
        // content source, so we can re-scale as necessary when the
        // application's Distance Unit changes.
        _importedGraphicsDistanceUnit = importedGraphicsDistanceUnit;

        // Make sure the Imported Graphics are visible against the current
        // Background Color, but only change Black and White vs. other Colors.
        final Color foreColor = ColorUtilities.getForegroundFromBackground( backColor );
        importedGraphics.setForeground( foreColor, false );

        // NOTE: This is just a hint, and rarely kicks in, so is low-risk in
        // terms of pixelated rendering for most imported geometry sets. In the
        // cases where it does cause a bitmap cache, it prevents crashes and
        // freeze-ups during zooming and other scaling-based changes to the
        // overall geometry group. Once zoomed in to where there is less overlap
        // of nodes, the caching is disengaged by the JavaFX core engine,
        // returning to sharp rendering. It's a performance vs. quality hint.
        // TODO: Make this a user preference, or only set the cache hint when
        // the node count for the geometry group exceeds 30,000 or even 20,000?
        // NOTE: We now make this dependent on the actual overall node count.
        final int numberOfNodes = importedGraphics.getChildren().size();
        final boolean optimizeGraphicsPerformance = numberOfNodes > 20000;
        importedGraphics.setCache( optimizeGraphicsPerformance );

        // Replace the imported graphics cache.
        _importedGraphics = importedGraphics;

        // Add the imported graphics to its parent Group container.
        getChildren().add( _importedGraphics );

        // Apply the Distance Unit scale transform to the Imported Geometry.
        // NOTE: This must be done last, or else we see an empty Node list.
        scaleImportedGraphics( displayToVenueScaleFactor );
    }

    private final void bindProperties() {
        // Bidirectionally bind the Opacity property to this Group Node.
        // NOTE: This is OK because we embed unit conversion in DoubleEditor.
        // NOTE: We now use a listener instead, as the data model uses the
        // displayed percentage instead of a real number representing a
        // percentage, so we have to multiply by 1/100.
        // opacityProperty().bindBidirectional(
        // importedGraphicsOpacityPercentProperty() );
        importedGraphicsOpacityPercentProperty()
                .addListener( ( observableValue, oldValue, newValue ) -> {
                    final double opacityPercentValue = 0.01d * newValue.doubleValue();
                    setOpacity( opacityPercentValue );
                } );
    }

    /**
     * Clear the imported graphics from the container, to recover large memory
     * chunks and to start afresh so that the old graphics are not displayed.
     */
    public final void clearImportedGraphics() {
        // Make sure we don't reference the previous Imported Geometry again.
        try {
            if ( hasImportedGraphics() ) {
                getChildren().clear();
                _importedGraphics.reset();
                _importedGraphics = null;
            }

            // Set the flag that Imported Graphics are inactive.
            setImportedGraphicsActive( false );
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }
    }

    public final DxfShapeGroup getImportedGraphics() {
        return _importedGraphics;
    }

    public final boolean hasImportedGraphics() {
        return !getChildren().isEmpty() && ( _importedGraphics != null )
                && !_importedGraphics.getChildren().isEmpty();
    }

    public final DoubleProperty importedGraphicsOpacityPercentProperty() {
        return importedGraphicsOpacityPercent;
    }

    /**
     * Set up the configuration of the Imported Graphics.
     */
    private final void initialize() {
        // Initialize the persistent shared attributes of the Imported Graphics
        // Group, which is application managed and is not directly interactive
        // at this time.
        GuiUtilities.initDecoratorNodeGroup( this );

        // It is desired to have Imported Graphics be less opaque than native
        // application graphics, so that it is easier to tell them apart.
        setOpacity( BACKGROUND_OPACITY_DEFAULT );

        // Hide the Imported Graphics Group until the next Graphics Import.
        setVisible( false );
    }

    /**
     * Returns the Imported Graphics active state.
     * 
     * @return The Imported Graphics active state
     */
    public final boolean isImportedGraphicsActive() {
        return _importedGraphicsActive;
    }

    /**
     * Return whether the imported graphics are drawn.
     *
     * @return True if the imported graphics are drawn.
     */
    public final boolean isShowImportedGraphics() {
        return _showImportedGraphics;
    }

    /**
     * Scale the imported graphics (if present), for Distance Unit and Stroke
     * Width.
     *
     * @param displayToVenueScaleFactor
     *            The display-to-venue scale factor
     */
    public final void scaleImportedGraphics( final double displayToVenueScaleFactor ) {
        // Scale from user-selected Distance Unit in the Graphics Import content
        // source to current Distance Unit, including Stroke Width resolution.
        if ( hasImportedGraphics() ) {
            scaleGraphicalNode( _importedGraphics,
                                _importedGraphicsDistanceUnit,
                                displayToVenueScaleFactor,
                                IMPORTED_GRAPHICS_STROKE_WIDTH_RATIO );
        }
    }

    public final void setForeground( final Color foreColor ) {
        // Make sure the Imported Graphics are visible against the new
        // Background Color, but only change Black and White vs. other Colors.
        if ( hasImportedGraphics() ) {
            // Delegate the real work to the various Graphical Node types.
            _importedGraphics.setForeground( foreColor, false );
        }
    }

    /**
     * Set the Imported Graphics active state.
     *
     * @param importedGraphicsActive
     *            Flag for whether Imported Graphics are active or not
     */
    public final void setImportedGraphicsActive( final boolean importedGraphicsActive ) {
        _importedGraphicsActive = importedGraphicsActive;
    }

    // Set and bind the Opacity Percent property reference.
    // NOTE: This should be done only once, to avoid breaking bindings.
    public final void setImportedGraphicsOpacityPercentProperty( final DoubleProperty pImportedGraphicsOpacityPercent ) {
        // Cache the Imported Graphics Opacity Percent property reference.
        importedGraphicsOpacityPercent = pImportedGraphicsOpacityPercent;

        // Bring the Group Node Opacity up-to-date before binding it.
        // NOTE: The data model uses the displayed percentage instead of a real
        // number representing a percentage, so we have to multiply by 1/100.
        final double opacityPercentValue = 0.01d * pImportedGraphicsOpacityPercent.get();
        setOpacity( opacityPercentValue );

        // Bind the data model to the appropriate level of the Group.
        bindProperties();
    }

    /**
     * Control whether the Imported Graphics drawn.
     *
     * @param showImportedGraphics
     *            If true, the Imported Graphics are drawn.
     */
    public final void setShowImportedGraphics( final boolean showImportedGraphics ) {
        _showImportedGraphics = showImportedGraphics;

        // Update the visibility of the Imported Graphics, if present.
        if ( hasImportedGraphics() ) {
            setVisible( showImportedGraphics );
        }
    }

    /**
     * Display new graphics from Graphics Import, such as a DXF.
     *
     * @param importedGraphics
     *            The container for all of the imported graphics
     * @param importedGraphicsDistanceUnit
     *            The Distance Unit chosen for the Graphics Import content
     *            source
     * @param displayToVenueScaleFactor
     *            The display-to-venue scale factor
     * @param backColor
     *            The background color to use for determining the foreground
     *            color
     */
    public final void updateImportedGraphics( final DxfShapeGroup importedGraphics,
                                              final DistanceUnit importedGraphicsDistanceUnit,
                                              final double displayToVenueScaleFactor,
                                              final Color backColor ) {
        // Remove the previous Imported Graphics from the container.
        clearImportedGraphics();

        // Replace the Imported Graphics and add to the graphics container.
        addImportedGraphics( importedGraphics,
                             importedGraphicsDistanceUnit,
                             displayToVenueScaleFactor,
                             backColor );

        // Set the flag that Imported Graphics are active.
        setImportedGraphicsActive( true );
    }

    /**
     * Update the Stroke Width of the imported graphics (if present).
     *
     * @param displayToVenueScaleFactor
     *            The display-to-venue scale factor
     */
    public final void updateImportedGraphicsStrokeWidths( final double displayToVenueScaleFactor ) {
        // Modify Stroke Width resolution to be appropriate for the new scale.
        if ( hasImportedGraphics() ) {
            final double strokeWidthBasis = displayToVenueScaleFactor;
            _importedGraphics.updateStrokeWidth( _importedGraphicsDistanceUnit,
                                                 _distanceUnit,
                                                 strokeWidthBasis,
                                                 IMPORTED_GRAPHICS_STROKE_WIDTH_RATIO );
        }
    }
}
