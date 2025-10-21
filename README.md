# FxDxfImport

FxDxfImport is a JavaFX based library for integrating pre-parsed DXF entities into GUI contexts such as Scene Graph Groups. 

To include this code in either FxDxfParser or FxConverter introduces too many additional POM dependencies for only a small fraction of the code in either library, so it has been split off from FxConverter after first merging a tiny amount of code into FxDxfParser.

This is a bare bones library that doesn't actually do the DXF Import. It provides JavaFX-based structural elements for hosting the results of calling FxDxfParser.
