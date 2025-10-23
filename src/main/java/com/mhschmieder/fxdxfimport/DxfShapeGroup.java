/*
 * MIT License
 *
 * Copyright (c) 2020, 2025 Mark Schmieder
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
 * This file is part of the FxDxfImport Library
 *
 * You should have received a copy of the MIT License along with the
 * FxDxfImport Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxdxfimport
 */
package com.mhschmieder.fxdxfimport;

import com.mhschmieder.fxdxfparser.geometry.DxfShapeContainer;
import com.mhschmieder.fxgraphics.paint.ColorUtilities;
import com.mhschmieder.fxgraphics.shape.ShapeContainer;
import com.mhschmieder.jphysics.DistanceUnit;
import com.mhschmieder.jphysics.UnitConversion;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

/**
 * This class is a graphical node container for a collection of bounded geometry
 * represented as a group of generated shapes -- usually from a Graphics Import.
 */
public final class DxfShapeGroup extends DxfShapeContainer implements ShapeContainer {

    /** Distance Unit, used as the Model Space's Unit of Measurement. */
    private DistanceUnit _distanceUnit;

    // Default constructor, when bounds and units are not known.
    public DxfShapeGroup() {
        this( DistanceUnit.UNITLESS, 0.0d, 0.0d, 0.0d, 0.0d );
    }

    // Fully qualified constructor, when bounds and units are known.
    // NOTE: It is safer to invoke this constructor than to reset an existing
    //  container and then set its bounds and units, as clearing a Group's
    //  children may run on a deferred thread.
    public DxfShapeGroup( final DistanceUnit distanceUnit,
                          final double minX,
                          final double minY,
                          final double maxX,
                          final double maxY ) {
        // Always call the superclass constructor first!
        super( minX, minY, maxX, maxY );

        _distanceUnit = distanceUnit;
    }

    public DistanceUnit getDistanceUnit() {
        return _distanceUnit;
    }

    @Override
    public ObservableList< Transform > getShapeTransforms() {
        return getTransforms();
    }

    /**
     * Sets the scale transform on the overall {@link ShapeContainer shape
     * container} to a uniform scale in this Node's frame of reference.
     *
     * @param distanceUnitOld
     *            The old or reference {@link DistanceUnit} to scale from
     * @param distanceUnitNew
     *            The new {@link DistanceUnit} to scale to
     */
    @Override
    public void scale( final DistanceUnit distanceUnitOld, final DistanceUnit distanceUnitNew ) {
        // Apply the Distance Unit scale transform, as it is generally
        // compensated for by the Transform on the overall wrapper group.
        // TODO: Verify it is safe to remove existing Transforms, but if we
        // don't, then we get no results if converting old to new and get
        // cumulative scaling if using Meters as the Distance Unit basis.
        final double distanceScaleFactor = UnitConversion
                .convertDistance( 1.0d, distanceUnitOld, distanceUnitNew );
        final ObservableList< Transform > transforms = getShapeTransforms();
        final Scale scaleTransform = Transform.scale( distanceScaleFactor, distanceScaleFactor );
        transforms.setAll( scaleTransform );
    }

    public void setDistanceUnit( final DistanceUnit distanceUnit ) {
        _distanceUnit = distanceUnit;
    }

    /**
     * This methods conditionally sets a new foreground color for the
     * graphics.
     *
     * @param foreColor
     *            The desired new foreground stroke color
     * @param forceOverride
     *            Flag for whether to override the current foreground stroke
     *            color even if neither white nor black
     */
    @Override
    public void setForeground( final Color foreColor, final boolean forceOverride ) {
        // Make sure the contained Shapes are visible against the Background
        // using the supplied Foreground Color, but only change Black and White
        // vs. other Colors so that we don't mess up custom cues.
        // NOTE: The implementation below is brittle as it assumes too much
        // knowledge of various derived class structures and also that there is
        // stability to those classes. So this is really a placeholder for now.
        getChildren().forEach( childNode -> {
            if ( childNode instanceof Shape ) {
                // Only Shapes can set Stroke (used as Foreground Color).
                final Shape shape = ( Shape ) childNode;
                ColorUtilities.adjustStrokeForContrast( shape, foreColor, forceOverride, null );
            }
        } );
    }

    /**
     * Sets the stroke on child {@link Shape shapes} to a uniform color in
     * this node's frame of reference.
     *
     * @param paint
     *            The color of the stroke
     */
    @Override
    public void setStroke( final Paint paint ) {
        getChildren().forEach( child -> {
            if ( child instanceof Shape ) {
                ( ( Shape ) child ).setStroke( paint );
            }
        } );
    }

    /**
     * Update the Stroke Width on the overall {@link ShapeContainer shape
     * container} to a recalculated scale in this Node's frame of reference.
     *
     * @param distanceUnitReference
     *            The reference Distance Unit to re-scale to
     * @param distanceUnitCurrent
     *            The current Distance Unit to scale Stroke Width from
     * @param strokeWidthBasis
     *            The Stroke Width basis, in current Distance Unit
     * @param strokeWidthRatio
     *            The ratio to apply to the basic Stroke Width
     */
    @Override
    public void updateStrokeWidth( final DistanceUnit distanceUnitReference,
                                   final DistanceUnit distanceUnitCurrent,
                                   final double strokeWidthBasis,
                                   final double strokeWidthRatio ) {
        // Modify Stroke Width resolution to be appropriate for the new scale.
        // NOTE: If default basis is used, no need to scale to Distance Unit,
        // but the zoom factor can then cause overly thick strokes.
        final double strokeWidthReference = UnitConversion
                .convertDistance( strokeWidthBasis, distanceUnitCurrent, distanceUnitReference );
        final double strokeWidthAdjusted = strokeWidthRatio * strokeWidthReference;

        // Globally set a uniform Stroke Width on the entire shape container.
        updateStrokeWidth( strokeWidthAdjusted );
    }

    /**
     * Updates the stroke on child {@link Shape shapes} to a uniform scale
     * in this node's frame of reference.
     *
     * @param pStrokeWidth
     *            The width of the stroke, roughly in pixels
     */
    @Override
    public void updateStrokeWidth( final double pStrokeWidth ) {
        setStrokeWidth( pStrokeWidth );
    }
}
