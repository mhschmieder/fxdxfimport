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

import com.mhschmieder.fxcadgraphics.DrawingLimits;
import com.mhschmieder.physicstoolkit.DistanceUnit;

import javafx.geometry.Bounds;

/**
 * The Graphics Import Options are the parameters needed for interpreting
 * geometry loaded from a Graphics File. They can either come from user input --
 * such as when a File Import Graphics command is issues -- or be set
 * programmatically to sensible values when the Graphics File is part of a
 * Project ZIP File Open action.
 */
public final class GraphicsImportOptions {

    // We generally remain unitless until units are explicitly chosen.
    private static final DistanceUnit DISTANCE_UNIT_DEFAULT = DistanceUnit.UNITLESS;

    // Keep track of the Distance Unit for the Graphics Import.
    private DistanceUnit              _distanceUnit;

    // Keep track of whether the initial Distance Unit came from either a
    // Project ZIP file or a DXF or other Graphics file.
    private boolean                   _initialDistanceUnitFromGraphicsFile;

    // Keep track of the Prospective Drawing Limits for the Geometry Import.
    private DrawingLimits             _prospectiveDrawingLimits;

    /*
     * Default constructor when nothing is known. 
     */
    public GraphicsImportOptions() {
        this( DISTANCE_UNIT_DEFAULT, new DrawingLimits() );
    }

    /*
     * Fully specified constructor when everything is known.
     */
    public GraphicsImportOptions( final DistanceUnit pDistanceUnit,
                                  final double pX,
                                  final double pY,
                                  final double pWidth,
                                  final double pHeight ) {
        // By default, the Graphics Import Distance Unit is not linked to a
        // Project ZIP file. This has to be set explicitly so that it is not
        // accidentally turned on, as we normally force the user to choose.
        _distanceUnit = pDistanceUnit;
        _initialDistanceUnitFromGraphicsFile = true;

        // NOTE: Unfortunately, there is no constructor that accepts min/max
        // pairs, and no setter methods to call post-construction.
        _prospectiveDrawingLimits = new DrawingLimits( pX, pY, pWidth, pHeight );
    }

    /*
     * Fully specified constructor when everything is known. 
     */
    public GraphicsImportOptions( final DistanceUnit pDistanceUnit,
                                  final DrawingLimits pProspectiveDrawingLimits ) {
        this( pDistanceUnit,
              pProspectiveDrawingLimits.getX(),
              pProspectiveDrawingLimits.getY(),
              pProspectiveDrawingLimits.getWidth(),
              pProspectiveDrawingLimits.getHeight() );
    }

    /*
     * Copy constructor. 
     */
    public GraphicsImportOptions( final GraphicsImportOptions pGraphicsExportOptions ) {
        this( pGraphicsExportOptions.getDistanceUnit(),
              pGraphicsExportOptions.getProspectiveDrawingLimits() );
    }

    public DistanceUnit getDistanceUnit() {
        return _distanceUnit;
    }

    public DrawingLimits getProspectiveDrawingLimits() {
        return _prospectiveDrawingLimits;
    }

    public boolean isInitialDistanceUnitFromGraphicsFile() {
        return _initialDistanceUnitFromGraphicsFile;
    }

    // Default pseudo-constructor.
    public void reset() {
        _distanceUnit = DISTANCE_UNIT_DEFAULT;
        _initialDistanceUnitFromGraphicsFile = true;

        _prospectiveDrawingLimits = new DrawingLimits();
    }

    public void setDistanceUnit( final DistanceUnit pDistanceUnit ) {
        _distanceUnit = pDistanceUnit;
    }

    // Fully specified pseudo-constructor.
    public void setGraphicsImportOptions( final DistanceUnit pDistanceUnit,
                                          final boolean pInitialDistanceUnitFromGraphicsFile,
                                          final DrawingLimits pProspectiveDrawingLimits ) {
        setDistanceUnit( pDistanceUnit );
        setInitialDistanceUnitFromGraphicsFile( pInitialDistanceUnitFromGraphicsFile );
        setProspectiveDrawingLimits( pProspectiveDrawingLimits );
    }

    // Pseudo-copy constructor.
    public void setGraphicsImportOptions( final GraphicsImportOptions pGraphicsExportOptions ) {
        setGraphicsImportOptions( pGraphicsExportOptions.getDistanceUnit(),
                                  pGraphicsExportOptions.isInitialDistanceUnitFromGraphicsFile(),
                                  pGraphicsExportOptions.getProspectiveDrawingLimits() );
    }

    public void setInitialDistanceUnitFromGraphicsFile( final boolean pInitialDistanceUnitFromGraphicsFile ) {
        _initialDistanceUnitFromGraphicsFile = pInitialDistanceUnitFromGraphicsFile;
    }

    /*
     * Set the bounds for the Drawing Limits of the Graphics Import, which could
     * become the actual Drawing Limits if the user chooses. Top-to-bottom sense
     * is flipped, as we are in the model space context vs. screen pixel space.
     * <p>
     * NOTE: We make a copy, so that reference-switching via user choice
     * doesn't cause confusion -- especially if we convert units more than once.
     */
    public void setProspectiveDrawingLimits( final DrawingLimits pProspectiveDrawingLimits ) {
        _prospectiveDrawingLimits = new DrawingLimits( pProspectiveDrawingLimits.getX(),
                                                       pProspectiveDrawingLimits.getY(),
                                                       pProspectiveDrawingLimits.getWidth(),
                                                       pProspectiveDrawingLimits.getHeight() );
    }

    /**
     * Implicitly specified pseudo-constructor; derives new defaults from known
     * geometry import metadata and container metrics.
     *
     * @param geometryContainer
     *            The overall container for the entire group of imported
     *            geometry, including explicit bounds when present in the file
     */
    public void updateGraphicsImportOptions( final DxfShapeGroup geometryContainer ) {
        // Start with the initial Distance Unit choice -- from the Graphics File
        // if present, or "unitless" if unspecified or unsupported.
        _distanceUnit = geometryContainer.getDistanceUnit();
        _initialDistanceUnitFromGraphicsFile = !DistanceUnit.UNITLESS.equals( _distanceUnit );

        // Cache the Computed Bounds, as the likely best prospective limits.
        final Bounds computedBounds = geometryContainer.getBoundsInLocal();
        _prospectiveDrawingLimits = new DrawingLimits( computedBounds );
    }
}
